/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.core.task;

import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAConfigManagerException;
import io.entgra.device.mgt.core.cea.mgt.core.bean.CEAConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.config.CEAConfigManager;
import io.entgra.device.mgt.core.cea.mgt.core.exception.CEAPolicyMonitoringTaskManagerException;
import io.entgra.device.mgt.core.cea.mgt.core.internal.CEAManagementDataHolder;
import io.entgra.device.mgt.core.cea.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class CEAPolicyMonitoringTaskManagerImpl implements CEAPolicyMonitoringTaskManager {
    private static final Log log = LogFactory.getLog(CEAPolicyMonitoringTaskManagerImpl.class);

    @Override
    public void startTask(long monitoringFrequency) throws CEAPolicyMonitoringTaskManagerException {
        if (monitoringFrequency <= 0) {
            throw new CEAPolicyMonitoringTaskManagerException("Invalid monitoring frequency");
        }
        TaskService taskService = CEAManagementDataHolder.getInstance().getTaskService();
        if (taskService == null) {
            throw new IllegalStateException("Task service is not initialized");
        }
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            CEAConfiguration ceaConfiguration = CEAConfigManager.getInstance().getCeaConfiguration();
            boolean isMonitoringEnable = ceaConfiguration.getMonitoringConfiguration().isMonitoringEnable();

            if (!isMonitoringEnable) {
                log.warn("CEA policy monitoring is disabled");
                return;
            }

            taskService.registerTaskType(Constants.CEA_MONITORING_TASK_TYPE);

            TaskManager taskManager = taskService.getTaskManager(Constants.CEA_MONITORING_TASK_TYPE);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setIntervalMillis(monitoringFrequency);
            triggerInfo.setRepeatCount(-1);

            Map<String, String> properties = new HashMap<>();
            properties.put(Constants.TENANT_ID_KEY, String.valueOf(tenantId));
            if (!taskManager.isTaskScheduled(Constants.CEA_MONITORING_TASK_NAME + tenantId)) {
                TaskInfo taskInfo = new TaskInfo(Constants.CEA_MONITORING_TASK_NAME + tenantId,
                        ceaConfiguration.getMonitoringConfiguration().getMonitoringClazz(), properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new CEAPolicyMonitoringTaskManagerException("CEA policy monitoring task is already active");
            }
        } catch (CEAConfigManagerException e) {
            String msg = "Error occurred while retrieving CEA config";
            log.error(msg, e);
            throw new CEAPolicyMonitoringTaskManagerException(msg, e);
        } catch (TaskException e) {
            String msg = "Error occurred while scheduling task for CEA policy monitoring";
            log.error(msg, e);
            throw new CEAPolicyMonitoringTaskManagerException(msg, e);
        }
    }

    @Override
    public void stopTask() throws CEAPolicyMonitoringTaskManagerException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            TaskService taskService = CEAManagementDataHolder.getInstance().getTaskService();
            if (taskService != null && taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(Constants.CEA_MONITORING_TASK_TYPE);
                taskManager.deleteTask(Constants.CEA_MONITORING_TASK_NAME + tenantId);
            }
        } catch (TaskException e) {
            String msg = "Error occurred while stopping the " + Constants.CEA_MONITORING_TASK_NAME + tenantId;
            log.error(msg, e);
            throw new CEAPolicyMonitoringTaskManagerException(msg, e);
        }
    }
}
