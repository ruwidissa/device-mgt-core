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

package io.entgra.device.mgt.core.subtype.mgt.dto;


import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashSet;
import java.util.Set;

public abstract class DeviceSubType {

    private String subTypeId;
    private int tenantId;
    private String deviceType;
    private String subTypeName;
    private String typeDefinition;
    private final Set<String> supportedOperations = new HashSet<>();
    
    public DeviceSubType() {
    }

    public DeviceSubType(String subTypeId, int tenantId, String deviceType,
                         String subTypeName, String typeDefinition,
                         Set<String> supportedOperations) {
        this.subTypeId = subTypeId;
        this.tenantId = tenantId;
        this.deviceType = deviceType;
        this.subTypeName = subTypeName;
        this.typeDefinition = typeDefinition;
        if (supportedOperations != null && !supportedOperations.isEmpty()) {
            this.supportedOperations.addAll(supportedOperations);
        }
    }

    public String getSubTypeId() {
        return subTypeId;
    }

    public void setSubTypeId(String subTypeId) {
        this.subTypeId = subTypeId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSubTypeName() {
        return subTypeName;
    }

    public void setSubTypeName(String subTypeName) {
        this.subTypeName = subTypeName;
    }

    public String getTypeDefinition() {
        return typeDefinition;
    }

    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    public abstract <T> DeviceSubType convertToDeviceSubType();

    public abstract String parseSubTypeToJson() throws JsonProcessingException;

    public void setSupportedOperations(Set<String> supportedOperations) {
        this.supportedOperations.addAll(supportedOperations);
    }

    public void addSupportedOperation(String code) {
        supportedOperations.add(code);
    }

    public Set<String> getSupportedOperations() {
        return supportedOperations;
    }

}
