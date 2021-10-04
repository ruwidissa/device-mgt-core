/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.HashMap;
import java.util.Map;

public class SubscribingDeviceIdHolder {
    private Map<DeviceIdentifier, Integer> appInstalledDevices = new HashMap<>();
    private Map<DeviceIdentifier, Integer> appInstallableDevices = new HashMap<>();
    private Map<DeviceIdentifier, Integer> appReInstallableDevices = new HashMap<>();
    private Map<DeviceIdentifier, Integer> appReUnInstallableDevices = new HashMap<>();
    private Map<DeviceIdentifier, Integer> skippedDevices = new HashMap<>();

    public Map<DeviceIdentifier, Integer> getAppInstalledDevices() {
        return appInstalledDevices;
    }

    public void setAppInstalledDevices(Map<DeviceIdentifier, Integer> appInstalledDevices) {
        this.appInstalledDevices = appInstalledDevices;
    }

    public Map<DeviceIdentifier, Integer> getAppInstallableDevices() {
        return appInstallableDevices;
    }

    public void setAppInstallableDevices(Map<DeviceIdentifier, Integer> appInstallableDevices) {
        this.appInstallableDevices = appInstallableDevices;
    }

    public Map<DeviceIdentifier, Integer> getAppReInstallableDevices() {
        return appReInstallableDevices;
    }

    public void setAppReInstallableDevices(Map<DeviceIdentifier, Integer> appReInstallableDevices) {
        this.appReInstallableDevices = appReInstallableDevices;
    }

    public Map<DeviceIdentifier, Integer> getSkippedDevices() { return skippedDevices; }

    public void setSkippedDevices(Map<DeviceIdentifier, Integer> skippedDevices) {
        this.skippedDevices = skippedDevices;
    }

    public Map<DeviceIdentifier, Integer> getAppReUnInstallableDevices() {
        return appReUnInstallableDevices;
    }

    public void setAppReUnInstallableDevices(Map<DeviceIdentifier, Integer> appReUnInstallableDevices) {
        this.appReUnInstallableDevices = appReUnInstallableDevices;
    }
}
