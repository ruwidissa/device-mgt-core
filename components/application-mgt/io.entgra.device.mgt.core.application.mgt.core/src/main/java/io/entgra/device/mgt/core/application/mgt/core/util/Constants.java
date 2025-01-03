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

package io.entgra.device.mgt.core.application.mgt.core.util;

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
    public static final String IDENTITY_SERVERS_CONFIG_XML_FILE = "identity-service-provider-config.xml";

    public static final String DEFAULT_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.APPLICATION_CONFIG_XML_FILE;
    public static final String DEFAULT_IDENTITY_SERVERS_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            IDENTITY_SERVERS_CONFIG_XML_FILE;
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String SCREENSHOT_NAME = "screenshot";
    public static final String ICON_NAME = "icon";
    public static final String PAYLOAD = "Payload";
    public static final String PLIST_NAME = "Info.plist";
    public static final String CF_BUNDLE_VERSION = "CFBundleVersion";
    public static final String APP_EXTENSION = ".app";
    public static final String IOT_CORE_HOST = "iot.core.host";
    public static final String IOT_CORE_HTTP_PORT = "iot.core.http.port";
    public static final String IOT_CORE_HTTPS_PORT = "iot.core.https.port";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final String SCHEME_SEPARATOR = "://";
    public static final String OPERATION_STATUS_UPDATE_API_BASE = "/api/device-mgt/v1.0/devices";
    public static final String OPERATION_STATUS_UPDATE_API_URI = "/operation";

    public static final String LIMIT_QUERY_PARAM = "limit";
    public static final String OFFSET_QUERY_PARAM = "offset";
    public static final String IS_APPS_API_CONTEXT_PATH = "identity-server-applications";
    public static final String IS_APPS_API_BASE_PATH = "identity-server-applications";
    public static final Double IS_APP_DEFAULT_PRICE = 0.0;
    public static final String SP_APP_CATEGORY = "SPApp";
    public static final String IS_APP_RELEASE_TYPE = "stable";
    public static final String IS_APP_DEFAULT_PAYMENT_CURRENCY = "$";
    public static final String IS_APP_DEFAULT_VERSION = "1.0";
    public static final String COLON = ":";
    public static final String FORWARD_SLASH = "/";
    public static final String URI_QUERY_SEPARATOR = "?";
    public static final String QUERY_STRING_SEPARATOR = "&";
    public static final String QUERY_KEY_VALUE_SEPARATOR = "=";
    public static final String ANY = "ANY";
    public static final String WEB_CLIP = "web-clip";
    public static final String DEFAULT_PCK_NAME = "default.app.com";
    public static final String ALL = "ALL";
    public static final String SHOW_ALL_ROLES = "SHOW_ALL_ROLES";
    public static final String IS_USER_ABLE_TO_VIEW_ALL_ROLES = "isUserAbleToViewAllRoles";
    public static final String GOOGLE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String APPLE_STORE_URL = "https://itunes.apple.com/country/app/app-name/id";
    public static final String MICROSOFT_STORE_URL = "https://apps.microsoft.com/detail/";
    public static final String GOOGLE_PLAY_SYNCED_APP = "GooglePlaySyncedApp";

    // Subscription task related constants
    public static final String SUBSCRIBERS = "SUBSCRIBERS";
    public static final String SUB_TYPE = "SUBSCRIPTION_TYPE";
    public static final String ACTION = "ACTION";
    public static final String APP_UUID = "APP_UUID";
    public static final String APP_PROPERTIES = "APP_PROPERTIES";
    public static final String SUBSCRIBER = "SUBSCRIBER";
    public static final String OPERATION_RE_EXECUtING = "OPERATION_RE_EXECUtING";
    public static final String TENANT_DOMAIN = "TENANT_DOMAIN";
    public static final String TENANT_ID = "__TENANT_ID_PROP__";
    public static final String TASK_NAME = "TASK_NAME";
    public  static final String SUBSCRIBED = "SUBSCRIBED";
    public  static final String UNSUBSCRIBED = "UNSUBSCRIBED";
    public static final String APPLE_LOOKUP_URL = "https://itunes.apple.com/us/lookup?id=";

    //App type constants related to window device type
    public static final String MSI = "MSI";
    public static final String APPX = "APPX";

    public static final String ENTERPRISE_APP_TYPE = "ENTERPRISE";
    public static final String PUBLIC_APP_TYPE = "ENTERPRISE";

    private static final Map<String, String> AGENT_DATA = new HashMap<>();
    static {
        AGENT_DATA.put("android", "android-agent.apk");
        AGENT_DATA.put("ios", "ios.ipa");
    }
    public static final Map<String, String> AGENT_FILE_NAMES = Collections.unmodifiableMap(AGENT_DATA);

    public static final class VPP {
        public static final String GET = "GET";
        public static final String BEARER = "Bearer ";
        public static final String EXECUTOR_EXCEPTION_PREFIX = "ExecutorException-";
        public static final String TOKEN_IS_EXPIRED = "ACCESS_TOKEN_IS_EXPIRED";
        public static final int INTERNAL_ERROR_CODE = 500;
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String EVENT_ID = "eventId";
        public static final String CLIENT_USER_ID_PARAM = "?clientUserId=";
        public static final String TOTAL_PAGES = "totalPages";
        public static final String GET_APP_DATA_RESPONSE_START = "results";
        public static final String REMOTE_FILE_NAME = "512x512w.png";

        private VPP() {
        }
    }


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
     * Query parameter for specifying the filename in the App artifact URL.
     */
    public static final String FILE_NAME_PARAM = "?fileName=";

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

        public static final String IOT_GATEWAY_HOST = "iot.gateway.host";
        public static final String IOT_CORE_PORT = "iot.core.https.port";
        public static final String ENROLLMENT_APP_INSTALL_PROTOCOL = "https://";
        public static final String GOOGLE_APP_INSTALL_URL = "/api/device-mgt/android/v1.0/enterprise/change-app";

        public static final String AUTHORIZATION = "Authorization";
        public static final String AUTHORIZATION_HEADER_VALUE = "Bearer ";
    }

    public final class ApplicationProperties {
        private ApplicationProperties() {
            throw new AssertionError();
        }

        public static final String NAME = "name";
        public static final String VERSION = "version";
        public static final String FREE_SUB_METHOD = "FREE";
        public static final String PAID_SUB_METHOD = "PAID";
        public static final String TYPE = "type";;
        public static final String PACKAGE_NAME = "packageName";
        public static final String APPLE_STORE_SYNCED_APP_CATEGORY = "AppleStoreSyncedApp";

        public static final String RESULTS = "results";
        public static final String ARTWORK = "artwork";
        public static final String URL = "url";
        public static final String DESCRIPTION = "description";
        public static final String STANDARD = "standard";
        public static final String OFFERS = "offers";
        public static final String PRICE = "price";
        public static final String DISPLAY = "display";
        public static final String GENRE_NAMES = "genreNames";
        public static final String PRICE_ZERO = "0.0";
        public static final String ASSOCIATION_DEVICE = "ASSOCIATION_DEVICE";
        public static final String ASSOCIATION_USER = "ASSOCIATION_USER";
    }

    /**
     * App name sanitization related constants
     */
    public static final int MAX_APP_NAME_CHARACTERS = 350;
    public static final String APP_NAME_REGEX = "[^a-zA-Z0-9.\\s-]";

    public static final String EXTENSION_APK = ".apk";
    public static final String EXTENSION_IPA = ".ipa";
    public static final String EXTENSION_MSI = ".msi";
    public static final String EXTENSION_APPX = ".appx";
    public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String MIME_TYPE_VND_ANDROID_PACKAGE_ARCHIVE = "application/vnd.android.package-archive";
    public static final String MIME_TYPE_VND_MS_WINDOWS_MSI = "application/vnd.ms-windows.msi";
    public static final String MIME_TYPE_X_MS_INSTALLER = "application/x-ms-installer";
    public static final String MIME_TYPE_VND_APPX = "application/vnd.appx";
}
