/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.task.mgt.watcher;


import io.entgra.task.mgt.common.bean.DynamicTask;
import io.entgra.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.core.util.TaskManagementUtil;
import io.entgra.task.mgt.watcher.internal.TaskWatcherDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IoTSStartupHandler implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(IoTSStartupHandler.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                compareTasks();
            }
        }, 200000, 600000);
    }

    private void compareTasks() {
        log.info("Comparing Tasks from carbon n task manager and engtra task manager");
        TaskManager taskManager = null;
        TaskService nTaskService = TaskWatcherDataHolder.getInstance().getnTaskService();
        if (nTaskService == null) {
            String msg = "Unable to load TaskService from the carbon n task core";
            log.error(msg);
        }
        try {
            if (!nTaskService.getRegisteredTaskTypes().contains(
                    TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            }
            taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);

            List<DynamicTask> dynamicTasks = TaskWatcherDataHolder.getInstance().getTaskManagementService()
                    .getAllDynamicTasks();
            List<TaskInfo> tasks = taskManager.getAllTasks();
            // add or update task into n task core
            for (DynamicTask dt : dynamicTasks) {
                String generatedTaskId = TaskManagementUtil.generateTaskId(dt.getDynamicTaskId());
                boolean isExist = false;
                for (TaskInfo taskInfo : tasks) {
                    if (taskInfo.getName().equals(generatedTaskId)) {
                        isExist = true;
                        TaskInfo.TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
                        String dynamicTaskPropMD5 = TaskManagementUtil.generateTaskPropsMD5(dt.getProperties());
                        String existingTaskPropMD5 = TaskManagementUtil.generateTaskPropsMD5(taskInfo.getProperties());
                        if (!triggerInfo.getCronExpression().equals(dt.getCronExpression())
                                || !dynamicTaskPropMD5.equals(existingTaskPropMD5)) {
                            triggerInfo.setCronExpression(dt.getCronExpression());
                            taskInfo.setTriggerInfo(triggerInfo);
                            taskInfo.setProperties(dt.getProperties());
                            taskManager.registerTask(taskInfo);
                            taskManager.rescheduleTask(generatedTaskId);
                            log.debug("Task - '" + generatedTaskId + "' updated according to the dynamic task table");
                        }
                        if (dt.isEnabled()
                                && taskManager.getTaskState(generatedTaskId) == TaskManager.TaskState.PAUSED) {
                            taskManager.resumeTask(generatedTaskId);
                            log.debug("Task - '" + generatedTaskId + "' enabled according to the dynamic task table");
                        } else if (!dt.isEnabled()
                                && taskManager.getTaskState(generatedTaskId) != TaskManager.TaskState.PAUSED) {
                            taskManager.pauseTask(generatedTaskId);
                            log.debug("Task - '" + generatedTaskId + "' disabled according to the dynamic task table");
                        }
                        break;
                    }
                }
                if (!isExist) {
                    TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                    triggerInfo.setCronExpression(dt.getCronExpression());
                    TaskInfo taskInfo = new TaskInfo(generatedTaskId, dt.getTaskClassName(),
                            dt.getProperties(), triggerInfo);
                    taskManager.registerTask(taskInfo);
                    taskManager.scheduleTask(generatedTaskId);
                    log.debug("New task -'" + generatedTaskId + "' created according to the dynamic task table");
                }
            }

            // Remove deleted items from the n task core
            for (TaskInfo taskInfo : tasks) {
                boolean isExist = false;
                for (DynamicTask dt : dynamicTasks) {
                    if (taskInfo.getName().equals(TaskManagementUtil.generateTaskId(dt.getDynamicTaskId()))) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    taskManager.deleteTask(taskInfo.getName());
                    log.debug("Task '" + taskInfo.getName() + "' deleted according to the dynamic task table");
                }
            }
            log.info("Task Comparison Completed and all tasks in current node are updated");
        } catch (
                TaskException e) {
            String msg = "Error occurred while accessing carbon n task manager.";
            log.error(msg);
        } catch (
                TaskManagementException e) {
            String msg = "Error occurred while retrieving all active tasks from entgra task manager";
            log.error(msg);
        }

    }
}
