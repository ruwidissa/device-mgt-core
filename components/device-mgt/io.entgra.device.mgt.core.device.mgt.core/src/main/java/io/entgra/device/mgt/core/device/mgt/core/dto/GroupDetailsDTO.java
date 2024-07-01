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

package io.entgra.device.mgt.core.device.mgt.core.dto;

import java.util.List;
import java.util.Map;

public class GroupDetailsDTO {
    private int groupId;
    private String groupName;
    private String groupOwner;
    private List<Integer> deviceIds;
    private int deviceCount;
    private String deviceOwner;
    private String deviceStatus;
    private Map<Integer, String> deviceOwners;
    private Map<Integer, String> deviceStatuses;
    private Map<Integer, String> deviceNames;
    private Map<Integer, String> deviceTypes;
    private Map<Integer, String> deviceIdentifiers;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public List<Integer> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<Integer> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Map<Integer, String> getDeviceOwners() {
        return deviceOwners;
    }

    public void setDeviceOwners(Map<Integer, String> deviceOwners) {
        this.deviceOwners = deviceOwners;
    }

    public Map<Integer, String> getDeviceStatuses() {
        return deviceStatuses;
    }

    public void setDeviceStatuses(Map<Integer, String> deviceStatuses) {
        this.deviceStatuses = deviceStatuses;
    }

    public Map<Integer, String> getDeviceNames() {
        return deviceNames;
    }

    public void setDeviceNames(Map<Integer, String> deviceNames) {
        this.deviceNames = deviceNames;
    }

    public Map<Integer, String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Map<Integer, String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public Map<Integer, String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(Map<Integer, String> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }
}
