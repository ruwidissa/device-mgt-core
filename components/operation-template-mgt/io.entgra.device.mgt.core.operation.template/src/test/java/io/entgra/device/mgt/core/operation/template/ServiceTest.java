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

import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.core.operation.template.impl.OperationTemplateServiceImpl;
import io.entgra.device.mgt.core.operation.template.mock.BaseOperationTemplatePluginTest;
import io.entgra.device.mgt.core.operation.template.spi.OperationTemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

public class ServiceTest extends BaseOperationTemplatePluginTest {

    private static final Log log = LogFactory.getLog(ServiceTest.class);
    private OperationTemplateService operationTemplateService;

    @BeforeClass
    public void init() {
        operationTemplateService = new OperationTemplateServiceImpl();
        log.info("Service test initialized");
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testGetOperationTemplate() throws OperationTemplateMgtPluginException {

        OperationTemplate operationTemplateActual = operationTemplateService.getOperationTemplate(
                TestUtils.deviceType, TestUtils.subtypeId, TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), operationTemplateActual.getSubTypeId());
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test
    public void testAddOperationTemplate() throws OperationTemplateMgtPluginException {

        OperationTemplate operationTemplate = new OperationTemplate();
        operationTemplate.setSubTypeId(TestUtils.subtypeId);
        operationTemplate.setCode(TestUtils.operationCode);
        operationTemplate.setDeviceType(TestUtils.deviceType);
        operationTemplate.setOperationDefinition(TestUtils.getOperationDefinition(TestUtils.subtypeId, TestUtils.operationCode));
        operationTemplateService.addOperationTemplate(operationTemplate);

        OperationTemplate operationTemplateActual = operationTemplateService.getOperationTemplate(TestUtils.deviceType, TestUtils.subtypeId, TestUtils.operationCode);
        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getOperationDefinition(), TestUtils.getOperationDefinition(TestUtils.subtypeId, TestUtils.operationCode));
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), operationTemplateActual.getSubTypeId());
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testUpdateOperationTemplate() throws OperationTemplateMgtPluginException {

        OperationTemplate operationTemplate = operationTemplateService.getOperationTemplate(TestUtils.deviceType, TestUtils.subtypeId, TestUtils.operationCode);
        operationTemplate.setOperationDefinition("{}");
        OperationTemplate operationTemplateActual = operationTemplateService.updateOperationTemplate(operationTemplate);

        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getOperationDefinition(), "{}");
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), operationTemplateActual.getSubTypeId());
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }


    public OperationTemplate getOperationTemplateBySubtypeIdAndDeviceTypeAndOperationCode(String deviceType, String subtypeId, String operationCode) throws OperationTemplateMgtPluginException {
        return operationTemplateService.getOperationTemplate(deviceType, subtypeId, operationCode);
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testGetOperationTemplateCodesByDeviceTypeAndSubTypeId() throws OperationTemplateMgtPluginException {

        Set<String> operationCodes = operationTemplateService.getOperationTemplateCodes(TestUtils.deviceType, TestUtils.subtypeId);
        Assert.assertNotNull(operationCodes, "Cannot be null");
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testGetAllOperationTemplatesByDeviceType() throws OperationTemplateMgtPluginException {

        List<OperationTemplate> operationTemplates = operationTemplateService.getAllOperationTemplates(TestUtils.deviceType);
        Assert.assertNotNull(operationTemplates, "Cannot be null");
        Assert.assertFalse(operationTemplates.isEmpty(), "operationTemplates is empty");
    }

    @Test(dependsOnMethods = {"testAddOperationTemplate", "testGetOperationTemplate", "testUpdateOperationTemplate",
            "testGetOperationTemplateCodesByDeviceTypeAndSubTypeId", "testGetAllOperationTemplatesByDeviceType"})
    public void testDeleteOperationTemplate() throws OperationTemplateMgtPluginException {
        operationTemplateService.deleteOperationTemplate(TestUtils.deviceType, TestUtils.subtypeId, TestUtils.operationCode);
        Assert.assertNull(getOperationTemplateBySubtypeIdAndDeviceTypeAndOperationCode(TestUtils.deviceType, TestUtils.subtypeId, TestUtils.operationCode));
    }
}
