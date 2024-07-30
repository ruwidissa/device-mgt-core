/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.common;

public class SubscriptionInfo {
    private String applicationUUID;
    private String subscriptionType;
    private String deviceSubscriptionStatus;
    private String identifier;
    private String subscriptionStatus;
    private DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria;

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public String getDeviceSubscriptionStatus() {
        return deviceSubscriptionStatus;
    }

    public void setDeviceSubscriptionStatus(String deviceSubscriptionStatus) {
        this.deviceSubscriptionStatus = deviceSubscriptionStatus;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public DeviceSubscriptionFilterCriteria getDeviceSubscriptionFilterCriteria() {
        return deviceSubscriptionFilterCriteria;
    }

    public void setDeviceSubscriptionFilterCriteria(DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria) {
        this.deviceSubscriptionFilterCriteria = deviceSubscriptionFilterCriteria;
    }
}
