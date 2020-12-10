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

package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.event.config.EventOperationTaskConfiguration;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.geo.task.EventOperationTaskException;
import org.wso2.carbon.device.mgt.core.geo.task.EventOperationTaskManagerImpl;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskManagerService;
import org.wso2.carbon.device.mgt.core.status.task.impl.DeviceStatusTaskManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.device.task.manager" immediate="true"
 * @scr.reference name="device.ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTaskService"
 * unbind="unsetTaskService"
 */

@SuppressWarnings("unused")
public class DeviceTaskManagerServiceComponent {

    private static Log log = LogFactory.getLog(DeviceTaskManagerServiceComponent.class);
    private DeviceManagementConfig deviceManagementConfig;

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device task manager bundle.");
            }
            startOperationMonitoringTask(componentContext.getBundleContext());
            startGroupEventCreationTask(componentContext.getBundleContext());
            //Start the DeviceStatusMonitoringTask for registered DeviceTypes
            deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
                startDeviceStatusMonitoringTask(componentContext.getBundleContext());
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

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        try {
            stopOperationMonitoringTask();
            if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
                stopDeviceStatusMonitoringTask();
            } else if (deviceManagementConfig != null && deviceManagementConfig.getEventOperationTaskConfiguration()
                    .isEnabled()) {
                stopGroupEventCreationTask();
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

    private void startGroupEventCreationTask(BundleContext bundleContext) {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        EventOperationTaskConfiguration eventTaskConfig = deviceManagementConfig.getEventOperationTaskConfiguration();
        if (eventTaskConfig.isEnabled()) {
            EventOperationTaskManagerImpl eventOperationTaskManager = new EventOperationTaskManagerImpl();
            DeviceManagementDataHolder.getInstance().setEventOperationTaskManager(eventOperationTaskManager);
            bundleContext.registerService(EventOperationTaskManagerImpl.class, eventOperationTaskManager, null);
            try {
                eventOperationTaskManager.startGroupEventOperationTask(eventTaskConfig);
            } catch (EventOperationTaskException e) {
                log.error("Error occurred while creating group event creation task");
            }
        }
    }

    private void stopGroupEventCreationTask() {
        EventOperationTaskManagerImpl eventOperationTaskManager = DeviceManagementDataHolder.getInstance()
                .getEventOperationTaskManager();
        try {
            eventOperationTaskManager.stopGroupEventOperationTaskTask();
        } catch (EventOperationTaskException e) {
            log.error("Error occurred while stopping group event creation task");
        }
    }

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
