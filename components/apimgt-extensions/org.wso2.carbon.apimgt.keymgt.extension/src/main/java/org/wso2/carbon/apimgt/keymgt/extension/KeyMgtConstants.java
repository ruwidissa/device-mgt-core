/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.extension;

public class KeyMgtConstants {
    public static final String CUSTOM_TYPE = "CustomKeyManager";
    public static final String RESERVED_OAUTH_APP_NAME_PREFIX = "reserved_app_for_";
    public static final String SUPER_TENANT = "carbon.super";
    public static final String DEFAULT_ADMIN_SCOPES =
            "openid apim:admin apim:admin_operations apim:subscribe apim:app_manage apim:sub_manage";
    public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    public static final String CONSUMER_KEY_CLAIM = "azp";
    public static final String SCOPE_CLAIM = "scope";
    public static final String REFERENCE = "REFERENCE";
    public static final String TOKEN_REGEX =
            "^<<tenantId>>*_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";
    public static final int TOKEN_VALIDITY_PERIOD = 3600;
    public static final String CUSTOM_KEY_MANAGER_NAME_PREFIX = "KM_";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String X_WSO2_TENANT_HEADER = "X-WSO2-Tenant";

    public static final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
    public static final String DCR_ENDPOINT = "/api-application-registration/register";
    public static final String INTROSPECT_ENDPOINT = "/oauth2/introspect";
    public static final String CLIENT_REGISTRATION_ENDPOINT = "/keymanager-operations/dcr/register";
    public static final String REVOKE_ENDPOINT = "";
    public static final String USER_INFO_ENDPOINT = "/oauth2/userInfo";
    public static final String AUTHORIZE_ENDPOINT = "/oauth2/authorize";
    public static final String SCOPE_MANAGEMENT_ENDPOINT = "/api/identity/oauth2/v1.0/scopes";
    public static final String JWKS_ENDPOINT = "/oauth2/jwks";
    public static final String CREATE_KEY_MANAGER_ENDPOINT = "/api/am/admin/v2/key-managers";
    public static final String APPLICATION_KEY_MAPPING_ENDPOINT =
            "/api/am/devportal/v2/applications/<applicationId>/map-keys";
    public static final String APPLICATION_TOKEN_ENDPOINT =
            "/api/am/devportal/v2/applications/<applicationId>/oauth-keys/<keyMappingId>/generate-token";
}
