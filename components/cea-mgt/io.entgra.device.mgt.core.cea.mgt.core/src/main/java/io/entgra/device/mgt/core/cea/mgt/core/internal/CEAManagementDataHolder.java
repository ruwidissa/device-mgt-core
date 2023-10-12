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

package io.entgra.device.mgt.core.cea.mgt.core.internal;

import io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager;
import io.entgra.device.mgt.core.cea.mgt.core.task.CEAPolicyMonitoringTaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

public class CEAManagementDataHolder {
    private EnforcementServiceManager enforcementServiceManager;
    private TaskService taskService;
    private CEAPolicyMonitoringTaskManager ceaPolicyMonitoringTaskManager;

    private CEAManagementDataHolder() {}

    public static CEAManagementDataHolder getInstance() {
        return CEAManagementDataHolderRegistry.INSTANCE;
    }

    public EnforcementServiceManager getEnforcementServiceManager() {
        return enforcementServiceManager;
    }

    public void setEnforcementServiceManager(EnforcementServiceManager enforcementServiceManager) {
        this.enforcementServiceManager = enforcementServiceManager;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public CEAPolicyMonitoringTaskManager getCeaPolicyMonitoringTaskManager() {
        return ceaPolicyMonitoringTaskManager;
    }

    public void setCeaPolicyMonitoringTaskManager(CEAPolicyMonitoringTaskManager ceaPolicyMonitoringTaskManager) {
        this.ceaPolicyMonitoringTaskManager = ceaPolicyMonitoringTaskManager;
    }

    private static class CEAManagementDataHolderRegistry {
        public static final CEAManagementDataHolder INSTANCE = new CEAManagementDataHolder();
    }
}
