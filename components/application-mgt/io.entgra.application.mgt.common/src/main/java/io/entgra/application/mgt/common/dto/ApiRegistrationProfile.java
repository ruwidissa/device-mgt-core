/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package io.entgra.application.mgt.common.dto;

public class ApiRegistrationProfile {

    private String applicationName;
    private String tags[];
    private boolean isAllowedToAllDomains;
    private boolean isMappingAnExistingOAuthApp;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public boolean isAllowedToAllDomains() {
        return isAllowedToAllDomains;
    }

    public void setAllowedToAllDomains(boolean allowedToAllDomains) {
        isAllowedToAllDomains = allowedToAllDomains;
    }

    public boolean isMappingAnExistingOAuthApp() {
        return isMappingAnExistingOAuthApp;
    }

    public void setMappingAnExistingOAuthApp(boolean mappingAnExistingOAuthApp) {
        isMappingAnExistingOAuthApp = mappingAnExistingOAuthApp;
    }
}
