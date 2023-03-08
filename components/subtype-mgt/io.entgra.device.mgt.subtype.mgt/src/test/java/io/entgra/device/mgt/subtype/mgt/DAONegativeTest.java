/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.device.mgt.subtype.mgt;

import io.entgra.device.mgt.subtype.mgt.dao.DeviceSubTypeDAO;
import io.entgra.device.mgt.subtype.mgt.dao.DeviceSubTypeDAOFactory;
import io.entgra.device.mgt.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.subtype.mgt.exception.SubTypeMgtDAOException;
import io.entgra.device.mgt.subtype.mgt.mock.BaseDeviceSubTypePluginTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class DAONegativeTest extends BaseDeviceSubTypePluginTest {
    private static final Log log = LogFactory.getLog(DAONegativeTest.class);

    private DeviceSubTypeDAO deviceSubTypeDAO;

    @BeforeClass
    public void init() {
        deviceSubTypeDAO = DeviceSubTypeDAOFactory.getDeviceSubTypeDAO();
        log.info("DAO Negative test initialized");
    }

    @Test(description = "This method tests the add device subtype method under negative circumstances with null data",
            expectedExceptions = {NullPointerException.class}
    )
    public void testAddDeviceSubType() throws SubTypeMgtDAOException {
        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType setDeviceSubType(T objType, String typeDef) {
                return null;
            }

            @Override
            public String parseSubTypeToJson(Object objType) {
                return null;
            }
        };
        try {
            ConnectionManagerUtil.beginDBTransaction();
            deviceSubTypeDAO.addDeviceSubType(deviceSubType);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (SubTypeMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while processing SQL to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Test(description = "This method tests the add device subtype method under negative circumstances while missing " +
            "required fields",
            expectedExceptions = {SubTypeMgtDAOException.class},
            expectedExceptionsMessageRegExp = "Error occurred while processing SQL to insert device subtype"
    )
    public void testAddDeviceSubTypes() throws SubTypeMgtDAOException {
        int subTypeId = 1;
        String subTypeName = "TestSubType";
        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType setDeviceSubType(T objType, String typeDef) {
                return null;
            }

            @Override
            public String parseSubTypeToJson(Object objType) {
                return null;
            }
        };
        deviceSubType.setSubTypeId(subTypeId);
        deviceSubType.setSubTypeName(subTypeName);
        deviceSubType.setDeviceType(DeviceSubType.DeviceType.COM);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            deviceSubTypeDAO.addDeviceSubType(deviceSubType);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (SubTypeMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while processing SQL to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Test(description = "This method tests the add device subtype method under negative circumstances while passing " +
            "same subtype id & device type",
            expectedExceptions = {SubTypeMgtDAOException.class},
            expectedExceptionsMessageRegExp = "Error occurred while processing SQL to insert device subtype"
    )
    public void testAddDeviceSubtypes() throws SubTypeMgtDAOException {
        int subTypeId = 1;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String subTypeName = "TestSubType";
        String typeDefinition = TestUtils.createNewDeviceSubType(subTypeId);
        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType setDeviceSubType(T objType, String typeDef) {
                return null;
            }

            @Override
            public String parseSubTypeToJson(Object objType) {
                return null;
            }
        };
        deviceSubType.setSubTypeName(subTypeName);
        deviceSubType.setDeviceType(DeviceSubType.DeviceType.COM);
        deviceSubType.setTenantId(tenantId);
        deviceSubType.setTypeDefinition(typeDefinition);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            deviceSubTypeDAO.addDeviceSubType(deviceSubType);
            deviceSubTypeDAO.addDeviceSubType(deviceSubType);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (SubTypeMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while processing SQL to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert device subtype";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

}
