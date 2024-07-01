/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.application.mgt.common.dto;

import io.entgra.device.mgt.core.device.mgt.core.dto.OperationResponseDTO;

import java.sql.Timestamp;
import java.util.List;

public class DeviceOperationDTO {
    private int deviceId;
    private String uuid;
    private String status;
    private int operationId;
    private String actionTriggeredFrom;
    private Timestamp actionTriggeredAt;
    private int appReleaseId;
    private String operationCode;
    private Object operationDetails;
    private Object operationProperties;
    private List<OperationResponseDTO> operationResponses;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getActionTriggeredFrom() {
        return actionTriggeredFrom;
    }

    public void setActionTriggeredFrom(String actionTriggeredFrom) {
        this.actionTriggeredFrom = actionTriggeredFrom;
    }

    public Timestamp getActionTriggeredAt() {
        return actionTriggeredAt;
    }

    public void setActionTriggeredAt(Timestamp actionTriggeredAt) {
        this.actionTriggeredAt = actionTriggeredAt;
    }

    public int getAppReleaseId() {
        return appReleaseId;
    }

    public void setAppReleaseId(int appReleaseId) {
        this.appReleaseId = appReleaseId;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public Object getOperationDetails() {
        return operationDetails;
    }

    public void setOperationDetails(Object operationDetails) {
        this.operationDetails = operationDetails;
    }

    public Object getOperationProperties() {
        return operationProperties;
    }

    public void setOperationProperties(Object operationProperties) {
        this.operationProperties = operationProperties;
    }

    public List<OperationResponseDTO> getOperationResponses() {
        return operationResponses;
    }

    public void setOperationResponses(List<OperationResponseDTO> operationResponses) {
        this.operationResponses = operationResponses;
    }
}
