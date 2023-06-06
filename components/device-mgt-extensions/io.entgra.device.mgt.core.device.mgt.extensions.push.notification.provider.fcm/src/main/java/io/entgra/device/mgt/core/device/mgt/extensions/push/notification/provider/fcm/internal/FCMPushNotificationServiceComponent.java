/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal;

import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMPushNotificationServiceComponent",
        immediate = true)
public class FCMPushNotificationServiceComponent {

    private static final Log log = LogFactory.getLog(FCMPushNotificationServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            //Do nothing
            if (log.isDebugEnabled()) {
                log.debug("FCM based push notification provider implementation bundle has been successfully " +
                        "initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing FCM based push notification provider " +
                    "implementation bundle", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //Do nothing
    }
    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementProviderService")
    protected void setDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        FCMDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        FCMDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

}
