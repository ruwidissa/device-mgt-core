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
 */

package io.entgra.device.mgt.core.device.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.common.DeviceStatusTaskPluginConfig;
import io.entgra.device.mgt.core.device.mgt.common.OperationMonitoringTaskConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.operation.timeout.OperationTimeout;
import io.entgra.device.mgt.core.device.mgt.core.config.operation.timeout.OperationTimeoutConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.OperationTimeoutTaskException;
import io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.OperationTimeoutTaskManagerService;
import io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.impl.OperationTimeoutTaskManagerServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.status.task.DeviceStatusTaskException;
import io.entgra.device.mgt.core.device.mgt.core.status.task.DeviceStatusTaskManagerService;
import io.entgra.device.mgt.core.device.mgt.core.status.task.impl.DeviceStatusTaskManagerServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.task.DeviceMgtTaskException;
import io.entgra.device.mgt.core.device.mgt.core.task.DeviceTaskManagerService;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.Map;
@Component(
        name = "io.entgra.device.mgt.core.device.mgt.core.internal.DeviceTaskManagerServiceComponent",
        immediate = true)
@SuppressWarnings("unused")
public class DeviceTaskManagerServiceComponent {

    private static Log log = LogFactory.getLog(DeviceTaskManagerServiceComponent.class);
    private DeviceManagementConfig deviceManagementConfig;

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device task manager bundle.");
            }
            startOperationMonitoringTask(componentContext.getBundleContext());
            //Start the DeviceStatusMonitoringTask for registered DeviceTypes
            deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
                startDeviceStatusMonitoringTask(componentContext.getBundleContext());
            }

            if (deviceManagementConfig != null && deviceManagementConfig.getOperationTimeoutConfiguration() != null) {
                startOperationTimeoutTask(componentContext.getBundleContext());
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device task manager service.", e);
        }
    }

    private void startOperationMonitoringTask(BundleContext bundleContext)
            throws DeviceMgtTaskException {
        DeviceTaskManagerService deviceTaskManagerService = new DeviceTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(deviceTaskManagerService);
        bundleContext.registerService(DeviceTaskManagerService.class, deviceTaskManagerService, null);
        Map<String, OperationMonitoringTaskConfig> deviceConfigMap = DeviceMonitoringOperationDataHolder
                .getInstance().getOperationMonitoringConfigFromMap();
        for (String platformType : deviceConfigMap.keySet()) {
            OperationMonitoringTaskConfig taskConfig = deviceConfigMap.get(platformType);
            if (taskConfig.isEnabled()) {
                deviceTaskManagerService.startTask(platformType, taskConfig);
            }
        }
    }

    private void startDeviceStatusMonitoringTask(BundleContext bundleContext) {
        DeviceStatusTaskManagerService deviceStatusTaskManagerService = new DeviceStatusTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceStatusTaskManagerService(deviceStatusTaskManagerService);
        bundleContext.registerService(DeviceStatusTaskManagerService.class, deviceStatusTaskManagerService, null);
        Map<DeviceType, DeviceStatusTaskPluginConfig> deviceStatusTaskPluginConfigs = DeviceManagementDataHolder.
                getInstance().getDeviceStatusTaskPluginConfigs();
        for (DeviceType deviceType : deviceStatusTaskPluginConfigs.keySet()) {
            try {
                deviceStatusTaskManagerService.startTask(deviceType, deviceStatusTaskPluginConfigs.get(deviceType));
            } catch (DeviceStatusTaskException e) {
                log.error("Exception occurred while starting the DeviceStatusMonitoring Task for deviceType '" +
                        deviceType + "'", e);
            }
        }
    }

    private void startOperationTimeoutTask(BundleContext bundleContext) {
        OperationTimeoutTaskManagerService operationTimeoutTaskManagerService =
                new OperationTimeoutTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setOperationTimeoutTaskManagerService(
                operationTimeoutTaskManagerService);
        bundleContext.registerService(OperationTimeoutTaskManagerService.class,
                operationTimeoutTaskManagerService, null);

        OperationTimeoutConfiguration configuration = deviceManagementConfig.getOperationTimeoutConfiguration();

        for (OperationTimeout operationTimeout : configuration.getOperationTimeoutList()) {
                try {
                    operationTimeoutTaskManagerService.startTask(operationTimeout);
                } catch (OperationTimeoutTaskException e) {
                    log.error("Error while starting the operation timeout task for device type (s) : "
                            + operationTimeout.getDeviceTypes() + ", operation code : "
                            +  operationTimeout.getInitialStatus());
                }
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        try {
            stopOperationMonitoringTask();
            if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
                stopDeviceStatusMonitoringTask();
            }
            if (deviceManagementConfig != null && deviceManagementConfig.getOperationTimeoutConfiguration() != null) {
                stopOperationTimeoutTask();
            }
        } catch (Throwable e) {
            log.error("Error occurred while shutting down device task manager service.", e);
        }
    }

    private void stopOperationMonitoringTask()
            throws DeviceMgtTaskException {
        DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance()
                .getDeviceTaskManagerService();
        Map<String, OperationMonitoringTaskConfig> deviceConfigMap = DeviceMonitoringOperationDataHolder
                .getInstance().getOperationMonitoringConfigFromMap();
        for (String platformType : deviceConfigMap.keySet()) {
            OperationMonitoringTaskConfig taskConfig = deviceConfigMap.get(platformType);
            if (taskConfig.isEnabled()) {
                deviceTaskManagerService.stopTask(platformType, taskConfig);
            }
        }
    }

    private void stopDeviceStatusMonitoringTask() {
        DeviceStatusTaskManagerService deviceStatusTaskManagerService = DeviceManagementDataHolder.getInstance()
                .getDeviceStatusTaskManagerService();
        Map<DeviceType, DeviceStatusTaskPluginConfig> deviceStatusTaskPluginConfigs = DeviceManagementDataHolder.
                getInstance().getDeviceStatusTaskPluginConfigs();
        for (DeviceType deviceType : deviceStatusTaskPluginConfigs.keySet()) {
            try {
                deviceStatusTaskManagerService.stopTask(deviceType, deviceStatusTaskPluginConfigs.get(deviceType));
            } catch (DeviceStatusTaskException e) {
                log.error("Exception occurred while stopping the DeviceStatusMonitoring Task for deviceType '" +
                        deviceType + "'", e);
            }
        }
    }

    private void stopOperationTimeoutTask() {
        OperationTimeoutTaskManagerService operationTimeoutTaskManagerService =
                DeviceManagementDataHolder.getInstance().getOperationTimeoutTaskManagerService();
        OperationTimeoutConfiguration configuration = deviceManagementConfig.getOperationTimeoutConfiguration();

        for (OperationTimeout operationTimeout : configuration.getOperationTimeoutList()) {
            try {
                operationTimeoutTaskManagerService.stopTask(operationTimeout);
            } catch (OperationTimeoutTaskException e) {
                log.error("Error while stopping the operation timeout task for device type (s) : "
                        + operationTimeout.getDeviceTypes() + ", operation code : "
                        +  operationTimeout.getInitialStatus());
            }
        }
    }

    @Reference(
            name = "task.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(null);
    }
}
