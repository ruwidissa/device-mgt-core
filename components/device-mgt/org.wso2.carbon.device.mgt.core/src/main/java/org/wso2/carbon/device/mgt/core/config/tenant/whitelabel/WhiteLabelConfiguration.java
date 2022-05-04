package org.wso2.carbon.device.mgt.core.config.tenant.whitelabel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WhiteLabelConfiguration")
public class WhiteLabelConfiguration {
    private String footerText;
    private WhiteLabelImages whiteLabelImages;

    @XmlElement(name = "FooterText", required = true)
    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    @XmlElement(name = "WhiteLabelImages", required = true)
    public WhiteLabelImages getWhiteLabelImages() {
        return whiteLabelImages;
    }

    public void setWhiteLabelImages(WhiteLabelImages whiteLabelImages) {
        this.whiteLabelImages = whiteLabelImages;
    }
}
