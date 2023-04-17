package io.entgra.devicemgt.apimgt.extension.publisher.api.internal;

import io.entgra.devicemgt.apimgt.extension.publisher.api.APIApplicationServices;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherRESTAPIServices;

public class PublisherRESTAPIDataHolder {

    private static final PublisherRESTAPIDataHolder thisInstance = new PublisherRESTAPIDataHolder();

    private APIApplicationServices apiApplicationServices;
//
//    private PublisherRESTAPIServices publisherRESTAPIServices;

    public static PublisherRESTAPIDataHolder getInstance(){
        return thisInstance;
    }
    public APIApplicationServices getApiApplicationServices() {
        return apiApplicationServices;
    }
    public void setApiApplicationServices(APIApplicationServices apiApplicationServices) {
        this.apiApplicationServices = apiApplicationServices;
    }
//    public PublisherRESTAPIServices getPublisherRESTAPIServices() {
//        return publisherRESTAPIServices;
//    }
//    public void setPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
//        this.publisherRESTAPIServices = publisherRESTAPIServices;
//    }

}
