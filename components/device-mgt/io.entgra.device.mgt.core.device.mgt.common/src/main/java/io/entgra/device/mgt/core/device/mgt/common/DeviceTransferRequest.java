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

package io.entgra.device.mgt.core.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Represents an individual configuration entry.
 */
@ApiModel(value = "DeviceTransferRequest", description = "This class carries all information related to device " +
        "transfer from super tenant to another tenant.")
public class DeviceTransferRequest {

    @ApiModelProperty(name = "deviceType", value = "Type of the device", required = true)
    private String deviceType;

    @ApiModelProperty(name = "deviceIds", value = "Ids of devices to transfer", required = true)
    private List<String> deviceIds;

    @ApiModelProperty(name = "destinationTenant", value = "Destination Tenant ID", required = true)
    private String destinationTenant;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceId(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public String getDestinationTenant() {
        return destinationTenant;
    }

    public void setDestinationTenant(String destinationTenant) {
        this.destinationTenant = destinationTenant;
    }

}
