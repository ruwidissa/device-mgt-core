package io.entgra.devicemgt.apimgt.extension.publisher.api.internal;

import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherRESTAPIServices;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherAPIServiceStartupHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * @scr.component name="io.entgra.devicemgt.apimgt.extension.publisher.api.internal.PublisherRESTAPIServiceComponent"
 * immediate="true"
 */
public class PublisherRESTAPIServiceComponent {

    private static Log log = LogFactory.getLog(PublisherRESTAPIServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing publisher API extension bundle");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            PublisherAPIServiceStartupHandler publisherAPIServiceStartupHandler = new PublisherAPIServiceStartupHandler();
            bundleContext.registerService(PublisherAPIServiceStartupHandler.class.getName(), publisherAPIServiceStartupHandler, null);
            bundleContext.registerService(ServerStartupObserver.class.getName(), publisherAPIServiceStartupHandler, null);
            bundleContext.registerService(ServerShutdownHandler.class.getName(), publisherAPIServiceStartupHandler, null);

            PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServices();
            bundleContext.registerService(PublisherRESTAPIServices.class.getName(), publisherRESTAPIServices, null);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }
}
