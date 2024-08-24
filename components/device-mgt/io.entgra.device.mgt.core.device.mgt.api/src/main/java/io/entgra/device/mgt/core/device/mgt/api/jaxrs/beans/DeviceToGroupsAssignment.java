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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class DeviceToGroupsAssignment extends BasePaginatedResult {

    @ApiModelProperty(value = "List of device group ids.")
    @JsonProperty("deviceGroupIds")
    private List<Integer> deviceGroupIds = new ArrayList<>();

    @ApiModelProperty(value = "Device identifier of the device needed to be assigned with group")
    @JsonProperty("deviceIdentifier")
    private DeviceIdentifier deviceIdentifier;


    public List<Integer> getDeviceGroupIds() {
        return deviceGroupIds;
    }

    public void setDeviceGroupIds(List<Integer> deviceGroupIds) {
        this.deviceGroupIds = deviceGroupIds;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

}
