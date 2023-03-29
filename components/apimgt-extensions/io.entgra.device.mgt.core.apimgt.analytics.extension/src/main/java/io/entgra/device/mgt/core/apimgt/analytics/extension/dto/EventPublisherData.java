/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.apimgt.analytics.extension.dto;

import java.util.List;

public class EventPublisherData {
    private String name;
    private String streamVersion;
    private String streamName;

    private List<Property> propertyList;

    private String eventAdaptorType;

    private String customMappingType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public String getEventAdaptorType() {
        return eventAdaptorType;
    }

    public void setEventAdaptorType(String eventAdaptorType) {
        this.eventAdaptorType = eventAdaptorType;
    }

    public String getCustomMappingType() {
        return customMappingType;
    }

    public void setCustomMappingType(String customMappingType) {
        this.customMappingType = customMappingType;
    }
}
