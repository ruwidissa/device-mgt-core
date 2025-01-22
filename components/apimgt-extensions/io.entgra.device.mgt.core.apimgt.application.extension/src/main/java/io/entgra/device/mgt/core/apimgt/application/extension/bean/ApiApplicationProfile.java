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

package io.entgra.device.mgt.core.apimgt.application.extension.bean;

public class ApiApplicationProfile {
    private String applicationName;
    private String[] tags;
    private String callbackUrl;
    private String grantTypes;
    private String owner;
    private TOKEN_TYPE tokenType = TOKEN_TYPE.JWT;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

    public TOKEN_TYPE getTokenType() {
        return tokenType;
    }

    public void setTokenType(TOKEN_TYPE tokenType) {
        this.tokenType = tokenType;
    }

    public enum TOKEN_TYPE {
        JWT("JWT"), OPAQUE("DEFAULT");
        private final String value;

        TOKEN_TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
