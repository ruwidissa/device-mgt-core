/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.common;

import java.sql.Timestamp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "LifecycleState", description = "LifecycleState represents the Lifecycle state for an application release")
public class LifecycleState {

    @ApiModelProperty(name = "currentState",
            value = "Current state of the application release",
            required = true)
    private String currentState;

    @ApiModelProperty(name = "previousState",
            value = "Previous state of the application release",
            required = true)
    private String previousState;

    @ApiModelProperty(name = "nextStates",
            value = "Next possible transferring states from the current state")
    private List<String> nextStates;

    @ApiModelProperty(name = "updatedBy",
            value = "Username who is update the application release state")
    private String updatedBy;

    @ApiModelProperty(name = "updatedAt",
            value = "Timestamp of the lifecycle has been updated")
    private Timestamp updatedAt;

    @ApiModelProperty(name = "reasonForChange",
            value = "Reason for the application release lifecycle change from previous state to current state.")
    private String reasonForChange;

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getNextStates() {
        return nextStates;
    }

    public void setNextStates(List<String> nextStates) {
        this.nextStates = nextStates;
    }

    public String getReasonForChange() { return reasonForChange; }

    public void setReasonForChange(String reasonForChange) { this.reasonForChange = reasonForChange; }
}
