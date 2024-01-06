package io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer;

import java.util.ArrayList;

public class ApplicationGrantTypeUpdater {

    private String callbackUrl;

    private ArrayList<String> supportedGrantTypes;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public ArrayList<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(ArrayList<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }
}
