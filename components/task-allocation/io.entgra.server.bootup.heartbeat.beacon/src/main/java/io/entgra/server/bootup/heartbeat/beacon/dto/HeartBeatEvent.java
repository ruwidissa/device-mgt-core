/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package io.entgra.server.bootup.heartbeat.beacon.dto;

import java.sql.Timestamp;

public class HeartBeatEvent {

    private String serverUUID;
    private Timestamp time;

    public HeartBeatEvent(String serverUUID){
        this.serverUUID = serverUUID;
        this.time = new Timestamp(System.currentTimeMillis());
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

}
