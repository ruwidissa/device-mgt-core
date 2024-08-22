/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.authorization;

import io.entgra.device.mgt.core.device.mgt.common.authorization.GroupAccessAuthorizationException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.GroupAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.common.authorization.GroupAuthorizationResult;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.Permission;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xmlsec.signature.G;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public class GroupAccessAuthorizationServiceImpl implements GroupAccessAuthorizationService {

    private final static String GROUP_ADMIN_PERMISSION = "/device-mgt/devices/any-group/permitted-actions-under-owning-group";
    private final static String GROUP_ADMIN = "Group Management Administrator";
    private static Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceImpl.class);

    public GroupAccessAuthorizationServiceImpl() {
        try {
            this.addAdminPermissionToRegistry();
        } catch (PermissionManagementException e) {
            log.error("Unable to add the group-admin permission to the registry.", e);
        }
    }

    @Override
    public boolean isUserAuthorized(int groupId, String username, String[] groupPermissions)
            throws GroupAccessAuthorizationException {
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
            username = this.getUserName();
        }
        //check for admin and ownership permissions
        if (isGroupAdminUser(username, tenantId) || isGroupOwner(groupId, username)) {
            return true;
        }

        //check for group permissions
        if (groupPermissions == null || groupPermissions.length == 0) {
            return false;
        } else {
            // if group permissions specified, check whether that permission is available in any user role of the group owner
            try {
                UserRealm userRealm = DeviceManagementDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(getTenantId());
                String[] userRoles = userRealm.getUserStoreManager().getRoleListOfUser(username);
                boolean isAuthorized;
                for (String groupPermission : groupPermissions) {
                    isAuthorized = false;
                    for (String role : userRoles) {
                        if (userRealm.getAuthorizationManager().
                                isRoleAuthorized(role, groupPermission, CarbonConstants.UI_PERMISSION_ACTION)) {
                            isAuthorized = true;
                            break;
                        }
                    }
                    if (!isAuthorized) {
                        return false;
                    }
                }
                return true;
            } catch (UserStoreException e) {
                throw new GroupAccessAuthorizationException("Unable to authorize the access to group : " +
                        groupId + " for the user : " + username, e);
            }
        }
    }

    @Override
    public GroupAuthorizationResult isUserAuthorized(List<Integer> groupIds, String username, String[] groupPermission)
            throws GroupAccessAuthorizationException {
        GroupAuthorizationResult result = new GroupAuthorizationResult();
        for (Integer groupId : groupIds) {
            if (isUserAuthorized(groupId, username, groupPermission)) {
                result.addAuthorizedGroupId(groupId);
            } else {
                result.addUnauthorizedGroupId(groupId);
            }
        }
        return result;
    }


    @Override
    public boolean isUserAuthorized(int groupId, String[] groupPermissions)
            throws GroupAccessAuthorizationException {
        return isUserAuthorized(groupId, this.getUserName(), groupPermissions);
    }

    private boolean isGroupOwner(int groupId, String username)
            throws GroupAccessAuthorizationException {
        //Check for group ownership. If the user is the owner of the group we allow the access.
        try {
            DeviceGroup group = DeviceManagementDataHolder.getInstance().
                    getGroupManagementProviderService().getGroup(groupId, false);
            return username.equals(group.getOwner());
        } catch (GroupManagementException e) {
            throw new GroupAccessAuthorizationException("Unable to authorize the access to group : " +
                    groupId + " for the user : " +
                    username, e);
        }
    }

    private boolean isGroupAdminUser(String username, int tenantId) throws GroupAccessAuthorizationException {
        try {
            UserRealm userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                return userRealm.getAuthorizationManager()
                        .isUserAuthorized(removeTenantDomain(username),
                                PermissionUtils.getAbsolutePermissionPath(GROUP_ADMIN_PERMISSION),
                                CarbonConstants.UI_PERMISSION_ACTION);
            }
            return false;
        } catch (UserStoreException e) {
            throw new GroupAccessAuthorizationException("Unable to authorize the access for the user : " +
                    username, e);
        }
    }

    private String getUserName() {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (username != null && !username.isEmpty()) {
            return removeTenantDomain(username);
        }
        return null;
    }

    private String removeTenantDomain(String username) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private boolean addAdminPermissionToRegistry() throws PermissionManagementException {
        Permission permission = new Permission();
        permission.setName(GROUP_ADMIN);
        permission.setPath(PermissionUtils.getAbsolutePermissionPath(GROUP_ADMIN_PERMISSION));
        return PermissionUtils.putPermission(permission);
    }

}
