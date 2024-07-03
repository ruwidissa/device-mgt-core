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

package io.entgra.device.mgt.core.device.mgt.core.dto;

import org.json.JSONObject;
import java.util.List;

public class OperationDTO {
    private int operationId;
    private String operationCode;
    private JSONObject operationDetails;
    private JSONObject operationProperties;
    private List<OperationResponseDTO> operationResponses;

    // Getters and Setters

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public JSONObject getOperationDetails() {
        return operationDetails;
    }

    public void setOperationDetails(JSONObject operationDetails) {
        this.operationDetails = operationDetails;
    }

    public JSONObject getOperationProperties() {
        return operationProperties;
    }

    public void setOperationProperties(JSONObject operationProperties) {
        this.operationProperties = operationProperties;
    }

    public List<OperationResponseDTO> getOperationResponses() {
        return operationResponses;
    }

    public void setOperationResponses(List<OperationResponseDTO> operationResponses) {
        this.operationResponses = operationResponses;
    }
}
