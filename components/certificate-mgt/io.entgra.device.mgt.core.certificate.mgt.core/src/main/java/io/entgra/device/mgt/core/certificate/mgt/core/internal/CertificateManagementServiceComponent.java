/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.certificate.mgt.core.internal;

import io.entgra.device.mgt.core.certificate.mgt.core.config.CertificateConfigurationManager;
import io.entgra.device.mgt.core.certificate.mgt.core.config.CertificateManagementConfig;
import io.entgra.device.mgt.core.certificate.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.CertificateManagementException;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManagerImpl;
import io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementService;
import io.entgra.device.mgt.core.certificate.mgt.core.service.CertificateManagementServiceImpl;
import io.entgra.device.mgt.core.certificate.mgt.core.util.CertificateManagementConstants;
import io.entgra.device.mgt.core.certificate.mgt.core.util.CertificateMgtSchemaInitializer;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.core.certificate.mgt.core.internal.CertificateManagementServiceComponent",
        immediate = true)
public class CertificateManagementServiceComponent {

    private static Log log = LogFactory.getLog(CertificateManagementServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing certificate management core bundle");
            }
            CertificateConfigurationManager.getInstance().initConfig();
            CertificateManagementConfig config = CertificateConfigurationManager.getInstance().getCertificateManagementConfig();
            DataSourceConfig dsConfig = config.getCertificateManagementRepository().getDataSourceConfig();
            CertificateManagementDAOFactory.init(dsConfig);

            BundleContext bundleContext = componentContext.getBundleContext();

            /* If -Dsetup option enabled then create Certificate management database schema */
            String setupOption =
                    System.getProperty(CertificateManagementConstants.SETUP_PROPERTY);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Certificate management repository schema initialization is about to " +
                              "begin");
                }
                this.setupDeviceManagementSchema(dsConfig);
            }
            bundleContext.registerService(CertificateManagementService.class.getName(),
                    CertificateManagementServiceImpl.getInstance(), null);

            bundleContext.registerService(SCEPManager.class.getName(),
                    new SCEPManagerImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("Certificate management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing certificate management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService")
    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagerService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service");
        }
        CertificateManagementDataHolder.getInstance().setDeviceManagementService(deviceManagerService);
    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Device Management Service");
        }
        CertificateManagementDataHolder.getInstance().setDeviceManagementService(null);
    }

    private void setupDeviceManagementSchema(DataSourceConfig config) throws CertificateManagementException {
        CertificateMgtSchemaInitializer initializer = new CertificateMgtSchemaInitializer(config);
        String checkSql = "select * from DM_DEVICE_CERTIFICATE";
        try {
            if (!initializer.isDatabaseStructureCreated(checkSql)) {
                log.info("Initializing Certificate management repository database schema");
                initializer.createRegistryDatabase();
            } else {
                log.info("Certificate management repository database already exists. Not creating a new database.");
            }
        } catch (Exception e) {
            throw new CertificateManagementException(
                    "Error occurred while initializing Certificate Management database schema", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Certificate management metadata repository schema has been successfully initialized");
        }
    }
}
