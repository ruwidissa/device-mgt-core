/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.keymgt.extension.service.KeyMgtService;
import org.wso2.carbon.apimgt.keymgt.extension.service.KeyMgtServiceImpl;

/**
 * @scr.component name="org.wso2.carbon.apimgt.keymgt.extension.keyMgtServiceComponent" immediate="true"
 */
public class KeyMgtServiceComponent {

    private static final Log log = LogFactory.getLog(KeyMgtServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing key management bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();

            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            bundleContext.registerService(KeyMgtService.class.getName(), keyMgtService, null);
            KeyMgtDataHolder.getInstance().setKeyMgtService(keyMgtService);

            if (log.isDebugEnabled()) {
                log.debug("Key management bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing key management bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Key Management Service Component");
        }
    }
}
