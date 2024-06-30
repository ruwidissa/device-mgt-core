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

package io.entgra.device.mgt.core.application.mgt.common;

import java.util.List;

public class CategorizedSubscriptionResult {
    private List<DeviceSubscriptionData> installedDevices;
    private List<DeviceSubscriptionData> pendingDevices;
    private List<DeviceSubscriptionData> errorDevices;
    private List<DeviceSubscriptionData> newDevices;
    private List<DeviceSubscriptionData> subscribedDevices;

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
        this.newDevices = null;
        this.subscribedDevices = null;
    }

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices,
                                         List<DeviceSubscriptionData> newDevices) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
        this.newDevices = newDevices;
        this.subscribedDevices = null;
    }

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices,
                                         List<DeviceSubscriptionData> newDevices,
                                         List<DeviceSubscriptionData> subscribedDevices) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
        this.newDevices = newDevices;
        this.subscribedDevices = subscribedDevices;
    }

    public List<DeviceSubscriptionData> getInstalledDevices() {
        return installedDevices;
    }

    public void setInstalledDevices(List<DeviceSubscriptionData> installedDevices) {
        this.installedDevices = installedDevices;
    }

    public List<DeviceSubscriptionData> getPendingDevices() {
        return pendingDevices;
    }

    public void setPendingDevices(List<DeviceSubscriptionData> pendingDevices) {
        this.pendingDevices = pendingDevices;
    }

    public List<DeviceSubscriptionData> getErrorDevices() {
        return errorDevices;
    }

    public void setErrorDevices(List<DeviceSubscriptionData> errorDevices) {
        this.errorDevices = errorDevices;
    }

    public List<DeviceSubscriptionData> getNewDevices() {
        return newDevices;
    }

    public void setNewDevices(List<DeviceSubscriptionData> newDevices) {
        this.newDevices = newDevices;
    }

    public List<DeviceSubscriptionData> getSubscribedDevices() {
        return subscribedDevices;
    }

    public void setSubscribedDevices(List<DeviceSubscriptionData> subscribedDevices) {
        this.subscribedDevices = subscribedDevices;
    }
}

