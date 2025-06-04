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

package io.entgra.device.mgt.core.device.mgt.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is use to wrap and send
 * Application registration data to the API publisher
 * Use only for application registration doing inside the IoTs
 */
public class ApplicationRegistration {
    private String applicationName;
    private List<String> tags;
    private boolean isAllowedToAllDomains;
    private long validityPeriod;
    private String callbackUrl;
    private ArrayList<String> supportedGrantTypes;
    private String tokenType;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isAllowedToAllDomains() {
        return isAllowedToAllDomains;
    }

    public void setAllowedToAllDomains(boolean allowedToAllDomains) {
        isAllowedToAllDomains = allowedToAllDomains;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public ArrayList<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(ArrayList<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
