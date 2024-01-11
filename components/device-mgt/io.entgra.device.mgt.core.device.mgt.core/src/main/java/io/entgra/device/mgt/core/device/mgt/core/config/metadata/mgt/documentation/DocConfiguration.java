package io.entgra.device.mgt.core.device.mgt.core.config.metadata.mgt.documentation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DocConfiguration")
public class DocConfiguration {
    private String docUrl;

    @XmlElement(name = "DocUrl", required = true)
    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }
}
