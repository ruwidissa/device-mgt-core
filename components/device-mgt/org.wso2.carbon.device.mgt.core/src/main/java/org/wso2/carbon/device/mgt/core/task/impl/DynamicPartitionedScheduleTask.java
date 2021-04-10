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
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;


public abstract class DynamicPartitionedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(DynamicPartitionedScheduleTask.class);

    private static DynamicTaskContext taskContext = null;
    private Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    @Override
    public final void init() {
        try {
            boolean dynamicTaskEnabled = DeviceManagementDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled();
            if(dynamicTaskEnabled){
                taskContext = new DynamicTaskContext();
                taskContext.setPartitioningEnabled(true);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.");
                }
            }
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function." , e);
        }
        setup();
    }

    @Override
    public final void execute() {
        refreshContext();
        executeDynamicTask();
    }

    public void refreshContext(){
        if(taskContext != null && taskContext.isPartitioningEnabled()) {
            try {
                updateContext();
            } catch (HeartBeatManagementException e) {
                log.error("Error refreshing Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.", e);
            }
        }
    }

    private void updateContext() throws HeartBeatManagementException {
        ServerCtxInfo ctxInfo = DeviceManagementDataHolder.getInstance().getHeartBeatService().getServerCtxInfo();
        if(ctxInfo != null) {
            populateContext(ctxInfo);
        } else {
            log.info("Dynamic Task Context not present. Tasks will run on regular worker/manager mode.");
        }
    }

    private void populateContext(ServerCtxInfo ctxInfo) {
        taskContext.setActiveServerCount(ctxInfo.getActiveServerCount());
        taskContext.setServerHashIndex(ctxInfo.getLocalServerHashIdx());

        if(log.isDebugEnabled()){
            log.debug("Initiating execution of dynamic task for server : " + taskContext.getServerHashIndex() +
                      " where active server count is : " + taskContext.getActiveServerCount() +
                      " partitioning task enabled : " + taskContext.isPartitioningEnabled());
        }
    }

    protected abstract void setup();

    protected abstract void executeDynamicTask();

    public static DynamicTaskContext getTaskContext() {
        return taskContext;
    }

    public static boolean isDynamicTaskEligible(){
        return taskContext != null && taskContext.isPartitioningEnabled();
    }

}
