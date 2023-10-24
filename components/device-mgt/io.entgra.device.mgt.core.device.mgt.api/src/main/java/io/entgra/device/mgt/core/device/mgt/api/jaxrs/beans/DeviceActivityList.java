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
import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.DeviceActivity;

import java.util.List;

@ApiModel(value = "ListOfDeviceActivities", description = "This contains a set of device activities that " +
        "matches a given"
        + " criteria as a collection")
public class DeviceActivityList extends BasePaginatedResult {

    private List<DeviceActivity> activities;

    @ApiModelProperty(value = "List of device activity Ids")
    @JsonProperty("activities")
    public List<DeviceActivity> getList() {
        return activities;
    }

    public void setList(List<DeviceActivity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
