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
package io.entgra.device.mgt.core.device.mgt.core;

import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManager;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.general.GeneralConfig;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitationDetails;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import io.entgra.device.mgt.core.device.mgt.common.pull.notification.PullNotificationSubscriber;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypePlatformDetails;

import java.util.ArrayList;
import java.util.List;

public class TestDeviceManagementService implements DeviceManagementService {

    private String providerType;
    private String tenantDomain;
    private String operationCode;

    public TestDeviceManagementService(String deviceType, String tenantDomain, String operationCode) {
        providerType = deviceType;
        this.tenantDomain = tenantDomain;
        this.operationCode = operationCode;
    }

    public TestDeviceManagementService(String deviceType, String tenantDomain) {
        providerType = deviceType;
        this.tenantDomain = tenantDomain;
        this.operationCode = "DEVICE_INFO";
    }

    @Override
    public String getType() {
        return providerType;
    }

    @Override
    public OperationMonitoringTaskConfig getOperationMonitoringConfig() {
        OperationMonitoringTaskConfig taskConfig = new OperationMonitoringTaskConfig();
        taskConfig.setEnabled(true);
        taskConfig.setFrequency(3000);
        List<MonitoringOperation> monitoringOperations = new ArrayList<>();
        MonitoringOperation monitoringOperation = new MonitoringOperation();
        monitoringOperation.setTaskName(operationCode);
        monitoringOperation.setRecurrentTimes(2);
        monitoringOperations.add(monitoringOperation);
        taskConfig.setMonitoringOperation(monitoringOperations);
        return taskConfig;
    }

    @Override
    public void init() throws DeviceManagementException {

    }

    @Override
    public DeviceManager getDeviceManager() {
        return new TestDeviceManager();
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return null;
    }

    @Override
    public ProvisioningConfig getProvisioningConfig() {
        return new ProvisioningConfig(tenantDomain, false);
    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        return null;
    }

    @Override
    public PolicyMonitoringManager getPolicyMonitoringManager() {
        return null;
    }

    @Override
    public InitialOperationConfig getInitialOperationConfig() {
        return null;
    }

    @Override
    public StartupOperationConfig getStartupOperationConfig() {
        return null;
    }

    @Override
    public PullNotificationSubscriber getPullNotificationSubscriber() {
        return null;
    }

    @Override
    public DeviceStatusTaskPluginConfig getDeviceStatusTaskPluginConfig() {
        return null;
    }

    @Override
    public GeneralConfig getGeneralConfig() {
        return null;
    }

    @Override
    public DeviceTypePlatformDetails getDeviceTypePlatformDetails() {
        return null;
    }

    @Override
    public DeviceEnrollmentInvitationDetails getDeviceEnrollmentInvitationDetails() {
        return null;
    }

    @Override public License getLicenseConfig() { return null; }

    @Override
    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        return null;
    }
}
