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

/**
 * Stream definition
 * Attributes : name, version, definition and attribute data
 */
public class EventStream {
    @ApiModelProperty(value = "Stream name")
    private String streamName;
    @ApiModelProperty(value = "Stream version")
    private String streamVersion;
    @ApiModelProperty(value = "Stream definition")
    private String streamDefinition;
    @ApiModelProperty(value = "Stream property attribute lists")
    private EventAttributeLists eventAttributeLists;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public EventAttributeLists getEventAttributeLists() {
        return eventAttributeLists;
    }

    public void setEventAttributeLists(
            EventAttributeLists eventAttributeLists) {
        this.eventAttributeLists = eventAttributeLists;
    }
}
