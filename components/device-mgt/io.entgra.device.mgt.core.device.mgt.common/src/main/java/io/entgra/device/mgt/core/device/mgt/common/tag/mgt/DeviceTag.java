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

package io.entgra.device.mgt.core.device.mgt.common.tag.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO of DeviceTag object which is used to manage Device Tags.
 */

@ApiModel(value = "DeviceTag", description = "This is used to manage device tags.")
public class DeviceTag {

    @ApiModelProperty(name = "enrolmentId", value = "Defines the device id.", required = true)
    private int enrolmentId;

    @ApiModelProperty(name = "tagId", value = "Defines the tag id.", required = true)
    private int tagId;

    public int getEnrolmentId() {
        return enrolmentId;
    }

    public void setEnrolmentId(int enrolmentId) {
        this.enrolmentId = enrolmentId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}
