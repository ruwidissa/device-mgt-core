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


package io.entgra.device.mgt.core.device.mgt.common.operation.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;

import java.util.List;

@ApiModel(value = "DeviceActivity", description = "An activity instance carries a unique identifier that can be " +
        "used to identify a particular operation instance uniquely")
public class DeviceActivity {

    public enum Type {
        CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY
    }

    public enum Status {
        IN_PROGRESS, PENDING, COMPLETED, ERROR, REPEATED, INVALID, UNAUTHORIZED, NOTNOW, REQUIRED_CONFIRMATION, CONFIRMED
    }

    @ApiModelProperty(
            name = "activityId",
            value = "Device Activity identifier",
            required = true,
            example = "ACTIVITY_1")
    @JsonProperty("activityId")
    private String activityId;
    @ApiModelProperty(
            name = "code",
            value = "Device Activity code",
            required = true,
            example = "DEVICE_RING")
    @JsonProperty("code")
    private String code;
    @ApiModelProperty(
            name = "operationId",
            value = "Operation Id",
            required = false,
            example = "10")
    @JsonProperty("operationId")
    private int operationId;
    @ApiModelProperty(
            name = "type",
            value = "Activity type",
            required = true,
            allowableValues = "CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY",
            example = "COMMAND")
    @JsonProperty("type")
    private Type type;

    @ApiModelProperty(
            name = "status",
            value = "Status of the device activity performed.",
            required = true,
            example = "PENDING")
    @JsonProperty("status")
    private Status status;
    @ApiModelProperty(
            name = "createdTimeStamp",
            value = "Timestamp recorded when the activity took place",
            required = true,
            example = "Thu Oct 06 11:18:47 IST 2016")
    @JsonProperty("createdTimestamp")
    private String createdTimeStamp;
    @ApiModelProperty(
            name = "deviceActivities",
            value = "Collection of devices activities corresponding to the activity",
            required = true)
    @JsonProperty("deviceActivities")
    private List<DeviceActivity> deviceActivities;
    @ApiModelProperty(
            name = "initiatedBy",
            value = "Initiated user",
            required = true)
    @JsonProperty("initiatedBy")
    private String initiatedBy;
    @ApiModelProperty(
            name = "deviceIdentifier",
            value = "Device identifier of the device.",
            required = true)
    @JsonProperty("deviceIdentifier")
    private DeviceIdentifier deviceIdentifier;
    @ApiModelProperty(
            name = "responses",
            value = "Responses received from devices.",
            required = true)
    @JsonProperty("responses")
    private List<OperationResponse> responses;
    @ApiModelProperty(
            name = "updatedTimestamp    ",
            value = "Last updated time of the activity.",
            required = true,
            example = "Thu Oct 06 11:18:47 IST 2016")
    @JsonProperty("updatedTimestamp")
    private String updatedTimestamp;

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public List<OperationResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<OperationResponse> responses) {
        this.responses = responses;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public List<DeviceActivity> getDeviceActivities() {
        return deviceActivities;
    }

    public void setDeviceActivities(List<DeviceActivity> deviceActivities) {
        this.deviceActivities = deviceActivities;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}

