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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.constants;

public final class Constants {

    private Constants() {
    }

    public static final String EMPTY_STRING = "";
    public static final String AMPERSAND = "&";
    public static final String SLASH = "/";
    public static final String EQUAL = "=";
    public static final String SERVER_USER = "WorkflowConfigurations.ServerUser";
    public static final String SERVER_PASSWORD = "WorkflowConfigurations.ServerPassword";
    public static final String REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME = "refresh_token";
    public static final String OAUTH_EXPIRES_IN = "expires_in";
    public static final String OAUTH_TOKEN_SCOPE = "scope";
    public static final String OAUTH_TOKEN_TYPE = "token_type";
    public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    public static final String SCOPE_PARAM_NAME = "scope";
    public static final String SCOPES = "apim:api_create apim:api_view apim:shared_scope_manage apim:api_import_export " +
            "apim:api_publish apim:admin apim:api_key apim:app_import_export apim:app_manage apim:store_settings " +
            "apim:sub_alert_manage apim:sub_manage apim:subscribe openid";
    public static final String DCR_END_POINT = "WorkflowConfigurations.DCREndPoint";
    public static final String TOKE_END_POINT = "WorkflowConfigurations.TokenEndPoint";
    public static final String ADAPTER_CONF_KEEP_ALIVE = "keepAlive";
    public static final int ADAPTER_CONF_DEFAULT_KEEP_ALIVE = 60000;
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Basic ";
    public static final String AUTHORIZATION_HEADER_PREFIX_BEARER = "Bearer ";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String PASSWORD_GRANT_TYPE_USERNAME = "username";
    public static final String PASSWORD_GRANT_TYPE_PASSWORD = "password";
    public static final String PASSWORD_GRANT_TYPE_SCOPES = "scopes";
    public static final String ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME = "access_token";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final String SCHEME_SEPARATOR = "://";
    public static final String COLON = ":";
    public static final String QUERY_KEY_VALUE_SEPARATOR = "=";
    public static final String SPACE = " ";
    public static final String IOT_CORE_HOST = "iot.core.host";
    public static final String IOT_CORE_HTTPS_PORT = "iot.core.https.port";
    public static final String GET_ALL_SCOPES = "/api/am/publisher/v4/scopes?limit=1000";
    public static final String SCOPE_API_ENDPOINT = "/api/am/publisher/v4/scopes/";
    public static final String API_ENDPOINT = "/api/am/publisher/v4/apis/";
    public static final String GET_ALL_APIS = "/api/am/publisher/v4/apis?limit=1000";
    public static final String APPLICATIONS_API = "/api/am/devportal/v3/applications";
    public static final String SUBSCRIPTION_API = "/api/am/devportal/v3/subscriptions";
    public static final String DEV_PORTAL_API = "/api/am/devportal/v3/apis?limit=1000";
    public static final String KEY_MANAGERS_API = "/api/am/devportal/v3/key-managers";
    public static final String SCOPE_PUBLISH_RESERVED_USER_NAME = "scope_publish_reserved_user";
    public static final String SCOPE_PUBLISH_RESERVED_USER_PASSWORD = "&gKfyE8E4rUY4Q";
    public static final String ADMIN_ROLE_KEY = "admin";
    public static final String PERM_SCOPE_MAPPING_META_KEY  = "perm-scope-mapping";
    public static final String PLACEHOLDING_CALLBACK_URL = HTTPS_PROTOCOL + SCHEME_SEPARATOR + "localhost";
    public static final String API_PUBLISHING_ENABLED_TENANT_LIST_KEY = "api-publishing-enabled-tenant-list";
}
