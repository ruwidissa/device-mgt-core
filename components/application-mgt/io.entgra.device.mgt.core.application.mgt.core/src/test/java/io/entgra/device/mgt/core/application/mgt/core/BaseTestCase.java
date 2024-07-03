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
package io.entgra.device.mgt.core.application.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.application.mgt.common.config.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.core.common.DataSourceConfig;
import io.entgra.device.mgt.core.application.mgt.core.config.ConfigurationManager;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.application.mgt.core.util.ApplicationManagementUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public abstract class BaseTestCase {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(BaseTestCase.class);


    @BeforeSuite
    public void setupDataSource() throws Exception {
        log.debug("Setting up the databases and initializing the privilege carbon context....................");
        this.initDatSource();
        this.initSQLScript();
        this.initiatePrivilegedCaronContext();
        DeviceConfigurationManager.getInstance().initConfig();

        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());

        LifecycleStateManager lifecycleStateManager = ApplicationManagementUtil.getLifecycleStateMangerInstance();
        DataHolder.getInstance().setLifecycleStateManger(lifecycleStateManager);

        List<LifecycleState> lifecycleStates = ConfigurationManager.getInstance().
                getConfiguration().getLifecycleStates();
        lifecycleStateManager.init(lifecycleStates);
    }


    protected void initializeServices() throws Exception {
        log.debug("Calling initializing Services.....................");
        initDatSource();
        initSQLScript();

    }


    public void initDatSource() throws Exception {
        this.dataSource = this.getDataSource(this.readDataSourceConfig());
        ApplicationManagementDAOFactory.init(dataSource);
        ConnectionManagerUtil.init(dataSource);
        DeviceManagementDAOFactory.init(dataSource);
        MetadataManagementDAOFactory.init(dataSource);
        OperationManagementDAOFactory.init(dataSource);
//        PolicyManagementDAOFactory.init(dataSource);
//        OperationManagementDAOFactory.init(dataSource);
//        GroupManagementDAOFactory.init(dataSource);
    }

    private DataSource getDataSource(DataSourceConfig config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    private DataSourceConfig readDataSourceConfig() throws ApplicationManagementException {
        try {
            File file = new File("src/test/resources/datasource/data-source-config.xml");
            Document doc = this.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new ApplicationManagementException("Error occurred while reading data source configuration", e);
        }
    }

    protected void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM '../../../features/application-mgt/io.entgra.device.mgt.core.application.mgt.server.feature/src/main/resources/dbscripts/cdm/application-mgt/h2.sql'");
            stmt.executeUpdate("RUNSCRIPT FROM '../../../features/device-mgt/io.entgra.device.mgt.core.device.mgt.basics.feature/src/main/resources/dbscripts/cdm/h2.sql'");

        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    public static Document convertToDocument(File file) throws ApplicationManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new ApplicationManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }


    public void initiatePrivilegedCaronContext() throws Exception {


        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("io.entgra.device.mgt.core.application.mgt.core/src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");


    }

    public DataSource getDataSource() {
        return dataSource;
    }

    protected RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                "carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }
}
