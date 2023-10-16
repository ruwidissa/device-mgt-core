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
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtPluginException;
import io.entgra.device.mgt.core.subtype.mgt.impl.DeviceSubTypeServiceImpl;
import io.entgra.device.mgt.core.subtype.mgt.mock.BaseDeviceSubTypePluginTest;
import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;


public class ServiceTest extends BaseDeviceSubTypePluginTest {

    private static final Log log = LogFactory.getLog(ServiceTest.class);
    private DeviceSubTypeService deviceSubTypeService;

    @BeforeClass
    public void init() {
        deviceSubTypeService = new DeviceSubTypeServiceImpl();
        log.info("Service test initialized");
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceType() throws SubTypeMgtPluginException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType("1", tenantId,
                "METER");
        TestUtils.verifyDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetAllDeviceTypes() throws SubTypeMgtPluginException {
        String deviceType = "METER";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceSubType> subTypesActual = deviceSubTypeService.getAllDeviceSubTypes(tenantId, deviceType);
        log.info(deviceType + " sub types count should be " + subTypesActual.size());
        Assert.assertNotNull(subTypesActual, "Should not be null");
    }

    @Test
    public void testAddDeviceSubType() throws SubTypeMgtPluginException {
        String subTypeId = "1";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String subTypeName = "TestSubType";
        String deviceType = "METER";
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
        deviceSubTypeService.addDeviceSubType(deviceSubType);

        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType(subTypeId, tenantId, deviceType);
        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testUpdateDeviceSubType() throws SubTypeMgtPluginException {
        String subTypeId = "1";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deviceType = "METER";
        String subTypeName = "TestSubType";
        String subTypeExpected = TestUtils.createUpdateDeviceSubType(subTypeId);

        deviceSubTypeService.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName, subTypeExpected);

        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType(subTypeId, tenantId, deviceType);

        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyUpdatedDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceTypeByProvider() throws SubTypeMgtPluginException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deviceType = "METER";
        String subTypeName = "TestSubType";
        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubTypeByProvider(subTypeName, tenantId,
                deviceType);
        TestUtils.verifyDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceTypeCount() throws SubTypeMgtPluginException {
        String deviceType = "METER";
        int subTypeCount = deviceSubTypeService.getDeviceSubTypeCount(deviceType);
        log.info(deviceType + " Device subtypes count: " + subTypeCount);
    }
}
