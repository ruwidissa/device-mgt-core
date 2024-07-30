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

import java.util.ArrayList;
import java.util.List;

public class CategorizedSubscriptionResult {
    private List<DeviceSubscriptionData> installedDevices;
    private int installedDevicesCount;
    private List<DeviceSubscriptionData> pendingDevices;
    private int pendingDevicesCount;
    private List<DeviceSubscriptionData> errorDevices;
    private int errorDevicesCount;
    private List<DeviceSubscriptionData> newDevices;
    private int newDevicesCount;
    private List<DeviceSubscriptionData> subscribedDevices;
    private int subscribedDevicesCount;

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

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices,
                                         List<DeviceSubscriptionData> newDevices,
                                         int installedDevicesCount,
                                         int pendingDevicesCount,
                                         int errorDevicesCount,
                                         int newDevicesCount
                                         ) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
        this.newDevices = newDevices;
        this.subscribedDevices = null;
        this.installedDevicesCount = installedDevicesCount;
        this.pendingDevicesCount = pendingDevicesCount;
        this.errorDevicesCount = errorDevicesCount;
        this.newDevicesCount = newDevicesCount;
        this.subscribedDevicesCount = 0;
    }

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> installedDevices,
                                         List<DeviceSubscriptionData> pendingDevices,
                                         List<DeviceSubscriptionData> errorDevices,
                                         List<DeviceSubscriptionData> newDevices,
                                         List<DeviceSubscriptionData> subscribedDevices, int installedDevicesCount,
                                         int pendingDevicesCount,
                                         int errorDevicesCount,
                                         int newDevicesCount,
                                         int subscribedDevicesCount) {
        this.installedDevices = installedDevices;
        this.pendingDevices = pendingDevices;
        this.errorDevices = errorDevices;
        this.newDevices = newDevices;
        this.subscribedDevices = subscribedDevices;
        this.installedDevicesCount = installedDevicesCount;
        this.pendingDevicesCount = pendingDevicesCount;
        this.errorDevicesCount = errorDevicesCount;
        this.newDevicesCount = newDevicesCount;
        this.subscribedDevicesCount = subscribedDevicesCount;
    }

    public CategorizedSubscriptionResult(List<DeviceSubscriptionData> devices, String tabActionStatus) {
        switch (tabActionStatus) {
            case "COMPLETED":
                this.installedDevices = devices;
                break;
            case "PENDING":
                this.pendingDevices = devices;
                break;
            case "ERROR":
                this.errorDevices = devices;
                break;
            case "NEW":
                this.newDevices = devices;
                break;
            case "SUBSCRIBED":
                this.subscribedDevices = devices;
                break;
            default:
                this.installedDevices = new ArrayList<>();
                this.pendingDevices = new ArrayList<>();
                this.errorDevices = new ArrayList<>();
                this.newDevices = new ArrayList<>();
                this.subscribedDevices = new ArrayList<>();
                break;
        }
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

    public int getInstalledDevicesCount() {
        return installedDevicesCount;
    }

    public void setInstalledDevicesCount(int installedDevicesCount) {
        this.installedDevicesCount = installedDevicesCount;
    }

    public int getPendingDevicesCount() {
        return pendingDevicesCount;
    }

    public void setPendingDevicesCount(int pendingDevicesCount) {
        this.pendingDevicesCount = pendingDevicesCount;
    }

    public int getErrorDevicesCount() {
        return errorDevicesCount;
    }

    public void setErrorDevicesCount(int errorDevicesCount) {
        this.errorDevicesCount = errorDevicesCount;
    }

    public int getNewDevicesCount() {
        return newDevicesCount;
    }

    public void setNewDevicesCount(int newDevicesCount) {
        this.newDevicesCount = newDevicesCount;
    }

    public int getSubscribedDevicesCount() {
        return subscribedDevicesCount;
    }

    public void setSubscribedDevicesCount(int subscribedDevicesCount) {
        this.subscribedDevicesCount = subscribedDevicesCount;
    }
}
