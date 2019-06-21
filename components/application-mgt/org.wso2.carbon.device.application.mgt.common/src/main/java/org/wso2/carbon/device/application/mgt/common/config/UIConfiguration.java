package org.wso2.carbon.device.application.mgt.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class UIConfiguration {

    private AppRegistration appRegistration;
    private List<String> scopes;
    private boolean isSsoEnable;
    private ErrorCallback errorCallback;

    @XmlElement(name = "AppRegistration", required=true)
    public AppRegistration getAppRegistration() {
        return appRegistration;
    }

    public void setAppRegistration(AppRegistration appRegistration) {
        this.appRegistration = appRegistration;
    }

    @XmlElementWrapper(name = "Scopes")
    @XmlElement(name = "Scope")
    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    @XmlElement(name = "EnableSSO")
    public boolean isSsoEnable() {
        return isSsoEnable;
    }

    public void setSsoEnable(boolean ssoEnable) {
        isSsoEnable = ssoEnable;
    }

    @XmlElement(name = "ErrorCallback", required=true)
    public ErrorCallback getErrorCallback() { return errorCallback; }

    public void setErrorCallback(ErrorCallback errorCallback) { this.errorCallback = errorCallback; }
}
