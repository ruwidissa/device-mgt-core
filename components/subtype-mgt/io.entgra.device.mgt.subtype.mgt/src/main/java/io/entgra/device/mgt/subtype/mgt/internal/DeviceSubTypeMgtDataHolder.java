/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
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

package io.entgra.device.mgt.subtype.mgt.internal;

import io.entgra.device.mgt.subtype.mgt.spi.DeviceSubTypeService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class DeviceSubTypeMgtDataHolder {

    public static final DeviceSubTypeMgtDataHolder thisInstance = new DeviceSubTypeMgtDataHolder();

    private RegistryService registryService;
    private DeviceManagementProviderService deviceManagementProviderService;
    private DeviceSubTypeService deviceSubTypeService;

    private DeviceSubTypeMgtDataHolder() {
    }

    public static DeviceSubTypeMgtDataHolder getInstance() {
        return thisInstance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }

    public DeviceSubTypeService getDeviceSubTypeService() {
        return deviceSubTypeService;
    }

    public void setDeviceSubTypeService(DeviceSubTypeService deviceSubTypeService) {
        this.deviceSubTypeService = deviceSubTypeService;
    }
}
