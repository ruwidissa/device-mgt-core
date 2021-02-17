/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.transport.mgt.sms.handler.core.internal;

import io.entgra.transport.mgt.sms.handler.core.config.SMSConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component
 * name="io.entgra.transport.mgt.sms.handler.core.internal.SMSHandlerServiceComponent" immediate="true"
 */
public class SMSHandlerServiceComponent {

    private static final Log log = LogFactory.getLog(SMSHandlerServiceComponent.class);

    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Activating SMS Handler Service Component");
        }
        try {
            SMSConfigurationManager.getInstance().initConfig();

            if (log.isDebugEnabled()) {
                log.debug("SMS Handler Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating SMS Handler Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating SMS Handler Service Component");
        }
    }
}
