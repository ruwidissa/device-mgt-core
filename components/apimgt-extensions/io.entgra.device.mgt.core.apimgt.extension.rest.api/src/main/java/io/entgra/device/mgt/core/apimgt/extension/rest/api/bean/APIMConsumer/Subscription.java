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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer;

import org.json.JSONObject;

import java.util.Objects;

public class Subscription {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Subscription that = (Subscription) o;
        return Objects.equals(apiId, that.apiId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(apiId);
    }

    private String subscriptionId;
    private String applicationId;
    private String apiId;
    private APIInfo apiInfo;
    private JSONObject applicationInfo;
    private String throttlingPolicy;
    private String requestedThrottlingPolicy;
    private String status;
    private String redirectionParams;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public APIInfo getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(APIInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    public JSONObject getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(JSONObject applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }

    public String getRequestedThrottlingPolicy() {
        return requestedThrottlingPolicy;
    }

    public void setRequestedThrottlingPolicy(String requestedThrottlingPolicy) {
        this.requestedThrottlingPolicy = requestedThrottlingPolicy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRedirectionParams() {
        return redirectionParams;
    }

    public void setRedirectionParams(String redirectionParams) {
        this.redirectionParams = redirectionParams;
    }
}
