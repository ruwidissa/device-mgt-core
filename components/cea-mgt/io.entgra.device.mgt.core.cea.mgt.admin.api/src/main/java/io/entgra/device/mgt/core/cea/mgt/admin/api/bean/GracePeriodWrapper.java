/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.admin.api.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "GracePeriodWrapper", description = "GracePeriod transferring DTO")
public class GracePeriodWrapper {

    @JsonProperty(value = "gracePeriod", required = true)
    @ApiModelProperty(name = "gracePeriod", value = "Grace period in days", required = true)
    private int gracePeriod;

    @JsonProperty(value = "graceAllowedPolicy", required = true)
    @ApiModelProperty(name = "graceAllowedPolicy", value = "Grace allowed policy values", required = true)
    private String graceAllowedPolicy;

    public int getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(int gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public String getGraceAllowedPolicy() {
        return graceAllowedPolicy;
    }

    public void setGraceAllowedPolicy(String graceAllowedPolicy) {
        this.graceAllowedPolicy = graceAllowedPolicy;
    }
}
