/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.operation.template;

import io.entgra.device.mgt.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.operation.template.mock.BaseOperationTemplatePluginTest;
import io.entgra.device.mgt.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DAONegativeTest extends BaseOperationTemplatePluginTest {
    private static final Log log = LogFactory.getLog(DAONegativeTest.class);

    private OperationTemplateDAO operationTemplateDAO;

    @BeforeClass
    public void init() {
        operationTemplateDAO = OperationTemplateDAOFactory.getOperationTemplateDAO();
        log.info("DAO Negative test initialized");
    }

    @Test(description = "This method tests the add operation template under negative circumstances with null data",
            expectedExceptions = {OperationTemplateManagementDAOException.class}
    )
    public void testAddOperationTemplate() throws OperationTemplateManagementDAOException {

        try {
            ConnectionManagerUtils.beginDBTransaction();
            OperationTemplate operationTemplate = new OperationTemplate();
            operationTemplate.setSubTypeId(0);
            operationTemplate.setCode(null);
            operationTemplate.setOperationDefinition(TestUtils.getOperationDefinition(TestUtils.subtypeId, TestUtils.operationCode));
            operationTemplateDAO.addOperationTemplate(operationTemplate);
            ConnectionManagerUtils.commitDBTransaction();
        } catch (OperationTemplateManagementDAOException e) {
            ConnectionManagerUtils.rollbackDBTransaction();
            String msg = "Error occurred while processing SQL to insert Operation template";
            log.error(msg);
            throw new OperationTemplateManagementDAOException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert Operation Template";
            log.error(msg);
            throw new OperationTemplateManagementDAOException(msg, e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();
        }
    }

}
