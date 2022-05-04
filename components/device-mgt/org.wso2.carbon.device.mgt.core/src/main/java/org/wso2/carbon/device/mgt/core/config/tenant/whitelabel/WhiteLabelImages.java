package org.wso2.carbon.device.mgt.core.config.tenant.whitelabel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WhiteLabelImages")
public class WhiteLabelImages {

    private String storagePath;
    private String defaultImagesLocation;
    private String defaultFaviconName;
    private String defaultLogoName;

    @XmlElement(name = "StoragePath", required = true)
    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @XmlElement(name = "DefaultFaviconName", required = true)
    public String getDefaultFaviconName() {
        return defaultFaviconName;
    }

    public void setDefaultFaviconName(String defaultFaviconName) {
        this.defaultFaviconName = defaultFaviconName;
    }

    @XmlElement(name = "DefaultLogoName", required = true)
    public String getDefaultLogoName() {
        return defaultLogoName;
    }

    public void setDefaultLogoName(String defaultLogoName) {
        this.defaultLogoName = defaultLogoName;
    }

    @XmlElement(name = "DefaultImagesLocation", required = true)
    public String getDefaultImagesLocation() {
        return defaultImagesLocation;
    }

    public void setDefaultImagesLocation(String defaultImagesLocation) {
        this.defaultImagesLocation = defaultImagesLocation;
    }
}
