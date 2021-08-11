/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSubscriptionInfo {

    List<Device> devices;
    List<String> subscribers;
    List<DeviceIdentifier> errorDeviceIdentifiers;
    String appSupportingDeviceTypeName;

    public List<Device> getDevices() { return devices; }

    public void setDevices(List<Device> devices) { this.devices = devices; }

    public List<String> getSubscribers() { return subscribers; }

    public void setSubscribers(List<String> subscribers) { this.subscribers = subscribers; }

    public List<DeviceIdentifier> getErrorDeviceIdentifiers() { return errorDeviceIdentifiers; }

    public void setErrorDeviceIdentifiers(List<DeviceIdentifier> errorDeviceIdentifiers) {
        this.errorDeviceIdentifiers = errorDeviceIdentifiers;
    }

    public String getAppSupportingDeviceTypeName() { return appSupportingDeviceTypeName; }

    public void setAppSupportingDeviceTypeName(String appSupportingDeviceTypeName) {
        this.appSupportingDeviceTypeName = appSupportingDeviceTypeName;
    }
}
