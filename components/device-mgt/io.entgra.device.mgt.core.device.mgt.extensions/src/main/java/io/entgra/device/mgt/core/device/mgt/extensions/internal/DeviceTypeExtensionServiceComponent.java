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

import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceTypeGeneratorService;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.DeviceTypeGeneratorServiceImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.DeviceTypePluginExtensionServiceImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.spi.DeviceTypePluginExtensionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.extensions.internal.DeviceTypeExtensionServiceComponent",
        immediate = true)
public class DeviceTypeExtensionServiceComponent {

    private static final Log log = LogFactory.getLog(DeviceTypeExtensionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Activating Device Type Extension Service Component");
            }
            ctx.getBundleContext()
                    .registerService(DeviceTypeGeneratorService.class, new DeviceTypeGeneratorServiceImpl(), null);
            ctx.getBundleContext().registerService(DeviceTypePluginExtensionService.class,
                                                   new DeviceTypePluginExtensionServiceImpl(), null);
            if (log.isDebugEnabled()) {
                log.debug("Device Type Extension Service Component successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device type extension component ", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating DeviceType Deployer Service Component");
        }
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService acquired");
        }
        DeviceTypeExtensionDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        DeviceTypeExtensionDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid  device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to android mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }
}
