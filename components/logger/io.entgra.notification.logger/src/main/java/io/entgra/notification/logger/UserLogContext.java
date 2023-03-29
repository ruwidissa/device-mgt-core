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

public class UserLogContext extends LogContext {
    private final String userName;
    private final String userEmail;
    private final String metaInfo;
    private final String tenantID;

    private UserLogContext(Builder builder) {
        this.userEmail = builder.userEmail;
        this.userName = builder.userName;
        this.metaInfo = builder.metaInfo;
        this.tenantID = builder.tenantID;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMetaInfo() {
        return metaInfo;
    }


    public static class Builder {
        private String userName;
        private String userEmail;
        private String metaInfo;
        private String tenantID;

        public Builder() {
        }

        public String getMetaInfo() {
            return metaInfo;
        }

        public Builder setMetaInfo(String metaInfo) {
            this.metaInfo = metaInfo;
            return this;
        }

        public String getTenantID() {
            return tenantID;
        }

        public Builder setTenantID(String tenantID) {
            this.tenantID = tenantID;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public Builder setUserEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public UserLogContext build() {
            return new UserLogContext(this);
        }

    }
}
