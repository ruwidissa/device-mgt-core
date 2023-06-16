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
package io.entgra.device.mgt.core.device.mgt.extensions.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This holds the necessary services required for the bundle.
 */
public class DeviceTypeExtensionDataHolder {

    private RegistryService registryService;
    private MetadataManagementService metadataManagementService;
    private DeviceManagementProviderService deviceManagementProviderService;

    private static DeviceTypeExtensionDataHolder thisInstance = new DeviceTypeExtensionDataHolder();

    private DeviceTypeExtensionDataHolder() {}

    public static DeviceTypeExtensionDataHolder getInstance() {
        return thisInstance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public MetadataManagementService getMetadataManagementService() {
        if (metadataManagementService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            metadataManagementService = (MetadataManagementService)
                    ctx.getOSGiService(MetadataManagementService.class, null);
        }
        return metadataManagementService;
    }

    public void setMetadataManagementService(MetadataManagementService metadataManagementService) {
        this.metadataManagementService = metadataManagementService;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        if (deviceManagementProviderService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            deviceManagementProviderService = (DeviceManagementProviderService)
                    ctx.getOSGiService(DeviceManagementProviderService.class, null);
        }
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }
}
