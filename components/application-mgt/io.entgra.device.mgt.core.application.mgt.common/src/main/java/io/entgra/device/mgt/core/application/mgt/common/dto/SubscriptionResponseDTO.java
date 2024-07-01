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

import java.util.List;

public class SubscriptionResponseDTO {

    private String UUID;
    private List<SubscriptionsDTO> subscriptions;
    private List<DeviceOperationDTO> DevicesOperations;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public List<DeviceOperationDTO> getDevicesOperations() {
        return DevicesOperations;
    }

    public void setDevicesOperations(List<DeviceOperationDTO> devicesOperations) {
        DevicesOperations = devicesOperations;
    }

    public List<SubscriptionsDTO> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionsDTO> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
