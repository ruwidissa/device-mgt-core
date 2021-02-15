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

package io.entgra.sms.mgt.core.internal;

import io.entgra.sms.mgt.core.config.SMSConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component
 * name="io.entgra.sms.mgt.core.internal.SMSMgtServiceComponent" immediate="true"
 */
public class SMSMgtServiceComponent {

    private static final Log log = LogFactory.getLog(SMSMgtServiceComponent.class);

    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Activating SMS Management Service Component");
        }
        try {
            SMSConfigurationManager.getInstance().initConfig();

            if (log.isDebugEnabled()) {
                log.debug("SMS Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating SMS Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating SMS Management Service Component");
        }
    }
}
