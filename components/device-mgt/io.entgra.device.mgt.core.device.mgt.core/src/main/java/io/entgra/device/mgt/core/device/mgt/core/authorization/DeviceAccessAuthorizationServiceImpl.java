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

package io.entgra.device.mgt.core.device.mgt.core.authorization;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAuthorizationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.Permission;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of DeviceAccessAuthorization service.
 */
public class DeviceAccessAuthorizationServiceImpl implements DeviceAccessAuthorizationService {

    private final static String CDM_ADMIN_PERMISSION = "/device-mgt/devices/any-device/permitted-actions-under-owning-device";
    private static Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceImpl.class);

    public DeviceAccessAuthorizationServiceImpl() {
        log.info("DeviceAccessAuthorizationServiceImpl initialized.");
    }

    @Override
    public boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String username, String[] groupPermissions)
            throws DeviceAccessAuthorizationException {
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
            return !DeviceManagementDataHolder.getInstance().requireDeviceAuthorization(deviceIdentifier.getType());
        }
        //check for admin and ownership permissions
        if (isDeviceAdminUser(username, tenantId) || isDeviceOwner(deviceIdentifier, username)) {
            return true;
        }
        //check for group permissions
        if (groupPermissions == null || groupPermissions.length == 0) {
            return false;
        } else {
            // if group permissions specified, check whether that permission is available in shared role
            try {
                boolean isAuthorized = true;
                for (String groupPermission : groupPermissions) {
                    if (!isAuthorizedViaSharedGroup(deviceIdentifier, username, groupPermission)) {
                        //if at least one failed, authorizations fails and break the loop
                        isAuthorized = false;
                        break;
                    }
                }
               return isAuthorized;
            } catch (DeviceAccessAuthorizationException e) {
                throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                        deviceIdentifier.getId() + " for the user : " +
                        username, e);
            }
        }
    }


    private boolean isAuthorizedViaSharedGroup(DeviceIdentifier deviceIdentifier, String username, String groupPermission)
            throws DeviceAccessAuthorizationException {
        try {
            List<DeviceGroup> groupsWithDevice = DeviceManagementDataHolder.getInstance()
                    .getGroupManagementProviderService().getGroups(deviceIdentifier, false);
            UserRealm userRealm = DeviceManagementDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(getTenantId());
            String[] userRoles = userRealm.getUserStoreManager().getRoleListOfUser(username);
            for (DeviceGroup deviceGroup : groupsWithDevice) {
                List<String> sharingRoles = DeviceManagementDataHolder.getInstance()
                        .getGroupManagementProviderService().getRoles(deviceGroup.getGroupId());
                for (String role : userRoles) {
                    if (sharingRoles.contains(role) && userRealm.getAuthorizationManager().
                            isRoleAuthorized(role, groupPermission, CarbonConstants.UI_PERMISSION_ACTION)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (GroupManagementException | UserStoreException e) {
            throw new DeviceAccessAuthorizationException("unable to authorized via shared role, " + groupPermission);
        }
    }

    @Override
    public boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String[] groupPermissions)
            throws DeviceAccessAuthorizationException {
        return isUserAuthorized(deviceIdentifier, this.getUserName(), groupPermissions);
    }

    @Override
    public boolean isDeviceAdminUser() throws DeviceAccessAuthorizationException {
        String username = this.getUserName();
        int tenantId = this.getTenantId();
        try {
            return isDeviceAdminUser(username, tenantId);
        } catch (DeviceAccessAuthorizationException e) {
            throw new DeviceAccessAuthorizationException("Unable to check the admin permissions of user : " +
                                                         username + " in tenant : " + tenantId, e);
        }
    }

    @Override
    public DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String username,
                                                      String[] groupPermissions)
            throws DeviceAccessAuthorizationException {
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
            return null;
        }
        DeviceAuthorizationResult deviceAuthorizationResult = new DeviceAuthorizationResult();
        if (isDeviceAdminUser(username, tenantId)) {
            deviceAuthorizationResult.setAuthorizedDevices(deviceIdentifiers);
            return deviceAuthorizationResult;
        }
        for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
            //check for admin and ownership permissions
            if (isDeviceOwner(deviceIdentifier, username)) {
                deviceAuthorizationResult.addAuthorizedDevice(deviceIdentifier);
            } else {
                try {
                    if (groupPermissions == null || groupPermissions.length == 0) {
                        deviceAuthorizationResult.setUnauthorizedDevices(deviceIdentifiers);
                        return deviceAuthorizationResult;
                    }
                    //check for group permissions
                    boolean isAuthorized = true;
                    for (String groupPermission : groupPermissions) {
                        if (!isAuthorizedViaSharedGroup(deviceIdentifier, username, groupPermission)) {
                            //if at least one failed, authorizations fails and break the loop
                            isAuthorized = false;
                            break;
                        }
                    }
                    if (isAuthorized) {
                        deviceAuthorizationResult.addAuthorizedDevice(deviceIdentifier);
                    } else {
                        deviceAuthorizationResult.addUnauthorizedDevice(deviceIdentifier);
                    }
                } catch (DeviceAccessAuthorizationException e) {
                    throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                                 deviceIdentifier.getId() + " for the user : " +
                                                                 username, e);
                }
            }
        }
        return deviceAuthorizationResult;
    }

    @Override
    public DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String[] groupPermissions)
            throws DeviceAccessAuthorizationException {
        return isUserAuthorized(deviceIdentifiers, this.getUserName(), groupPermissions);
    }

    private boolean isDeviceOwner(DeviceIdentifier deviceIdentifier, String username)
            throws DeviceAccessAuthorizationException {
        //Check for device ownership. If the user is the owner of the device we allow the access.
        try {
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    isEnrolled(deviceIdentifier, username);
        } catch (DeviceManagementException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                                 deviceIdentifier.getId() + " for the user : " +
                                                                 username, e);
        }
    }

    private boolean isDeviceAdminUser(String username, int tenantId) throws DeviceAccessAuthorizationException {
        try {
            UserRealm userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                return userRealm.getAuthorizationManager()
                        .isUserAuthorized(removeTenantDomain(username),
                                PermissionUtils.getAbsolutePermissionPath(CDM_ADMIN_PERMISSION),
                                CarbonConstants.UI_PERMISSION_ACTION);
            }
            return false;
        } catch (UserStoreException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access for the user : " +
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
}