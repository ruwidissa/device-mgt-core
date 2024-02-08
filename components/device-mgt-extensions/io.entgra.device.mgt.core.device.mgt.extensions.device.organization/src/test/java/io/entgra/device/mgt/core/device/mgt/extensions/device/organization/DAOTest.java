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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAO;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.AdditionResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNodeResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DBConnectionException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtDAOException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.mock.BaseDeviceOrganizationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

public class DAOTest extends BaseDeviceOrganizationTest {

    private static final Log log = LogFactory.getLog(DAOTest.class);

    private DeviceOrganizationDAO deviceOrganizationDAO;

    @BeforeClass
    public void init() {
        deviceOrganizationDAO = DeviceOrganizationDAOFactory.getDeviceOrganizationDAO();
        log.info("DAO test initialized");
    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testGetChildrenOfDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        ConnectionManagerUtil.openDBConnection();
        int deviceId = 2;
        int maxDepth = 4;
        boolean includeDevice = true;
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceNodeResult childrenList = deviceOrganizationDAO.getChildrenOfDeviceNode(deviceId, maxDepth, includeDevice, tenantID);
        ConnectionManagerUtil.closeDBConnection();
        Assert.assertNotNull(childrenList, "Cannot be null");
    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testGetParentsOfDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        ConnectionManagerUtil.openDBConnection();
        int deviceID = 4;
        int maxDepth = 4;
        boolean includeDevice = false;
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceNodeResult parentList = deviceOrganizationDAO.getParentsOfDeviceNode(deviceID, maxDepth, includeDevice, tenantID);
        ConnectionManagerUtil.closeDBConnection();
        Assert.assertNotNull(parentList, "Cannot be null");
    }

    @Test
    public void testAddDeviceOrganizationDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.beginDBTransaction();
        deviceOrganizationDAO.deleteDeviceAssociations(1, tenantID);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
        ConnectionManagerUtil.beginDBTransaction();
        deviceOrganizationDAO.deleteDeviceAssociations(2, tenantID);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
        DeviceOrganization deviceOrganization = new DeviceOrganization();
        deviceOrganization.setDeviceId(2);
        deviceOrganization.setParentDeviceId(null);
        deviceOrganization.setUpdateTime(new Date(System.currentTimeMillis()));
        ConnectionManagerUtil.beginDBTransaction();
        AdditionResult result = deviceOrganizationDAO.addDeviceOrganization(deviceOrganization);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
        DeviceOrganization deviceOrganization1 = new DeviceOrganization();
        deviceOrganization1.setDeviceId(4);
        deviceOrganization1.setParentDeviceId(1);
        deviceOrganization1.setUpdateTime(new Date(System.currentTimeMillis()));
        ConnectionManagerUtil.beginDBTransaction();
        AdditionResult result1 = deviceOrganizationDAO.addDeviceOrganization(deviceOrganization1);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();

        DeviceOrganization deviceOrganization2 = new DeviceOrganization();
        deviceOrganization1.setDeviceId(3);
        deviceOrganization1.setParentDeviceId(1);
        deviceOrganization1.setUpdateTime(new Date(System.currentTimeMillis()));
        ConnectionManagerUtil.beginDBTransaction();
        AdditionResult result2 = deviceOrganizationDAO.addDeviceOrganization(deviceOrganization1);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();

    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testGetDeviceOrganizationByIDDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.beginDBTransaction();
        DeviceOrganization deviceOrganization = deviceOrganizationDAO.getDeviceOrganizationByID(1, tenantID);
        ConnectionManagerUtil.closeDBConnection();
        if (deviceOrganization != null) {
            log.info("Device Organization device ID : " + deviceOrganization.getDeviceId() +
                    " ,Device Organization Parent Device ID : " + deviceOrganization.getParentDeviceId());
        }
    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testDoesDeviceIdExistDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.beginDBTransaction();
        boolean isDeviceIdExist = deviceOrganizationDAO.isDeviceIdExist(1, tenantID);
        ConnectionManagerUtil.closeDBConnection();

    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testDeleteDeviceOrganizationByIDDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.beginDBTransaction();
        boolean result = deviceOrganizationDAO.deleteDeviceOrganizationByID(1, tenantID);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void deleteDeviceOrganizationsByDeviceIdDAO() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.beginDBTransaction();
        boolean result = deviceOrganizationDAO.deleteDeviceAssociations(1, tenantID);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test(dependsOnMethods = "testAddDeviceOrganizationDAO")
    public void testGetAllOrganizations() throws DBConnectionException, DeviceOrganizationMgtDAOException {
        ConnectionManagerUtil.beginDBTransaction();
        List<DeviceOrganization> organizations = deviceOrganizationDAO.getAllDeviceOrganizations();
        for (DeviceOrganization organization : organizations) {
            log.info("organizationID = " + organization.getOrganizationId());
            log.info("deviceID = " + organization.getDeviceId());
            log.info("parentDeviceID = " + organization.getParentDeviceId());
            log.info("updateTime = " + organization.getUpdateTime());
            log.info("----------------------------------------------");
        }
        ConnectionManagerUtil.closeDBConnection();
    }

}
