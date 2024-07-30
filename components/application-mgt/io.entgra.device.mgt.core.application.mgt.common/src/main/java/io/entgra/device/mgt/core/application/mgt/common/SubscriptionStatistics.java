/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.common;

public class SubscriptionStatistics {
    private float completedPercentage;
    private float failedPercentage;
    private float pendingPercentage;
    private float newDevicesPercentage;

    public SubscriptionStatistics() {}
    public SubscriptionStatistics(float completedPercentage, float failedPercentage, float pendingPercentage,
                                  float newDevicesPercentage) {
        this.completedPercentage = completedPercentage;
        this.failedPercentage = failedPercentage;
        this.pendingPercentage = pendingPercentage;
        this.newDevicesPercentage = newDevicesPercentage;
    }

    public float getCompletedPercentage() {
        return completedPercentage;
    }

    public void setCompletedPercentage(float completedPercentage) {
        this.completedPercentage = completedPercentage;
    }

    public float getFailedPercentage() {
        return failedPercentage;
    }

    public void setFailedPercentage(float failedPercentage) {
        this.failedPercentage = failedPercentage;
    }

    public float getPendingPercentage() {
        return pendingPercentage;
    }

    public void setPendingPercentage(float pendingPercentage) {
        this.pendingPercentage = pendingPercentage;
    }

    public float getNewDevicesPercentage() {
        return newDevicesPercentage;
    }

    public void setNewDevicesPercentage(float newDevicesPercentage) {
        this.newDevicesPercentage = newDevicesPercentage;
    }
}
