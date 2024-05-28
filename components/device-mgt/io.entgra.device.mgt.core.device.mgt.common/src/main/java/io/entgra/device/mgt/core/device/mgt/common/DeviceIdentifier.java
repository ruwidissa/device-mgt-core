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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;


@ApiModel(value = "DeviceIdentifier", description = "This contains device details that is used to identify a device " +
                                                    "uniquely.")
public class DeviceIdentifier implements Serializable{

    @ApiModelProperty(
            name = "id",
            value = "Identity of the device.",
            required = true,
            example = "123456")
    @JsonProperty(value = "id", required = true)
    private String id;

    @ApiModelProperty(
            name = "type",
            value = "Type of the device.",
            required = true,
            example = "android")
    @JsonProperty(value = "type", required = true)
    private String type;

    public DeviceIdentifier() {}

    public DeviceIdentifier(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return type + "|" + id;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceIdentifier) {
            return (this.hashCode() == obj.hashCode());
        }
        return false;
    }

}
