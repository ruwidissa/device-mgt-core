/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.ui.request.interceptor.util;

public class HandlerConstants {
    public static final String PUBLISHER_APPLICATION_NAME = "application-mgt-publisher";
    public static final String APP_REG_ENDPOINT = "/api-application-registration/register";
    public static final String UI_CONFIG_ENDPOINT = "/api/device-mgt-config/v1.0/configurations/ui-config";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String INTROSPECT_ENDPOINT = "/oauth2/introspect";
    public static final String AUTHORIZATION_ENDPOINT = "/oauth2/authorize";
    public static final String APIM_APPLICATIONS_ENDPOINT = "/api/am/store/v0.12/applications/";
    public static final String IDENTITY_APP_MGT_ENDPOINT = "/services/IdentityApplicationManagementService.IdentityApplicationManagementServiceHttpsSoap11Endpoint";
    public static final String LOGIN_PAGE = "/login";
    public static final String SSO_LOGIN_CALLBACK = "/ssoLoginCallback";
    public static final String BASIC = "Basic ";
    public static final String BEARER = "Bearer ";
    public static final String TAGS_KEY = "tags";
    public static final String APP_NAME_KEY = "applicationName";
    public static final String SESSION_AUTH_DATA_KEY = "authInfo";
    public static final String SESSION_DEFAULT_AUTH_DATA_KEY = "defaultAuthInfo";
    public static final String UI_CONFIG_KEY = "ui-config";
    public static final String CALLBACK_URL_KEY = "callbackUrl";
    public static final String IS_ALLOWED_TO_ALL_DOMAINS_KEY = "isAllowedToAllDomains";
    public static final String JSESSIONID_KEY = "JSESSIONID";
    public static final String COMMON_AUTH_ID_KEY = "commonAuthId";
    public static final String PLATFORM = "platform";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String API_COMMON_CONTEXT = "/api";
    public static final String EXECUTOR_EXCEPTION_PREFIX = "ExecutorException-";
    public static final String TOKEN_IS_EXPIRED = "ACCESS_TOKEN_IS_EXPIRED";
    public static final String REPORTS = "Reports";
    public static final String APP_NAME = "App-Name";
    public static final String[] SSO_LOGOUT_COOKIE_PATHS = new String[]{"/", "/entgra-ui-request-handler",
            "/store-ui-request-handler", "/publisher-ui-request-handler", "/mdm-reports-ui-request-handler", "/devicemgt"};
    public static final String CODE_GRANT_TYPE = "authorization_code";
    public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String JWT_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    public static final String PRODUCTION_KEY = "PRODUCTION";
    public static final String LOGIN_CACHE = "LOGIN_CACHE";

    public static final String SCHEME_SEPARATOR = "://";
    public static final String COLON = ":";
    public static final String HTTP_PROTOCOL = "http";
    public static final String UNDERSCORE = "_";

    public static final int INTERNAL_ERROR_CODE = 500;
    public static final long TIMEOUT = 1200;

    public static final String OTP_HEADER = "one-time-token";

    public static final String AX_PREFIX = "ax2317:";
    public static final String PAYLOADS_DIR = "repository/resources/payloads";
    public static final String SOAP_ACTION_HEADER = "SOAPAction";

    public static final String WSS_PROTOCOL = "wss";
    public static final String REMOTE_SESSION_CONTEXT = "/remote/session/clients";

    public static final String IOT_CORE_HOST_ENV_VAR = "iot.core.host";
    public static final String IOT_CORE_HTTP_PORT_ENV_VAR = "iot.core.http.port";
    public static final String IOT_CORE_HTTPS_PORT_ENV_VAR = "iot.core.https.port";
    public static final String IOT_GW_HOST_ENV_VAR = "iot.gateway.host";
    public static final String IOT_GW_HTTP_PORT_ENV_VAR = "iot.gateway.http.port";
    public static final String IOT_GW_HTTPS_PORT_ENV_VAR = "iot.gateway.https.port";
    public static final String USER_SCOPES = "userScopes";
}
