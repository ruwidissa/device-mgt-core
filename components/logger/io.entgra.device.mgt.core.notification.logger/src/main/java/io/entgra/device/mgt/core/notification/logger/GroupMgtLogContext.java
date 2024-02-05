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

public class GroupMgtLogContext extends LogContext {
    private final String groupId;
    private final String name;
    private final String owner;
    private final String actionTag;
    private final String roles;
    private final String deviceCount;
    private final String deviceIdentifiers;
    private final String userName;
    private final String tenantID;
    private final String tenantDomain;

    private GroupMgtLogContext(Builder builder) {
        this.groupId = builder.groupId;
        this.name = builder.name;
        this.owner = builder.owner;
        this.actionTag = builder.actionTag;
        this.roles = builder.roles;
        this.deviceCount = builder.deviceCount;
        this.deviceIdentifiers = builder.deviceIdentifiers;
        this.userName = builder.userName;
        this.tenantID = builder.tenantID;
        this.tenantDomain = builder.tenantDomain;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getActionTag() {
        return actionTag;
    }

    public String getRoles() {
        return roles;
    }

    public String getDeviceCount() {
        return deviceCount;
    }

    public String getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public String getUserName() {
        return userName;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public static class Builder {
        private String groupId;
        private String name;
        private String owner;
        private String actionTag;
        private String roles;
        private String deviceCount;
        private String deviceIdentifiers;
        private String userName;
        private String tenantID;
        private String tenantDomain;

        public Builder() {
        }

        public String getGroupId() {
            return groupId;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getOwner() {
            return owner;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public String getActionTag() {
            return actionTag;
        }

        public Builder setActionTag(String actionTag) {
            this.actionTag = actionTag;
            return this;
        }

        public String getRoles() {
            return roles;
        }

        public Builder setRoles(String roles) {
            this.roles = roles;
            return this;
        }

        public String getDeviceCount() {
            return deviceCount;
        }

        public Builder setDeviceCount(String deviceCount) {
            this.deviceCount = deviceCount;
            return this;
        }

        public String getDeviceIdentifiers() {
            return deviceIdentifiers;
        }

        public Builder setDeviceIdentifiers(String deviceIdentifiers) {
            this.deviceIdentifiers = deviceIdentifiers;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
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

        public GroupMgtLogContext build() {
            return new GroupMgtLogContext(this);
        }
    }
}
