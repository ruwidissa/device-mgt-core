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
package io.entgra.device.mgt.core.task.mgt.core.service;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.device.mgt.core.task.mgt.common.exception.*;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.task.mgt.core.dao.DynamicTaskDAO;
import io.entgra.device.mgt.core.task.mgt.core.dao.DynamicTaskPropDAO;
import io.entgra.device.mgt.core.task.mgt.core.dao.common.TaskManagementDAOFactory;
import io.entgra.device.mgt.core.task.mgt.core.internal.TaskManagerDataHolder;
import io.entgra.device.mgt.core.task.mgt.core.util.TaskManagementUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManagementServiceImpl implements TaskManagementService {
    private static final Log log = LogFactory.getLog(TaskManagementServiceImpl.class);

    private final DynamicTaskDAO dynamicTaskDAO;

    private final DynamicTaskPropDAO dynamicTaskPropDAO;

    public TaskManagementServiceImpl() {
        this.dynamicTaskDAO = TaskManagementDAOFactory.getDynamicTaskDAO();
        this.dynamicTaskPropDAO = TaskManagementDAOFactory.getDynamicTaskPropDAO();
    }

    /**
     * Get task manager for DYNAMIC_TASK type.
     *
     * @return {@link TaskManager}
     * @throws TaskManagementException Throws when failed to get the carbon ntask manager service.
     */
    private TaskManager getTaskManager() throws TaskManagementException {
        TaskService nTaskService = TaskManagerDataHolder.getInstance().getnTaskService();
        TaskManager taskManager;
        if (nTaskService == null) {
            String msg = "Unable to load TaskService, hence unable to schedule the task.";
            log.error(msg);
            throw new TaskManagementException(msg);
        }

        try {
            if (!nTaskService.getRegisteredTaskTypes().contains(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            }
            taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
        } catch (TaskException e) {
            String msg = "Error occurred while registering task type [" + TaskMgtConstants.Task.DYNAMIC_TASK_TYPE +
                    "], hence unable to schedule the task.";
            log.error(msg);
            throw new TaskManagementException(msg, e);
        }

        if (taskManager == null) {
            String msg =
                    "Failed to get the carbon ntask manager service for task type [" + TaskMgtConstants.Task.DYNAMIC_TASK_TYPE
                            + "] in [" + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain() + "]" +
                            " tenant space.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        return taskManager;
    }

    @Override
    public void createTask(DynamicTask dynamicTask) throws TaskManagementException {
        String nTaskName;
        int dynamicTaskId;
        int serverHashIdx;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        final TaskManager taskManager = getTaskManager();
        try {
            // add into the dynamic task tables
            TaskManagementDAOFactory.beginTransaction();
            dynamicTaskId = dynamicTaskDAO.addTask(dynamicTask, tenantId);
            dynamicTask.setDynamicTaskId(dynamicTaskId);
            dynamicTaskPropDAO.addTaskProperties(dynamicTaskId, dynamicTask.getProperties(), tenantId);

            try {
                serverHashIdx = TaskManagerDataHolder.getInstance().getHeartBeatService()
                        .getServerCtxInfo().getLocalServerHashIdx();
                nTaskName = TaskManagementUtil.generateNTaskName(dynamicTaskId, serverHashIdx);
            } catch (HeartBeatManagementException e) {
                String msg = "Unexpected exception when getting server hash index.";
                log.error(msg, e);
                throw new TaskManagementException(msg, e);
            }

            if (isTaskExists(nTaskName)) {
                String msg = "Task '" + nTaskName + "' is already exists in the ntask core. "
                        + "Hence removing existing task from nTask before adding new one.";
                log.warn(msg);
                taskManager.deleteTask(nTaskName);
            }

            // add into the ntask core
            Map<String, String> taskProperties = TaskManagementUtil
                    .populateNTaskProperties(dynamicTask, nTaskName, serverHashIdx);
            TaskInfo taskInfo = new TaskInfo(nTaskName, dynamicTask.getTaskClassName(), taskProperties,
                    dynamicTask.getTriggerInfo());
            taskManager.registerTask(taskInfo);
            taskManager.scheduleTask(nTaskName);
            if (!dynamicTask.isEnabled()) {
                taskManager.pauseTask(nTaskName);
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
    public void updateTask(int dynamicTaskId, DynamicTask dynamicTask)
            throws TaskManagementException, TaskNotFoundException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        final TaskManager taskManager = getTaskManager();
        try {
            //Update dynamic task table
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTask(dynamicTaskId, tenantId);

            if (existingTask != null) {
                existingTask.setEnabled(dynamicTask.isEnabled());
                existingTask.setCronExpression(dynamicTask.getCronExpression());
                existingTask.setIntervalMillis(dynamicTask.getIntervalMillis());
                dynamicTaskDAO.updateDynamicTask(existingTask, tenantId);
                if (!dynamicTask.getProperties().isEmpty()) {
                    dynamicTaskPropDAO.updateDynamicTaskProps(dynamicTaskId, dynamicTask.getProperties(), tenantId);
                }
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists in the dynamic task table.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            // Update task in the ntask core
            updateNTask(existingTask.getDynamicTaskId(), dynamicTask);
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
        } catch (TaskManagementNTaskException e) {
            TaskManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating task '" + dynamicTask.getDynamicTaskId() + "'";
            log.error(msg);
            throw new TaskManagementException(msg, e);
        } finally {
            TaskManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void toggleTask(int dynamicTaskId, boolean isEnabled)
            throws TaskManagementException, TaskNotFoundException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        final TaskManager taskManager = getTaskManager();
        try {
            //update dynamic task table
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTask(dynamicTaskId, tenantId);
            if (existingTask != null) {
                existingTask.setEnabled(isEnabled);
                dynamicTaskDAO.updateDynamicTask(existingTask, tenantId);
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            // Update task in the ntask core
            String taskName = TaskManagementUtil.generateNTaskName(existingTask.getDynamicTaskId());
            if (isTaskExists(taskName)) {
                if (isEnabled) {
                    taskManager.resumeTask(taskName);
                } else {
                    taskManager.pauseTask(taskName);
                }
            } else {
                String msg = "Task '" + taskName + "' is not exists in the ntask core "
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
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            TaskManagementDAOFactory.beginTransaction();
            DynamicTask existingTask = dynamicTaskDAO.getDynamicTask(dynamicTaskId, tenantId);
            if (existingTask != null) {
                dynamicTaskDAO.deleteDynamicTask(dynamicTaskId, tenantId);
            } else {
                String msg = "Task '" + dynamicTaskId + "' is not exists.";
                log.error(msg);
                throw new TaskNotFoundException(msg);
            }

            String taskName = TaskManagementUtil.generateNTaskName(existingTask.getDynamicTaskId());
            if (isTaskExists(taskName)) {
                getTaskManager().deleteTask(taskName);
            } else {
                String msg = "Task '" + taskName + "' is not exists in the ntask core "
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
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DynamicTask> dynamicTasks;
        try {
            if (log.isTraceEnabled()) {
                log.trace("Fetching the details of all dynamic tasks");
            }
            TaskManagementDAOFactory.openConnection();
            dynamicTasks = dynamicTaskDAO.getAllDynamicTasks(tenantId);
            if (dynamicTasks != null) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    dynamicTask.setProperties(dynamicTaskPropDAO
                            .getDynamicTaskProps(dynamicTask.getDynamicTaskId(), tenantId));
                }
            }
        } catch (TaskManagementDAOException e) {
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
    public Map<Integer, List<DynamicTask>> getDynamicTasksForAllTenants() throws TaskManagementException {
        List<DynamicTask> dynamicTasks;
        try {
            if (log.isTraceEnabled()) {
                log.trace("Fetching the details of dynamic tasks for all tenants");
            }
            TaskManagementDAOFactory.openConnection();
            dynamicTasks = dynamicTaskDAO.getAllDynamicTasks();
            if (dynamicTasks != null) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    dynamicTask.setProperties(dynamicTaskPropDAO
                            .getDynamicTaskProps(dynamicTask.getDynamicTaskId(), dynamicTask.getTenantId()));
                }
            }
        } catch (TaskManagementDAOException e) {
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
        Map<Integer, List<DynamicTask>> tenantedDynamicTasks = new HashMap<>();
        List<DynamicTask> dts;
        if (dynamicTasks != null) {
            for (DynamicTask dt : dynamicTasks) {
                if (tenantedDynamicTasks.containsKey(dt.getTenantId())) {
                    dts = tenantedDynamicTasks.get(dt.getTenantId());
                } else {
                    dts = new ArrayList<>();
                }
                dts.add(dt);
                tenantedDynamicTasks.put(dt.getTenantId(), dts);
            }
        }
        return tenantedDynamicTasks;
    }

    @Override
    public DynamicTask getDynamicTask(int dynamicTaskId) throws TaskManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DynamicTask dynamicTask;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of dynamic task '" + dynamicTaskId + "'");
            }
            TaskManagementDAOFactory.openConnection();
            dynamicTask = dynamicTaskDAO.getDynamicTask(dynamicTaskId, tenantId);
            if (dynamicTask != null) {
                dynamicTask.setProperties(dynamicTaskPropDAO.getDynamicTaskProps(dynamicTask.getDynamicTaskId(),
                        tenantId));
            }
        } catch (TaskManagementDAOException e) {
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
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DynamicTask> dynamicTasks;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all active dynamic tasks");
            }
            TaskManagementDAOFactory.openConnection();
            dynamicTasks = dynamicTaskDAO.getActiveDynamicTasks(tenantId);
            if (dynamicTasks != null) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    dynamicTask.setProperties(dynamicTaskPropDAO.getDynamicTaskProps(dynamicTask.getDynamicTaskId(),
                            tenantId));
                }
            }
        } catch (TaskManagementDAOException e) {
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
    private boolean isTaskExists(String taskName) throws TaskManagementException, TaskException {
        if (StringUtils.isEmpty(taskName)) {
            String msg = "Task Name must not be null or empty.";
            log.error(msg);
            throw new TaskManagementException(msg);
        }
        List<TaskInfo> tasks = getTaskManager().getAllTasks();
        for (TaskInfo t : tasks) {
            if (taskName.equals(t.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the task if exists in ntask core.
     *
     * @param existingTaskId Existing task id.
     * @param dynamicTask    Dynamic task.
     * @throws TaskManagementNTaskException Throws when error encountered while ntask updating procedure.
     */
    private void updateNTask(int existingTaskId, DynamicTask dynamicTask) throws TaskManagementNTaskException {
       try {
           String nTaskName = TaskManagementUtil.generateNTaskName(existingTaskId);
           if (isTaskExists(nTaskName)) {
               TaskManager taskManager = getTaskManager();
               TaskInfo taskInfo = taskManager.getTask(nTaskName);

               Map<String, String> taskProperties = TaskManagementUtil.populateNTaskProperties(dynamicTask, nTaskName);
               taskInfo.setProperties(taskProperties);
               taskInfo.setTriggerInfo(dynamicTask.getTriggerInfo());
               // rescheduling the ntask is not properly update the ntask context, hence delete, register and schedule
               // the ntask again with the updated context as a workaround
               taskManager.deleteTask(nTaskName);
               taskManager.registerTask(taskInfo);
               taskManager.scheduleTask(nTaskName);

               if (!dynamicTask.isEnabled()) {
                   taskManager.pauseTask(nTaskName);
               }
           } else {
               String msg = "Task '" + nTaskName + "' is not exists in the n task core Hence cannot update the task.";
               log.error(msg);
               throw new TaskManagementNTaskException(msg);
           }
       } catch (TaskManagementException e) {
           String msg = "Error encountered while generating ntask name for existing task id " + existingTaskId + ".";
           log.error(msg, e);
           throw new TaskManagementNTaskException(msg, e);
       } catch (TaskException e) {
           String msg = "Error encountered while updating the carbon ntask in ntask core.";
           log.error(msg, e);
           throw new TaskManagementNTaskException(msg, e);
       }
    }
}
