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

import java.util.ArrayList;
import java.util.List;

/**
 * Stream definition
 * Attributes : name, version, definition and attribute data
 */
public class EventStream {
    @ApiModelProperty(value = "Stream name")
    private String name;
    @ApiModelProperty(value = "Stream version")
    private String version;
    @ApiModelProperty(value = "Stream nickName")
    private String nickName;
    @ApiModelProperty(value = "Stream description")
    private String description;
    @ApiModelProperty(value = "Meta attribute list")
    private List<Attribute> metaData;
    @ApiModelProperty(value = "Correlation attribute list")
    private List<Attribute> correlationData;
    @ApiModelProperty(value = "Payload attribute list")
    private List<Attribute> payloadData;

    @ApiModelProperty(value = "Stream definition" , notes = "use only when creating stream as a String")
    private String definition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Attribute> getMetaData() {
        return metaData;
    }

    public void setMetaData(
            List<Attribute> metaData) {
        this.metaData = metaData;
    }

    public List<Attribute> getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(
            List<Attribute> correlationData) {
        this.correlationData = correlationData;
    }

    public List<Attribute> getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(
            List<Attribute> payloadData) {
        this.payloadData = payloadData;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
