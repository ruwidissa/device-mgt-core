package org.wso2.carbon.device.mgt.core.config.tenant.whitelabel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MetaDataConfiguration")
public class MetaDataConfiguration {
    private WhiteLabelConfiguration whiteLabelConfiguration;

    @XmlElement(name = "WhiteLabelConfiguration", required = true)
    public WhiteLabelConfiguration getWhiteLabelConfiguration() {
        return whiteLabelConfiguration;
    }

    public void setWhiteLabelConfiguration(WhiteLabelConfiguration whiteLabelConfiguration) {
        this.whiteLabelConfiguration = whiteLabelConfiguration;
    }
}
