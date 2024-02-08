/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class ActiveSyncDevice {
    @JsonProperty(value = "DeviceID", required = true)
    private String deviceId;
    @JsonProperty(value = "FirstSyncTime", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date firstSyncTime;
    @JsonProperty(value = "UserPrincipalName", required = true)
    private String userPrincipalName;
    @JsonProperty(value = "Identity", required = true)
    private String identity;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getFirstSyncTime() {
        return firstSyncTime;
    }

    public void setFirstSyncTime(Date firstSyncTime) {
        this.firstSyncTime = firstSyncTime;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveSyncDevice)) return false;
        ActiveSyncDevice that = (ActiveSyncDevice) o;
        return Objects.equals(deviceId, that.deviceId)
                && Objects.equals(userPrincipalName, that.userPrincipalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, userPrincipalName, identity);
    }
}
