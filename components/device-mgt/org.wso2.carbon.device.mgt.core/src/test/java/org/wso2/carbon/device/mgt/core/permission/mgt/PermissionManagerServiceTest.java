/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.core.permission.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This contains unit tests for PermissionManagerService class.
 */
@PrepareForTest(PermissionUtils.class)
public class PermissionManagerServiceTest {

    private static final Log log = LogFactory.getLog(PermissionManagerServiceTest.class);;
    private static final String PERMISSION_URL = "permission/admin/device-mgt/test/testPermission";
    private static final String PERMISSION_PATH = "permission/admin/device-mgt/test/testPermission";
    private static final String PERMISSION_METHOD = "ui.execute";
    private static final String PERMISSION_NAME = "Test Permission";
    private static final String PERMISSION_CONTEXT = "permission/admin/device-mgt/test/testPermission";
    private static final String INVALID_PERMISSION_CONTEXT = "permission/INVALID";


    //For create properties to retrieve permission.
    private static final String HTTP_METHOD = "HTTP_METHOD";
    private static final String URL = "URL";
    private Permission permission;
    private final List<Permission> permissionList = new ArrayList<>();
    private PermissionManagerService permissionManagerService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws RegistryException {
        initMocks(this);
        this.permissionManagerService = PermissionManagerServiceImpl.getInstance();
        this.permission = new Permission();
        this.permission.setName(PERMISSION_NAME);
        this.permission.setPath(PERMISSION_PATH);
        this.permission.setMethod(PERMISSION_METHOD);
        this.permission.setUrl(PERMISSION_URL);
        this.permissionList.add(this.permission);
    }

    @Test (description = "Create a new permission in the permission tree.")
    public void testCreatePermission() {
        try {
            PowerMockito.mockStatic(PermissionUtils.class);
            PowerMockito.when(PermissionUtils.putPermission(permission)).thenReturn(true);
            Assert.assertTrue(permissionManagerService.addPermission(PERMISSION_CONTEXT, this.permissionList));
        } catch (PermissionManagementException e) {
            log.error("Error creating permission " + e.getErrorMessage());
        }
    }

    @Test (dependsOnMethods = {"testCreatePermission"}, description = "Test for retrieving the created permission " +
            "from the permission tree.")
    public void testGetPermission() throws PermissionManagementException {
        List<Permission> permissions = permissionManagerService.getPermission(PERMISSION_CONTEXT);
        for (Permission permission : permissions) {
            Assert.assertEquals(permission.getMethod(), PERMISSION_METHOD);
            Assert.assertEquals(permission.getName(), PERMISSION_NAME);
            Assert.assertEquals(permission.getPath(), PERMISSION_PATH);
            Assert.assertEquals(permission.getUrl(), PERMISSION_URL);
        }
    }

    @Test (dependsOnMethods = {"testCreatePermission"})
    public void testGetPermissionError() throws PermissionManagementException {
        List<Permission> permissions = permissionManagerService.getPermission(INVALID_PERMISSION_CONTEXT);
        Assert.assertNull(permissions);
    }
}
