/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;

import java.util.List;

public class ApplicationInstallResponse {
    @ApiModelProperty(
            name = "alreadyInstalledDevices",
            value = "List of devices that application release is already installed.",
            dataType = "List[org.wso2.carbon.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceIdentifier> alreadyInstalledDevices;

    @ApiModelProperty(
            name = "activity",
            value = "Activity corresponding to the operation"
    )
    private Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<DeviceIdentifier> getAlreadyInstalledDevices() {
        return alreadyInstalledDevices;
    }

    public void setAlreadyInstalledDevices(List<DeviceIdentifier> alreadyInstalledDevices) {
        this.alreadyInstalledDevices = alreadyInstalledDevices;
    }
}
