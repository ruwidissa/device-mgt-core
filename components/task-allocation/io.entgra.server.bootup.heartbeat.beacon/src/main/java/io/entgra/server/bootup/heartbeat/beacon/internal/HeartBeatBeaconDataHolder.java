/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.server.bootup.heartbeat.beacon.internal;

import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;

public class HeartBeatBeaconDataHolder {

    private HeartBeatManagementService heartBeatManagementService;
    private String localServerUUID;

    private static HeartBeatBeaconDataHolder thisInstance = new HeartBeatBeaconDataHolder();

    private HeartBeatBeaconDataHolder() {}

    public static HeartBeatBeaconDataHolder getInstance() {
        return thisInstance;
    }

    public HeartBeatManagementService getHeartBeatManagementService() {
        return heartBeatManagementService;
    }

    public void setHeartBeatManagementService(HeartBeatManagementService heartBeatManagementService) {
        this.heartBeatManagementService = heartBeatManagementService;
    }

    public String getLocalServerUUID() {
        return localServerUUID;
    }

    public void setLocalServerUUID(String localServerUUID) {
        this.localServerUUID = localServerUUID;
    }
}
