/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.mgt.jaxrs.beans.analytics;

import io.swagger.annotations.ApiModelProperty;

/**
 * Receiver definition including :
 * Attributes : Name, type, config and attached stream.
 */
public class Adapter {
    @ApiModelProperty(value = "Adapter name")
    private String adapterName;
    @ApiModelProperty(value = "Attached stream name:version")
    private String eventStreamWithVersion;
    @ApiModelProperty(value = "Adapter type")
    private TransportType adapterType;
    @ApiModelProperty(value = "Adapter main configurations")
    private AdapterConfiguration adapterConfiguration;

    @ApiModelProperty(value = "Adapter definition", notes = "use only when creating adapter as a String")
    private String definition;

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getEventStreamWithVersion() {
        return eventStreamWithVersion;
    }

    public void setEventStreamWithVersion(String eventStreamWithVersion) {
        this.eventStreamWithVersion = eventStreamWithVersion;
    }

    public TransportType getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(TransportType adapterType) {
        this.adapterType = adapterType;
    }

    public AdapterConfiguration getAdapterConfiguration() {
        return adapterConfiguration;
    }

    public void setAdapterConfiguration(
            AdapterConfiguration adapterConfiguration) {
        this.adapterConfiguration = adapterConfiguration;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
