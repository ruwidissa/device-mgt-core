/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 *   Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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
 */
package org.wso2.carbon.device.mgt.common.spi;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.general.GeneralConfig;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitationDetails;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationSubscriber;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypePlatformDetails;

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
}
