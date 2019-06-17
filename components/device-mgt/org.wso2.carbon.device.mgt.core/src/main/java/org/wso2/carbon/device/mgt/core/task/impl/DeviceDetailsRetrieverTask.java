/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.core.task.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;
import java.util.Map;

public class DeviceDetailsRetrieverTask implements Task {

    private static Log log = LogFactory.getLog(DeviceDetailsRetrieverTask.class);
    private String deviceType;
    private boolean executeForTenants = false;
    private final String IS_CLOUD = "is.cloud";
    private DeviceManagementProviderService deviceManagementProviderService;

    @Override
    public void setProperties(Map<String, String> map) {
        deviceType = map.get("DEVICE_TYPE");
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        deviceManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getDeviceManagementProvider();
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceManagementProviderService
                .getDeviceMonitoringConfig(deviceType);
        StartupOperationConfig startupOperationConfig = deviceManagementProviderService
                .getStartupOperationConfig(deviceType);

        if (System.getProperty(IS_CLOUD) != null && Boolean.parseBoolean(System.getProperty(IS_CLOUD))) {
            executeForTenants = true;
        }
        if (executeForTenants) {
            this.executeForAllTenants(operationMonitoringTaskConfig, startupOperationConfig);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Device details retrieving task started to run.");
            }
            DeviceTaskManager deviceTaskManager = new DeviceTaskManagerImpl(deviceType, operationMonitoringTaskConfig,
                                                                            startupOperationConfig);
            //pass the configurations also from here, monitoring tasks
            try {
                if (deviceManagementProviderService.isDeviceMonitoringEnabled(deviceType)) {
                    deviceTaskManager.addOperations();
                }
            } catch (DeviceMgtTaskException e) {
                log.error("Error occurred while trying to add the operations to device to retrieve device details.", e);
            }
        }
    }

    private void executeForAllTenants(OperationMonitoringTaskConfig operationMonitoringTaskConfig,
                                      StartupOperationConfig startupOperationConfig) {

        if (log.isDebugEnabled()) {
            log.debug("Device details retrieving task started to run for all tenants.");
        }
        try {
            List<Integer> tenants = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDeviceEnrolledTenants();
            if (log.isDebugEnabled()) {
                log.debug("Task is running for " + tenants.size() + " tenants and the device type is " + deviceType);
            }

            for (Integer tenant : tenants) {
                String tenantDomain = DeviceManagementDataHolder.getInstance().
                        getRealmService().getTenantManager().getDomain(tenant);
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant);
                    DeviceTaskManager deviceTaskManager = new DeviceTaskManagerImpl(deviceType,
                                                                                    operationMonitoringTaskConfig,
                                                                                    startupOperationConfig);
                    //pass the configurations also from here, monitoring tasks
                    try {
                        if (deviceManagementProviderService.isDeviceMonitoringEnabled(deviceType)) {
                            deviceTaskManager.addOperations();
                        }
                    } catch (DeviceMgtTaskException e) {
                        log.error("Error occurred while trying to add the operations to " +
                                "device to retrieve device details.", e);
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while trying to get the available tenants", e);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while trying to get the available tenants " +
                    "from device manager provider service.", e);
        }
    }

}
