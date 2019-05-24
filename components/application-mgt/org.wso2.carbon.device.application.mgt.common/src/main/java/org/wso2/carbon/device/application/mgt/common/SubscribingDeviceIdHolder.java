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

package org.wso2.carbon.device.application.mgt.common;

import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscribingDeviceIdHolder {
    private List<DeviceIdentifier> installedDevices = new ArrayList<>();
    private Map<DeviceIdentifier, Integer> compatibleDevices = new HashMap<>();
    private Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = new HashMap<>();

    public List<DeviceIdentifier> getInstalledDevices() {
        return installedDevices;
    }

    public void setInstalledDevices(List<DeviceIdentifier> installedDevices) {
        this.installedDevices = installedDevices;
    }

    public Map<DeviceIdentifier, Integer> getCompatibleDevices() {
        return compatibleDevices;
    }

    public void setCompatibleDevices(Map<DeviceIdentifier, Integer> compatibleDevices) {
        this.compatibleDevices = compatibleDevices;
    }

    public Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions() {
        return deviceSubscriptions;
    }

    public void setDeviceSubscriptions(Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions) {
        this.deviceSubscriptions = deviceSubscriptions;
    }
}
