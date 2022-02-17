/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.operation.timeout.task.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.config.operation.timeout.OperationTimeout;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.timeout.task.OperationTimeoutTaskException;
import org.wso2.carbon.device.mgt.core.operation.timeout.task.OperationTimeoutTaskManagerService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class OperationTimeoutTaskManagerServiceImpl implements OperationTimeoutTaskManagerService {

    private static final Log log = LogFactory.getLog(OperationTimeoutTaskManagerServiceImpl.class);

    public static final String OPERATION_TIMEOUT_TASK = "OPERATION_TIMEOUT_TASK";
    static final String DEVICE_TYPES = "DEVICE_TYPES";
    static final String OPERATION_TIMEOUT_TASK_CONFIG = "OPERATION_TIMEOUT_TASK_CONFIG";
    static final String INITIAL_STATUS = "INITIAL_STATUS";
    private static final String TASK_CLASS = OperationTimeoutTask.class.getName();

    @Override
    public void startTask(OperationTimeout config)
            throws OperationTimeoutTaskException {
        log.info("Operation timeout task adding for device type(s) : " + config.getDeviceTypes()
                + ", operation code : " + config.getInitialStatus());

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(OPERATION_TIMEOUT_TASK);

            if (log.isDebugEnabled()) {
                log.debug("Operation timeout task is started for the device type(s) : " + config.getDeviceTypes()
                        + ", operation code : " + config.getInitialStatus());
                log.debug(
                        "Operation timeout task is at frequency of : " + config.getTimeout() + " minutes");
            }

            TaskManager taskManager = taskService.getTaskManager(OPERATION_TIMEOUT_TASK);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            //Convert to milli seconds
            triggerInfo.setIntervalMillis(config.getTimeout() * 60 * 1000);
            triggerInfo.setRepeatCount(-1);

            Gson gson = new Gson();
            String operationTimeoutConfig = gson.toJson(config);

            Map<String, String> properties = new HashMap<>();

            String deviceTypes = StringUtils.join(config.getDeviceTypes(), "_");
            properties.put(DEVICE_TYPES, deviceTypes);
            properties.put(INITIAL_STATUS, config.getInitialStatus());
            properties.put(OPERATION_TIMEOUT_TASK_CONFIG, operationTimeoutConfig);

            String taskName = OPERATION_TIMEOUT_TASK + "_" + config.getInitialStatus() + "_" + deviceTypes;

            if (!taskManager.isTaskScheduled(taskName)) {
                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new OperationTimeoutTaskException(
                        "Operation Timeout task is already started for the device type(s) : " + config.getDeviceTypes()
                                + ", operation code : " + config.getInitialStatus());
            }
        } catch (TaskException e) {
            throw new OperationTimeoutTaskException("Error occurred while creating the Operation timeout task " +
                    "for the device type(s) : " + config.getDeviceTypes() + ", operation code : " + config
                    .getInitialStatus(), e);
        }
    }

    @Override
    public void stopTask(OperationTimeout config)
            throws OperationTimeoutTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            String deviceTypes = StringUtils.join(config.getDeviceTypes(), "_");
            String taskName = OPERATION_TIMEOUT_TASK + "_" + config.getInitialStatus() + "_" + deviceTypes;
            if (taskService != null && taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(OPERATION_TIMEOUT_TASK);
                taskManager.deleteTask(taskName);
            }
        } catch (TaskException e) {
            throw new OperationTimeoutTaskException("Error occurred while deleting the Operation timeout task " +
                    "for the device type(s) : " + config.getDeviceTypes() + ", operation code : " + config
                    .getInitialStatus(), e);
        }
    }

    @Override
    public void updateTask(OperationTimeout config)
            throws OperationTimeoutTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(OPERATION_TIMEOUT_TASK);
            String deviceTypes = StringUtils.join(config.getDeviceTypes(), "_");
            String taskName = OPERATION_TIMEOUT_TASK + "_" + config.getInitialStatus() + "_" + deviceTypes;

            if (taskManager.isTaskScheduled(taskName)) {
                taskManager.deleteTask(taskName);
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setIntervalMillis(config.getTimeout() * 60 * 1000);
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put(DEVICE_TYPES, deviceTypes);
                properties.put(INITIAL_STATUS, config.getInitialStatus());

                Gson gson = new Gson();
                String deviceStatusTaskConfigs = gson.toJson(config);
                properties.put(OPERATION_TIMEOUT_TASK_CONFIG, deviceStatusTaskConfigs);

                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new OperationTimeoutTaskException(
                        "Operation timeout task has not been started for this device-type the device type(s) : "
                                + config.getDeviceTypes() + ", operation code : " + config.getInitialStatus() +
                                ". Please start the task first.");
            }

        } catch (TaskException e) {
            throw new OperationTimeoutTaskException("Error occurred while updating the Operation timeout task " +
                    "for the device type(s) : " + config.getDeviceTypes() + ", operation code : " +
                    config.getInitialStatus(), e);
        }
    }

    @Override
    public boolean isTaskScheduled(OperationTimeout config) throws OperationTimeoutTaskException {
        String deviceTypes = StringUtils.join(config.getDeviceTypes(), "_");
        String taskName = OPERATION_TIMEOUT_TASK + "_" + config.getInitialStatus() + "_" + deviceTypes;
        TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
        TaskManager taskManager;
        try {
            taskManager = taskService.getTaskManager(OPERATION_TIMEOUT_TASK);
            return taskManager.isTaskScheduled(taskName);
        } catch (TaskException e) {
            throw new OperationTimeoutTaskException("Error occurred while checking the task schedule status " +
                    "of the Operation timeout task for the device type(s) : " + config.getDeviceTypes() +
                    ", operation code : " + config.getInitialStatus(), e);
        }
    }
}
