/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (https://entgra.io) All Rights Reserved.
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
package io.entgra.task.mgt.core.service;

import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.task.mgt.common.bean.DynamicTask;
import io.entgra.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.task.mgt.common.exception.TaskManagementDAOException;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.common.exception.TaskNotFoundException;
import io.entgra.task.mgt.common.exception.TransactionManagementException;
import io.entgra.task.mgt.common.spi.TaskManagementService;
import io.entgra.task.mgt.core.dao.DynamicTaskDAO;
import io.entgra.task.mgt.core.dao.DynamicTaskPropDAO;
import io.entgra.task.mgt.core.dao.common.TaskManagementDAOFactory;
import io.entgra.task.mgt.core.internal.TaskManagerDataHolder;
import io.entgra.task.mgt.core.util.TaskManagementUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.List;
import java.util.Map;

public class TaskManagementServiceImpl implements TaskManagementService {
    private static final Log log = LogFactory.getLog(TaskManagementServiceImpl.class);

    private final DynamicTaskDAO dynamicTaskDAO;

    private final DynamicTaskPropDAO dynamicTaskPropDAO;
    private TaskManager taskManager;

    public TaskManagementServiceImpl() {
        this.dynamicTaskDAO = TaskManagementDAOFactory.getDynamicTaskDAO();
        this.dynamicTaskPropDAO = TaskManagementDAOFactory.getDynamicTaskPropDAO();
    }

    @Override
    public void init() throws TaskManagementException {
        TaskService nTaskService = TaskManagerDataHolder.getInstance().getnTaskService();
        if (nTaskService == null) {
            String msg = "Unable to load TaskService, hence unable to schedule the task.";
            log.error(msg);
            throw new TaskManagementException(msg);
        }
        if (!nTaskService.getRegisteredTaskTypes().contains(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
            try {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
                this.taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            } catch (TaskException e) {
                String msg = "Error occurred while registering task type ["
                        + TaskMgtConstants.Task.DYNAMIC_TASK_TYPE
                        + "], hence unable to schedule the task.";
                log.error(msg);
                throw new TaskManagementException(msg, e);
            }
        }
    }

    @Override
    public void createTask(DynamicTask dynamicTask) throws TaskManagementException {
        String taskId;
        try {
            // add into the dynamic task tables
            TaskManagementDAOFactory.beginTransaction();
            int dynamicTaskId = dynamicTaskDAO.addTask(dynamicTask);

            Map<String, String> taskProperties = dynamicTask.getProperties();
            dynamicTaskPropDAO.addTaskProperties(dynamicTaskId, taskProperties);

            // add into the ntask core
            taskId = TaskManagementUtil.generateTaskId(dynamicTaskId);

            try {
                int serverHashIdx = TaskManagerDataHolder.getInstance().getHeartBeatService()
                        .getServerCtxInfo().getLocalServerHashIdx();
                taskProperties.put(TaskMgtConstants.Task.LOCAL_HASH_INDEX, String.valueOf(serverHashIdx));
                taskProperties.put(TaskMgtConstants.Task.LOCAL_TASK_NAME, taskId);
            } catch (HeartBeatManagementException e) {
                String msg = "Unexpected exception when getting server hash index.";
                log.error(msg, e);
                throw new TaskManagementException(msg, e);
            }

            if (!isTaskExists(taskId)) {
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setCronExpression(dynamicTask.getCronExpression());
                TaskInfo taskInfo = new TaskInfo(taskId, dynamicTask.getTaskClassName(), taskProperties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.scheduleTask(taskId);
                if (!dynamicTask.isEnabled()) {
                    taskManager.pauseTask(taskId);
                }
            } else {
                String msg = "Task '" + taskId + "' is already exists in the ntask core "
                        + "Hence not creating another task for the same name.";
                log.error(msg);
            }

            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Failed to add dynamic task " + dynamicTask.getName();
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to add dynamic task";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TaskException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while scheduling task '" + dynamicTask.getName() + "'";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateTask(int dynamicTaskId, DynamicTask dynamicTask) throws TaskManagementException
            , TaskNotFoundException {
        try {
            //Update dynamic task table
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTaskById(dynamicTaskId);

            if (existingTask != null) {
                existingTask.setEnabled(dynamicTask.isEnabled());
                existingTask.setCronExpression(dynamicTask.getCronExpression());
                dynamicTaskDAO.updateDynamicTask(existingTask);
                if (!dynamicTask.getProperties().isEmpty()) {
                    dynamicTaskPropDAO.updateDynamicTaskProps(dynamicTaskId, dynamicTask.getProperties());
                }
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists in the dynamic task table.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            // Update task in the ntask core
            String taskId = TaskManagementUtil.generateTaskId(existingTask.getDynamicTaskId());
            if (isTaskExists(taskId)) {
                TaskInfo taskInfo = taskManager.getTask(taskId);
                if (!dynamicTask.getProperties().isEmpty()) {
                    taskInfo.setProperties(dynamicTask.getProperties());
                }
                TaskInfo.TriggerInfo triggerInfo;
                if (taskInfo.getTriggerInfo() == null) {
                    triggerInfo = new TaskInfo.TriggerInfo();
                } else {
                    triggerInfo = taskInfo.getTriggerInfo();
                }
                triggerInfo.setCronExpression(dynamicTask.getCronExpression());
                taskInfo.setTriggerInfo(triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskId);
            } else {
                String msg = "Task '" + taskId + "' is not exists in the n task core "
                        + "Hence cannot update the task.";
                log.error(msg);
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Failed to update dynamic task " + dynamicTask.getDynamicTaskId();
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to update dynamic task";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TaskException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating task '" + dynamicTask.getDynamicTaskId() + "'";
            log.error(msg);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void toggleTask(int dynamicTaskId, boolean isEnabled) throws TaskManagementException
            , TaskNotFoundException {

        try {
            //update dynamic task table
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTaskById(dynamicTaskId);
            if (existingTask != null) {
                existingTask.setEnabled(isEnabled);
                dynamicTaskDAO.updateDynamicTask(existingTask);
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            // Update task in the ntask core
            String taskId = TaskManagementUtil.generateTaskId(existingTask.getDynamicTaskId());
            if (isTaskExists(taskId)) {
                if (isEnabled) {
                    taskManager.resumeTask(taskId);
                } else {
                    taskManager.pauseTask(taskId);
                }
            } else {
                String msg = "Task '" + taskId + "' is not exists in the ntask core "
                        + "Hence cannot toggle the task in the ntask.";
                log.error(msg);
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Failed to toggle dynamic task " + dynamicTaskId;
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to toggle dynamic task";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TaskException e) {
            String msg = "Error occurred while toggling task '" + dynamicTaskId + "' to '" + isEnabled + "'";
            log.error(msg);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteTask(int dynamicTaskId) throws TaskManagementException, TaskNotFoundException {
        // delete task from dynamic task table
        try {
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTaskById(dynamicTaskId);
            if (existingTask != null) {
                dynamicTaskDAO.deleteDynamicTask(dynamicTaskId);
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            String taskId = TaskManagementUtil.generateTaskId(existingTask.getDynamicTaskId());
            if (isTaskExists(taskId)) {
                taskManager.deleteTask(taskId);
            } else {
                String msg = "Task '" + taskId + "' is not exists in the ntask core "
                        + "Hence cannot delete from the ntask core.";
                log.error(msg);
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Failed to update dynamic task " + dynamicTaskId;
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to delete dynamic task";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TaskException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving task manager to delete task '" + dynamicTaskId + "'";
            log.error(msg);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DynamicTask> getAllDynamicTasks() throws TaskManagementException {
        List<DynamicTask> dynamicTasks;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all dynamic tasks");
            }
            TaskManagementDAOFactory.beginTransaction();
            dynamicTasks = dynamicTaskDAO.getAllDynamicTasks();
            if (dynamicTasks != null) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    dynamicTask.setProperties(dynamicTaskPropDAO
                            .getDynamicTaskProps(dynamicTask.getDynamicTaskId()));
                }
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while fetching all dynamic tasks";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to get all dynamic tasks";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
        return dynamicTasks;
    }

    @Override
    public DynamicTask getDynamicTaskById(int dynamicTaskId) throws TaskManagementException {
        DynamicTask dynamicTask;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of dynamic task '" + dynamicTaskId + "'");
            }
            TaskManagementDAOFactory.beginTransaction();
            dynamicTask = dynamicTaskDAO.getDynamicTaskById(dynamicTaskId);
            if (dynamicTask != null) {
                dynamicTask.setProperties(dynamicTaskPropDAO.getDynamicTaskProps(dynamicTask.getDynamicTaskId()));
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while fetching dynamic task '" + dynamicTaskId + "'";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to get dynamic task '" + dynamicTaskId + "'";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
        return dynamicTask;
    }

    @Override
    public List<DynamicTask> getActiveDynamicTasks() throws TaskManagementException {
        List<DynamicTask> dynamicTasks;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all active dynamic tasks");
            }
            TaskManagementDAOFactory.beginTransaction();
            dynamicTasks = dynamicTaskDAO.getActiveDynamicTasks();
            if (dynamicTasks != null) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    dynamicTask.setProperties(dynamicTaskPropDAO.getDynamicTaskProps(dynamicTask.getDynamicTaskId()));
                }
            }
            TaskManagementDAOFactory.commitTransaction();
        } catch (TaskManagementDAOException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while fetching all active dynamic tasks";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to get all active dynamic tasks";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
        return dynamicTasks;
    }

    // check whether task exist in the ntask core
    private boolean isTaskExists(String taskId) throws TaskManagementException, TaskException {
        if (StringUtils.isEmpty(taskId)) {
            String msg = "Task ID must not be null or empty.";
            log.error(msg);
            throw new TaskManagementException(msg);
        }
        List<TaskInfo> tasks = taskManager.getAllTasks();
        for (TaskInfo t : tasks) {
            if (taskId.equals(t.getName())) {
                return true;
            }
        }
        return false;
    }
}
