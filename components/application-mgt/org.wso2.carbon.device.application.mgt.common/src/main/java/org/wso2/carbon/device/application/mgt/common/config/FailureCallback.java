package org.wso2.carbon.device.application.mgt.common.config;

import javax.xml.bind.annotation.XmlElement;

public class FailureCallback {

    private String badRequest;
    private String unauthorized;
    private String forbidden;
    private String notFound;
    private String methodNotAllowed;
    private String notAcceptable;
    private String unsupportedMediaType;
    private String internalServerError;
    private String defaultPage;

    @XmlElement(name = "BadRequest", required=true)
    public String getBadRequest() {
        return badRequest;
    }

    public void setBadRequest(String badRequest) {
        this.badRequest = badRequest;
    }

    @XmlElement(name = "Unauthorized", required=true)
    public String getUnauthorized() {
        return unauthorized;
    }

    public void setUnauthorized(String unauthorized) {
        this.unauthorized = unauthorized;
    }

    @XmlElement(name = "Forbidden", required=true)
    public String getForbidden() {
        return forbidden;
    }

    public void setForbidden(String forbidden) {
        this.forbidden = forbidden;
    }
    @XmlElement(name = "NotFound", required=true)
    public String getNotFound() {
        return notFound;
    }

    public void setNotFound(String notFound) {
        this.notFound = notFound;
    }

    @XmlElement(name = "MethodNotAllowed", required=true)
    public String getMethodNotAllowed() {
        return methodNotAllowed;
    }

    public void setMethodNotAllowed(String methodNotAllowed) {
        this.methodNotAllowed = methodNotAllowed;
    }

    @XmlElement(name = "NotAcceptable", required=true)
    public String getNotAcceptable() {
        return notAcceptable;
    }

    public void setNotAcceptable(String notAcceptable) {
        this.notAcceptable = notAcceptable;
    }

    @XmlElement(name = "UnsupportedMediaType", required=true)
    public String getUnsupportedMediaType() {
        return unsupportedMediaType;
    }

    public void setUnsupportedMediaType(String unsupportedMediaType) {
        this.unsupportedMediaType = unsupportedMediaType;
    }

    @XmlElement(name = "InternalServerError", required=true)
    public String getInternalServerError() {
        return internalServerError;
    }

    public void setInternalServerError(String internalServerError) {
        this.internalServerError = internalServerError;
    }

    @XmlElement(name = "DefaultPage", required=true)
    public String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }
}
