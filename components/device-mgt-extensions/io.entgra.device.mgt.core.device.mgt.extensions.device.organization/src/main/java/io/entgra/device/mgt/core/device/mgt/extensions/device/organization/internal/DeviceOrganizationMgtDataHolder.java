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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.internal;

import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.spi.DeviceOrganizationService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * Device Organization data holder class
 */
public class DeviceOrganizationMgtDataHolder {

    public static final DeviceOrganizationMgtDataHolder thisInstance = new DeviceOrganizationMgtDataHolder();

    private RegistryService registryService;
    private DeviceOrganizationService deviceOrganizationService;
    private DeviceManagementProviderService deviceManagementProviderService;

    private DeviceOrganizationMgtDataHolder() {
    }

    public static DeviceOrganizationMgtDataHolder getInstance() {
        return thisInstance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }


    public DeviceOrganizationService getDeviceOrganizationService() {
        return deviceOrganizationService;
    }

    public void setDeviceOrganizationService(DeviceOrganizationService deviceOrganizationService) {
        this.deviceOrganizationService = deviceOrganizationService;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }
}
