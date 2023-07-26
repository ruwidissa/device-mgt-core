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

public class AppInstallLogContext extends LogContext {
    private final String appId;
    private final String appName;
    private final String appType;
    private final String subType;
    private final String tenantId;
    private final String tenantDomain;
    private final String device;
    private final String userName;
    private final String action;

    private AppInstallLogContext(Builder builder) {
        this.appId = builder.appId;
        this.appName = builder.appName;
        this.appType = builder.appType;
        this.subType = builder.subType;
        this.tenantId = builder.tenantId;
        this.tenantDomain = builder.tenantDomain;
        this.device = builder.device;
        this.userName = builder.userName;
        this.action = builder.action;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppType() {
        return appType;
    }

    public String getSubType() {
        return subType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getDevice() {
        return device;
    }

    public String getUserName() {
        return userName;
    }

    public String getAction() {
        return action;
    }

    public static class Builder {
        private String appId;
        private String appName;
        private String appType;
        private String subType;
        private String tenantId;
        private String tenantDomain;
        private String device;
        private String userName;
        private String action;

        public Builder() {
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getAppId() {
            return appId;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public String getAppName() {
            return appName;
        }

        public Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public String getAppType() {
            return appType;
        }

        public Builder setAppType(String appType) {
            this.appType = appType;
            return this;
        }

        public String getSubType() {
            return subType;
        }

        public Builder setSubType(String subType) {
            this.subType = subType;
            return this;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public Builder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public String getTenantId() {
            return tenantId;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public String getDevice() {
            return device;
        }

        public Builder setDevice(String device) {
            this.device = device;
            return this;
        }

        public String getAction() {
            return action;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public AppInstallLogContext build() {
            return new AppInstallLogContext(this);
        }
    }
}
