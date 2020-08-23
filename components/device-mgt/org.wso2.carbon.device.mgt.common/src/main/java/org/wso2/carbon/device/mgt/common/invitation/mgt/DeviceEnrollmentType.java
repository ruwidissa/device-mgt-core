/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common.invitation.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

@ApiModel(
        value = "DeviceEnrollmentType",
        description = "Holds data of enrollment types against device types.")
public class DeviceEnrollmentType implements Serializable {

    private static final long serialVersionUID = 6563596191450032613L;

    @ApiModelProperty(
            name = "deviceType",
            value = "Device type (i.e: android, ios, windows)",
            required = true)
    private String deviceType;

    @ApiModelProperty(
            name = "enrollmentType",
            value = "Enrollment type (i.e: BYOD, COPE, COSU)",
            required = true)
    private List<String> enrollmentType;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(List<String> enrollmentType) {
        this.enrollmentType = enrollmentType;
    }
}
