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
    public static final String LOGIN_PAGE = "/login";
    public static final String BASIC = "Basic ";
    public static final String BEARER = "Bearer ";
    public static final String TAGS_KEY = "tags";
    public static final String APP_NAME_KEY = "applicationName";
    public static final String SESSION_AUTH_DATA_KEY = "authInfo";
    public static final String SESSION_DEFAULT_AUTH_DATA_KEY = "defaultAuthInfo";
    public static final String UI_CONFIG_KEY = "ui-config";
    public static final String PLATFORM = "platform";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String API_COMMON_CONTEXT = "/api";
    public static final String EXECUTOR_EXCEPTION_PREFIX = "ExecutorException-";
    public static final String TOKEN_IS_EXPIRED = "ACCESS_TOKEN_IS_EXPIRED";
    public static final String REPORTS = "Reports";
    public static final String APP_NAME = "App-Name";

    public static final String SCHEME_SEPARATOR = "://";
    public static final String COLON = ":";
    public static final String HTTP_PROTOCOL = "http";

    public static final int INTERNAL_ERROR_CODE = 500;
    public static final long TIMEOUT = 1200;
}
