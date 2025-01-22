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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents the Consumer API Information.
 */

public class APIInfo {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        APIInfo apiInfo = (APIInfo) o;
        return Objects.equals(id, apiInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private String id;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private String lifeCycleStatus;
    private String thumbnailUri;
    private String avgRating;
    private List<String> throttlingPolicies;
    private JSONObject advertiseInfo;
    private JSONObject businessInformation;
    private boolean isSubscriptionAvailable;
    private String monetizationLabel;
    private String gatewayVendor;
    private List<String> additionalProperties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public List<String> getThrottlingPolicies() {
        return throttlingPolicies;
    }

    public void setThrottlingPolicies(List<String> throttlingPolicies) {
        this.throttlingPolicies = throttlingPolicies;
    }

    public JSONObject getAdvertiseInfo() {
        return advertiseInfo;
    }

    public void setAdvertiseInfo(JSONObject advertiseInfo) {
        this.advertiseInfo = advertiseInfo;
    }

    public JSONObject getBusinessInformation() {
        return businessInformation;
    }

    public void setBusinessInformation(JSONObject businessInformation) {
        this.businessInformation = businessInformation;
    }

    public boolean isSubscriptionAvailable() {
        return isSubscriptionAvailable;
    }

    public void setSubscriptionAvailable(boolean subscriptionAvailable) {
        isSubscriptionAvailable = subscriptionAvailable;
    }

    public String getMonetizationLabel() {
        return monetizationLabel;
    }

    public void setMonetizationLabel(String monetizationLabel) {
        this.monetizationLabel = monetizationLabel;
    }

    public String getGatewayVendor() {
        return gatewayVendor;
    }

    public void setGatewayVendor(String gatewayVendor) {
        this.gatewayVendor = gatewayVendor;
    }

    public List<String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(List<String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
