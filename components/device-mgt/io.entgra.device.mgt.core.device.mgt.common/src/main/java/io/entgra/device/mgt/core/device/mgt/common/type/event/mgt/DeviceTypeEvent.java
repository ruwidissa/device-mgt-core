/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.common.type.event.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * This hold device type event data record
 */
public class DeviceTypeEvent {

    private String eventName;
    private EventAttributeList eventAttributes;
    private TransportType transport;

    private String eventTopicStructure;

    @ApiModelProperty(value = "Attributes related to device type event")
    @JsonProperty("eventAttributes")
    public EventAttributeList getEventAttributeList() {
        return eventAttributes;
    }

    public void setEventAttributeList(
            EventAttributeList eventAttributes) {
        this.eventAttributes = eventAttributes;
    }

    @ApiModelProperty(value = "Transport to be used for device to server communication.")
    @JsonProperty("transport")
    public TransportType getTransportType() {
        return transport;
    }

    public void setTransportType(TransportType transport) {
        this.transport = transport;
    }

    @ApiModelProperty(value = "event topic structure")
    @JsonProperty("eventTopicStructure")
    public String getEventTopicStructure() {
        return eventTopicStructure;
    }

    public void setEventTopicStructure(String eventTopicStructure) {
        this.eventTopicStructure = eventTopicStructure;
    }

    @ApiModelProperty(value = "event topic name")
    @JsonProperty("eventName")
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceTypeEvent)) return false;
        DeviceTypeEvent that = (DeviceTypeEvent) o;
        return Objects.equals(eventName, that.eventName) &&
                Objects.equals(eventAttributes, that.eventAttributes) &&
                transport == that.transport &&
                Objects.equals(eventTopicStructure, that.eventTopicStructure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventAttributes, transport, eventTopicStructure);
    }
}

