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

package io.entgra.device.mgt.core.device.mgt.common.device.details;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(
        value = "LocationBean",
        description = "This class carries all information related IOS Device location."
)
public class LocationBean {

    @ApiModelProperty(
            name = "latitude",
            value = "Latitude of the IOS device Location.",
            required = true
    )
    private float latitude;
    @ApiModelProperty(
            name = "longitude",
            value = "Longitude of the IOS device Location.",
            required = true
    )
    private float longitude;
    @ApiModelProperty(
            name = "operationId",
            value = "Specific Id of the Location operation.",
            required = true
    )
    private String operationId;

    @ApiModelProperty(
            name = "locationEvents",
            value = "If this is a device initiated location publishing."
    )
    private List<LocationEventBean> locations;

    public List<LocationEventBean> getLocationEvents() {
        return locations;
    }

    public void setLocationEvents(List<LocationEventBean> locationEvents) {
        this.locations = locationEvents;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }


    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
