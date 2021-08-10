/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package io.entgra.application.mgt.core.dao;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.entgra.application.mgt.core.BaseTestCase;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.application.mgt.core.dto.ApplicationsDTO;
import io.entgra.application.mgt.core.dto.DeviceTypeCreator;
import io.entgra.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;

public class ApplicationManagementDAOTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementDAOTest.class);

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing ApplicationManagementDAOTest tests");
        ConfigurationManager configurationManager =  ConfigurationManager.getInstance();
//        super.initializeServices();
    }

    @Test
    public void testAddApplication() throws Exception {
        ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        ConnectionManagerUtil.beginDBTransaction();
        applicationDAO.createApplication(ApplicationsDTO.getApp1(), -1234);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test
    public void addDeviceType() throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            deviceTypeDAO.addDeviceType(DeviceTypeCreator.getDeviceType(), -1234, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            log.error("Error occurred while adding dummy device type", e);
            Assert.fail();
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating a transaction to add dummy device type", e);
            Assert.fail();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
