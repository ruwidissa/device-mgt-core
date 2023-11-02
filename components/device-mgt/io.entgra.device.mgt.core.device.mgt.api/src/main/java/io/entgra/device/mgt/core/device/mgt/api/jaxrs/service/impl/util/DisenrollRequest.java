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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

@ApiModel(value = "DisenrollRequest", description = "Contains the multiple devices specified by device IDs")
public class DisenrollRequest {
    @ApiModelProperty(name = "deviceTypeWithDeviceIds", value = "Contains the multiple devices specified by device IDs with type",
            required = true)
    private Map<String, List<String>> deviceTypeWithDeviceIds;

    public Map<String, List<String>> getDeviceTypeWithDeviceIds() {
        return deviceTypeWithDeviceIds;
    }

    public void setDeviceTypeWithDeviceIds(Map<String, List<String>> deviceTypeWithDeviceIds) {
        this.deviceTypeWithDeviceIds = deviceTypeWithDeviceIds;
    }
}
