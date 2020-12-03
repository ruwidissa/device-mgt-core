package org.wso2.carbon.policy.mgt.core.mock;

import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;

public class TestHeartBeatManagementService implements HeartBeatManagementService {
    @Override
    public boolean isTaskPartitioningEnabled() throws HeartBeatManagementException {
        return false;
    }

    @Override
    public ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException {
        return null;
    }

    @Override
    public String updateServerContext(ServerContext ctx) throws HeartBeatManagementException {
        return null;
    }

    @Override
    public boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException {
        return false;
    }

    @Override
    public void electCandidate(int elapsedTimeInSeconds) throws HeartBeatManagementException {

    }

    @Override
    public boolean updateTaskExecutionAcknowledgement(String newTask)
            throws HeartBeatManagementException {
        return false;
    }

    @Override
    public boolean isQualifiedToExecuteTask() throws HeartBeatManagementException {
        return false;
    }
}
