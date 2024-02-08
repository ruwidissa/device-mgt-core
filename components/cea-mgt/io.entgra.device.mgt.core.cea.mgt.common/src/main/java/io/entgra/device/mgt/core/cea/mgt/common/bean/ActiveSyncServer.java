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

package io.entgra.device.mgt.core.cea.mgt.common.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ActiveSyncServer", description = "Active sync server properties")
public class ActiveSyncServer {
    @JsonProperty(value = "key", required = true)
    @ApiModelProperty(name = "key", value = "Key describing the server type according to cea-config.xml", required = true)
    private String key;

    @JsonProperty(value = "gatewayUrl", required = true)
    @ApiModelProperty(name = "gatewayUrl", value = "Gateway URL of the active sync server", required = true)
    private String gatewayUrl;

    @JsonProperty(value = "client", required = true)
    @ApiModelProperty(name = "client", value = "Client identifier", required = true)
    private String client;
    @JsonProperty(value = "secret", required = true)
    @ApiModelProperty(name = "secret", value = "Client secret", required = true)
    private String secret;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
