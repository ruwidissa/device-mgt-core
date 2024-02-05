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

import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.impl.DeviceOrganizationServiceImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.spi.DeviceOrganizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="io.entgra.device.mgt.core.device.mgt.extensions.device.organization.internal.DeviceOrganizationMgtServiceComponent" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 */
public class DeviceOrganizationMgtServiceComponent {

    private static final Log log = LogFactory.getLog(DeviceOrganizationMgtServiceComponent.class);

    /**
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Activating Device Organization Management Service Component");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
            DeviceOrganizationDAOFactory.init(dsConfig);

            DeviceOrganizationService deviceOrganizationService = new DeviceOrganizationServiceImpl();
            DeviceOrganizationMgtDataHolder.getInstance().setDeviceOrganizationService(deviceOrganizationService);
            bundleContext.registerService(DeviceOrganizationService.class, deviceOrganizationService, null);

            if (log.isDebugEnabled()) {
                log.debug("Device Organization Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Device Organization Management Service Component", e);
        }
    }

    /**
     * @param componentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Device Organization Management Service Component");
        }
    }

    @SuppressWarnings("unused")
    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service to Device Organization Mgt SC");
        }
        DeviceOrganizationMgtDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    @SuppressWarnings("unused")
    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Device Management Service from Device Organization Mgt SC");
        }
        DeviceOrganizationMgtDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

    /**
     * @param dataSourceService
     */
    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to Device Organization Mgt component");
        }
    }

    /**
     * @param dataSourceService
     */
    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
        if (log.isDebugEnabled()) {
            log.debug("Removing Data Source service from Device Organization Mgt component");
        }
    }


    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set to Device Organization Mgt component");
        }
        DeviceOrganizationMgtDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing RegistryService from Device Organization Mgt component");
        }
        DeviceOrganizationMgtDataHolder.getInstance().setRegistryService(null);
    }


}
