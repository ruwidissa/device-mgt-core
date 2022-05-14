/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.common;

public class TraccarHandlerConstants {

    public static class TraccarConfig {
        public static final String TRACCAR_CONFIG_XML_NAME = "traccar-config.xml";
        public static final String GATEWAY_NAME = "sample";
        public static final String ENDPOINT = "api-endpoint";
        public static final String AUTHORIZATION = "authorization";
        public static final String AUTHORIZATION_KEY = "authorization-key";
        public static final String DEFAULT_PORT = "default-port";
        public static final String LOCATION_UPDATE_PORT = "location-update-port";
    }

    public static class Methods {
        public static final String POST = "POST";
        public static final String GET = "GET";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
    }

    public static class Types {
        public static final String DEVICE = "DEVICE";
        public static final String GROUP = "GROUP";
        public static final String USER = "USER";
        public static final String PERMISSION = "PERMISSION";

        public static final String USER_CREATE = "USER_CREATE";
        public static final String USER_CREATE_WITH_INSERT_DEVICE = "USER_CREATE_WITH_INSERT_DEVICE";
        public static final String USER_UPDATE = "USER_UPDATE";
        public static final String USER_UPDATE_WITH_INSERT_DEVICE = "USER_UPDATE_WITH_INSERT_DEVICE";
        public static final String USER_SEARCH = "USER_SEARCH";
        public static final String FETCH_ALL_USERS = "FETCH_ALL_USERS";
        public static final String FETCH_ALL_DEVICES = "FETCH_ALL_DEVICES";

        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

        public static final int DEFAULT_RANDOM = 10;
        public static final int TRACCAR_TOKEN = 32;
    }

}
