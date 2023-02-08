/*
 * Copyright (c) 2022, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.group.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceTypesOfGroups", description = "This class carries whether the groups has device type or not.")
public class DeviceTypesOfGroups implements Serializable {

    private static final long serialVersionUID = 5562356373277828099L;
    @ApiModelProperty(name = "hasAndroid", value = "groups has Android devices.")
    private boolean hasAndroid;
    @ApiModelProperty(name = "id", value = "groups has iOS devices.")
    private boolean hasIos;
    @ApiModelProperty(name = "hasAndroid", value = "groups has Windows devices.")
    private boolean hasWindows;

    public boolean isHasAndroid() {
        return hasAndroid;
    }

    public void setHasAndroid(boolean hasAndroid) {
        this.hasAndroid = hasAndroid;
    }

    public boolean isHasIos() {
        return hasIos;
    }

    public void setHasIos(boolean hasIos) {
        this.hasIos = hasIos;
    }

    public boolean isHasWindows() {
        return hasWindows;
    }

    public void setHasWindows(boolean hasWindows) {
        this.hasWindows = hasWindows;
    }
}
