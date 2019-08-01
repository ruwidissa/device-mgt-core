package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeviceTypeManagerExtensionConfig")
public class DeviceTypeManagerExtensionConfig {

    private String extensionClass;

    @XmlElement(name = "ExtensionClass", required = true)
    public String getExtensionClass() {
        return extensionClass;
    }

    public void setExtensionClass(String extensionClass) {
        this.extensionClass = extensionClass;
    }
}
