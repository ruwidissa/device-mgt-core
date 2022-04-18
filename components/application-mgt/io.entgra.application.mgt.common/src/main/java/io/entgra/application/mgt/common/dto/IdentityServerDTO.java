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

package io.entgra.application.mgt.common.dto;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IdentityServerDTO {

    private int id;
    private String providerName;
    private String name;
    private String description;
    private String url;
    private String username;
    private String password;
    private Map<String, String> apiParams;

    public IdentityServerDTO() {
        this.apiParams = new HashMap<>();
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String constructApiParamsJsonString() {
        return new Gson().toJson(apiParams);
    }

    public Set<String> getApiParamKeys() {
        return apiParams.keySet();
    }

    public Map<String, String> getApiParams() {
        return apiParams;
    }

    public void setApiParams(Map<String, String> apiParams) {
        this.apiParams = apiParams;
    }
}
