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

package io.entgra.device.mgt.core.device.mgt.common.type.mgt;

import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.InitialOperationConfig;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;

import java.util.ArrayList;
import java.util.List;

public class DeviceTypeMetaDefinition {

    private List<String> properties;
    private List<Feature> features;
    private boolean claimable;
    private PushNotificationConfig pushNotificationConfig;
    private boolean policyMonitoringEnabled;
    private InitialOperationConfig initialOperationConfig;
    private License license;
    private String description;
    private boolean isSharedWithAllTenants;

    private List<String> mqttEventTopicStructures;

    private boolean longLivedToken = false;

    private boolean storeVisibilityEnabled = true;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public PushNotificationConfig getPushNotificationConfig() {
        return pushNotificationConfig;
    }

    public void setPushNotificationConfig(
            PushNotificationConfig pushNotificationConfig) {
        this.pushNotificationConfig = pushNotificationConfig;
    }

    public boolean isPolicyMonitoringEnabled() {
        return policyMonitoringEnabled;
    }

    public void setPolicyMonitoringEnabled(boolean policyMonitoringEnabled) {
        this.policyMonitoringEnabled = policyMonitoringEnabled;
    }

    public InitialOperationConfig getInitialOperationConfig() {
        return initialOperationConfig;
    }

    public void setInitialOperationConfig(InitialOperationConfig initialOperationConfig) {
        this.initialOperationConfig = initialOperationConfig;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public boolean isSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    public void setSharedWithAllTenants(boolean sharedWithAllTenants) {
        isSharedWithAllTenants = sharedWithAllTenants;
    }

    public List<String> getMqttEventTopicStructures() {
        return mqttEventTopicStructures;
    }

    public void setMqttEventTopicStructures(List<String> mqttEventTopicStructures) {
        this.mqttEventTopicStructures = mqttEventTopicStructures;
    }

    public boolean isLongLivedToken() {
        return longLivedToken;
    }

    public void setLongLivedToken(boolean longLivedToken) {
        this.longLivedToken = longLivedToken;
    }

    public boolean isStoreVisibilityEnabled() {
        return storeVisibilityEnabled;
    }

    public void setStoreVisibilityEnabled(boolean storeVisibilityEnabled) {
        this.storeVisibilityEnabled = storeVisibilityEnabled;
    }
}
