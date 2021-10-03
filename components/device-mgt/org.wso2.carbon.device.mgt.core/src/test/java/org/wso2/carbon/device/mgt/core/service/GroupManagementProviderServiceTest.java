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
 *
 *
 * Copyright (c) 2021, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.core.service;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupNotExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.cache.DeviceCacheConfiguration;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

public class GroupManagementProviderServiceTest extends BaseDeviceManagementTest {

    private GroupManagementProviderService groupManagementProviderService;
    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
            "/permission/device-mgt/user/groups"};

    @BeforeClass
    @Override
    public void init() throws Exception {
        groupManagementProviderService = new GroupManagementProviderServiceImpl();
        RealmService realmService = new InMemoryRealmService();
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        realmService.getTenantManager().getSuperTenantDomain();
        DeviceConfigurationManager.getInstance().initConfig();
    }

    @Test(expectedExceptions = {GroupManagementException.class, GroupAlreadyExistException.class})
    public void createGroupNull() throws GroupManagementException, GroupAlreadyExistException {
        groupManagementProviderService.createGroup(null, null, null);
    }


    @Test(expectedExceptions = {GroupManagementException.class, GroupAlreadyExistException.class, TransactionManagementException.class})
    public void createGroupError() throws GroupManagementException, GroupAlreadyExistException, TransactionManagementException {
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup4(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }


    @Test
    public void createGroup() throws GroupManagementException, GroupAlreadyExistException {
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup1(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup2(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup3(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup4(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }

    @Test(dependsOnMethods = "createGroup")
    public void updateGroup() throws GroupManagementException, GroupNotExistException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup1().getName(), false);
        deviceGroup.setName(deviceGroup.getName() + "_UPDATED");
        groupManagementProviderService.updateGroup(deviceGroup, deviceGroup.getGroupId());
    }

    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class})
    public void getGroupNull() throws GroupManagementException, GroupNotExistException {
        groupManagementProviderService.getGroup(null, false);
    }

    // Rename again to use in different place.
    @Test(dependsOnMethods = "updateGroup")
    public void updateGroupSecondTime() throws GroupManagementException, GroupNotExistException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup1().getName() + "_UPDATED", true);
        deviceGroup.setName(TestUtils.createDeviceGroup1().getName());
        groupManagementProviderService.updateGroup(deviceGroup, deviceGroup.getGroupId());
    }

    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class, GroupNotExistException.class})
    public void updateGroupError() throws GroupManagementException, GroupNotExistException {
        groupManagementProviderService.updateGroup(null, 1);
    }

    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class, GroupNotExistException.class})
    public void updateGroupErrorNotExist() throws GroupManagementException, GroupNotExistException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup2().getName(), false);
        deviceGroup.setName(deviceGroup.getName() + "_UPDATED");
        groupManagementProviderService.updateGroup(deviceGroup, 6);
    }

    @Test(dependsOnMethods = "createGroup")
    public void deleteGroup() throws GroupManagementException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup4().getName(), false);
        Assert.assertTrue(groupManagementProviderService.deleteGroup(deviceGroup.getGroupId(), false));
    }


    @Test(dependsOnMethods = "createGroup")
    public void deleteGroupNotExists() throws GroupManagementException {
        groupManagementProviderService.deleteGroup(8, false);
    }


    @Test(dependsOnMethods = "createGroup")
    public void getGroup() throws GroupManagementException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup3().getName(), false);
        Assert.assertNotNull(groupManagementProviderService.getGroup(deviceGroup.getGroupId(), false));
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupByName() throws GroupManagementException {
        Assert.assertNotNull(groupManagementProviderService.getGroup(TestUtils.createDeviceGroup3().getName(), false));
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroups() throws GroupManagementException {
        List<DeviceGroup> deviceGroups = groupManagementProviderService.getGroups(true);
        Assert.assertNotNull(deviceGroups);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupsByUsername() throws GroupManagementException {
        List<DeviceGroup> deviceGroups = groupManagementProviderService.getGroups("admin", true);
        Assert.assertNotNull(deviceGroups);
    }

    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class})
    public void getGroupsByUsernameError() throws GroupManagementException {
        groupManagementProviderService.getGroups((String) null, false);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupsByPagination() throws GroupManagementException {
        PaginationResult result = groupManagementProviderService.getGroups(TestUtils.createPaginationRequest(), true);
        Assert.assertNotNull(result);
    }

    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class})
    public void getGroupsByPaginationError() throws GroupManagementException {
        groupManagementProviderService.getGroups((GroupPaginationRequest) null, true);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupsByUsernameAndPagination()
            throws GroupManagementException {
        PaginationResult result = groupManagementProviderService.getGroups("admin", TestUtils.createPaginationRequest(), false);
        Assert.assertNotNull(result);
    }


    @Test(dependsOnMethods = "createGroup", expectedExceptions = {GroupManagementException.class})
    public void getGroupsByUsernameAndPaginationError()
            throws GroupManagementException {
        groupManagementProviderService.getGroups(null, TestUtils.createPaginationRequest(), true);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupCount() throws GroupManagementException {
        int x = groupManagementProviderService.getGroupCount();
        Assert.assertNotNull(x);
    }

    @DataProvider(name = "getGroupCountByUsernameProvider")
    public static Object[][] userName() {
        return new Object[][] {{"admin"}};
    }

    @Test(dependsOnMethods = "createGroup", dataProvider = "getGroupCountByUsernameProvider")
    public void getGroupCountByUsername(String username) throws GroupManagementException {
        int x = groupManagementProviderService.getGroupCount(username, null);
        Assert.assertNotEquals(x, 0);
    }

    @Test(dependsOnMethods = "updateGroupSecondTime")
    public void manageGroupSharing() throws GroupManagementException, RoleDoesNotExistException, UserStoreException {
        groupManagementProviderService.manageGroupSharing(0, null);
        List<String> newRoles = new ArrayList<>();
        newRoles.add("TEST_ROLE_1");
        newRoles.add("TEST_ROLE_2");
        newRoles.add("TEST_ROLE_3");

        UserStoreManager userStoreManager =
                DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                        -1234).getUserStoreManager();
        Permission[] permissions = new Permission[1];
        Permission perm = new Permission("/admin/test/perm", "add");
        permissions[0] = perm;

        userStoreManager.addRole("TEST_ROLE_1", null, permissions);
        userStoreManager.addRole("TEST_ROLE_2", null, permissions);
        userStoreManager.addRole("TEST_ROLE_3", null, permissions);

        groupManagementProviderService.manageGroupSharing(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup1().getName(), false).getGroupId(), newRoles);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getRoles() throws GroupManagementException {
        List<String> roles = groupManagementProviderService.getRoles(1);
        Assert.assertNotNull(roles);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getDevices() throws GroupManagementException {
        List<Device> devices = groupManagementProviderService.getDevices(1, 1, 50, false);
        Assert.assertNotNull(devices);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getDeviceCount() throws GroupManagementException {
        int x = groupManagementProviderService.getDeviceCount(1);
        Assert.assertEquals(0, x);
    }

    @Test(dependsOnMethods = "createGroup")
    public void addDevices() throws GroupManagementException, DeviceNotFoundException {

        DeviceCacheConfiguration configuration = new DeviceCacheConfiguration();
        configuration.setEnabled(false);

        DeviceConfigurationManager.getInstance().getDeviceManagementConfig().setDeviceCacheConfiguration(configuration);
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
        groupManagementProviderService.addDevices(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup1().getName(), false).getGroupId(), list);
        groupManagementProviderService.addDevices(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup2().getName(), false).getGroupId(), list);
        groupManagementProviderService.addDevices(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup3().getName(), false).getGroupId(), list);
    }

    @Test(dependsOnMethods = "addDevices")
    public void removeDevice() throws GroupManagementException, DeviceNotFoundException {
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
        groupManagementProviderService.removeDevice(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup2().getName(), false).getGroupId(), list);
        groupManagementProviderService.removeDevice(groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup3().getName(), false).getGroupId(), list);
    }

    @Test(dependsOnMethods = "createGroup")
    public void getGroupsByUsernameAndPermissions() throws GroupManagementException {
        List<DeviceGroup> groups = groupManagementProviderService.getGroups("admin", "/permission/device-mgt/admin/groups", true);
        Assert.assertNotNull(groups);
    }

    @Test(dependsOnMethods = "addDevices")
    public void getGroupsByDeviceIdentifier() throws GroupManagementException {
        DeviceIdentifier identifier = new DeviceIdentifier();
        identifier.setId("12345");
        identifier.setType(TestDataHolder.TEST_DEVICE_TYPE);
        List<DeviceGroup> groups = groupManagementProviderService.getGroups(identifier, true);
        Assert.assertNotNull(groups);
    }

    @Test
    public void createDefaultGroup() throws GroupManagementException {
        groupManagementProviderService.createDefaultGroup("BYOD");
    }

    @Test(dependsOnMethods = ("createDefaultGroup"))
    public void createDefaultGroupTwice() throws GroupManagementException {
        groupManagementProviderService.createDefaultGroup("BYOD");
    }

    @Test(dependsOnMethods = {"createGroup", "addDevices", "updateGroupSecondTime"})
    public void checkDeviceBelongsToGroup() throws GroupManagementException {
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
        boolean isMapped = groupManagementProviderService
                .isDeviceMappedToGroup(groupManagementProviderService.getGroup(
                        TestUtils.createDeviceGroup1().getName(), true).getGroupId(), list.get(0));
        Assert.assertEquals(isMapped, true);
    }

    @Test
    public void checkDeviceBelongsToNonExistingGroup() throws GroupManagementException {
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
        boolean isMapped = groupManagementProviderService
                .isDeviceMappedToGroup(1500, list.get(0));
        Assert.assertEquals(isMapped, false);
    }


    @Test(dependsOnMethods = {"createGroup", "updateGroupSecondTime"}, expectedExceptions = {GroupManagementException.class})
    public void checkNullDeviceBelongsToGroup() throws GroupManagementException {
        groupManagementProviderService.isDeviceMappedToGroup(groupManagementProviderService.getGroup(
                        TestUtils.createDeviceGroup1().getName(), true).getGroupId(), null);
    }

}

