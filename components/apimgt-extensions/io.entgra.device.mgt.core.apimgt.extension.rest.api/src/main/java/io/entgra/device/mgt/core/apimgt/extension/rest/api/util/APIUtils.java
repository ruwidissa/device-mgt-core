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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.util;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;

/**
 * This class represents the api data.
 */
public class APIUtils {

    private APIIdentifier id;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private String type;
    private String lifeCycleStatus;
    private String workflowStatus;
    private String hasThumbnail;
    private String transport;

    public APIIdentifier getId() {
        return id;
    }

    public void setId(APIIdentifier id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public String getHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(String hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String toJSON() {
        String jsonString = "{\n" +
                "   \"name\":\" " + name + "\",\n" +
                "   \"description\":\" " + description + "\",\n" +
                "   \"context\":\" " + context + " \",\n" +
                "   \"transport\":[\n" +
                "      \" " + transport + " \"\n" +
                "   ]\n" +
                "}";
        return jsonString;
    }
}