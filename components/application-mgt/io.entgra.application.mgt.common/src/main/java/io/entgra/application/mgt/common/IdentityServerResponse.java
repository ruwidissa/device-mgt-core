/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityServerResponse {
    private int id;
    private String providerName;
    private String name;
    private String description;
    private String url;
    private String serviceProviderAppsUrl;
    private String username;
    private List<Map<String, String>> apiParamList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getServiceProviderAppsUrl() {
        return serviceProviderAppsUrl;
    }

    public void setServiceProviderAppsUrl(String serviceProviderAppsUrl) {
        this.serviceProviderAppsUrl = serviceProviderAppsUrl;
    }

    public List<Map<String, String>> getApiParamList() {
        return apiParamList;
    }

    public void setApiParamList(Map<String, String> apiParams) {
        this.apiParamList = apiParams.entrySet().stream().map(param -> {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put(param.getKey(), param.getValue());
            return paramMap;
        }).collect(Collectors.toList());
    }
}
