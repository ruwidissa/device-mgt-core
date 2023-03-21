/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.operation.template.dto;

public class OperationTemplate {
    private int subTypeId;

    private String deviceType;
    private int operationTemplateId;
    private String code;
    private String operationDefinition;

    public int getSubTypeId() {
        return subTypeId;
    }

    public void setSubTypeId(int subTypeId) {
        this.subTypeId = subTypeId;
    }

    public int getOperationTemplateId() {
        return operationTemplateId;
    }

    public void setOperationTemplateId(int operationTemplateId) {
        this.operationTemplateId = operationTemplateId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperationDefinition() {
        return operationDefinition;
    }

    public void setOperationDefinition(String operationDefinition) {
        this.operationDefinition = operationDefinition;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
