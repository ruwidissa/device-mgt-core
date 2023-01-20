/*
 * Copyright (c) 2023, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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

package io.entgra.notification.logger;

import io.entgra.device.mgt.extensions.logger.LogContext;

public class DeviceLogContext extends LogContext {
    private final String deviceName;
    private final String operationCode;
    private final String deviceType;
    private final String tenantID;

    private DeviceLogContext(Builder builder) {
        this.operationCode = builder.operationCode;
        this.deviceName = builder.deviceName;
        this.deviceType = builder.deviceType;
        this.tenantID = builder.tenantID;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public String getDeviceType() {
        return deviceType;
    }


    public static class Builder {
        private String deviceName;
        private String operationCode;
        private String deviceType;
        private String tenantID;

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

        public String getDeviceName() {
            return deviceName;
        }

        public Builder setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public String getOperationCode() {
            return operationCode;
        }

        public Builder setOperationCode(String operationCode) {
            this.operationCode = operationCode;
            return this;
        }

        public DeviceLogContext build() {
            return new DeviceLogContext(this);
        }

    }
}
