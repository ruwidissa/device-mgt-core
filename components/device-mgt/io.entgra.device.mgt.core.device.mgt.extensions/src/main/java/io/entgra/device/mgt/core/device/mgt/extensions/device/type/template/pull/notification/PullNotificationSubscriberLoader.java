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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.pull.notification;

import io.entgra.device.mgt.core.device.mgt.common.pull.notification.PullNotificationSubscriber;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.ConfigProperties;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.Property;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;

import java.util.HashMap;
import java.util.Map;

/**
 * This creates an instance of the pull notification executor strategy with the given class name.
 * makes sure the class name starts with the package prefix io.entgra.device.mgt.core.pull.notification.*
 */
public class PullNotificationSubscriberLoader {

    private PullNotificationSubscriber pullNotificationSubscriber;

    public PullNotificationSubscriberLoader(String className, ConfigProperties configProperties) {
        try {
            Class<? extends PullNotificationSubscriber> pullNotificationExecutorClass
                    = Class.forName(className).asSubclass(PullNotificationSubscriber.class);
            Map<String, String> properties = new HashMap<>();
            if (configProperties != null) {
                for (Property property : configProperties.getProperty()) {
                    properties.put(property.getName(), property.getValue());
                }
            }
            pullNotificationSubscriber = pullNotificationExecutorClass.newInstance();
            pullNotificationSubscriber.init(properties);
        } catch (ClassNotFoundException e) {
            throw new DeviceTypeDeployerPayloadException("Unable to find the class pull notification executor: " + className, e);
        } catch (InstantiationException e) {
            throw new DeviceTypeDeployerPayloadException("Unable to create an instance of :" + className, e);
        } catch (IllegalAccessException e) {
            throw new DeviceTypeDeployerPayloadException("Access of the instance in not allowed.", e);
        }
    }

    public PullNotificationSubscriber getPullNotificationSubscriber() {
        return pullNotificationSubscriber;
    }
}
