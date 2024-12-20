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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import com.google.common.base.Strings;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.RoleMgtLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraRoleMgtLoggerImpl;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.RoleManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.FilteringUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.*;
import org.springframework.util.StringUtils;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.services.utils.ChangeRolePermissionsUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants.PRIMARY_USER_STORE;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final String API_BASE_PATH = "/roles";
    RoleMgtLogContext.Builder roleMgtContextBuilder = new RoleMgtLogContext.Builder();
    private static final EntgraLogger log = new EntgraRoleMgtLoggerImpl(RoleManagementServiceImpl.class);

    @GET
    @Override
    public Response getRoles(
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStore,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (limit == 0){
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }
        List<String> filteredRoles;
        RoleList targetRoles = new RoleList();

        //if user store is null set it to primary
        if (userStore == null || "".equals(userStore)) {
            userStore = PRIMARY_USER_STORE;
        }

        try {
            //Get the total role count that matches the given filter
            filteredRoles = getRolesFromUserStore(filter, userStore);
            targetRoles.setCount(filteredRoles.size());

            filteredRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(filter, userStore), offset, limit);
            targetRoles.setList(filteredRoles);

            return Response.ok().entity(targetRoles).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving roles from the underlying user stores";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/visible/{metaKey}")
    @Override
    public Response getVisibleRole(
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStore,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("username") String username,
            @QueryParam("domain") String domain,
            @PathParam("metaKey") String metaKey) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (limit == 0){
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }
        if (!Strings.isNullOrEmpty(domain)) {
            username = domain + '/' + username;
        }
        Metadata metadata;
        List<String> visibleRoles;
        RoleList visibleRoleList = new RoleList();
        try {
            boolean decision = false;
            if(DeviceMgtAPIUtils.getMetadataManagementService().retrieveMetadata(metaKey) != null){
                metadata = DeviceMgtAPIUtils.getMetadataManagementService().retrieveMetadata(metaKey);
                String metaValue = metadata.getMetaValue();
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(metaValue);
                decision = (boolean) jsonObject.get(Constants.IS_USER_ABLE_TO_VIEW_ALL_ROLES);
            }
            if (decision) {
                if (Strings.isNullOrEmpty(userStore)){
                    userStore = PRIMARY_USER_STORE;
                }
                try {
                    visibleRoles = getRolesFromUserStore(filter, userStore);
                    visibleRoleList.setList(visibleRoles);

                    visibleRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(filter, userStore), offset, limit);
                    visibleRoleList.setList(visibleRoles);

                    return Response.status(Response.Status.OK).entity(visibleRoleList).build();
                } catch (UserStoreException e) {
                    String msg = "Error occurred while retrieving roles from the underlying user stores";
                    log.error(msg, e);
                    return Response.serverError().entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            } else {
                try {
                    UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
                    if (!userStoreManager.isExistingUser(username)) {
                        if (log.isDebugEnabled()) {
                            log.debug("User by username: " + username + " does not exist for role retrieval.");
                        }
                        String msg = "User by username: " + username + " does not exist for role retrieval.";
                        return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
                    }
                    visibleRoleList.setList(getFilteredVisibleRoles(userStoreManager, username));

                    return Response.status(Response.Status.OK).entity(visibleRoleList).build();
                } catch (UserStoreException e) {
                    String msg = "Error occurred while trying to retrieve roles of the user '" + username + "'";
                    log.error(msg, e);
                    return Response.serverError().entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ParseException e) {
            String msg = "Error occurred while parsing JSON metadata: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    private List<String> getFilteredVisibleRoles(UserStoreManager userStoreManager, String username)
            throws UserStoreException {
        String[] roleListOfUser;
        roleListOfUser = userStoreManager.getRoleListOfUser(username);
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roleListOfUser) {
            if (!(role.startsWith("Internal/") || role.startsWith("Authentication/"))) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    @GET
    @Path("/filter/{prefix}")
    @Override
    public Response getFilteredRoles(
            @PathParam("prefix") String prefix,
            @QueryParam("filter") String filter,
            @QueryParam("user-store") String userStore,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        List<String> finalRoleList;
        RoleList targetRoles = new RoleList();

        //if user store is null set it to primary
        if (userStore == null || "".equals(userStore)) {
            userStore = PRIMARY_USER_STORE;
        }

        try {

            //Get the total role count that matches the given filter
            List<String> filteredRoles = getRolesFromUserStore(filter, userStore);
            finalRoleList = new ArrayList<String>();

            filteredRoles = FilteringUtil.getFilteredList(getRolesFromUserStore(filter, userStore), offset, limit);
            for (String rolename : filteredRoles){
                if (rolename.startsWith(prefix)){
                    finalRoleList.add(rolename);
                }
            }
            targetRoles.setCount(finalRoleList.size());
            targetRoles.setList(finalRoleList);

            return Response.ok().entity(targetRoles).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving roles from the underlying user stores";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{roleName}/permissions")
    @Override
    public Response getPermissionsOfRole(@PathParam("roleName") String roleName,
                                         @QueryParam("user-store") String userStoreName,
                                         @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        try {
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            if (!userRealm.getUserStoreManager().isExistingRole(roleName)) {

                String msg = "No role exists with the name : " + roleName ;
                return Response.status(404).entity(msg).build();
            }

            final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
            if (rolePermissions == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No permissions found for the role '" + roleName + "'");
                }
            }
            return Response.status(Response.Status.OK).entity(rolePermissions).build();
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the permissions of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the underlying user realm attached to the " +
                    "current logged in user";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Retrieve filtered permissions by analyzing all the permission paths.
     *
     * @param rolePermissions All the permission paths
     * @param permissionPaths Permission paths that needs to filter
     * @param permissions List of filtered permissions
     * @return {@link List<String>}
     */
    private List<String> processAndFilterPermissions(UIPermissionNode[] rolePermissions, List<String> permissionPaths, List<String> permissions) {

        for (UIPermissionNode rolePermission : rolePermissions) {
            if (permissionPaths.isEmpty()) {
                return permissions;
            }

            if (rolePermission.getNodeList().length == 0) {
                if (permissionPaths.contains(rolePermission.getResourcePath())) {
                    permissions.add(rolePermission.getResourcePath());
                }
            }
            permissionPaths.remove(rolePermission.getResourcePath());
            if (rolePermission.getNodeList().length != 0) {
                processAndFilterPermissions(rolePermission.getNodeList(), permissionPaths, permissions);
            }
        }
        return permissions;
    }

    /**
     * Getting platform permissions
     *
     * @param roleName Role Name
     * @param userRealm {@link UserRealm}
     * @param permissions list of permissions
     * @return {@link List<String>}
     * @throws UserAdminException if error occurred when getting {@link UIPermissionNode}
     */
    private String[] getPlatformUIPermissions(String roleName, UserRealm userRealm, String[] permissions)
            throws UserAdminException {
        UIPermissionNode uiPermissionNode = getUIPermissionNode(roleName, userRealm);
        List<String> permissionsArray = processAndFilterPermissions(uiPermissionNode.getNodeList(), new ArrayList<>(Arrays.asList(permissions)),
                new ArrayList<>());
        return permissionsArray.toArray(new String[0]);
    }

    private UIPermissionNode getUIPermissionNode(String roleName, UserRealm userRealm)
            throws UserAdminException {
        org.wso2.carbon.user.core.UserRealm userRealmCore = null;
        if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        }
        final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
        final UIPermissionNode rolePermissions =
                userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
        List<UIPermissionNode> deviceMgtPermissionsList = new ArrayList<>();

        for (UIPermissionNode permissionNode : rolePermissions.getNodeList()) {
            if (Constants.Permission.ADMIN.equals(permissionNode.getResourcePath())) {
                for (UIPermissionNode node : permissionNode.getNodeList()) {
                    if (Constants.Permission.LOGIN.equals(node.getResourcePath()) ||
                            Constants.Permission.DEVICE_MGT.equals(node.getResourcePath()) ||
                            Constants.Permission.APP_MGT.equals(node.getResourcePath()) ||
                            Constants.Permission.TENANT.equals(node.getResourcePath()) ||
                            Constants.Permission.DEFAULT.equals(node.getResourcePath())) {
                        deviceMgtPermissionsList.add(node);
                    }
                }
            }
        }
        UIPermissionNode[] deviceMgtPermissions = new UIPermissionNode[deviceMgtPermissionsList.size()];
        rolePermissions.setNodeList(deviceMgtPermissionsList.toArray(deviceMgtPermissions));
        return rolePermissions;
    }

    @GET
    @Path("/{roleName}")
    @Override
    public Response getRole(@PathParam("roleName") String roleName, @QueryParam("user-store") String userStoreName,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        RoleInfo roleInfo = new RoleInfo();
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            if (!userStoreManager.isExistingRole(roleName)) {
                String msg = "No role exists with the name : " + roleName ;
                return Response.status(404).entity(msg).build();
            }
            roleInfo.setRoleName(roleName);
            roleInfo.setUsers(userStoreManager.getUserListOfRole(roleName));
            // Get the permission nodes and hand picking only device management and login perms
            final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
            List<String> permList = new ArrayList<>();
            DeviceMgtAPIUtils.iteratePermissions(rolePermissions, permList);
            roleInfo.setPermissionList(rolePermissions);
            String[] permListAr = new String[permList.size()];
            roleInfo.setPermissions(permList.toArray(permListAr));
            return Response.status(Response.Status.OK).entity(roleInfo).build();
        } catch (UserAdminException e) {
            String msg = "Error occurred due to Unable to retrieve user role'" + roleName + "'";
            log.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the UserStoreManager of the user role '"
                    + roleName + "'";
            log.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        }
    }

    @POST
    @Override
    public Response addRole(RoleInfo roleInfo) {
        RequestValidationUtil.validateRoleDetails(roleInfo);
        RequestValidationUtil.validateRoleName(roleInfo.getRoleName());

        String role;
        String[] roles = roleInfo.getRoleName().split("/");

        if (roles.length > 1) {
            role = roleInfo.getRoleName().split("/")[1];
        } else {
            role = roleInfo.getRoleName().split("/")[0];
        }

        try {
            String tenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String userName = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role in the underlying user store");
            }

            Permission[] permissions = null;
            if (roleInfo.getPermissions() != null && roleInfo.getPermissions().length > 0) {
                permissions = new Permission[roleInfo.getPermissions().length];
                for (int i = 0; i < permissions.length; i++) {
                    String permission = roleInfo.getPermissions()[i];
                    permissions[i] = new Permission(permission, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
            userStoreManager.addRole(roleInfo.getRoleName(), roleInfo.getUsers(), permissions);
            try {
                if (roleInfo.getPermissions() != null && roleInfo.getPermissions().length > 0) {
                    String[] roleName = roleInfo.getRoleName().split("/");
                    roleInfo.setRemovedPermissions(new String[0]);
                    updatePermissions(roleName[roleName.length - 1], roleInfo, DeviceMgtAPIUtils.getUserRealm());
                }
            } catch (UserStoreException e) {
                String msg = "Error occurred while loading the user store.";
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            String stringUsers = new Gson().toJson(roleInfo.getUsers());
            log.info(
                    "Role " + role + " created",
                    roleMgtContextBuilder
                            .setActionTag("ADD_ROLE")
                            .setUserStoreDomain(roleInfo.getRoleName().split("/")[0])
                            .setRoleName(role)
                            .setUsers(stringUsers)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(userName)
                            .build()
            );
            //TODO fix what's returned in the entity
            return Response.created(new URI(API_BASE_PATH + "/" + URLEncoder.encode(roleInfo.getRoleName(), "UTF-8"))).
                    entity("Role '" + roleInfo.getRoleName() + "' has " + "successfully been"
                            + " added").build();
        } catch (UserStoreException e) {
            String errorCode = "";
            String errorMessage = e.getMessage();
            if (errorMessage != null && !errorMessage.isEmpty() &&
                    errorMessage.contains(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode())) {
                errorCode = e.getMessage().split("-")[0].trim();
            }
            if (ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode().equals(errorCode)) {
                String msg = "Role already exists with name : " + role + ". Try with another role name.";
                log.warn(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            } else {
                String msg = "Error occurred while adding role '" + roleInfo.getRoleName() + "'";
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the URI at which the information of the newly created role " +
                    "can be retrieved";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding role name";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/create-combined-role/{roleName}")
    @Override
    public Response addCombinedRole(List<String> roles, @PathParam("roleName") String roleName,
                                    @QueryParam("user-store") String userStoreName) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        if (roles.size() < 2) {
            String msg = "Combining Roles requires at least two roles.";
            return Response.status(400).entity(msg).build();
        }
        for (String role : roles) {
            RequestValidationUtil.validateRoleName(role);
        }
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role in the underlying user store");
            }

            HashSet<Permission> permsSet = new HashSet<>();
            try {
                for (String role : roles) {
                    mergePermissions(new UIPermissionNode[]{getRolePermissions(role)}, permsSet);
                }
            } catch (IllegalArgumentException e) {
                return Response.status(404).entity(e.getMessage()).build();
            }

            Permission[] permissions = permsSet.toArray(new Permission[permsSet.size()]);
            userStoreManager.addRole(roleName, new String[0], permissions);

            //TODO fix what's returned in the entity
            return Response.created(new URI(API_BASE_PATH + "/" + URLEncoder.encode(roleName, "UTF-8"))).
                    entity("Role '" + roleName + "' has " + "successfully been"
                            + " added").build();
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the permissions of role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while adding role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the URI at which the information of the newly created role " +
                    "can be retrieved";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding role name";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}")
    @Override
    public Response updateRole(@PathParam("roleName") String roleName, RoleInfo roleInfo,
                               @QueryParam("user-store") String userStoreName) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateRoleDetails(roleInfo);
        try {
            String tenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String userName = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
            String[] stringUserList;
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!userStoreManager.isExistingRole(roleName)) {
                String msg = "No role exists with the name : " + roleName;
                return Response.status(404).entity(msg).build();
            }

            if (log.isDebugEnabled()) {
                log.debug("Updating the role to user store");
            }

            String newRoleName = roleInfo.getRoleName();
            if (newRoleName != null && !roleName.equals(newRoleName)) {
                userStoreManager.updateRoleName(roleName, newRoleName);
            }

            if (roleInfo.getUsers() != null) {
                SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
                transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(newRoleName)),
                        Arrays.asList(roleInfo.getUsers()));
                final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                        .getObjectsToAdd().size()]);
                final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                        .getObjectsToRemove().size()]);
                userStoreManager.updateUserListOfRole(newRoleName, usersToDelete, usersToAdd);
                stringUserList = roleInfo.getUsers();
            } else {
                stringUserList = userStoreManager.getUserListOfRole(roleName);
            }

            if (roleInfo.getPermissions() != null) {
                String[] roleDetails = roleName.split("/");
                updatePermissions(roleDetails[roleDetails.length - 1], roleInfo, userRealm);
            }
            String stringUsers = new Gson().toJson(stringUserList);

            String role;
            String[] roles = roleInfo.getRoleName().split("/");

            if (roles.length > 1) {
                role = roleInfo.getRoleName().split("/")[1];
            } else {
                role = roleInfo.getRoleName().split("/")[0];
            }

            log.info(
                    "Role " + role + " updated",
                    roleMgtContextBuilder
                            .setActionTag("UPDATE_ROLE")
                            .setUserStoreDomain(roleInfo.getRoleName().split("/")[0])
                            .setRoleName(role)
                            .setUsers(stringUsers)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(userName)
                            .build()
            );
            //TODO: Need to send the updated role information in the entity back to the client
            return Response.status(Response.Status.OK).entity("Role '" + roleInfo.getRoleName() + "' has " +
                    "successfully been updated").build();
        } catch (UserStoreException e) {
            String errorCode = "";
            String errorMessage = e.getMessage();
            if (errorMessage != null && !errorMessage.isEmpty() &&
                    errorMessage.contains(ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode())) {
                errorCode = e.getMessage().split("-")[0].trim();
            }
            if (ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode().equals(errorCode)) {
                String role = roleInfo.getRoleName().split("/")[1];
                String msg = "Role already exists with name : " + role + ". Try with another role name.";
                log.warn(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            } else {
                String msg = "Error occurred while updating role '" + roleName + "'";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        }
    }

    @DELETE
    @Path("/{roleName}")
    @Consumes(MediaType.WILDCARD)
    @Override
    public Response deleteRole(@PathParam("roleName") String roleName, @QueryParam("user-store") String userStoreName) {
        String roleToDelete = roleName;
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        try {
            String tenantDomain = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String userName = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
            final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            final UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (!userStoreManager.isExistingRole(roleName)) {
                String msg = "No role exists with the name : " + roleName ;
                return Response.status(404).entity(msg).build();
            }

            final AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            if (log.isDebugEnabled()) {
                log.debug("Deleting the role in user store");
            }
            DeviceMgtAPIUtils.getGroupManagementProviderService().deleteRoleAndRoleGroupMapping(roleName, roleToDelete, tenantId, userStoreManager, authorizationManager);
            String role;
            String[] roles = roleName.split("/");

            if (roles.length > 1) {
                role = roleName.split("/")[1];
            } else {
                role = roleName.split("/")[0];
            }

            log.info(
                    "Role " + role + " deleted",
                    roleMgtContextBuilder
                            .setActionTag("DELETE_ROLE")
                            .setUserStoreDomain(userStoreName)
                            .setRoleName(role)
                            .setTenantID(String.valueOf(tenantId))
                            .setTenantDomain(tenantDomain)
                            .setUserName(userName)
                            .build()
            );
            return Response.status(Response.Status.OK).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while deleting group-role mapping records";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/{roleName}/users")
    @Override
    public Response updateUsersOfRole(@PathParam("roleName") String roleName,
                                      @QueryParam("user-store") String userStoreName, List<String> users) {
        if (userStoreName != null && !userStoreName.isEmpty()) {
            roleName = userStoreName + "/" + roleName;
        }
        RequestValidationUtil.validateRoleName(roleName);
        RequestValidationUtil.validateUsers(users);
        try {
            final UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (log.isDebugEnabled()) {
                log.debug("Updating the users of a role");
            }
            SetReferenceTransformer<String> transformer = new SetReferenceTransformer<>();
            transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(roleName)),
                    users);
            final String[] usersToAdd = transformer.getObjectsToAdd().toArray(new String[transformer
                    .getObjectsToAdd().size()]);
            final String[] usersToDelete = transformer.getObjectsToRemove().toArray(new String[transformer
                    .getObjectsToRemove().size()]);

            userStoreManager.updateUserListOfRole(roleName, usersToDelete, usersToAdd);

            return Response.status(Response.Status.OK).entity("Role '" + roleName + "' has " +
                            "successfully been updated with the user list")
                    .build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating the users of the role '" + roleName + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private List<String> getRolesFromUserStore(String filter, String userStore) throws UserStoreException {
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) DeviceMgtAPIUtils.getUserStoreManager();
        String[] roles;
        if ((filter == null) || filter.isEmpty()) {
            filter = "*";
        } else {
            filter = "*" + filter + "*";
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of user roles");
        }
        if (userStore.equals("all")) {
            roles = userStoreManager.getRoleNames(filter, -1, true, true, true);
        } else {
            roles = userStoreManager.getRoleNames(userStore + "/" + filter, -1, true, true, true);
        }
        List<String> filteredRoles = new ArrayList<>();
        for (String role : roles) {
            filteredRoles.add(role);
        }
        return filteredRoles;
    }

    private Set<Permission> mergePermissions(UIPermissionNode[] permissionNodes, Set<Permission> permissions)
            throws UserStoreException, UserAdminException {
        for (UIPermissionNode permissionNode : permissionNodes) {
            if (permissionNode.getNodeList().length > 0) {
                mergePermissions(permissionNode.getNodeList(), permissions);
            }
            if (permissionNode.isSelected()) {
                permissions.add(new Permission(permissionNode.getResourcePath(), CarbonConstants.UI_PERMISSION_ACTION));
            }
        }
        return permissions;
    }

    private UIPermissionNode getRolePermissions(String roleName) throws UserStoreException, UserAdminException {
        final UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
        if (!userRealm.getUserStoreManager().isExistingRole(roleName)) {
            throw new IllegalArgumentException("No role exists with the name '" + roleName + "'");
        }

        final UIPermissionNode rolePermissions = this.getUIPermissionNode(roleName, userRealm);
        if (rolePermissions == null) {
            if (log.isDebugEnabled()) {
                log.debug("No permissions found for the role '" + roleName + "'");
            }
        }
        return rolePermissions;
    }

    /**
     * Update the role's permissions. This will function in the fire and forget pattern and run on a new thread.
     *
     * @param roleName Role Name
     * @param roleInfo {@link RoleInfo}
     * @param userRealm {@link UserRealm}
     */
    private void updatePermissions(String roleName, RoleInfo roleInfo, UserRealm userRealm) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    DeviceMgtAPIUtils.getApiPublisher().updateScopeRoleMapping(roleName,
                            RoleManagementServiceImpl.this.getPlatformUIPermissions(roleName, userRealm,
                                    roleInfo.getPermissions()), RoleManagementServiceImpl.this.getPlatformUIPermissions(roleName, userRealm,
                                    roleInfo.getRemovedPermissions()));
                } catch (APIManagerPublisherException | UserAdminException e) {
                    log.error("Error Occurred while updating role scope mapping. ", e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        });
        thread.start();
    }
}
