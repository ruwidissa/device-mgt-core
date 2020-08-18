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
        value = "DeviceEnrollmentInvitation",
        description = "Holds data to send  device enrollment invitation to list of existing users.")
public class DeviceEnrollmentInvitation implements Serializable {

    private static final long serialVersionUID = 6933837278652532052L;

    @ApiModelProperty(
            name = "usernames",
            value = "List of usernames of users.",
            required = true)
    private List<String> usernames;

    @ApiModelProperty(
            name = "deviceEnrollmentTypes",
            value = "List of enrollment types against device types.")
    private List<DeviceEnrollmentType> deviceEnrollmentTypes;

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<DeviceEnrollmentType> getDeviceEnrollmentTypes() {
        return deviceEnrollmentTypes;
    }

    public void setDeviceEnrollmentTypes(
            List<DeviceEnrollmentType> deviceEnrollmentTypes) {
        this.deviceEnrollmentTypes = deviceEnrollmentTypes;
    }
}
