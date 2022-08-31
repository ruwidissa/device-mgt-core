/*
 *   Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;

public class TrackerDeviceInfo implements Serializable {

    private static final long serialVersionUID = 1998101712L;

    private int id;
    private int traccarDeviceId;
    private int deviceId;
    private int tenantId;
    private int status;

    public TrackerDeviceInfo() {
    }

    public TrackerDeviceInfo(int traccarDeviceId, int deviceId, int tenantId) {
        this.traccarDeviceId = traccarDeviceId;
        this.deviceId = deviceId;
        this.tenantId = tenantId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTraccarDeviceId() {
        return traccarDeviceId;
    }

    public void setTraccarDeviceId(int traccarDeviceId) {
        this.traccarDeviceId = traccarDeviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
