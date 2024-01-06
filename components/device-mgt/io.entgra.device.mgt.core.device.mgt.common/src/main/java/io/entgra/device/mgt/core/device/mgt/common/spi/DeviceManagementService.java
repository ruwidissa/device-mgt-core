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
package io.entgra.device.mgt.core.device.mgt.common.spi;

import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManager;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.general.GeneralConfig;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitationDetails;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import io.entgra.device.mgt.core.device.mgt.common.pull.notification.PullNotificationSubscriber;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypePlatformDetails;

/**
 * Composite interface that acts as the SPI exposing all device management as well as application management
 * functionalities.
 */
public interface DeviceManagementService {

    void init() throws DeviceManagementException;

    String getType();

    OperationMonitoringTaskConfig getOperationMonitoringConfig();

    DeviceManager getDeviceManager();

    ApplicationManager getApplicationManager();

    ProvisioningConfig getProvisioningConfig();

    PushNotificationConfig getPushNotificationConfig();

    PolicyMonitoringManager getPolicyMonitoringManager();

    InitialOperationConfig getInitialOperationConfig();

    StartupOperationConfig getStartupOperationConfig();

    PullNotificationSubscriber getPullNotificationSubscriber();

    DeviceStatusTaskPluginConfig getDeviceStatusTaskPluginConfig();

    GeneralConfig getGeneralConfig();

    DeviceTypePlatformDetails getDeviceTypePlatformDetails();

    DeviceEnrollmentInvitationDetails getDeviceEnrollmentInvitationDetails();

    License getLicenseConfig();

    DeviceTypeMetaDefinition getDeviceTypeMetaDefinition();
}
