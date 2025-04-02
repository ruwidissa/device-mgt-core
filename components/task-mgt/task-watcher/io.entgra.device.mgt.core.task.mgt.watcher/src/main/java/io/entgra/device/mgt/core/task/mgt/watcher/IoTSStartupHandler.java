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

package io.entgra.device.mgt.core.task.mgt.watcher;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementException;
import io.entgra.device.mgt.core.task.mgt.core.util.TaskManagementUtil;
import io.entgra.device.mgt.core.task.mgt.watcher.internal.TaskWatcherDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class IoTSStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(IoTSStartupHandler.class);

    private static int lastHashIndex = -1;

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    compareTasks();
                } catch (Exception e) {
                    log.error("Error occurred when comparing tasks.", e);
                }
            }
        }, 200000, 300000);
    }

    private void compareTasks() {
        if (log.isDebugEnabled()) {
            log.debug("Comparing Tasks from carbon nTask manager and Entgra task manager.");
        }
        TaskService nTaskService = TaskWatcherDataHolder.getInstance().getnTaskService();
        if (nTaskService == null) {
            String msg = "Unable to load TaskService from the carbon nTask core.";
            log.error(msg);
            return;
        }
        try {
            Map<Integer, List<DynamicTask>> tenantedDynamicTasks = TaskWatcherDataHolder.getInstance()
                    .getTaskManagementService().getDynamicTasksForAllTenants();

            int serverHashIdx;
            try {
                serverHashIdx = TaskWatcherDataHolder.getInstance().getHeartBeatService()
                        .getServerCtxInfo().getLocalServerHashIdx();
            } catch (HeartBeatManagementException e) {
                String msg = "Failed to get server hash index.";
                log.error(msg, e);
                throw new TaskManagementException(msg, e);
            }

            if (serverHashIdx != lastHashIndex) {
                log.info("Server hash index changed. Old: " + lastHashIndex + ", new: " + serverHashIdx);
                deleteAllDynamicNTasks(nTaskService, tenantedDynamicTasks, serverHashIdx);
                lastHashIndex = serverHashIdx;
            }

            scheduleMissingTasks(nTaskService, tenantedDynamicTasks);
            deleteObsoleteTasks(nTaskService, tenantedDynamicTasks);

            if (log.isDebugEnabled()) {
                log.debug("Task Comparison Completed and all tasks in current node are updated.");
            }
        } catch (TaskException e) {
            String msg = "Error occurred while accessing carbon nTask manager.";
            log.error(msg, e);
        } catch (TaskManagementException e) {
            String msg = "Error occurred while retrieving all active tasks from Entgra task manager.";
            log.error(msg, e);
        }

    }

    private void deleteAllDynamicNTasks(TaskService nTaskService, Map<Integer,
            List<DynamicTask>> tenantedDynamicTasks, int serverHashIdx) throws TaskException {
        List<Tenant> tenants = getAllTenants();

        TaskManager taskManager;

        for (Tenant tenant : tenants) {
            if (tenantedDynamicTasks.get(tenant.getId()) == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Dynamic tasks not running for tenant: [" + tenant.getId() + "] "
                            + tenant.getDomain());
                }
                continue;
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant.getId(), true);
            if (!nTaskService.getRegisteredTaskTypes().contains(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            }
            taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            List<TaskInfo> tasks = taskManager.getAllTasks();
            // Remove all applicable dynamic tasks from the nTask core
            for (TaskInfo taskInfo : tasks) {
                for (DynamicTask dt : tenantedDynamicTasks.get(tenant.getId())) {
                    if (tenant.getId() == dt.getTenantId()
                            && taskInfo.getName()
                            .equals(TaskManagementUtil.generateNTaskName(dt.getDynamicTaskId(), serverHashIdx))) {
                        taskManager.deleteTask(taskInfo.getName());
                        if (log.isDebugEnabled()) {
                            log.debug("Task '" + taskInfo.getName() + "' deleted as server hash changed.");
                        }
                    }
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void scheduleMissingTasks(TaskService nTaskService, Map<Integer,
            List<DynamicTask>> tenantedDynamicTasks)
            throws TaskException, TaskManagementException {

        TaskManager taskManager;
        for (Integer tenantId : tenantedDynamicTasks.keySet()) {
            if (tenantId == -1) {
                log.warn("Found " + tenantedDynamicTasks.get(tenantId).size() +
                        " invalid tasks without a valid tenant id.");
                continue;
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            if (!nTaskService.getRegisteredTaskTypes().contains(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            }
            taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            List<TaskInfo> tasks = taskManager.getAllTasks();
            // add or update task into nTask core
            for (DynamicTask dt : tenantedDynamicTasks.get(tenantId)) {
                int serverHashIdx;
                try {
                    serverHashIdx = TaskWatcherDataHolder.getInstance().getHeartBeatService()
                            .getServerCtxInfo().getLocalServerHashIdx();
                } catch (HeartBeatManagementException e) {
                    String msg = "Failed to get server hash index for dynamic task " + dt.getDynamicTaskId();
                    log.error(msg, e);
                    throw new TaskManagementException(msg, e);
                }

                String nTaskName = TaskManagementUtil.generateNTaskName(dt.getDynamicTaskId(), serverHashIdx);
                boolean isExist = false;
                for (TaskInfo taskInfo : tasks) {
                    if (taskInfo.getName().equals(nTaskName)) {

                        TaskInfo.TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
                        if (taskInfo.getProperties() == null) {
                            String msg = "Task properties not found for task " + nTaskName
                                    + ". Therefore deleting the nTask schedule.";
                            log.warn(msg);
                            taskManager.deleteTask(nTaskName);
                            break;
                        }

                        isExist = true;
                        if (!dt.isTriggerInfoEquals(triggerInfo)) {
                            taskInfo.setTriggerInfo(dt.getTriggerInfo());
                            taskInfo.setProperties(TaskManagementUtil
                                    .populateNTaskProperties(dt, taskInfo.getName(), serverHashIdx));
                            taskManager.registerTask(taskInfo);
                            taskManager.rescheduleTask(nTaskName);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + nTaskName
                                        + "' updated according to the dynamic task table.");
                            }
                        }
                        if (dt.isEnabled()
                                && taskManager.getTaskState(nTaskName) == TaskManager.TaskState.PAUSED) {
                            taskManager.resumeTask(nTaskName);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + nTaskName
                                        + "' enabled according to the dynamic task table.");
                            }
                        } else if (!dt.isEnabled()
                                && taskManager.getTaskState(nTaskName) != TaskManager.TaskState.PAUSED) {
                            taskManager.pauseTask(nTaskName);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + nTaskName
                                        + "' disabled according to the dynamic task table.");
                            }
                        }
                        break;
                    }
                }
                if (!isExist) {
                    TaskInfo taskInfo = new TaskInfo(nTaskName, dt.getTaskClassName(), TaskManagementUtil
                            .populateNTaskProperties(dt, nTaskName, serverHashIdx), dt.getTriggerInfo());
                    taskManager.registerTask(taskInfo);
                    taskManager.scheduleTask(nTaskName);
                    if (log.isDebugEnabled()) {
                        log.debug("New task -'" + nTaskName + "' created according to the dynamic task table.");
                    }

                    if (!dt.isEnabled()) {
                        taskManager.pauseTask(nTaskName);
                        if (log.isDebugEnabled()) {
                            log.debug("Task - '" + nTaskName + "' disabled according to the dynamic task table.");
                        }
                    }
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void deleteObsoleteTasks(TaskService nTaskService,
                                     Map<Integer, List<DynamicTask>> tenantedDynamicTasks)
            throws TaskManagementException, TaskException {

        TaskManager taskManager;
        Set<Integer> hashIds;
        try {
            hashIds = TaskWatcherDataHolder.getInstance().getHeartBeatService().getActiveServers().keySet();
        } catch (HeartBeatManagementException e) {
            String msg = "Unexpected exception when getting hash indexes of active servers";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }

        List<Tenant> tenants = getAllTenants();

        for (Tenant tenant : tenants) {
            if (tenantedDynamicTasks.get(tenant.getId()) == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Dynamic tasks not running for tenant: [" + tenant.getId() + "] "
                            + tenant.getDomain());
                }
                continue;
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant.getId(), true);
            if (!nTaskService.getRegisteredTaskTypes().contains(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE)) {
                nTaskService.registerTaskType(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            }
            taskManager = nTaskService.getTaskManager(TaskMgtConstants.Task.DYNAMIC_TASK_TYPE);
            List<TaskInfo> tasks = taskManager.getAllTasks();
            // Remove deleted items from the nTask core
            for (TaskInfo taskInfo : tasks) {
                boolean isExist = false;
                for (DynamicTask dt : tenantedDynamicTasks.get(tenant.getId())) {
                    for (int hid : hashIds) {
                        if (tenant.getId() == dt.getTenantId() &&
                                taskInfo.getName().equals(TaskManagementUtil.generateNTaskName(dt.getDynamicTaskId(), hid))) {
                            isExist = true;
                            break;
                        }
                    }
                    if (isExist) {
                        break;
                    }
                }
                if (!isExist) {
                    taskManager.deleteTask(taskInfo.getName());
                    if (log.isDebugEnabled()) {
                        log.debug("Task '" + taskInfo.getName() + "' deleted according to the dynamic task table");
                    }
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private List<Tenant> getAllTenants() {
        List<Tenant> tenants = new ArrayList<>();
        try {
            RealmService realmService = TaskWatcherDataHolder.getInstance().getRealmService();
            Tenant[] tenantArray = realmService.getTenantManager().getAllTenants();
            if (tenantArray != null && tenantArray.length != 0) {
                tenants.addAll(Arrays.asList(tenantArray));
            }
            Tenant superTenant = new Tenant();
            superTenant.setId(-1234);
            tenants.add(superTenant);
            return tenants;
        } catch (UserStoreException e) {
            String msg = "Unable to load tenants";
            log.error(msg, e);
            return new ArrayList<>();
        }
    }

}
