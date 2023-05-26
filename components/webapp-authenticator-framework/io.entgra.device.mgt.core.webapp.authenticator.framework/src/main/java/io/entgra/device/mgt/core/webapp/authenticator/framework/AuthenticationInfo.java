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

package io.entgra.device.mgt.core.webapp.authenticator.framework;

import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.WebappAuthenticator;

/**
 * DTO class to hold the information of authenticated user AND STATUS.
 */
public class AuthenticationInfo {

    private WebappAuthenticator.Status status = WebappAuthenticator.Status.FAILURE;
    private String message;
    private String username;
    private String tenantDomain;
    private int tenantId = -1;
    private boolean isSuperTenantAdmin;

    public WebappAuthenticator.Status getStatus() {
        return status;
    }

    public void setStatus(
            WebappAuthenticator.Status status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isSuperTenantAdmin() {
        return isSuperTenantAdmin;
    }

    public void setSuperTenantAdmin(boolean superTenantAdmin) {
        isSuperTenantAdmin = superTenantAdmin;
    }
}
