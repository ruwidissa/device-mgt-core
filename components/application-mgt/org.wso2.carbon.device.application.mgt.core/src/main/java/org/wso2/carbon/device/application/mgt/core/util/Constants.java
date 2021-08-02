/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.util;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Management related constants.
 */
public class Constants {

    public static final String APPLICATION_CONFIG_XML_FILE = "application-mgt.xml";

    public static final String DEFAULT_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.APPLICATION_CONFIG_XML_FILE;
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String PAYLOAD = "Payload";
    public static final String PLIST_NAME = "Info.plist";
    public static final String CF_BUNDLE_VERSION = "CFBundleVersion";
    public static final String APP_EXTENSION = ".app";
    public static final String IOT_CORE_HOST = "iot.core.host";
    public static final String IOT_CORE_HTTP_PORT = "iot.core.http.port";
    public static final String IOT_CORE_HTTPS_PORT = "iot.core.https.port";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";

    public static final String FORWARD_SLASH = "/";
    public static final String ANY = "ANY";
    public static final String DEFAULT_PCK_NAME = "default.app.com";
    public static final String ALL = "ALL";

    public static final String GOOGLE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String APPLE_STORE_URL = "https://itunes.apple.com/country/app/app-name/id";

    // Subscription task related constants
    public static final String SUBSCRIBERS = "SUBSCRIBERS";
    public static final String SUB_TYPE = "SUBSCRIPTION_TYPE";
    public static final String ACTION = "ACTION";
    public static final String APP_UUID = "APP_UUID";
    public static final String APP_PROPERTIES = "APP_PROPERTIES";
    public static final String SUBSCRIBER = "SUBSCRIBER";
    public static final String TENANT_DOMAIN = "TENANT_DOMAIN";
    public static final String TENANT_ID = "__TENANT_ID_PROP__";
    public static final String TASK_NAME = "TASK_NAME";
    public  static final String SUBSCRIBED = "SUBSCRIBED";
    public  static final String UNSUBSCRIBED = "UNSUBSCRIBED";
    public static final String APPLE_LOOKUP_URL = "https://itunes.apple.com/us/lookup?id=";

    //App type constants related to window device type
    public static final String MSI = "MSI";
    public static final String APPX = "APPX";

    private static final Map<String, String> AGENT_DATA = new HashMap<>();
    static {
        AGENT_DATA.put("android", "android-agent.apk");
        AGENT_DATA.put("ios", "ios.ipa");
    }
    public static final Map<String, String> AGENT_FILE_NAMES = Collections.unmodifiableMap(AGENT_DATA);


    /**
     * Database types supported by Application Management.
     */
    public static final class DataBaseTypes {
        private DataBaseTypes() {
        }
        public static final String DB_TYPE_MYSQL = "MySQL";
        public static final String DB_TYPE_ORACLE = "Oracle";
        public static final String DB_TYPE_MSSQL = "Microsoft SQL Server";
        public static final String DB_TYPE_DB2 = "DB2";
        public static final String DB_TYPE_H2 = "H2";
        public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";
    }

    /**
     * Directory name of the icon artifact that are saved in the file system.
     */
    public static final String ICON_ARTIFACT = "icon";

    /**
     * Directory name of the banner artifact that are saved in the file system.
     */
    public static final String BANNER_ARTIFACT = "banner";

    /**
     * Common directory name of the screenshot artifact that are saved in the file system.
     */
    public static final String SCREENSHOT_ARTIFACT = "screenshot";

    /**
     * Naming directory name of the application artifact that are saved in the file system.
     */
    public static final String APP_ARTIFACT = "app";

    public static final int REVIEW_PARENT_ID = -1;

    public static final int MAX_RATING = 5;

    public final class ApplicationInstall {
        private ApplicationInstall() {
            throw new AssertionError();
        }

        public static final String APPLICATION_NAME = "device_type_android";
        public static final String ENROLLMENT_APP_INSTALL_FEATURE_CODE = "ENROLLMENT_APP_INSTALL";
        public static final String DEFAULT_TOKEN_TYPE = "PRODUCTION";
        public static final String DEFAULT_VALIDITY_PERIOD = "3600";
        public static final String SUBSCRIPTION_SCOPE = "appm:subscribe";
        public static final String ENROLLMENT_APP_INSTALL_UUID = "uuid";
        public static final String GOOGLE_POLICY_PAYLOAD = "installGooglePolicyPayload";
        public static final String ENROLLMENT_APP_INSTALL_CODE = "enrollmentAppInstall";
        public static final String ENCODING = "UTF-8";
        public static final String AT = "@";
        public static final String DEVICE_TYPE_ANDROID = "android";
        public static final String COLON = ":";
        public static final String IOT_CORE_HOST = "iot.core.host";
        public static final String IOT_CORE_PORT = "iot.core.https.port";
        public static final String ENROLLMENT_APP_INSTALL_PROTOCOL = "https://";
        public static final String GOOGLE_APP_INSTALL_URL = "/api/device-mgt/android/v1.0/enterprise/change-app";

        public static final String AUTHORIZATION = "Authorization";
        public static final String AUTHORIZATION_HEADER_VALUE = "Bearer ";
    }
}
