/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.common;

import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Timestamp;

public class DeviceSubscriptionData {

    private int subId;
    private String action;
    private long actionTriggeredTimestamp;
    private String actionTriggeredBy;
    private String actionType;
    private String status;
    private Device device;
    private String currentInstalledVersion;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getActionTriggeredTimestamp() {
        return actionTriggeredTimestamp;
    }

    public void setActionTriggeredTimestamp(long actionTriggeredTimestamp) {
        this.actionTriggeredTimestamp = actionTriggeredTimestamp;
    }

    public String getActionTriggeredBy() {
        return actionTriggeredBy;
    }

    public void setActionTriggeredBy(String actionTriggeredBy) {
        this.actionTriggeredBy = actionTriggeredBy;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getCurrentInstalledVersion() { return currentInstalledVersion; }

    public void setCurrentInstalledVersion(String currentInstalledVersion) { this.currentInstalledVersion = currentInstalledVersion; }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }
}
