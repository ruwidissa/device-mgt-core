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

package io.entgra.device.mgt.core.application.mgt.common.dto;

public class SubscriptionStatisticDTO {
    private int completedDeviceCount = 0;
    private int pendingDevicesCount = 0;
    private int failedDevicesCount = 0;

    public void addToComplete(int count) {
        completedDeviceCount += count;
    }

    public void addToPending(int count) {
        pendingDevicesCount += count;
    }

    public void addToFailed(int count) {
        failedDevicesCount += count ;
    }

    public int getCompletedDeviceCount() {
        return completedDeviceCount;
    }

    public void setCompletedDeviceCount(int completedDeviceCount) {
        this.completedDeviceCount = completedDeviceCount;
    }

    public int getPendingDevicesCount() {
        return pendingDevicesCount;
    }

    public void setPendingDevicesCount(int pendingDevicesCount) {
        this.pendingDevicesCount = pendingDevicesCount;
    }

    public int getFailedDevicesCount() {
        return failedDevicesCount;
    }

    public void setFailedDevicesCount(int failedDevicesCount) {
        this.failedDevicesCount = failedDevicesCount;
    }
}
