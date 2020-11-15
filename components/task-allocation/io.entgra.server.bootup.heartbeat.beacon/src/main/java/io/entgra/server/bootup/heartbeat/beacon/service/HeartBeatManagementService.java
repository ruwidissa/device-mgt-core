/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.server.bootup.heartbeat.beacon.service;

import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;

public interface HeartBeatManagementService {

    int getActiveServerCount() throws HeartBeatManagementException;

    int getServerLocalHashIndex() throws HeartBeatManagementException;

    String updateServerContext(ServerContext ctx) throws HeartBeatManagementException;

    boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException;

}
