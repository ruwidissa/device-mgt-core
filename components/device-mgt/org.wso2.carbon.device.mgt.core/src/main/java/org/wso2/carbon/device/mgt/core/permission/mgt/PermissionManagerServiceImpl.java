/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;

import java.util.List;

/**
 * This class will add, update custom permissions defined in permission.xml in webapps and it will
 * use Registry as the persistence storage.
 */
public class PermissionManagerServiceImpl implements PermissionManagerService {

    private static PermissionManagerServiceImpl registryBasedPermissionManager;
    private static APIResourcePermissions apiResourcePermissions;
    private PermissionManagerServiceImpl() {
    }

    public static PermissionManagerServiceImpl getInstance() {
        if (registryBasedPermissionManager == null) {
            synchronized (PermissionManagerServiceImpl.class) {
                if (registryBasedPermissionManager == null) {
                    registryBasedPermissionManager = new PermissionManagerServiceImpl();
                    apiResourcePermissions = new APIResourcePermissions();
                }
            }
        }
        return registryBasedPermissionManager;
    }

    @Override
    public boolean addPermission(String context, List<Permission> permissions) throws PermissionManagementException {
        try {
            for (Permission permission : permissions) {
                PermissionUtils.putPermission(permission);
            }
            apiResourcePermissions.addPermissionList(context, permissions);
        } catch (PermissionManagementException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<Permission> getPermission(String context) throws PermissionManagementException {
        return apiResourcePermissions.getPermissions(context);
    }
}
