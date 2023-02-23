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
package io.entgra.task.mgt.core.internal;

import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import io.entgra.task.mgt.common.spi.TaskManagementService;
import org.wso2.carbon.ntask.core.service.TaskService;

public class TaskManagerDataHolder {
    private TaskManagementService taskManagerService;
    private TaskService nTaskService;

    private HeartBeatManagementService heartBeatService;

    private static final TaskManagerDataHolder thisInstance = new TaskManagerDataHolder();

    private TaskManagerDataHolder() {
    }

    public static TaskManagerDataHolder getInstance() {
        return thisInstance;
    }

    public TaskManagementService getTaskManagementService() {
        return taskManagerService;
    }

    public void setTaskManagementService(TaskManagementService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    public TaskService getnTaskService() {
        return nTaskService;
    }

    public void setnTaskService(TaskService nTaskService) {
        this.nTaskService = nTaskService;
    }

    public HeartBeatManagementService getHeartBeatService() {
        return heartBeatService;
    }

    public void setHeartBeatService(HeartBeatManagementService heartBeatService) {
        this.heartBeatService = heartBeatService;
    }
}
