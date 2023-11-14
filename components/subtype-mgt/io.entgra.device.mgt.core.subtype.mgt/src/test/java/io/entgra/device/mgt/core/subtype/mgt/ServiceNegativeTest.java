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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ServiceNegativeTest extends BaseDeviceSubTypePluginTest {

    private static final Log log = LogFactory.getLog(ServiceNegativeTest.class);
    private DeviceSubTypeService deviceSubTypeService;

    @BeforeClass
    public void init() {
        deviceSubTypeService = new DeviceSubTypeServiceImpl();
        log.info("Service test initialized");
    }

    @Test(description = "This method tests Add Device Subtype method under negative circumstances with null data",
            expectedExceptions = {NullPointerException.class})
    public void testAddDeviceSubType() throws SubTypeMgtPluginException {
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
        deviceSubTypeService.addDeviceSubType(deviceSubType);
    }

    @Test(description = "This method tests Add Device Subtype method under negative circumstances while missing " +
            "required fields",
            expectedExceptions = {SubTypeMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Error occurred in the database level while adding device subtype for " +
                    "SIM subtype & subtype Id: 1")
    public void testAddDeviceSubTypes() throws SubTypeMgtPluginException {
        String subTypeId = "1";
        String subTypeName = "TestSubType";
        String deviceType = "SIM";

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
        deviceSubTypeService.addDeviceSubType(deviceSubType);
    }

    @Test(description = "This method tests Update Device Subtype method under negative circumstances with invalid " +
            "subtype Id",
            expectedExceptions = {SubTypeMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Cannot find device subtype for SIM subtype & subtype Id: 15")
    public void testUpdateDeviceSubTypes() throws SubTypeMgtPluginException {
        String subTypeId = "15";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String deviceType = "SIM";
        String subTypeName = "TestSubType";
        String subTypeExpected = TestUtils.createUpdateDeviceSubType(subTypeId);

        deviceSubTypeService.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName, subTypeExpected);
    }


}
