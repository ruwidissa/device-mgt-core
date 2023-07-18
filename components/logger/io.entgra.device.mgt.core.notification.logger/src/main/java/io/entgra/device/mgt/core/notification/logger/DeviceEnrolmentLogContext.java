/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.notification.logger;

import io.entgra.device.mgt.core.device.mgt.extensions.logger.LogContext;

public class DeviceEnrolmentLogContext extends LogContext {
    private final String deviceId;
    private final String deviceType;
    private final String owner;
    private final String ownership;
    private final String tenantID;
    private final String tenantDomain;
    private final String userName;

    private DeviceEnrolmentLogContext(Builder builder) {
        this.deviceId = builder.deviceId;
        this.deviceType = builder.deviceType;
        this.owner = builder.owner;
        this.ownership = builder.ownership;
        this.tenantID = builder.tenantID;
        this.tenantDomain = builder.tenantDomain;
        this.userName = builder.userName;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getOwner() {
        return owner;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getOwnership() {
        return ownership;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getUserName() {
        return userName;
    }
    public static class Builder {
        private String deviceId;
        private String deviceType;
        private String owner;
        private String ownership;
        private String tenantID;
        private String tenantDomain;
        private String userName;

        public Builder() {
        }

        public String getDeviceType() {
            return deviceType;
        }

        public Builder setDeviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public String getTenantID() {
            return tenantID;
        }

        public Builder setTenantID(String tenantID) {
            this.tenantID = tenantID;
            return this;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public Builder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public String getOwner() {
            return owner;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public String getOwnership() {
            return ownership;
        }

        public Builder setOwnership(String ownership) {
            this.ownership = ownership;
            return this;
        }

        public DeviceEnrolmentLogContext build() {
            return new DeviceEnrolmentLogContext(this);
        }

    }
}
