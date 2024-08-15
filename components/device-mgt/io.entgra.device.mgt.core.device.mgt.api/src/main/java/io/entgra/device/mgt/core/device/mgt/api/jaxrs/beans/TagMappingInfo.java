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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "TagMappingInfo", description = "Tag mapping details including parameters associated with " +
        "device mapping wrapped here.")
public class TagMappingInfo {

    @ApiModelProperty(name = "deviceIdentifiers", value = "Defines the device identifiers.", required = true)
    private List<String> deviceIdentifiers;

    @ApiModelProperty(name = "deviceType", value = "Defines the device type.", required = true)
    private String deviceType;

    @ApiModelProperty(name = "tags", value = "Defines the tags.", required = true)
    private List<String> tags;

    public List<String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<String> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
