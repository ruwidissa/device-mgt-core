/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceMonitoringData;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This implements the Task service which monitors the device activity periodically & update the device-status if
 * necessary.
 */
public class DeviceStatusMonitoringTask extends DynamicPartitionedScheduleTask {

    private static final Log log = LogFactory.getLog(DeviceStatusMonitoringTask.class);
    private String deviceType;
    private DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig;
    private int deviceTypeId = -1;

    @Override
    public void setProperties(Map<String, String> properties) {
        deviceType = properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE);
        deviceTypeId = Integer.parseInt(properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE_ID));
        String deviceStatusTaskConfigStr = properties.get(DeviceStatusTaskManagerServiceImpl.DEVICE_STATUS_TASK_CONFIG);
        Gson gson = new Gson();
        deviceStatusTaskPluginConfig = gson.fromJson(deviceStatusTaskConfigStr, DeviceStatusTaskPluginConfig.class);
    }

    @Override
    protected void setup() {
    }

    public List<DeviceMonitoringData> getAllDevicesForMonitoring() throws DeviceManagementException {

        try {
            DeviceManagementDAOFactory.openConnection();
            DynamicTaskContext ctx = getTaskContext();
            if (ctx != null && ctx.isPartitioningEnabled()) {
                return DeviceManagementDAOFactory.getDeviceDAO()
                        .getAllDevicesForMonitoring(this.deviceTypeId, this.deviceType,
                                ctx.getActiveServerCount(), ctx.getServerHashIndex());
            } else {
                return DeviceManagementDAOFactory.getDeviceDAO()
                        .getAllDevicesForMonitoring(this.deviceTypeId, this.deviceType, -1, -1);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving devices list for monitoring.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void executeDynamicTask() {
        try {
            List<EnrolmentInfo> enrolmentInfoTobeUpdated = new ArrayList<>();
            List<DeviceMonitoringData> allDevicesForMonitoring = getAllDevicesForMonitoring();
            long timeMillis = System.currentTimeMillis();
            for (DeviceMonitoringData monitoringData : allDevicesForMonitoring) {
                long lastUpdatedTime = (timeMillis - monitoringData
                        .getLastUpdatedTime()) / 1000;

                EnrolmentInfo enrolmentInfo = monitoringData.getDevice().getEnrolmentInfo();
                EnrolmentInfo.Status status = null;
                if (lastUpdatedTime >= this.deviceStatusTaskPluginConfig
                        .getIdleTimeToMarkInactive()) {
                    status = EnrolmentInfo.Status.INACTIVE;
                } else if (lastUpdatedTime >= this.deviceStatusTaskPluginConfig
                        .getIdleTimeToMarkUnreachable()) {
                    status = EnrolmentInfo.Status.UNREACHABLE;
                }

                if (status != null) {
                    enrolmentInfo.setStatus(status);
                    enrolmentInfoTobeUpdated.add(enrolmentInfo);
                    DeviceIdentifier deviceIdentifier =
                            new DeviceIdentifier(monitoringData.getDevice()
                                    .getDeviceIdentifier(), deviceType);
                    monitoringData.getDevice().setEnrolmentInfo(enrolmentInfo);
                    DeviceCacheManagerImpl.getInstance().addDeviceToCache(deviceIdentifier,
                            monitoringData.getDevice(), monitoringData.getTenantId());
                }
            }

            if (!enrolmentInfoTobeUpdated.isEmpty()) {
                try {
                    this.updateDeviceStatus(enrolmentInfoTobeUpdated);
                } catch (DeviceStatusTaskException e) {
                    log.error("Error occurred while updating non-responsive " +
                            "device-status of devices of type '" + deviceType + "'", e);
                }
            }


        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving devices list for monitoring.";
            log.error(msg, e);
        }
    }


    private boolean updateDeviceStatus(List<EnrolmentInfo> enrolmentInfos) throws
            DeviceStatusTaskException {
        boolean updateStatus;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updateStatus = DeviceManagementDAOFactory.getEnrollmentDAO().updateEnrollmentStatus(enrolmentInfos);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceStatusTaskException("Error occurred while updating enrollment status of devices of type '"
                    + deviceType + "'", e);
        } catch (TransactionManagementException e) {
            throw new DeviceStatusTaskException("Error occurred while initiating a transaction for updating the device " +
                    "status of type '" + deviceType + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return updateStatus;
    }

}
