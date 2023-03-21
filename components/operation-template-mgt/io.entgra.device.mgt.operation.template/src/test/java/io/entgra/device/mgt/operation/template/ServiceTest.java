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

import com.google.common.cache.CacheLoader;
import io.entgra.device.mgt.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.operation.template.impl.OperationTemplateServiceImpl;
import io.entgra.device.mgt.operation.template.mock.BaseOperationTemplatePluginTest;
import io.entgra.device.mgt.operation.template.spi.OperationTemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

        OperationTemplate operationTemplateActual = operationTemplateService.getOperationTemplate(TestUtils.subtypeId, TestUtils.deviceType, TestUtils.operationCode);
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

        OperationTemplate operationTemplateActual = operationTemplateService.getOperationTemplate(TestUtils.subtypeId, TestUtils.deviceType, TestUtils.operationCode);
        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getOperationDefinition(), TestUtils.getOperationDefinition(TestUtils.subtypeId, TestUtils.operationCode));
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), operationTemplateActual.getSubTypeId());
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test(dependsOnMethods = "testAddOperationTemplate")
    public void testUpdateOperationTemplate() throws OperationTemplateMgtPluginException {

        OperationTemplate operationTemplate = operationTemplateService.getOperationTemplate(TestUtils.subtypeId, TestUtils.deviceType, TestUtils.operationCode);
        operationTemplate.setOperationDefinition("{}");
        OperationTemplate operationTemplateActual = operationTemplateService.updateOperationTemplate(operationTemplate);

        Assert.assertNotNull(operationTemplateActual, "Cannot be null");
        Assert.assertEquals(operationTemplateActual.getOperationDefinition(), "{}");
        Assert.assertEquals(operationTemplateActual.getSubTypeId(), operationTemplateActual.getSubTypeId());
        Assert.assertEquals(operationTemplateActual.getCode(), TestUtils.operationCode);
        Assert.assertEquals(operationTemplateActual.getDeviceType(), TestUtils.deviceType);
    }

    @Test(dependsOnMethods = {"testAddOperationTemplate", "testGetOperationTemplate", "testUpdateOperationTemplate"},
            expectedExceptions = {CacheLoader.InvalidCacheLoadException.class})
    public void testDeleteOperationTemplate() throws OperationTemplateMgtPluginException {
        operationTemplateService.deleteOperationTemplate(TestUtils.subtypeId, TestUtils.deviceType, TestUtils.operationCode);
        getOperationTemplateBySubtypeIdAndDeviceTypeAndOperationCode(TestUtils.subtypeId, TestUtils.deviceType, TestUtils.operationCode);
    }

    public OperationTemplate getOperationTemplateBySubtypeIdAndDeviceTypeAndOperationCode(int subtypeId, String deviceType, String operationCode) throws OperationTemplateMgtPluginException {
        return operationTemplateService.getOperationTemplate(subtypeId, deviceType, operationCode);
    }
}
