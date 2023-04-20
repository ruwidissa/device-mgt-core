package io.entgra.devicemgt.apimgt.extension.rest.api.internal;

import io.entgra.devicemgt.apimgt.extension.rest.api.APIApplicationServices;

public class PublisherRESTAPIDataHolder {

    private static final PublisherRESTAPIDataHolder thisInstance = new PublisherRESTAPIDataHolder();

    private APIApplicationServices apiApplicationServices;

    public static PublisherRESTAPIDataHolder getInstance(){
        return thisInstance;
    }
    public APIApplicationServices getApiApplicationServices() {
        return apiApplicationServices;
    }
    public void setApiApplicationServices(APIApplicationServices apiApplicationServices) {
        this.apiApplicationServices = apiApplicationServices;
    }

}
