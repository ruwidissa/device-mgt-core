/*
 *   Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.core.task.impl;

import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.ntask.core.Task;


public abstract class RandomlyAssignedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(RandomlyAssignedScheduleTask.class);

    private static String taskName = "UNSPECIFIED";
    private static boolean qualifiedToExecuteTask = false;
    private static boolean dynamicTaskEnabled = false;

    @Override
    public final void init() {
        try {
            dynamicTaskEnabled = DeviceManagementDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled();
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Randomly Assigned Task Scheduling." , e);
        }
        //This is done so that sub class extending this abstract class is forced to specify a task name.
        taskName = getTaskName();
        setup();
    }

    @Override
    public final void execute() {
        refreshContext();
        executeRandomlyAssignedTask();
    }

    public void refreshContext(){
        if(dynamicTaskEnabled) {
            try {
                qualifiedToExecuteTask = DeviceManagementDataHolder.getInstance().getHeartBeatService().isQualifiedToExecuteTask();
                log.info("## NODE Qualified to execute Randomly Assigned Task : " + taskName);
                DeviceManagementDataHolder.getInstance().getHeartBeatService().updateTaskExecutionAcknowledgement(taskName);
            } catch (HeartBeatManagementException e) {
                log.error("Error refreshing Variables necessary for Randomly Assigned Scheduled Task. " +
                          "Dynamic Tasks will not function.", e);
            }
        } else {
            qualifiedToExecuteTask = true;
        }
    }

    protected abstract void setup();

    protected abstract void executeRandomlyAssignedTask();

    public static boolean isQualifiedToExecuteTask() {
        return qualifiedToExecuteTask;
    }

    public abstract String getTaskName();
}
