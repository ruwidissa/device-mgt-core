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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto;


import io.entgra.device.mgt.core.device.mgt.common.Device;

import java.util.Date;
import java.util.Objects;

/**
 * This abstract class represents a device organization entity used in DeviceOrganizationService.
 * It serves as a base class for defining various organizational structures related to devices.
 */
public class DeviceOrganization {

    private int organizationId;
    private int deviceId;
    private Device device;
    private Integer parentDeviceId;
    private String deviceOrganizationMeta;
    private Date updateTime;
    private int tenantID;
    private boolean isCheckCyclicRelationship;

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Device getDevice() {
        return device;
    }
    public void setDevice(Device device) {
        this.device = device;
    }

    public Integer getParentDeviceId() {
        return parentDeviceId;
    }

    public void setParentDeviceId(Integer parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    public String getDeviceOrganizationMeta() {
        return deviceOrganizationMeta;
    }

    public void setDeviceOrganizationMeta(String deviceOrganizationMeta) {
        this.deviceOrganizationMeta = deviceOrganizationMeta;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getTenantID() {
        return tenantID;
    }

    public void setTenantID(int tenantID) {
        this.tenantID = tenantID;
    }

    public boolean isCheckCyclicRelationship() {
        return isCheckCyclicRelationship;
    }

    public void setCheckCyclicRelationship(boolean checkCyclicRelationship) {
        isCheckCyclicRelationship = checkCyclicRelationship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceOrganization that = (DeviceOrganization) o;
        // Compare fields for equality
        return Objects.equals(organizationId, that.organizationId)
                && Objects.equals(tenantID, that.tenantID)
                && Objects.equals(deviceId, that.deviceId)
                && Objects.equals(parentDeviceId, that.parentDeviceId)
                && Objects.equals(deviceOrganizationMeta, that.deviceOrganizationMeta);
    }

    @Override
    public int hashCode() {
        // Hash based on fields
        return Objects.hash(organizationId, tenantID, deviceId, parentDeviceId, deviceOrganizationMeta);
    }
}
