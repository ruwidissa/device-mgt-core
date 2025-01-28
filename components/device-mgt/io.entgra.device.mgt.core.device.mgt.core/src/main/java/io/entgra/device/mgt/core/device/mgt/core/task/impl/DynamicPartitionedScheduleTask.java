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

import io.entgra.device.mgt.core.device.mgt.common.DynamicTaskContext;
import io.entgra.device.mgt.core.device.mgt.common.ServerCtxInfo;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.task.mgt.common.constant.TaskMgtConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;


public abstract class DynamicPartitionedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(DynamicPartitionedScheduleTask.class);

    private volatile DynamicTaskContext taskContext = null;
    private Map<String, String> properties;

    @Override
    public final void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public final String getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    @Override
    public final void init() {
        try {
            boolean dynamicTaskEnabled = DeviceManagementDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled();
            if (dynamicTaskEnabled) {
                taskContext = new DynamicTaskContext();
                taskContext.setPartitioningEnabled(true);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.");
                }
            }
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.", e);
        }
        setup();
    }

    @Override
    public final void execute() {
        refreshContext();
        if (taskContext != null && taskContext.isPartitioningEnabled()) {
            String localHashIndex = getProperty(TaskMgtConstants.Task.LOCAL_HASH_INDEX);
            // These tasks are not dynamically scheduled. They are added via a config so scheduled in each node
            // during the server startup
            if (localHashIndex == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Executing startup scheduled task (" + getTaskName() + ") with class: " +
                            this.getClass().getName());
                }
                executeDynamicTask();
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Local hash index: " + localHashIndex + ", Server hash index: " +
                        taskContext.getServerHashIndex());
            }
            if (localHashIndex.equals(String.valueOf(taskContext.getServerHashIndex()))) {
                if (log.isDebugEnabled()) {
                    log.debug("Executing dynamically scheduled task (" + getTaskName() +
                            ") for current server hash index: " + taskContext.getServerHashIndex());
                }
                executeDynamicTask();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring execution of task (" + getTaskName() +
                            ") not belonging to current server hash index: " + taskContext.getServerHashIndex());
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Task context is null or partitioning is not enabled. Executing dynamic task.");
            }
            executeDynamicTask();
        }
    }

    public String getTaskName() {
        return getProperty(TaskMgtConstants.Task.LOCAL_TASK_NAME);
    }

    public synchronized void refreshContext() {
        if (taskContext != null && taskContext.isPartitioningEnabled()) {
            try {
                updateContext();
            } catch (HeartBeatManagementException e) {
                log.error("Error refreshing Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.", e);
            }
        }
    }

    private synchronized void updateContext() throws HeartBeatManagementException {
        ServerCtxInfo ctxInfo = DeviceManagementDataHolder.getInstance().getHeartBeatService().getServerCtxInfo();
        if (ctxInfo != null) {
            if (log.isDebugEnabled()) {
                log.debug("Received ServerCtxInfo. ActiveServerCount: " + ctxInfo.getActiveServerCount() +
                        ", LocalServerHashIdx: " + ctxInfo.getLocalServerHashIdx());
            }
            populateContext(ctxInfo);
        } else {
            log.info("Dynamic Task Context not present. Tasks will run on regular worker/manager mode.");
        }
    }

    private synchronized void populateContext(ServerCtxInfo ctxInfo) {
        if (log.isDebugEnabled()) {
            log.debug("Populating task context with ServerCtxInfo. " +
                    "ActiveServerCount: " + ctxInfo.getActiveServerCount() +
                    ", LocalServerHashIdx: " + ctxInfo.getLocalServerHashIdx());
        }
        taskContext.setActiveServerCount(ctxInfo.getActiveServerCount());
        taskContext.setServerHashIndex(ctxInfo.getLocalServerHashIdx());

        if (log.isDebugEnabled()) {
            log.debug("Initiating execution of dynamic task for server : " + taskContext.getServerHashIndex() +
                    " where active server count is : " + taskContext.getActiveServerCount() +
                    " partitioning task enabled : " + taskContext.isPartitioningEnabled());
        }
    }

    protected abstract void setup();

    protected abstract void executeDynamicTask();

    public DynamicTaskContext getTaskContext() {
        return taskContext;
    }

    @Deprecated
    public boolean isDynamicTaskEligible() {
        return taskContext != null && taskContext.isPartitioningEnabled();
    }

}
