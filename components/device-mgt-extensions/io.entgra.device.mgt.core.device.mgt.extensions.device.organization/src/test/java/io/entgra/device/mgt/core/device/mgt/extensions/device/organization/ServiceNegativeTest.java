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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtPluginException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.impl.DeviceOrganizationServiceImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.mock.BaseDeviceOrganizationTest;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.spi.DeviceOrganizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ServiceNegativeTest extends BaseDeviceOrganizationTest {

    private static final Log log = LogFactory.getLog(ServiceNegativeTest.class);

    private DeviceOrganizationService deviceOrganizationService;

    @BeforeClass
    public void init() {
        deviceOrganizationService = new DeviceOrganizationServiceImpl();
        log.info("Service test initialized");
    }

    @Test(description = "This method tests Get Children Of method under negative circumstances with negative deviceId",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testGetChildrenOfWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int deviceId = -1;
        int maxDepth = -1;
        boolean includeDevice = true;
        deviceOrganizationService.getChildrenOfDeviceNode(deviceId, maxDepth, includeDevice);
    }

    @Test(description = "This method tests Get Children Of method under negative circumstances with an invalid deviceId",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testGetChildrenOfWithInvalidDeviceNode() throws DeviceOrganizationMgtPluginException {
        int deviceId = 0;
        int maxDepth = 2;
        boolean includeDevice = true;
        deviceOrganizationService.getChildrenOfDeviceNode(deviceId, maxDepth, includeDevice);
    }

    @Test(description = "This method tests Get Parents Of method under negative circumstances with invalid data",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testGetParentsOfWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int deviceID = 0;
        int maxDepth = -1;
        boolean includeDevice = true;
        deviceOrganizationService.getParentsOfDeviceNode(deviceID, maxDepth, includeDevice);
    }

    @Test(description = "This method tests Get Parents Of method under negative circumstances with an invalid ID"
            , expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testGetParentsOfWithInvalidDeviceNode() throws DeviceOrganizationMgtPluginException {
        int deviceID = -2;
        int maxDepth = 2;
        boolean includeDevice = true;
        deviceOrganizationService.getParentsOfDeviceNode(deviceID, maxDepth, includeDevice);
    }

    @Test(description = "This method tests Get Parents Of method under negative circumstances with an invalid DeviceNode"
            , expectedExceptions = {DeviceOrganizationMgtPluginException.class}
    )
    public void testGetParentsOfWithNullDeviceNode() throws DeviceOrganizationMgtPluginException {
        int deviceID = -1;
        int maxDepth = 2;
        boolean includeDevice = true;
        deviceOrganizationService.getParentsOfDeviceNode(deviceID, maxDepth, includeDevice);
    }


    @Test(description = "This method tests Add Device Organization method under negative circumstances with null data",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testAddDeviceOrganizationWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        DeviceOrganization invalidOrganization = new DeviceOrganization();
        deviceOrganizationService.addDeviceOrganization(invalidOrganization);
    }

    @Test(description = "This method tests Add Device Organization method under negative circumstances with invalid" +
            "parent ID",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testAddDeviceOrganizationWithInvalidParentID() throws DeviceOrganizationMgtPluginException {

        DeviceOrganization deviceOrganizationOne = new DeviceOrganization();
        deviceOrganizationOne.setDeviceId(3);
        deviceOrganizationOne.setParentDeviceId(30);
        deviceOrganizationOne.setDeviceOrganizationMeta("Physical Relationship");

        boolean result1 = deviceOrganizationService.addDeviceOrganization(deviceOrganizationOne);
    }

    @Test(description = "This method tests Add Device Organization method under negative circumstances with invalid" +
            "child ID",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testAddDeviceOrganizationWithInvalidChildID() throws DeviceOrganizationMgtPluginException {

        DeviceOrganization deviceOrganizationOne = new DeviceOrganization();
        deviceOrganizationOne.setDeviceId(30);
        deviceOrganizationOne.setParentDeviceId(3);
        deviceOrganizationOne.setDeviceOrganizationMeta("Physical Relationship");
        boolean result1 = deviceOrganizationService.addDeviceOrganization(deviceOrganizationOne);

    }

    @Test(description = "This method tests isDeviceOrganizationExist method under negative circumstances with an organization that doesn't exist")
    public void testOrganizationDoesNotExist() throws DeviceOrganizationMgtPluginException {
        int nonExistentDeviceId = 9999; // An ID that doesn't exist
        int nonExistentParentDeviceId = 8888; // An ID that doesn't exist
        boolean exists = deviceOrganizationService.isDeviceOrganizationExist(nonExistentDeviceId, nonExistentParentDeviceId);
        Assert.assertFalse(exists, "Organization should not exist for non-existent IDs.");
    }

    @Test(description = "This method tests Exception Handling when adding a duplicate Device Organization",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testAddDuplicateDeviceOrganization() throws DeviceOrganizationMgtPluginException {
        // Create a valid organization
        DeviceOrganization validOrganization = new DeviceOrganization();
        validOrganization.setDeviceId(1);
        validOrganization.setParentDeviceId(0);

        try {
            // Add the organization once
            deviceOrganizationService.addDeviceOrganization(validOrganization);

            // Attempt to add the same organization again, which should throw an exception
            deviceOrganizationService.addDeviceOrganization(validOrganization);
        } finally {
            // Clean up: Delete the added organization if it was successfully added to avoid conflicts in future tests
            deviceOrganizationService.deleteDeviceAssociations(validOrganization.getDeviceId());
        }
    }

    @Test(description = "This method tests Update Device Organization method under negative circumstances with null " +
            "data", expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testUpdateDeviceOrganizationWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        DeviceOrganization invalidOrganization = new DeviceOrganization();
        deviceOrganizationService.updateDeviceOrganization(invalidOrganization);
    }

    @Test(description = "This method tests Update Device Organization method under negative circumstances with an invalid organization ID",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testUpdateDeviceOrganizationWithInvalidID() throws DeviceOrganizationMgtPluginException {
        DeviceOrganization invalidOrganization = new DeviceOrganization();
        invalidOrganization.setOrganizationId(-1); // Provide an invalid organization ID
        deviceOrganizationService.updateDeviceOrganization(invalidOrganization);
    }


    @Test(description = "This method tests Get Device Organization By ID method under negative circumstances with " +
            "invalid input",
            expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testGetDeviceOrganizationByIDWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int invalidOrganizationId = 0;
        deviceOrganizationService.getDeviceOrganizationByID(invalidOrganizationId);
    }

    @Test(description = "This method tests Delete Device Organization By ID method under negative circumstances with " +
            "invalid input", expectedExceptions = {DeviceOrganizationMgtPluginException.class})
    public void testDeleteDeviceOrganizationByIDWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int invalidOrganizationId = 0;
        deviceOrganizationService.deleteDeviceOrganizationByID(invalidOrganizationId);
    }

    @Test(description = "This method tests Does Device ID Exist method under negative circumstances with invalid input",
            expectedExceptions = {BadRequestException.class})
    public void testDoesDeviceIdExistWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int invalidDeviceId = 0;
        deviceOrganizationService.isDeviceIdExist(invalidDeviceId);
    }

    @Test(description = "This method tests Delete Device Associations method under negative circumstances with invalid " +
            "input", expectedExceptions = {BadRequestException.class})
    public void testDeleteDeviceAssociationsWithInvalidInput() throws DeviceOrganizationMgtPluginException {
        int invalidDeviceId = 0;
        deviceOrganizationService.deleteDeviceAssociations(invalidDeviceId);
    }

}
