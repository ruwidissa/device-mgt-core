package org.wso2.carbon.device.mgt.core.config.ui;

import javax.xml.bind.annotation.XmlElement;

public class HubspotChat {
    private boolean isEnableHubspot;
    private String trackingUrl;
    private String accessToken;
    private String senderActorId;
    private long channelAccountId;

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
    @XmlElement(name = "ChannelAccountId")
    public long getChannelAccountId() {
        return channelAccountId;
    }

    public void setChannelAccountId(long channelAccountId) {
        this.channelAccountId = channelAccountId;
    }
}
