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

import io.entgra.device.mgt.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.operation.template.impl.OperationTemplateServiceImpl;
import io.entgra.device.mgt.operation.template.spi.OperationTemplateService;
import io.entgra.device.mgt.operation.template.mock.BaseOperationTemplatePluginTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ServiceNegativeTest extends BaseOperationTemplatePluginTest {

    private static final Log log = LogFactory.getLog(ServiceNegativeTest.class);
    private OperationTemplateService operationTemplateService;

    @BeforeClass
    public void init() {
        operationTemplateService = new OperationTemplateServiceImpl();
        log.info("Service test initialized");
    }

    @Test(description = "This method tests Add operation template under negative circumstances with null data",
            expectedExceptions = {OperationTemplateMgtPluginException.class})
    public void testAddOperationTemplate() throws OperationTemplateMgtPluginException {
        OperationTemplate operationTemplate = new OperationTemplate();
        operationTemplateService.addOperationTemplate(operationTemplate);
    }

    @Test(description = "This method tests Add Operation template under negative circumstances while missing " +
            "required fields",
            expectedExceptions = {OperationTemplateMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Invalid meter device subtype id: 0")
    public void testAddOperationTemplates() throws OperationTemplateMgtPluginException {

        OperationTemplate operationTemplate = new OperationTemplate();
        operationTemplate.setDeviceType(TestUtils.deviceType);
        operationTemplate.setCode(TestUtils.operationCode);
        operationTemplate.setSubTypeId(0);
        operationTemplate.setOperationDefinition(TestUtils.getOperationDefinition(TestUtils.subtypeId, TestUtils.operationCode));
        operationTemplateService.addOperationTemplate(operationTemplate);
    }

}
