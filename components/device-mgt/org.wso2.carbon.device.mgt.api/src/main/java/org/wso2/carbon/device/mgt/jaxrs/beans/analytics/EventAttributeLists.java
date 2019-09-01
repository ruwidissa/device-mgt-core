/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.jaxrs.beans.analytics;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * Stream event attribute data
 * Attributes : three lists for each attribute type
 */
public class EventAttributeLists {
    @ApiModelProperty(value = "Meta attribute list")
    private List<Attribute> metaAttributes;
    @ApiModelProperty(value = "Correlation attribute list")
    private List<Attribute> correlationAttributes;
    @ApiModelProperty(value = "Payload attribute list")
    private List<Attribute> payloadAttributes;

    public List<Attribute> getMetaAttributes() {
        return metaAttributes;
    }

    public void setMetaAttributes(
            List<Attribute> metaAttributes) {
        this.metaAttributes = metaAttributes;
    }

    public List<Attribute> getCorrelationAttributes() {
        return correlationAttributes;
    }

    public void setCorrelationAttributes(
            List<Attribute> correlationAttributes) {
        this.correlationAttributes = correlationAttributes;
    }

    public List<Attribute> getPayloadAttributes() {
        return payloadAttributes;
    }

    public void setPayloadAttributes(
            List<Attribute> payloadAttributes) {
        this.payloadAttributes = payloadAttributes;
    }
}
