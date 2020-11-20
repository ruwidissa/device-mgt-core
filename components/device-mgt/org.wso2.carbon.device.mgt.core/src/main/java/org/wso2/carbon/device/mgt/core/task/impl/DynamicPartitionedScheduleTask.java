package org.wso2.carbon.device.mgt.core.task.impl;

import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.ntask.core.Task;


public abstract class DynamicPartitionedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(DynamicPartitionedScheduleTask.class);

    private static DynamicTaskContext taskContext = null;

    @Override
    public final void init() {
        try {
            ServerCtxInfo ctxInfo = DeviceManagementDataHolder.getInstance().getHeartBeatService().getServerCtxInfo();
            if(ctxInfo!=null){
                taskContext = new DynamicTaskContext();
                taskContext.setActiveServerCount(ctxInfo.getActiveServerCount());
                taskContext.setServerHashIndex(ctxInfo.getLocalServerHashIdx());

                if(ctxInfo.getActiveServerCount() > 0){
                    taskContext.setPartitioningEnabled(true);
                }

                if(log.isDebugEnabled()){
                    log.debug("Initiating execution of dynamic task for server : " + taskContext.getServerHashIndex() +
                              " where active server count is : " + taskContext.getActiveServerCount() +
                              " partitioning task enabled : " + taskContext.isPartitioningEnabled());
                }
            } else {
                log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.");
            }
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function." , e);
        }
        setup();
    }

    protected abstract void setup();

    public static DynamicTaskContext getTaskContext() {
        return taskContext;
    }

}
