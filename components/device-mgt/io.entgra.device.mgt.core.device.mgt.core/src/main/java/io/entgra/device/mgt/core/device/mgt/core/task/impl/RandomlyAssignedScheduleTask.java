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

package io.entgra.device.mgt.core.device.mgt.core.task.impl;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.ntask.core.Task;


public abstract class RandomlyAssignedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(RandomlyAssignedScheduleTask.class);
    private static boolean qualifiedToExecuteTask = false;
    private static boolean dynamicTaskEnabled = false;

    @Override
    public final void init() {
        try {
            dynamicTaskEnabled = DeviceManagementDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled();
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Randomly Assigned Task Scheduling.", e);
        }
        setup();
    }

    @Override
    public final void execute() {
        refreshContext();
        if (isQualifiedToExecuteTask()) {
            executeRandomlyAssignedTask();
        }
    }

    public void refreshContext() {
        if (dynamicTaskEnabled) {
            try {
                qualifiedToExecuteTask = DeviceManagementDataHolder.getInstance().getHeartBeatService().isQualifiedToExecuteTask();
            } catch (HeartBeatManagementException e) {
                log.error("Error refreshing variables necessary for " +
                        "Randomly Assigned Scheduled Task: " + getTaskName(), e);
            }
        } else {
            qualifiedToExecuteTask = true;
        }
        log.info("Node is " + (qualifiedToExecuteTask ? "" : "not")
                + " qualified to execute Randomly Assigned Task : " + getTaskName());
    }

    protected abstract void setup();

    protected abstract void executeRandomlyAssignedTask();

    public static boolean isQualifiedToExecuteTask() {
        return qualifiedToExecuteTask;
    }

    public abstract String getTaskName();

}
