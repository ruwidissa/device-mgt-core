/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.policy.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.util.ArrayList;
import java.util.List;

public class MonitoringTask extends DynamicPartitionedScheduleTask {

    private static final Log log = LogFactory.getLog(MonitoringTask.class);

    private String tenant;

    /**
     * Check whether Device platform (ex: android) is exist in the cdm-config.xml file before adding a
     * Monitoring operation to a specific device type.
     *
     * @param deviceType available device types.
     * @return return platform is exist(true) or not (false).
     */

    private boolean isPlatformExist(String deviceType) {
        PolicyMonitoringManager policyMonitoringManager = PolicyManagementDataHolder.getInstance()
                .getDeviceManagementService().getPolicyMonitoringManager(deviceType);
        return policyMonitoringManager != null;
    }

    @Override
    public void executeDynamicTask() {
        tenant = getProperty(PolicyManagementConstants.TENANT_ID);
        if (tenant == null) {
            log.warn("Tenant id of the Monitoring Task got null");
            return;
        }
        int tenantId = Integer.parseInt(tenant);
        if (log.isDebugEnabled()) {
            log.debug("Monitoring task started to run for tenant: " + tenant);
        }
        if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
            this.executeTask();
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            this.executeTask();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void executeTask() {
        MonitoringManager monitoringManager = PolicyManagementDataHolder.getInstance().getMonitoringManager();
        List<String> deviceTypes = new ArrayList<>();
        List<String> configDeviceTypes = new ArrayList<>();
        try {
            deviceTypes = monitoringManager.getDeviceTypes();
            for (String deviceType : deviceTypes) {
                if (isPlatformExist(deviceType)) {
                    configDeviceTypes.add(deviceType);
                }
            }
        } catch (PolicyComplianceException e) {
            log.error("TID:[" + tenant + "] Error occurred while getting the device types.");
        }
        if (!deviceTypes.isEmpty()) {
            try {
                DeviceManagementProviderService deviceManagementProviderService =
                        PolicyManagementDataHolder.getInstance().getDeviceManagementService();
                for (String deviceType : configDeviceTypes) {
                    if (log.isDebugEnabled()) {
                        log.debug("TID:[" + tenant + "] Running task for device type : " + deviceType);
                    }
                    PolicyMonitoringManager monitoringService =
                            PolicyManagementDataHolder.getInstance().getDeviceManagementService()
                                    .getPolicyMonitoringManager(deviceType);
                    List<Device> devices;
                    if(isDynamicTaskEligible()){
                        devices = deviceManagementProviderService
                                .getAllocatedDevices(deviceType, getTaskContext().getActiveServerCount(),
                                        getTaskContext().getServerHashIndex());
                    } else {
                        devices = deviceManagementProviderService.getAllDevices(deviceType, false);
                    }
                    if (monitoringService != null && !devices.isEmpty()) {
                        List<Device> notifiableDevices = new ArrayList<>();
                        if (log.isDebugEnabled()) {
                            log.debug("TID:[" + tenant + "] Removing inactive and blocked devices from " +
                                    "the list for the device type : " + deviceType);
                        }
                        StringBuilder sb = new StringBuilder();
                        for (Device device : devices) {
                            EnrolmentInfo.Status status = device.getEnrolmentInfo().getStatus();
                            if (status.equals(EnrolmentInfo.Status.ACTIVE) ||
                                    status.equals(EnrolmentInfo.Status.UNREACHABLE)) {
                                notifiableDevices.add(device);
                                if (log.isDebugEnabled()) {
                                    if (sb.length() > 0) {
                                        sb.append(", ");
                                    }
                                    sb.append(device.getDeviceIdentifier());
                                }
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("TID:[" + tenant + "] Sending monitoring to '" + deviceType +
                                    "' devices with ids [" + sb + "]");
                        }
                        if (!notifiableDevices.isEmpty()) {
                            monitoringManager.addMonitoringOperation(deviceType, notifiableDevices);
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("TID:[" + tenant + "] Monitoring task running completed.");
                }
            } catch (Exception e) {
                log.error("TID:[" + tenant + "] Error occurred while trying to run a task.", e);
            }
        } else {
            log.info("TID:[" + tenant + "] No device types registered currently. " +
                    "So did not run the monitoring task.");
        }
    }

    @Override
    protected void setup() {

    }

}
