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

package io.entgra.device.mgt.core.operation.template;

import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.core.operation.template.mock.BaseOperationTemplatePluginTest;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DAOTest extends BaseOperationTemplatePluginTest {

    private static final Log log = LogFactory.getLog(DAOTest.class);

    private OperationTemplateDAO operationTemplateDAO;

    @BeforeClass
    public void init() {
        operationTemplateDAO = OperationTemplateDAOFactory.getOperationTemplateDAO();
        log.info("DAO test initialized");
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testGetOperationTemplate()
            throws DBConnectionException, OperationTemplateManagementDAOException {

        ConnectionManagerUtils.openDBConnection();
        OperationTemplate operationTemplateActual = operationTemplateDAO.getOperationTemplate(
                 TestUtils.deviceType, "4", TestUtils.operationCode);
        ConnectionManagerUtils.closeDBConnection();
        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), "4");
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test
    public void testAddOperationTemplate()
            throws DBConnectionException, OperationTemplateManagementDAOException {

        OperationTemplate operationTemplate = new OperationTemplate();
        operationTemplate.setSubTypeId("4");
        operationTemplate.setCode(TestUtils.operationCode);
        operationTemplate.setDeviceType(TestUtils.deviceType);
        operationTemplate.setOperationDefinition(
                TestUtils.getOperationDefinition("4", TestUtils.operationCode));

        ConnectionManagerUtils.beginDBTransaction();
        operationTemplateDAO.addOperationTemplate(operationTemplate);
        ConnectionManagerUtils.commitDBTransaction();

        OperationTemplate operationTemplateActual = operationTemplateDAO.getOperationTemplate(
                TestUtils.deviceType, "4", TestUtils.operationCode);
        ConnectionManagerUtils.closeDBConnection();
        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), "4");
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testUpdateOperationTemplate()
            throws DBConnectionException, OperationTemplateManagementDAOException {

        ConnectionManagerUtils.beginDBTransaction();
        OperationTemplate operationTemplate = operationTemplateDAO.getOperationTemplate(
                TestUtils.deviceType, "4", TestUtils.operationCode);
        OperationTemplate operationTemplateActual = operationTemplateDAO.updateOperationTemplate(
                operationTemplate);
        ConnectionManagerUtils.commitDBTransaction();
        ConnectionManagerUtils.closeDBConnection();

        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), "4");
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

}
