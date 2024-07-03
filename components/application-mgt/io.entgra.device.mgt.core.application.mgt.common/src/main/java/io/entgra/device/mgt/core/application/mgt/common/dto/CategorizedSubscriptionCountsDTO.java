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

public class CategorizedSubscriptionCountsDTO {
    private String subscriptionType;
    private int subscriptionCount;
    private int unsubscriptionCount;

    public CategorizedSubscriptionCountsDTO(String subscriptionType, int subscriptionCount, int unsubscriptionCount) {
        this.subscriptionType = subscriptionType;
        this.subscriptionCount = subscriptionCount;
        this.unsubscriptionCount = unsubscriptionCount;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }

    public int getUnsubscriptionCount() {
        return unsubscriptionCount;
    }

    public void setUnsubscriptionCount(int unsubscriptionCount) {
        this.unsubscriptionCount = unsubscriptionCount;
    }
}
