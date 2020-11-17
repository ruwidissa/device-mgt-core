/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.server.bootup.heartbeat.beacon.internal;

import io.entgra.server.bootup.heartbeat.beacon.HeartBeatBeaconConfig;
import io.entgra.server.bootup.heartbeat.beacon.HeartBeatBeaconConfigurationException;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeatInternalUtils {

    private static Log log = LogFactory.getLog(HeartBeatInternalUtils.class);
    private static final int DEFAULT__NOTIFIER_INTERVAL = 5;
    private static final int DEFAULT_NOTIFIER_DELAY = 5;
    private static HeartBeatBeaconConfig CONFIG;

    static {
        CONFIG = HeartBeatBeaconConfig.getInstance();
    }

    static void setUpNotifiers(ServerContext ctx) throws HeartBeatBeaconConfigurationException {
        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        if(CONFIG == null){
            throw new HeartBeatBeaconConfigurationException("Error while initiating schedule taks for recording heartbeats.");
        }

        try {
            String uuid = HeartBeatBeaconDataHolder.getInstance().getHeartBeatManagementService().updateServerContext(ctx);
            HeartBeatBeaconDataHolder.getInstance().setLocalServerUUID(uuid);
            Runnable periodicTask = new Runnable() {
                public void run() {
                    try {
                        recordHeartBeat(uuid);
                    } catch (HeartBeatManagementException e) {
                        log.error("Error while executing record heart beat task. This will result in schedule operation malfunction.", e);
                    }
                }
            };
            executor.scheduleAtFixedRate(periodicTask,
                                         CONFIG.getNotifierDelay() != 0 ? CONFIG.getNotifierDelay() : DEFAULT_NOTIFIER_DELAY,
                                         CONFIG.getNotifierFrequency() != 0 ? CONFIG.getNotifierFrequency() : DEFAULT__NOTIFIER_INTERVAL,
                                         TimeUnit.SECONDS);
        } catch (HeartBeatManagementException e) {
            throw new HeartBeatBeaconConfigurationException("Error occured while updating initial server context.");
        }
    }

    static void recordHeartBeat(String uuid) throws HeartBeatManagementException {
        HeartBeatBeaconDataHolder.getInstance().getHeartBeatManagementService().recordHeartBeat(new HeartBeatEvent(uuid));
    }

}
