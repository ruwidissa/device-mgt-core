/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.config.ui;

import javax.xml.bind.annotation.XmlElement;

public class HubspotChat {
    private boolean isEnableHubspot;
    private String trackingUrl;
    private String accessToken;
    private String senderActorId;

    @XmlElement(name = "EnableHubspot")
    public boolean isEnableHubspot() {
        return isEnableHubspot;
    }

    public void setEnableHubspot(boolean enableHubspot) {
        isEnableHubspot = enableHubspot;
    }

    @XmlElement(name = "TrackingUrl")
    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    @XmlElement(name = "AccessToken")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    @XmlElement(name = "SenderActorId")
    public String getSenderActorId() {
        return senderActorId;
    }

    public void setSenderActorId(String senderActorId) {
        this.senderActorId = senderActorId;
    }
}
