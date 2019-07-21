/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.util;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

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
    public static final String IOT_HOST_PROPERTY = "iot.core.host";
    public static final String IOT_HTTP_PORT_PROPERTY = "iot.core.http.port";
    public static final String IOT_HTTPS_PORT_PROPERTY = "iot.core.https.port";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";

    public static final String FORWARD_SLASH = "/";
    public static final String ANY = "ANY";
    public static final String DEFAULT_PCK_NAME = "default.app.com";

    public static final String GOOGLE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String APPLE_STORE_URL = "https://itunes.apple.com/country/app/app-name/id";

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
}
