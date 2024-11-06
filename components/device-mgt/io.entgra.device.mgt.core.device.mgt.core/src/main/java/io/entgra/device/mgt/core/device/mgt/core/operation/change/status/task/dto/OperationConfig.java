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

package io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto;

/**
 * DTO for Operation configuration.
 */

public class OperationConfig {

    private String[] deviceTypes;
    private String initialOperationStatus;
    private String requiredStatusChange;

    public String[] getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(String[] deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public String getInitialOperationStatus() {
        return initialOperationStatus;
    }

    public void setInitialOperationStatus(String initialOperationStatus) {
        this.initialOperationStatus = initialOperationStatus;
    }

    public String getRequiredStatusChange() {
        return requiredStatusChange;
    }

    public void setRequiredStatusChange(String requiredStatusChange) {
        this.requiredStatusChange = requiredStatusChange;
    }

}
