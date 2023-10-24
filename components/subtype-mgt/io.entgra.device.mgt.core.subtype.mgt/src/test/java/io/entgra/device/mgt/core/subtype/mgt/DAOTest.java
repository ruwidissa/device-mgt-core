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

package io.entgra.device.mgt.core.subtype.mgt;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAO;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAOFactory;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;
import io.entgra.device.mgt.core.subtype.mgt.mock.BaseDeviceSubTypePluginTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;

public class DAOTest extends BaseDeviceSubTypePluginTest {
    private static final Log log = LogFactory.getLog(DAOTest.class);

    private DeviceSubTypeDAO deviceSubTypeDAO;

    @BeforeClass
    public void init() {
        deviceSubTypeDAO = DeviceSubTypeDAOFactory.getDeviceSubTypeDAO();
        log.info("DAO test initialized");
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceSubType() throws DBConnectionException, SubTypeMgtDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.openDBConnection();
        DeviceSubType subTypeActual = deviceSubTypeDAO.getDeviceSubType("1", tenantId,
                "COM");
        ConnectionManagerUtil.closeDBConnection();
        Assert.assertNotNull(subTypeActual, "Should not be null");
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetAllDeviceSubTypes() throws DBConnectionException, SubTypeMgtDAOException {
        String deviceType = "COM";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConnectionManagerUtil.openDBConnection();
        List<DeviceSubType> subTypesActual = deviceSubTypeDAO.getAllDeviceSubTypes(tenantId, deviceType);
        ConnectionManagerUtil.closeDBConnection();
        log.info(deviceType + " sub types count should be " + subTypesActual.size());
        Assert.assertNotNull(subTypesActual, "Should not be null");
    }

    @Test
    public void testAddDeviceSubType() throws DBConnectionException, SubTypeMgtDAOException {
        String subTypeId = "1";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String subTypeName = "TestSubType";
        String deviceType = "COM";
        String typeDefinition = TestUtils.createNewDeviceSubType(subTypeId);

        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType convertToDeviceSubType() {
                return null;
            }

            @Override
            public String parseSubTypeToJson() throws JsonProcessingException {
                return null;
            }
        };
        deviceSubType.setSubTypeId(subTypeId);
        deviceSubType.setSubTypeName(subTypeName);
        deviceSubType.setDeviceType(deviceType);
        deviceSubType.setTenantId(tenantId);
        deviceSubType.setTypeDefinition(typeDefinition);

        ConnectionManagerUtil.beginDBTransaction();
        deviceSubTypeDAO.addDeviceSubType(deviceSubType);
        ConnectionManagerUtil.commitDBTransaction();
        DeviceSubType subTypeActual = deviceSubTypeDAO.getDeviceSubType(subTypeId, tenantId, deviceType);
        ConnectionManagerUtil.closeDBConnection();
        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyDeviceSubTypeDAO(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testUpdateDeviceSubType() throws DBConnectionException, SubTypeMgtDAOException {
        String subTypeId = "1";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deviceType = "COM";
        String subTypeName = "TestSubType";
        String subTypeExpected = TestUtils.createUpdateDeviceSubType(subTypeId);

        ConnectionManagerUtil.beginDBTransaction();
        deviceSubTypeDAO.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName, subTypeExpected);
        ConnectionManagerUtil.commitDBTransaction();
        DeviceSubType subTypeActual = deviceSubTypeDAO.getDeviceSubType(subTypeId, tenantId, deviceType);
        ConnectionManagerUtil.closeDBConnection();

        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyUpdatedDeviceSubTypeDAO(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceTypeByProvider() throws DBConnectionException, SubTypeMgtDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deviceType = "COM";
        String subTypeName = "TestSubType";
        ConnectionManagerUtil.openDBConnection();
        DeviceSubType subTypeActual = deviceSubTypeDAO.getDeviceSubTypeByProvider(subTypeName, tenantId, deviceType);
        ConnectionManagerUtil.closeDBConnection();
        Assert.assertNotNull(subTypeActual, "Should not be null");
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceTypeCount() throws DBConnectionException, SubTypeMgtDAOException {
        String deviceType = "COM";
        ConnectionManagerUtil.openDBConnection();
        int subTypeCount = deviceSubTypeDAO.getDeviceSubTypeCount(deviceType);
        ConnectionManagerUtil.closeDBConnection();
        log.info(deviceType + " Device subtypes count: " + subTypeCount);
        Assert.assertEquals(subTypeCount, 1);
    }

}
