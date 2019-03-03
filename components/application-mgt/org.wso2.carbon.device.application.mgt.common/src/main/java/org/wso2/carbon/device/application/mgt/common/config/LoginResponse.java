package org.wso2.carbon.device.application.mgt.common.config;

import javax.xml.bind.annotation.XmlElement;

public class LoginResponse {

    private String successCallback;
    private FailureCallback failureCallback;

    @XmlElement(name = "SuccessCallback", required=true)
    public String getSuccessCallback() {
        return successCallback;
    }

    public void setSuccessCallback(String successCallback) {
        this.successCallback = successCallback;
    }

    @XmlElement(name = "FailureCallback", required=true)
    public FailureCallback getFailureCallback() {
        return failureCallback;
    }

    public void setFailureCallback(FailureCallback failureCallback) {
        this.failureCallback = failureCallback;
    }
}
