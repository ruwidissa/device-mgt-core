/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
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

package io.entgra.server.bootup.heartbeat.beacon.service;

import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;

import java.util.Map;

public interface HeartBeatManagementService {

    boolean isTaskPartitioningEnabled() throws HeartBeatManagementException;

    ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException;

    String updateServerContext(ServerContext ctx) throws HeartBeatManagementException;

    boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException;

    void electCandidate(int elapsedTimeInSeconds) throws HeartBeatManagementException;

    boolean updateTaskExecutionAcknowledgement(String newTask) throws HeartBeatManagementException;

    boolean isQualifiedToExecuteTask() throws HeartBeatManagementException;

    Map<Integer, ServerContext> getActiveServers() throws HeartBeatManagementException;
}
