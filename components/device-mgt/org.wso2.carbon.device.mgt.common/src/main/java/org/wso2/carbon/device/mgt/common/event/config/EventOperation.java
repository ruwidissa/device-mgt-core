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

package org.wso2.carbon.device.mgt.common.event.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class EventOperation {
    private String eventSource;
    private EventMetaData eventDefinition;
    private String eventTriggers;

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public EventMetaData getEventDefinition() {
        return eventDefinition;
    }

    public void setEventDefinition(EventMetaData eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    public String getEventTriggers() {
        return eventTriggers;
    }

    public void setEventTriggers(List<EventConfig> eventList) {
        JsonArray eventTriggers = new JsonArray();
        JsonObject eventTrigger;
        for (EventConfig eventConfig : eventList) {
            eventTrigger = new JsonObject();
            eventTrigger.addProperty("eventId", eventConfig.getEventId());
            eventTrigger.addProperty("eventLogic", eventConfig.getEventLogic());
            eventTrigger.addProperty("actions", eventConfig.getActions());
            eventTriggers.add(eventTrigger);
        }
        this.eventTriggers = eventTriggers.toString();
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
