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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template;

import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.mockito.Mockito;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.MetadataManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.device.mgt.extensions.common.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;
import io.entgra.device.mgt.core.device.mgt.extensions.license.mgt.meta.data.MetaRepositoryBasedLicenseManager;
import io.entgra.device.mgt.core.device.mgt.extensions.mock.TypeXDeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.FileUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;

/**
 * This class handles all the setup that need to be done before starting to run the test cases.
 */
public class BaseExtensionsTest {

    protected static final String DATASOURCE_EXT = ".xml";
    private DataSource dataSource;
    private static String datasourceLocation;

    @BeforeSuite
    @Parameters({"datasource"})
    public void init(
            @Optional("src/test/resources/carbon-home/repository/conf/datasource/data-source-config") String datasource)
            throws Exception {

        datasourceLocation = datasource;
        this.initDataSource();
        this.initSQLScript();

        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "license.rxt");
        String rxt = null;
        File carbonHome;
        if (resourceUrl != null) {
            rxt = FileUtil.readFileToString(resourceUrl.getFile());
        }
        resourceUrl = classLoader.getResource("carbon-home");

        if (resourceUrl != null) {
            carbonHome = new File(resourceUrl.getFile());
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        RegistryService registryService = Utils.getRegistryService();
        OSGiDataHolder.getInstance().setRegistryService(registryService);
        UserRegistry systemRegistry =
                registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);

        GovernanceArtifactConfiguration configuration =  getGovernanceArtifactConfiguration(rxt);
        List<GovernanceArtifactConfiguration> configurations = new ArrayList<>();
        configurations.add(configuration);
        GovernanceUtils.loadGovernanceArtifacts(systemRegistry, configurations);
        Registry governanceSystemRegistry = registryService.getConfigSystemRegistry();
        DeviceTypeExtensionDataHolder.getInstance().setRegistryService(registryService);

        DeviceManagementProviderService deviceManagementProviderService = new DeviceManagementProviderServiceImpl();
        deviceManagementProviderService.registerDeviceType(new TypeXDeviceManagementService("defectiveDeviceType"));
        deviceManagementProviderService.registerDeviceType(new TypeXDeviceManagementService("arduino"));
        deviceManagementProviderService.registerDeviceType(new TypeXDeviceManagementService("androidsense"));
        deviceManagementProviderService.registerDeviceType(new TypeXDeviceManagementService("sample"));
        deviceManagementProviderService.registerDeviceType(new TypeXDeviceManagementService("wrong"));

        DeviceTypeExtensionDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
        DeviceTypeExtensionDataHolder.getInstance().setMetadataManagementService(new MetadataManagementServiceImpl());

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.SYSTEM_CONFIGURATION, governanceSystemRegistry);
    }

    protected void initDataSource() throws Exception {
        this.dataSource = this.getDataSource(this.
                readDataSourceConfig(datasourceLocation + DATASOURCE_EXT));
        DeviceManagementDAOFactory.init(dataSource);
        MetadataManagementDAOFactory.init(dataSource);
        OperationManagementDAOFactory.init(dataSource);
    }

    protected DataSourceConfig readDataSourceConfig(String configLocation) throws DeviceManagementException {
        try {
            File file = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementException("Error occurred while reading data source configuration", e);
        }
    }

    protected DataSource getDataSource(DataSourceConfig config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    private void initSQLScript() throws Exception {
        try (Connection conn = this.getDataSource().getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql-files/h2.sql'");
                stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql-files/android_h2.sql'");
            }
        }
    }
}
