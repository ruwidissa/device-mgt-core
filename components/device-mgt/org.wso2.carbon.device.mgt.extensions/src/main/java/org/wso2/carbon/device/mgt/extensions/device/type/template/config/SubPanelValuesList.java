package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;

public class SubPanelValuesList {
    @XmlElement(name = "PanelSwitch")
    private String itemSwitch;

    @XmlElement(name = "PayloadKey")
    private String itemPayload;

    public String getSwitchItem() {
        return itemSwitch;
    }

    public void setSwitchItem(String itemSwitch) {
        this.itemSwitch = itemSwitch;
    }

    public String getPayloadItem() {
        return itemPayload;
    }

    public void setPayloadItem(String itemPayload) {
        this.itemPayload = itemPayload;
    }
}
