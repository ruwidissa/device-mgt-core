/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.common.app.mgt;

public class ApplicationFilter {
    public static final class FilterProperties {
        public static final String REGEX_WHITESPACE = ".*\\s.*";
        public static final String REGEX_WHITESPACE_REPLACER = "\\s+";
        public static final String URL_ENCODE_SPACE = "%20";
    }

    private String appName;
    private String packageName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
        if (this.appName != null && !this.appName.isEmpty() &&
                //Check if the filter contains spaces and replace URL encode them
                this.appName.matches(FilterProperties.REGEX_WHITESPACE)) {
            this.appName = this.appName
                    .replaceAll(FilterProperties.REGEX_WHITESPACE_REPLACER,
                            FilterProperties.URL_ENCODE_SPACE);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
