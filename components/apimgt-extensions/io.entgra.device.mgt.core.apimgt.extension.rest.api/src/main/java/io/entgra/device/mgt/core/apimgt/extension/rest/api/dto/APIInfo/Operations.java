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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo;

import org.json.JSONObject;
import java.util.List;

/**
 * This hold the api operations information.
 */
public class Operations {
    private String id;
    private String target;
    private String verb;
    private String authType;
    private String throttlingPolicy;
    private List<String> scopes;
    private List<String> usedProductIds;
    private String amznResourceName;
    private String amznResourceTimeout;
    private String payloadSchema;
    private String uriMapping;

    public Operations() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getUsedProductIds() {
        return usedProductIds;
    }

    public void setUsedProductIds(List<String> usedProductIds) {
        this.usedProductIds = usedProductIds;
    }

    public String getAmznResourceName() {
        return amznResourceName;
    }

    public void setAmznResourceName(String amznResourceName) {
        this.amznResourceName = amznResourceName;
    }

    public String getAmznResourceTimeout() {
        return amznResourceTimeout;
    }

    public void setAmznResourceTimeout(String amznResourceTimeout) {
        this.amznResourceTimeout = amznResourceTimeout;
    }

    public String getPayloadSchema() {
        return payloadSchema;
    }

    public void setPayloadSchema(String payloadSchema) {
        this.payloadSchema = payloadSchema;
    }

    public String getUriMapping() {
        return uriMapping;
    }

    public void setUriMapping(String uriMapping) {
        this.uriMapping = uriMapping;
    }

}
