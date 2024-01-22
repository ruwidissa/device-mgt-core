/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CEAPolicyWrapper", description = "CEA policy transferring DTO")
public class CEAPolicyWrapper {
    @JsonProperty(value = "activeSyncServerEntries", required = true)
    @ApiModelProperty(name = "activeSyncServerEntries", value = "Active sync server properties", required = true)
    private ActiveSyncServer activeSyncServerEntries;

    @JsonProperty(value = "conditionalAccessPolicyEntries", required = true)
    @ApiModelProperty(name = "conditionalAccessPolicyEntries", value = "Definition of the access policy", required = true)
    private AccessPolicyWrapper conditionalAccessPolicyEntries;

    @JsonProperty(value = "gracePeriodEntries", required = true)
    @ApiModelProperty(name = "gracePeriodEntries", value = "Definition of the grace period", required = true)
    private GracePeriodWrapper gracePeriodEntries;

    public ActiveSyncServer getActiveSyncServerEntries() {
        return activeSyncServerEntries;
    }

    public void setActiveSyncServerEntries(ActiveSyncServer activeSyncServerEntries) {
        this.activeSyncServerEntries = activeSyncServerEntries;
    }

    public AccessPolicyWrapper getConditionalAccessPolicyEntries() {
        return conditionalAccessPolicyEntries;
    }

    public void setConditionalAccessPolicyEntries(AccessPolicyWrapper conditionalAccessPolicyEntries) {
        this.conditionalAccessPolicyEntries = conditionalAccessPolicyEntries;
    }

    public GracePeriodWrapper getGracePeriodEntries() {
        return gracePeriodEntries;
    }

    public void setGracePeriodEntries(GracePeriodWrapper gracePeriodEntries) {
        this.gracePeriodEntries = gracePeriodEntries;
    }
}
