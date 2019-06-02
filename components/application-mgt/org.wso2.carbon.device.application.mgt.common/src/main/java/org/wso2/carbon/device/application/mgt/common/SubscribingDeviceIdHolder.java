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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.HashMap;
import java.util.Map;

public class SubscribingDeviceIdHolder {
    private Map<DeviceIdentifier, Integer> subscribedDevices = new HashMap<>();
    private Map<DeviceIdentifier, Integer> subscribableDevices = new HashMap<>();
    public Map<DeviceIdentifier, Integer> getSubscribedDevices() {
        return subscribedDevices;
    }

    public void setSubscribedDevices(Map<DeviceIdentifier, Integer> subscribedDevices) {
        this.subscribedDevices = subscribedDevices;
    }

    public Map<DeviceIdentifier, Integer> getSubscribableDevices() {
        return subscribableDevices;
    }

    public void setSubscribableDevices(Map<DeviceIdentifier, Integer> subscribableDevices) {
        this.subscribableDevices = subscribableDevices;
    }
}
