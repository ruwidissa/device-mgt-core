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

import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.task.mgt.common.bean.DynamicTask;
import io.entgra.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.core.util.TaskManagementUtil;
import io.entgra.task.mgt.watcher.internal.TaskWatcherDataHolder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            log.debug("Comparing Tasks from carbon nTask manager and entgra task manager");
        }
        TaskService nTaskService = TaskWatcherDataHolder.getInstance().getnTaskService();
        if (nTaskService == null) {
            String msg = "Unable to load TaskService from the carbon nTask core";
            log.error(msg);
            return;
        }
        try {
            List<DynamicTask> dynamicTasks = TaskWatcherDataHolder.getInstance().getTaskManagementService()
                    .getAllDynamicTasks();

            scheduleMissingTasks(nTaskService, dynamicTasks);
            deleteObsoleteTasks(nTaskService, dynamicTasks);

            if (log.isDebugEnabled()) {
                log.debug("Task Comparison Completed and all tasks in current node are updated");
            }
        } catch (TaskException e) {
            String msg = "Error occurred while accessing carbon nTask manager.";
            log.error(msg, e);
        } catch (TaskManagementException e) {
            String msg = "Error occurred while retrieving all active tasks from entgra task manager";
            log.error(msg, e);
        }

    }

    private static void scheduleMissingTasks(TaskService nTaskService, List<DynamicTask> dynamicTasks)
            throws TaskException, TaskManagementException {
        Map<Integer, List<DynamicTask>> tenantedDynamicTasks = new HashMap<>();
        List<DynamicTask> dts;
        for (DynamicTask dt : dynamicTasks) {
            if (tenantedDynamicTasks.containsKey(dt.getTenantId())) {
                dts = tenantedDynamicTasks.get(dt.getTenantId());
            } else {
                dts = new ArrayList<>();
            }
            dts.add(dt);
            tenantedDynamicTasks.put(dt.getTenantId(), dts);
        }
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
                            taskInfo.setProperties(populateTaskProperties(tenantId, generatedTaskId, dt.getProperties()));
                            taskManager.registerTask(taskInfo);
                            taskManager.rescheduleTask(generatedTaskId);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + generatedTaskId + "' updated according to the dynamic task table");
                            }
                        }
                        if (dt.isEnabled()
                                && taskManager.getTaskState(generatedTaskId) == TaskManager.TaskState.PAUSED) {
                            taskManager.resumeTask(generatedTaskId);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + generatedTaskId + "' enabled according to the dynamic task table");
                            }
                        } else if (!dt.isEnabled()
                                && taskManager.getTaskState(generatedTaskId) != TaskManager.TaskState.PAUSED) {
                            taskManager.pauseTask(generatedTaskId);
                            if (log.isDebugEnabled()) {
                                log.debug("Task - '" + generatedTaskId + "' disabled according to the dynamic task table");
                            }
                        }
                        break;
                    }
                }
                if (!isExist) {
                    TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                    triggerInfo.setCronExpression(dt.getCronExpression());
                    TaskInfo taskInfo = new TaskInfo(generatedTaskId, dt.getTaskClassName(),
                            populateTaskProperties(tenantId, generatedTaskId, dt.getProperties()), triggerInfo);
                    taskManager.registerTask(taskInfo);
                    taskManager.scheduleTask(generatedTaskId);
                    if (log.isDebugEnabled()) {
                        log.debug("New task -'" + generatedTaskId + "' created according to the dynamic task table");
                    }
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static Map<String, String> populateTaskProperties(int tenantId, String generatedTaskId,
                                                              Map<String, String> taskProperties)
            throws TaskManagementException {
        try {
            int serverHashIdx = TaskWatcherDataHolder.getInstance().getHeartBeatService()
                    .getServerCtxInfo().getLocalServerHashIdx();
            taskProperties.put(TaskMgtConstants.Task.LOCAL_HASH_INDEX, String.valueOf(serverHashIdx));
            taskProperties.put(TaskMgtConstants.Task.LOCAL_TASK_NAME, generatedTaskId);
            taskProperties.put(TaskMgtConstants.Task.TENANT_ID_PROP, String.valueOf(tenantId));
            return taskProperties;
        } catch (HeartBeatManagementException e) {
            String msg = "Unexpected exception when getting server hash index.";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }
    }

    private static void deleteObsoleteTasks(TaskService nTaskService, List<DynamicTask> dynamicTasks)
            throws TaskManagementException, TaskException {

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
        } catch (UserStoreException e) {
            String msg = "Unable to load tenants";
            log.error(msg, e);
            return;
        }

        TaskManager taskManager;
        Set<Integer> hashIds;
        try {
            hashIds = TaskWatcherDataHolder.getInstance().getHeartBeatService().getActiveServers().keySet();
        } catch (HeartBeatManagementException e) {
            String msg = "Unexpected exception when getting hash indexes of active servers";
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }

        for (Tenant tenant : tenants) {
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
                for (DynamicTask dt : dynamicTasks) {
                    for (int hid : hashIds) {
                        if (tenant.getId() == dt.getTenantId() &&
                                taskInfo.getName().equals(TaskManagementUtil.generateTaskId(dt.getDynamicTaskId(), hid))) {
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

}
