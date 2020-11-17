package org.wso2.carbon.device.mgt.core.task.impl;

import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.ntask.core.Task;


public abstract class DynamicPartitionedScheduleTask implements Task {

    private static final Log log = LogFactory.getLog(DynamicPartitionedScheduleTask.class);

    private static int serverHashIndex;
    private static int activeServerCount;

    @Override
    public final void init() {
        try {
            ServerCtxInfo ctxInfo = DeviceManagementDataHolder.getInstance().getHeartBeatService().getServerCtxInfo();
            if(ctxInfo!=null){
                activeServerCount = ctxInfo.getActiveServerCount();
                serverHashIndex = ctxInfo.getLocalServerHashIdx();
                setup();
            } else {
                log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function.");
            }
        } catch (HeartBeatManagementException e) {
            log.error("Error Instantiating Variables necessary for Dynamic Task Scheduling. Dynamic Tasks will not function." , e);
        }
    }

    protected abstract void setup();

    public int getLocalServerHash(){
        return serverHashIndex;
    }

    public int getActiveServerCount(){
        return activeServerCount;
    }
}
