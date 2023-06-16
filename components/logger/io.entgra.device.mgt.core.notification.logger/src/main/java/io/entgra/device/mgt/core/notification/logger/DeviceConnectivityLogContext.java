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

public class DeviceConnectivityLogContext extends LogContext {

    private final String deviceId;
    private final String deviceType;
    private final String actionTag;
    private final String operationCode;
    private final String tenantId;
    private final String tenantDomain;
    private final String userName;

    private DeviceConnectivityLogContext(Builder builder) {
        this.deviceId = builder.deviceId;
        this.deviceType = builder.deviceType;
        this.actionTag = builder.actionTag;
        this.operationCode = builder.operationCode;
        this.tenantId = builder.tenantId;
        this.tenantDomain = builder.tenantDomain;
        this.userName = builder.userName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getActionTag() {
        return actionTag;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public String getTenantId() {
        return tenantId;
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
        private String operationCode;
        private String actionTag;
        private String tenantId;
        private String tenantDomain;
        private String userName;

        public Builder() {
        }

        public String getDeviceId() {
            return deviceId;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public Builder setDeviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public String getOperationCode() {
            return operationCode;
        }

        public Builder setOperationCode(String operationCode) {
            this.operationCode = operationCode;
            return this;
        }

        public String getTenantId() {
            return tenantId;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
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

        public String getActionTag() {
            return actionTag;
        }

        public Builder setActionTag(String actionTag) {
            this.actionTag = actionTag;
            return this;
        }

        public DeviceConnectivityLogContext build() {
            return new DeviceConnectivityLogContext(this);
        }
    }
}
