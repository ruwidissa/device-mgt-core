/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.geo.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.event.config.EventOperationTaskConfiguration;
import org.wso2.carbon.device.mgt.core.event.config.GroupEventOperationCreationTask;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

public class EventOperationTaskManagerImpl {
    private static final Log log = LogFactory.getLog(EventOperationTaskManagerImpl.class);

    public static final String GROUP_EVENT_OPERATION_TASK_TYPE = "GROUP_EVENT_CREATION";
    private static final String TASK_NAME = "GROUP_EVENT_TASK";
    private static final String TASK_CLASS = GroupEventOperationCreationTask.class.getName();

    public void startGroupEventOperationTask(EventOperationTaskConfiguration eventTaskConfig)
            throws EventOperationTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(GROUP_EVENT_OPERATION_TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Group event creation task is started");
                log.debug(
                        "Group event creation task is at frequency of : " + eventTaskConfig.getFrequency());
            }
            TaskManager taskManager = taskService.getTaskManager(GROUP_EVENT_OPERATION_TASK_TYPE);
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            //Convert to milli seconds
            triggerInfo.setIntervalMillis(eventTaskConfig.getFrequency());
            triggerInfo.setRepeatCount(-1);
            if (!taskManager.isTaskScheduled(GROUP_EVENT_OPERATION_TASK_TYPE)) {
                TaskInfo taskInfo = new TaskInfo(TASK_NAME, TASK_CLASS, null, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                String msg = "Group event creation task is already started";
                log.error(msg);
                throw new EventOperationTaskException(msg);
            }
        } catch (TaskException e) {
            String msg = "Error occurred while creating the group event operation task";
            log.error(msg, e);
            throw new EventOperationTaskException(msg, e);
        }
    }

    public void stopGroupEventOperationTaskTask() throws EventOperationTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            if (taskService != null && taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(GROUP_EVENT_OPERATION_TASK_TYPE);
                taskManager.deleteTask(TASK_NAME);
            }
        } catch (TaskException e) {
            String msg = "Error occurred while stopping the group event operation task";
            log.error(msg, e);
            throw new EventOperationTaskException(msg, e);
        }
    }
}
