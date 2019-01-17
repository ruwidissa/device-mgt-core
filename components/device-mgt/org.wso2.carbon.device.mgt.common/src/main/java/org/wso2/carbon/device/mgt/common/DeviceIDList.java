/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;


@ApiModel(value = "DeviceIDList", description = "This contains status of device against device identifier.")
public class DeviceIDList implements Serializable{

    @ApiModelProperty(
            name = "id",
            value = "Identity of the device.",
            required = true,
            example = "123456")
    @JsonProperty(value = "id", required = true)
    private List<String> ids;

    public DeviceIDList() {}

    public DeviceIDList(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getId() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        String deviceIds = new Gson().toJson(ids);
        return "["+deviceIds+"]";
    }
}
