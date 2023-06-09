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

package io.entgra.device.mgt.core.operation.template.internal;

import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.core.operation.template.dao.impl.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.operation.template.dao.impl.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.operation.template.dao.impl.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.operation.template.impl.OperationTemplateServiceImpl;
import io.entgra.device.mgt.core.operation.template.spi.OperationTemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.ndatasource.core.DataSourceService;

@Component(
        name = "io.entgra.device.mgt.core.operation.template.internal.OperationTemplateMgtServiceComponent",
        immediate = true)
public class OperationTemplateMgtServiceComponent {

    private static final Log log = LogFactory.getLog(OperationTemplateMgtServiceComponent.class);

    /**
     *
     * @param componentContext
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Activating Operation Template Management Service Component");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            DeviceConfigurationManager.getInstance().initConfig();
            DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            DataSourceConfig dsConfig = config.getDeviceManagementRepository().getDataSourceConfig();
            OperationTemplateDAOFactory.init(dsConfig);

            OperationTemplateService operationTemplateService = new OperationTemplateServiceImpl();
            OperationTemplateMgtDataHolder.getInstance().setOperationTemplateService(operationTemplateService);
            bundleContext.registerService(OperationTemplateService.class, operationTemplateService, null);

            if (log.isDebugEnabled()) {
                log.debug("Operation Template Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Operation Template Management Service Component", e);
        }
    }

    /**
     *
     * @param componentContext
     */
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Operation Template Management Service Component");
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Operation Template Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Operation Template Management bundle", e);
        }
    }

    /**
     *
     * @param dataSourceService
     */
    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to Operation Template Mgt component");
        }
    }

    /**
     *
     * @param dataSourceService
     */
    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
        if (log.isDebugEnabled()) {
            log.debug("Removing Data Source service from Operation Template Mgt component");
        }
    }

}
