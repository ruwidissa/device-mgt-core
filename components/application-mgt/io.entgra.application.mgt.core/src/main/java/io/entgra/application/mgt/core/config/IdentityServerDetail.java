package io.entgra.application.mgt.core.config;

import javax.xml.bind.annotation.XmlAttribute;

public class IdentityServerDetail {
    private String providerName;
    private String serviceProvidersPageUri;
    private String serviceProvidersAPIContextPath;

    @XmlAttribute(name = "ProviderName")
    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }


    @XmlAttribute(name = "ServiceProvidersPageUri")
    public String getServiceProvidersPageUri() {
        return serviceProvidersPageUri;
    }

    public void setServiceProvidersPageUri(String serviceProvidersPageUri) {
        this.serviceProvidersPageUri = serviceProvidersPageUri;
    }

    @XmlAttribute(name = "ServiceProvidersAPIContextPath")
    public String getServiceProvidersAPIContextPath() {
        return serviceProvidersAPIContextPath;
    }

    public void setServiceProvidersAPIContextPath(String serviceProvidersAPIContextPath) {
        this.serviceProvidersAPIContextPath = serviceProvidersAPIContextPath;
    }
}
