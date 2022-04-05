package io.entgra.application.mgt.core.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "IdentityServiceProvider")
public class IdentityServiceProvider {
    private String providerName;
    private String providerClassName;
    private String serviceProvidersPageUri;

    @XmlElement(name = "ProviderName")
    public String getProviderName() {
        return providerName;
    }

    @XmlElement(name = "ProviderClassName")
    public String getProviderClassName() {
        return providerClassName;
    }

    @XmlElement(name = "ServiceProvidersPageUri")
    public String getServiceProvidersPageUri() {
        return serviceProvidersPageUri;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setServiceProvidersPageUri(String serviceProvidersPageUri) {
        this.serviceProvidersPageUri = serviceProvidersPageUri;
    }

    public void setProviderClassName(String providerClassName) {
        this.providerClassName = providerClassName;
    }
}
