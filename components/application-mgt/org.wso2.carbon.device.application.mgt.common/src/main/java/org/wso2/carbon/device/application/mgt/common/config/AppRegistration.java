package org.wso2.carbon.device.application.mgt.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class AppRegistration {

    private List<String> tags;
    private boolean isAllowToAllDomains;

    @XmlElementWrapper(name = "Tags")
    @XmlElement(name = "Tag")
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @XmlElement(name = "AllowToAllDomains")
    public boolean isAllowToAllDomains() {
        return isAllowToAllDomains;
    }

    public void setAllowToAllDomains(boolean allowToAllDomains) {
        isAllowToAllDomains = allowToAllDomains;
    }

}
