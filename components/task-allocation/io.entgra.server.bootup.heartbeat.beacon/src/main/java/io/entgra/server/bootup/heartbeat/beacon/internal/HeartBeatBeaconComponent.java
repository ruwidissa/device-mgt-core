/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.server.bootup.heartbeat.beacon.internal;

import io.entgra.server.bootup.heartbeat.beacon.HeartBeatBeaconConfig;
import io.entgra.server.bootup.heartbeat.beacon.HeartBeatBeaconUtils;
import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import io.entgra.server.bootup.heartbeat.beacon.service.HeartBeatManagementServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="io.entgra.server.bootup.heartbeat.beacon.heartbeatBeaconComponent"
 * immediate="true"
 */
public class HeartBeatBeaconComponent {

    private static Log log = LogFactory.getLog(HeartBeatBeaconComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing email sender core bundle");
            }
            //heart beat notifier configuration */
            HeartBeatBeaconConfig.init();

            this.registerHeartBeatServices(componentContext);

            //Setting up executors to notify heart beat status */
            HeartBeatInternalUtils.setUpNotifiers(HeartBeatBeaconUtils.getServerDetails());

            if (log.isDebugEnabled()) {
                log.debug("Email sender core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing email sender core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void registerHeartBeatServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering Heart Beat Management service");
        }
        HeartBeatManagementService heartBeatServiceProvider = new HeartBeatManagementServiceImpl();
        HeartBeatBeaconDataHolder.getInstance().setHeartBeatManagementService(heartBeatServiceProvider);
        componentContext.getBundleContext().registerService(HeartBeatManagementService.class, heartBeatServiceProvider, null);
    }

}
