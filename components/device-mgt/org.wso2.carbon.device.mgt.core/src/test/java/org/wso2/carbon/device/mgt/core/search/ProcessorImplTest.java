/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.search;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.search.Condition;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.search.mgt.InvalidOperatorException;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.ProcessorImpl;
import org.wso2.carbon.device.mgt.core.search.util.ChangeEnumValues;
import org.wso2.carbon.device.mgt.core.search.util.Utils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds unit test cases for org.wso2.carbon.device.mgt.core.search.mgt.impl.ProcessorImpl
 */
public class ProcessorImplTest extends BaseDeviceManagementTest {

    private static List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private static final String DEVICE_ID_PREFIX = "SEARCH-DEVICE-ID-";
    private static final String DEVICE_TYPE = "SEARCH_TYPE";

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < 5; i++) {
            deviceIdentifiers.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        List<Device> devices = TestDataHolder.generateDummyDeviceData(deviceIdentifiers);
        for (Device device : devices) {
            device.setDeviceInfo(Utils.getDeviceInfo());
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE, true);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
    }

    @Test (description = "Search for device with and condition")
    public void testSearchDevicesWIthAndCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMEI");
        condition.setOperator("=");
        condition.setValue("e6f236ac82537a8e");
        condition.setState(Condition.State.AND);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(devices.size(), 5, "There should be exactly 5 devices with matching search criteria");
    }

    @Test (description = "Search for device with or condition")
    public void testSearchDevicesWIthORCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMSI");
        condition.setOperator("=");
        condition.setValue("432659632123654845");
        condition.setState(Condition.State.OR);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(devices.size(), 5, "There should be exactly 5 devices with matching search criteria");
    }

    @Test (description = "Search for device with wrong condition")
    public void testSearchDevicesWIthWrongCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMSI");
        condition.setOperator("=");
        condition.setValue("43265963212378466");
        condition.setState(Condition.State.OR);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(0, devices.size(), "There should be no devices with matching search criteria");
    }

    @Test(description = "Test for invalid state")
    public void testInvalidState() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();
        ChangeEnumValues.addEnum(Condition.State.class, "BLA");
        Condition.State state = Condition.State.valueOf("BLA");

        Condition cond = new Condition();
        cond.setKey("batteryLevel");
        cond.setOperator("=");
        cond.setValue("40");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        Condition cond2 = new Condition();
        cond2.setKey("LOCATION");
        cond2.setOperator("=");
        cond2.setValue("Karandeniya");
        cond2.setState(Condition.State.AND);
        conditions.add(cond2);

        Condition cond3 = new Condition();
        cond3.setKey("batteryLevel");
        cond3.setOperator("=");
        cond3.setValue("23.0");
        cond3.setState(state);
        conditions.add(cond3);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        try {
            processor.execute(context);
        } catch (SearchMgtException e) {
            if (!(e.getCause() instanceof InvalidOperatorException)) {
                throw e;
            }
        }
    }
}
