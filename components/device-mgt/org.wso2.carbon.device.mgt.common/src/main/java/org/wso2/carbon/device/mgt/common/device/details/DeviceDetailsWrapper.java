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

package org.wso2.carbon.device.mgt.common.device.details;

import com.google.gson.Gson;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

import java.util.List;

public class DeviceDetailsWrapper {

    DeviceInfo deviceInfo;
    Device device;
    List<Application> applications;
    DeviceLocation location;
    int tenantId;

    List<DeviceGroup> groups;
    String [] role;

    public List<DeviceGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<DeviceGroup> groups) {
        this.groups = groups;
    }

    public String [] getRole() {
        return role;
    }

    public void setRole(String [] role) {
        this.role = role;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public DeviceLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
    }

    public String getJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this).toString();
    }
}
