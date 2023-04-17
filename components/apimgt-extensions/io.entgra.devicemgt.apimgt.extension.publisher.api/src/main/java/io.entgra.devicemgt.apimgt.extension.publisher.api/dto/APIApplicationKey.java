package io.entgra.devicemgt.apimgt.extension.publisher.api.dto;

/**
 * This holds api application consumer id and secret.
 */
public class APIApplicationKey {
    private String clientName;
    private String clientId;
    private String clientSecret;
    private String callBackURL;
    private String isSaasApplication;
    private String appOwner;
    private String jsonString;
    private String jsonAppAttribute;
    private String tokenType;

    public APIApplicationKey(String clientId, String clientSecret){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getIsSaasApplication() {
        return isSaasApplication;
    }

    public void setIsSaasApplication(String isSaasApplication) {
        this.isSaasApplication = isSaasApplication;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getJsonAppAttribute() {
        return jsonAppAttribute;
    }

    public void setJsonAppAttribute(String jsonAppAttribute) {
        this.jsonAppAttribute = jsonAppAttribute;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

}
