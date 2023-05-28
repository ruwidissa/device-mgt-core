/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.core.operation;

import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;

import java.util.HashMap;

public class TestNotificationStrategy implements NotificationStrategy {
    private PushNotificationConfig pushNotificationConfig;
    private boolean setToThrowException = false;

    public TestNotificationStrategy(boolean setToThrowException){
        this.setToThrowException = setToThrowException;
        this.pushNotificationConfig = new PushNotificationConfig("TEST", true, new HashMap<>());
    }

    public TestNotificationStrategy(){
       this.pushNotificationConfig = new PushNotificationConfig("TEST", true, new HashMap<>());
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        if (setToThrowException) {
            throw new PushNotificationExecutionFailedException("Generated exception");
        }
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {

    }

    @Override
    public PushNotificationConfig getConfig() {
        return pushNotificationConfig;
    }
}
