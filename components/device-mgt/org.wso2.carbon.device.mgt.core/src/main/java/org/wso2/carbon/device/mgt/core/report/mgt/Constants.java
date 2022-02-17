/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.report.mgt;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class Constants {
    public static final String DEFAULT_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.REPORT_MGT_CONFIG_XML_FILE;
    public static final String REPORT_MGT_CONFIG_XML_FILE = "reporting-mgt.xml";
    // device types
    public static final String ANDROID = "android";
    public static final String IOS = "ios";
    // device properties
    public static final String OS_BUILD_DATE = "OS_BUILD_DATE";
    public static final String OS_VERSION = "OS_VERSION";
    public static final String OS_VALUE = "OS_VALUE";
    // OS version value generating properties
    public static final int NUM_OF_OS_VERSION_DIGITS= 5;
    public static final int NUM_OF_OS_VERSION_POSITIONS = 3;
    public static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String WSS_PROTOCOL = "wss";
    public static final String WS_PROTOCOL = "ws";
    public static final String SCHEME_SEPARATOR = "://";
    public static final String URI_SEPARATOR = "/";
    public static final String URI_QUERY_SEPARATOR = "?";
    public static final String COLON = ":";
}

