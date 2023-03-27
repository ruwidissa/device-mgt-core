package io.entgra.devicemgt.apimgt.extension.publisher.api.internal;


import com.sun.jndi.toolkit.ctx.ComponentContext;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherAPIService;
import io.entgra.devicemgt.apimgt.extension.publisher.api.PublisherAPIServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import java.io.IOException;

/**
 * @scr.component name="io.entgra.devicemgt.apimgt.extension.publisher.api.internal.PublisherAPIServiceComponent"
 * immediate="true"
 */
public class PublisherAPIServiceComponent {

    private static Log log = LogFactory.getLog(PublisherAPIServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing publisher API extension bundle");
        }

        PublisherAPIService publisherAPIService = new PublisherAPIServiceImpl();

    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }
}
