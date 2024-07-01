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

package io.entgra.device.mgt.core.application.mgt.common.dto;

import java.sql.Timestamp;

public class DeviceSubscriptionDTO {

    private int id;
    private String subscribedBy;
    private Timestamp subscribedTimestamp;
    private boolean isUnsubscribed;
    private String unsubscribedBy;
    private Timestamp unsubscribedTimestamp;
    private String actionTriggeredFrom;
    private String status;
    private int deviceId;
    private int appReleaseId;
    private String appUuid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubscribedBy() {
        return subscribedBy;
    }

    public void setSubscribedBy(String subscribedBy) {
        this.subscribedBy = subscribedBy;
    }

    public Timestamp getSubscribedTimestamp() {
        return subscribedTimestamp;
    }

    public void setSubscribedTimestamp(Timestamp subscribedTimestamp) {
        this.subscribedTimestamp = subscribedTimestamp;
    }

    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    public void setUnsubscribed(boolean unsubscribed) {
        isUnsubscribed = unsubscribed;
    }

    public String getUnsubscribedBy() {
        return unsubscribedBy;
    }

    public void setUnsubscribedBy(String unsubscribedBy) {
        this.unsubscribedBy = unsubscribedBy;
    }

    public Timestamp getUnsubscribedTimestamp() {
        return unsubscribedTimestamp;
    }

    public void setUnsubscribedTimestamp(Timestamp unsubscribedTimestamp) {
        this.unsubscribedTimestamp = unsubscribedTimestamp;
    }

    public String getActionTriggeredFrom() {
        return actionTriggeredFrom;
    }

    public void setActionTriggeredFrom(String actionTriggeredFrom) {
        this.actionTriggeredFrom = actionTriggeredFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getAppReleaseId() {
        return appReleaseId;
    }

    public void setAppReleaseId(int appReleaseId) {
        this.appReleaseId = appReleaseId;
    }

    public String getAppUuid() {
        return appUuid;
    }

    public void setAppUuid(String appUuid) {
        this.appUuid = appUuid;
    }
}
