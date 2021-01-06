/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * This class represents the information of uninstall application operation.
 */
@ApiModel(value = "ApplicationUninstallation",
        description = "This class carries all information related to application uninstallation.")
public class ApplicationUninstallation {
    @ApiModelProperty(name = "appIdentifier", value = "The package name of the application to be uninstalled.", required = true)
    @Size(min = 2, max = 45)
    @Pattern(regexp = "^[A-Za-z0-9]*$")
    String appIdentifier;

    @ApiModelProperty(name = "type", value = "The type of the application. The following types of applications " +
            "are supported: enterprise, public", required = true)
    @Size(min = 2, max = 12)
    @Pattern(regexp = "^[A-Za-z]*$")
    String type;

    public ApplicationUninstallation() {
    }

    public ApplicationUninstallation(String appIdentifier, String type) {
        this.appIdentifier = appIdentifier;
        this.type = type;
    }

    public String getAppIdentifier() {
        return appIdentifier;
    }

    public void setAppIdentifier(String appIdentifier) {
        this.appIdentifier = appIdentifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
