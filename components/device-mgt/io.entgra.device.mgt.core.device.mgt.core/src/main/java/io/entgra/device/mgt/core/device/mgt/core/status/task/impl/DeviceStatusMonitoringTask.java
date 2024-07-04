/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.core.status.task.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DeviceStatusTaskPluginConfig;
import io.entgra.device.mgt.core.device.mgt.common.DynamicTaskContext;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceMonitoringData;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.status.task.DeviceStatusTaskException;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implements the Task service which monitors the device activity periodically & update the device-status if
 * necessary.
 */
public class DeviceStatusMonitoringTask extends DynamicPartitionedScheduleTask {

    private static final Log log = LogFactory.getLog(DeviceStatusMonitoringTask.class);
    private String deviceType;
    private int deviceTypeId = -1;

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
        deviceType = getProperty(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE);
        deviceTypeId = Integer.parseInt(getProperty(DeviceStatusTaskManagerServiceImpl.DEVICE_TYPE_ID));
        String deviceStatusTaskConfigStr = getProperty(DeviceStatusTaskManagerServiceImpl.DEVICE_STATUS_TASK_CONFIG);
        Gson gson = new Gson();
        DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig = gson.fromJson(deviceStatusTaskConfigStr, DeviceStatusTaskPluginConfig.class);
        try {
            List<EnrolmentInfo> enrolmentInfoTobeUpdated = new ArrayList<>();
            List<DeviceMonitoringData> allDevicesForMonitoring = getAllDevicesForMonitoring();
            Map<Integer, List<DeviceMonitoringData>> tenantDevicesMap = new HashMap<>();
            List<DeviceMonitoringData> tenantMonitoringData = null;
            //Delegate the devices in each tenant to a separate list to be updated the statuses.
            //This improvement has been done since the tenants maintain a separate caches and the task is running
            //in the super-tenant space. Hence, the device status updates are not reflected in the tenant caches.
            //Refer to https://roadmap.entgra.net/issues/11386 for more information.
            for (DeviceMonitoringData deviceMonitoringData : allDevicesForMonitoring) {
                tenantMonitoringData = tenantDevicesMap.get(deviceMonitoringData.getTenantId());
                if (tenantMonitoringData == null) {
                    tenantMonitoringData = new ArrayList<>();
                }
                tenantMonitoringData.add(deviceMonitoringData);
                tenantDevicesMap.put(deviceMonitoringData.getTenantId(), tenantMonitoringData);
            }

            List<DeviceMonitoringData> monitoringDevices = null;
            long timeMillis = System.currentTimeMillis();
            //Retrieving the devices belongs for each tenants and updating the status of the devices.
            for (Map.Entry<Integer, List<DeviceMonitoringData>> entry : tenantDevicesMap.entrySet()) {
                Integer tenantId = entry.getKey();
                RealmService realmService = DeviceManagementDataHolder.getInstance().getRealmService();
                if (realmService != null) {
                    String domain = realmService.getTenantManager().getDomain(tenantId);
                    if (domain != null) {
                        try {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain, true);
                            monitoringDevices = entry.getValue();
                            for (DeviceMonitoringData monitoringData : monitoringDevices) {
                                long lastUpdatedTime = (timeMillis - monitoringData
                                        .getLastUpdatedTime()) / 1000;

                                EnrolmentInfo enrolmentInfo = monitoringData.getDevice().getEnrolmentInfo();
                                EnrolmentInfo.Status status = null;
                                if (lastUpdatedTime >= deviceStatusTaskPluginConfig
                                        .getIdleTimeToMarkInactive()) {
                                    status = EnrolmentInfo.Status.INACTIVE;
                                } else if (lastUpdatedTime >= deviceStatusTaskPluginConfig
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
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    } else {
                        log.error("Failed while running the device status update task. Failed while " +
                                "extracting tenant domain of the tenant id : " + tenantId);
                    }
                } else {
                    log.error("Failed while running the device status update task. RealmService is not initiated");
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
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving RealmService instance for updating device status.";
            log.error(msg, e);
        }
    }


    private boolean updateDeviceStatus(List<EnrolmentInfo> enrolmentInfos) throws
            DeviceStatusTaskException {
        boolean updateStatus;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceStatusManagementService deviceStatusManagementService = DeviceManagementDataHolder
                .getInstance().getDeviceStatusManagementService();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updateStatus = DeviceManagementDAOFactory.getEnrollmentDAO().updateEnrollmentStatus(enrolmentInfos);
            boolean isEnableDeviceStatusCheck = deviceStatusManagementService.getDeviceStatusCheck(tenantId);
            if (updateStatus) {
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    if (isEnableDeviceStatusCheck) {
                        if (deviceStatusManagementService.isDeviceStatusValid(this.deviceType, enrolmentInfo.getStatus().name(), tenantId)) {
                            DeviceManagementDAOFactory.getEnrollmentDAO().addDeviceStatus(enrolmentInfo);
                        }
                    } else {
                        DeviceManagementDAOFactory.getEnrollmentDAO().addDeviceStatus(enrolmentInfo);
                    }
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException | MetadataManagementException e) {
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
