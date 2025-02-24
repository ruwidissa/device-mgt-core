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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.UserMgtLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraUserMgtLoggerImpl;
import org.apache.http.HttpStatus;
import org.eclipse.wst.common.uriresolver.internal.util.URIEncoder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.OTPManagementException;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.EmailMetaInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ActivityList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.BasicUserInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.BasicUserInfoList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.BasicUserInfoWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.Credential;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.EnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.InvitationMailProfile;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.JITEnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.OldPasswordResetWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PermissionList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.UserInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.UserStoreList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.exception.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.UserManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.CredentialManagementResponseBuilder;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementAdminService;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Properties;
import java.util.*;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ROLE_EVERYONE = "Internal/everyone";
    private static final String API_BASE_PATH = "/users";
    UserMgtLogContext.Builder userMgtContextBuilder = new UserMgtLogContext.Builder();
    private static final EntgraLogger log = new EntgraUserMgtLoggerImpl(UserManagementServiceImpl.class);

    // Permissions that are given for a normal device user.
    private static final Permission[] PERMISSIONS_FOR_DEVICE_USER = {
            new Permission("/permission/admin/Login", "ui.execute"),
            new Permission("/permission/admin/device-mgt/device/api/subscribe", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/enroll", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/disenroll", "ui.execute"),
            new Permission("/permission/admin/device-mgt/devices/owning-device/view", "ui.execute"),
            new Permission("/permission/admin/manage/portal", "ui.execute")
    };

    @POST
    @Override
    public Response addUser(UserInfo userInfo) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userInfo.getUsername())) {
                // if user already exists
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + userInfo.getUsername() +
                            " already exists. Therefore, request made to add user was refused.");
                }
                // returning response with bad request state
                String msg = "User by username: " + userInfo.getUsername() + " already exists. Try with another username." ;
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }

            String initialUserPassword;
            if (userInfo.getPassword() != null) {
                initialUserPassword = userInfo.getPassword();
            } else {
                initialUserPassword = this.generateInitialUserPassword();
            }

            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress(), true);

            userStoreManager.addUser(userInfo.getUsername(), initialUserPassword,
                    userInfo.getRoles(), defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User '" + userInfo.getUsername() + "' has successfully been added.");
            }
            String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String loggeduserName = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getUsername());
            String stringRoles = new Gson().toJson(userInfo.getRoles());
            BasicUserInfo createdUserInfo = this.getBasicUserInfo(userInfo.getUsername());
            // Outputting debug message upon successful retrieval of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + userInfo.getUsername() + " was found.");
            }
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            String[] bits = userInfo.getUsername().split("/");
            String username = bits[bits.length - 1];
            String recipient = userInfo.getEmailAddress();
            Properties props = new Properties();
            props.setProperty("first-name", userInfo.getFirstname());
            props.setProperty("last-name", userInfo.getLastname());
            props.setProperty("username", username);
            props.setProperty("password", initialUserPassword);
            log.info(
                    "User " + username + " created",
                    userMgtContextBuilder
                            .setActionTag("ADD_USER")
                            .setUserStoreDomain(userInfo.getUsername().split("/")[0])
                            .setFirstName(userInfo.getFirstname())
                            .setLastName(userInfo.getLastname())
                            .setEmail(userInfo.getEmailAddress())
                            .setUserRoles(stringRoles)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(loggeduserName)
                            .build()
            );

            EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);
            BasicUserInfoWrapper userInfoWrapper = new BasicUserInfoWrapper();
            String message;
            try {
                dms.sendRegistrationEmail(metaInfo);
                message = "An invitation mail will be sent to this user to initiate device enrollment.";
            } catch (ConfigurationManagementException e) {
                message = "Mail Server is not configured. Email invitation will not be sent.";
            }
            userInfoWrapper.setBasicUserInfo(createdUserInfo);
            userInfoWrapper.setMessage(message);
            return Response.created(new URI(API_BASE_PATH + "/" + URIEncoder.encode(userInfo.getUsername(),
                    "UTF-8"))).entity(userInfoWrapper).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to add user '" + userInfo.getUsername() + "' to the " +
                    "underlying user management system";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the location URI, which represents information of the " +
                    "newly created user '" + userInfo.getUsername() + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occurred while encoding username in the URI for the newly created user " +
                    userInfo.getUsername();
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while sending registration email to the user " + userInfo.getUsername();
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    public Response getUser(@QueryParam("username") String username, @QueryParam("domain") String domain,
                            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist.");
                }
                String msg = "User by username: " + username + " does not exist.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }

            BasicUserInfo user = this.getBasicUserInfo(username);
            return Response.status(Response.Status.OK).entity(user).build();
        } catch (UserStoreException | DeviceManagementException e) {
            String msg = "Error occurred while retrieving information of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Override
    public Response updateUser(@QueryParam("username") String username, @QueryParam("domain") String domain, UserInfo userInfo) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String loggeduserName = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getUsername());
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username +
                            " doesn't exists. Therefore, request made to update user was refused.");
                }
                String msg = "User by username: " + username + " does not exist.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }

            Map<String, String> defaultUserClaims =
                    this.buildDefaultUserClaims(userInfo.getFirstname(), userInfo.getLastname(),
                            userInfo.getEmailAddress(), false);
            if (StringUtils.isNotEmpty(userInfo.getPassword())) {
                // Decoding Base64 encoded password
                userStoreManager.updateCredentialByAdmin(username,
                        userInfo.getPassword());
                log.debug("User credential of username: " + username + " has been changed");
            }
            List<String> currentRoles = this.getFilteredRoles(userStoreManager, username);

            List<String> newRoles = new ArrayList<>();
            if (userInfo.getRoles() != null) {
                newRoles = Arrays.asList(userInfo.getRoles());
            }

            List<String> rolesToAdd = new ArrayList<>(newRoles);
            List<String> rolesToDelete = new ArrayList<>();

            for (String role : currentRoles) {
                if (newRoles.contains(role)) {
                    rolesToAdd.remove(role);
                } else {
                    rolesToDelete.add(role);
                }
            }
            rolesToDelete.remove(ROLE_EVERYONE);
            rolesToAdd.remove(ROLE_EVERYONE);
            userStoreManager.updateRoleListOfUser(username,
                    rolesToDelete.toArray(new String[rolesToDelete.size()]),
                    rolesToAdd.toArray(new String[rolesToAdd.size()]));
            userStoreManager.setUserClaimValues(username, defaultUserClaims, null);
            // Outputting debug message upon successful addition of user
            if (log.isDebugEnabled()) {
                log.debug("User by username: " + username + " was successfully updated.");
            }
            String stringRoles = new Gson().toJson(newRoles);
            log.info(
                    "User " + username + " updated",
                    userMgtContextBuilder
                            .setActionTag("UPDATE_USER")
                            .setUserStoreDomain(username.split("/")[0])
                            .setFirstName(userInfo.getFirstname())
                            .setLastName(userInfo.getLastname())
                            .setEmail(userInfo.getEmailAddress())
                            .setUserRoles(stringRoles)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(loggeduserName)
                            .build()
            );

            BasicUserInfo updatedUserInfo = this.getBasicUserInfo(username);
            return Response.ok().entity(updatedUserInfo).build();
        } catch (UserStoreException | DeviceManagementException e) {
            String msg = "Error occurred while trying to update user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private List<String> getFilteredRoles(UserStoreManager userStoreManager, String username)
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

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Override
    public Response removeUser(@QueryParam("username") String username, @QueryParam("domain") String domain) {
        boolean nameWithDomain = false;
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
            nameWithDomain = true;
        }
        String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        try {
            int deviceCount;
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by user: " + username + " does not exist for removal.");
                }
                String msg = "User by user: " + username + " does not exist for removal.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
            if (nameWithDomain) {
                deviceCount = deviceManagementService.getDeviceCount(username.split("/")[1]);
            } else {
                deviceCount = deviceManagementService.getDeviceCount(username);
            }
            if (deviceCount == 0) {
                userStoreManager.deleteUser(username);
                if (log.isDebugEnabled()) {
                    log.debug("User '" + username + "' was successfully removed.");
                }
                log.info(
                        "User " + username + " removed",
                        userMgtContextBuilder
                                .setActionTag("REMOVE_USER")
                                .setUserStoreDomain(domain)
                                .setTenantID(tenantId)
                                .setTenantDomain(tenantDomain)
                                .setUserName(username)
                                .build()
                );
                return Response.status(Response.Status.OK).build();
            } else {
                String msg = "Before deleting this user, ensure there are no devices assigned to the user. You can either remove the devices or change their owner through an update enrollment operation.";
                log.error(msg);
                return Response.status(409).entity(msg).build();
            }
        } catch (DeviceManagementException | UserStoreException e) {
            String msg = "Exception in trying to remove user by user: " + username;
            log.error(msg, e);
            return Response.status(400).entity(msg).build();
        }
    }

    @GET
    @Path("/roles")
    @Override
    public Response getRolesOfUser(@QueryParam("username") String username, @QueryParam("domain") String domain) {
        if (domain != null && !domain.isEmpty()) {
            username = domain + '/' + username;
        }
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("User by username: " + username + " does not exist for role retrieval.");
                }
                String msg = "User by username: " + username + " does not exist for role retrieval.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }

            RoleList result = new RoleList();
            result.setList(getFilteredRoles(userStoreManager, username));
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/list")
    @Override
    public Response getUsers(@QueryParam("filter") String filter, @HeaderParam("If-Modified-Since") String timestamp,
                             @QueryParam("offset") int offset, @QueryParam("limit") int limit,
                             @QueryParam("domain") String domain) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information");
        }

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (limit == 0) {
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }
        List<BasicUserInfo> userList, offsetList;
        String appliedFilter = ((filter == null) || filter.isEmpty() ? "*" : filter + "*");
        // to get whole set of users, appliedLimit is set to -1
        // by default, this whole set is limited to 100 - MaxUserNameListLength of user-mgt.xml
        int appliedLimit = -1;

        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();

            //As the listUsers function accepts limit only to accommodate offset we are passing offset + limit
            List<String> users = Arrays.asList(userStoreManager.listUsers(appliedFilter, appliedLimit));
            if (domain != null && !domain.isEmpty()) {
                users = getUsersFromDomain(domain, users);
            }
            userList = new ArrayList<>(users.size());
            BasicUserInfo user;
            for (String username : users) {
                if (Constants.APIM_RESERVED_USER.equals(username) || Constants.RESERVED_USER.equals(username) ||
                        io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants.IDN_REST_API_INVOKER_USER.equals(username)) {
                    continue;
                }
                user = getBasicUserInfo(username);
                userList.add(user);
            }

            int toIndex = offset + limit;
            int listSize = userList.size();
            int lastIndex = listSize - 1;

            if (offset <= lastIndex) {
                if (toIndex <= listSize) {
                    offsetList = userList.subList(offset, toIndex);
                } else {
                    offsetList = userList.subList(offset, listSize);
                }
            } else {
                offsetList = new ArrayList<>();
            }
            BasicUserInfoList result = new BasicUserInfoList();
            result.setList(offsetList);
            result.setCount(userList.size());

            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException | DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/search")
    @Override
    public Response getUsers(@QueryParam("username") String username, @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName, @QueryParam("emailAddress") String emailAddress,
            @HeaderParam("If-Modified-Since") String timestamp, @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {

        if (RequestValidationUtil.isNonFilterRequest(username,firstName, lastName, emailAddress)) {
            return getUsers(null, timestamp, offset, limit, null);
        }

        RequestValidationUtil.validatePaginationParameters(offset, limit);

        if(log.isDebugEnabled()) {
            log.debug("Filtering users - filter: {username: " + username  +", firstName: " + firstName + ", lastName: "
                    + lastName + ", emailAddress: " + emailAddress + "}");
        }

        if (limit == 0) {
            limit = Constants.DEFAULT_PAGE_LIMIT;
        }

        List<BasicUserInfo> filteredUserList = new ArrayList<>();
        List<String> commonUsers = null, tempList;

        try {
            if (StringUtils.isNotEmpty(username)) {
                commonUsers = getUserList(null, "*" + username + "*");
            }
            if (commonUsers != null) {
                commonUsers.remove(Constants.APIM_RESERVED_USER);
                commonUsers.remove(Constants.RESERVED_USER);
                commonUsers.remove(io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants.IDN_REST_API_INVOKER_USER);
            }

            if (!skipSearch(commonUsers) && StringUtils.isNotEmpty(firstName)) {
                tempList = getUserList(Constants.USER_CLAIM_FIRST_NAME, "*" + firstName + "*");
                if (commonUsers == null) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            if (!skipSearch(commonUsers) && StringUtils.isNotEmpty(lastName)) {
                tempList = getUserList(Constants.USER_CLAIM_LAST_NAME, "*" + lastName + "*");
                if (commonUsers == null || commonUsers.size() == 0) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            if (!skipSearch(commonUsers) && StringUtils.isNotEmpty(emailAddress)) {
                tempList = getUserList(Constants.USER_CLAIM_EMAIL_ADDRESS, "*" + emailAddress + "*");
                if (commonUsers == null || commonUsers.size() == 0) {
                    commonUsers = tempList;
                } else {
                    commonUsers.retainAll(tempList);
                }
            }

            BasicUserInfo basicUserInfo;
            if (commonUsers != null) {
                for (String user : commonUsers) {
                    basicUserInfo = new BasicUserInfo();
                    basicUserInfo.setUsername(user);
                    basicUserInfo.setEmailAddress(getClaimValue(user, Constants.USER_CLAIM_EMAIL_ADDRESS));
                    basicUserInfo.setFirstname(getClaimValue(user, Constants.USER_CLAIM_FIRST_NAME));
                    basicUserInfo.setLastname(getClaimValue(user, Constants.USER_CLAIM_LAST_NAME));
                    basicUserInfo.setRemovable(isUserRemovable(user));
                    filteredUserList.add(basicUserInfo);
                }
            }

            int toIndex = offset + limit;
            int listSize = filteredUserList.size();
            int lastIndex = listSize - 1;

            List<BasicUserInfo> offsetList;
            if (offset <= lastIndex) {
                if (toIndex <= listSize) {
                    offsetList = filteredUserList.subList(offset, toIndex);
                } else {
                    offsetList = filteredUserList.subList(offset, listSize);
                }
            } else {
                offsetList = new ArrayList<>();
            }

            BasicUserInfoList result = new BasicUserInfoList();
            result.setList(offsetList);
            result.setCount(commonUsers != null ? commonUsers.size() : 0);

            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException | DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of users.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/count")
    @Override
    public Response getUserCount() {
        try {
            UserStoreCountRetriever userStoreCountRetrieverService = DeviceMgtAPIUtils.getUserStoreCountRetrieverService();
            RealmConfiguration secondaryRealmConfiguration = DeviceMgtAPIUtils.getUserRealm().getRealmConfiguration()
                    .getSecondaryRealmConfig();

            if (secondaryRealmConfiguration != null) {
                if (!secondaryRealmConfiguration.isPrimary() && !Constants.JDBC_USERSTOREMANAGER.
                        equals(secondaryRealmConfiguration.getUserStoreClass().getClass())) {
                    return getUserCountViaUserStoreManager();
                }
            }
            if (userStoreCountRetrieverService != null) {
                long count = userStoreCountRetrieverService.countUsers("");
                if (count != -1) {
                    BasicUserInfoList result = new BasicUserInfoList();
                    result.setCount(count);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
            }
        } catch (UserStoreCounterException e) {
            String msg =
                    "Error occurred while retrieving the count of users that exist within the current tenant";
            log.error(msg, e);
        } catch (UserStoreException e) {
            String msg =
                    "Error occurred while retrieving user stores.";
            log.error(msg, e);
        }
        return getUserCountViaUserStoreManager();
    }

    /**
     * This method returns the count of users using UserStoreManager.
     *
     * @return user count
     */
    private Response getUserCountViaUserStoreManager() {
        if (log.isDebugEnabled()) {
            log.debug("Getting the user count");
        }

        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            int userCount = userStoreManager.listUsers("*", -1).length;
            BasicUserInfoList result = new BasicUserInfoList();
            result.setCount(userCount);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the user count.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/checkUser")
    @Override
    public Response isUserExists(@QueryParam("username") String userName) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (userStoreManager.isExistingUser(userName)) {
                return Response.status(Response.Status.OK).entity(true).build();
            } else {
                return Response.status(Response.Status.OK).entity(false).build();
            }
        } catch (UserStoreException e) {
            String msg = "Error while retrieving the user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/search/usernames")
    @Override
    public Response getUserNames(@QueryParam("filter") String filter, @QueryParam("domain") String domain,
                                 @HeaderParam("If-Modified-Since") String timestamp,
                                 @QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        if (log.isDebugEnabled()) {
            log.debug("Getting the list of users with all user-related information using the filter : " + filter);
        }
        String userStoreDomain = Constants.PRIMARY_USER_STORE;
        if (domain != null && !domain.isEmpty()) {
            userStoreDomain = domain;
        }
        if (limit == 0){
            //If there is no limit is passed, then return all.
            limit = -1;
        }
        List<UserInfo> userList;
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            String[] users;
            if (userStoreDomain.equals("all")) {
                users = userStoreManager.listUsers(filter + "*", limit);
            } else {
                users = userStoreManager.listUsers(userStoreDomain + "/" + filter + "*", limit);
            }
            userList = new ArrayList<>();
            UserInfo user;
            for (String username : users) {
                if (Constants.APIM_RESERVED_USER.equals(username) || Constants.RESERVED_USER.equals(username) ||
                        io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants.IDN_REST_API_INVOKER_USER.equals(username)) {
                    continue;
                }
                user = new UserInfo();
                user.setUsername(username);
                user.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
                user.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
                user.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
                userList.add(user);
            }
            return Response.status(Response.Status.OK).entity(userList).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of users using the filter : " + filter;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/credentials")
    @Override
    public Response resetPassword(OldPasswordResetWrapper credentials) {
        return CredentialManagementResponseBuilder.buildChangePasswordResponse(credentials);
    }


    @POST
    @Path("/send-invitation")
    @Produces({MediaType.APPLICATION_JSON})
    public Response inviteExistingUsersToEnrollDevice(DeviceEnrollmentInvitation deviceEnrollmentInvitation) {
        if (deviceEnrollmentInvitation.getUsernames() == null || deviceEnrollmentInvitation.getUsernames().isEmpty()) {
            String msg = "Error occurred while validating list of user-names. User-names cannot be empty.";
            log.error(msg);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending device enrollment invitation mail to existing user/s.");
        }
        OTPManagementService oms = DeviceMgtAPIUtils.getOTPManagementService();
        try {
            oms.sendDeviceEnrollmentInvitationMail(deviceEnrollmentInvitation);
        } catch (OTPManagementException e) {
            String msg = "Error occurred while generating OTP and inviting user/s to enroll their device/s.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }

    @POST
    @Path("/enrollment-invite")
    @Override
    public Response inviteToEnrollDevice(EnrollmentInvitation enrollmentInvitation) {
        if (log.isDebugEnabled()) {
            log.debug("Sending enrollment invitation mail to existing user.");
        }
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Set<String> recipients = new HashSet<>();
            recipients.addAll(enrollmentInvitation.getRecipients());
            Properties props = new Properties();
            String username = DeviceMgtAPIUtils.getAuthenticatedUser();
            String firstName = getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME);
            String lastName = getClaimValue(username, Constants.USER_CLAIM_LAST_NAME);
            if (firstName == null) {
                firstName = username;
            }
            if (lastName == null) {
                lastName = "";
            }
            props.setProperty("first-name", firstName);
            props.setProperty("last-name", lastName);
            props.setProperty("device-type", enrollmentInvitation.getDeviceType());
            EmailMetaInfo metaInfo = new EmailMetaInfo(recipients, props);
            dms.sendEnrolmentInvitation(getEnrollmentTemplateName(enrollmentInvitation.getDeviceType()), metaInfo);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while inviting user to enrol their device";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting claim values to invite user";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while sending the email invitations. Mail server not configured.";
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }

    @POST
    @Path("jit-enrollment-invite")
    @Override
    public Response inviteExternalUsers(JITEnrollmentInvitation jitEnrollmentInvitation) {
        if (jitEnrollmentInvitation.getMailProfiles() == null || jitEnrollmentInvitation.getMailProfiles().isEmpty()) {
            String msg = "Error occurred while validating mail profiles. Mail profiles cannot be empty";
            log.error(msg);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).setCode(HttpStatus.SC_BAD_REQUEST).
                            build());
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String inviteBy = DeviceMgtAPIUtils.getAuthenticatedUser();
        try {
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            for (InvitationMailProfile mailProfile : jitEnrollmentInvitation.getMailProfiles()) {
                Properties props = new Properties();
                props.setProperty("username", mailProfile.getUsername());
                props.setProperty("tenant-domain", tenantDomain);
                props.setProperty("sp", jitEnrollmentInvitation.getSp());
                props.setProperty("ownership-type", jitEnrollmentInvitation.getOwnershipType());
                props.setProperty("device-type", jitEnrollmentInvitation.getDeviceType());
                props.setProperty("invite-by", inviteBy);
                Set<String> recipients = new HashSet<>();
                recipients.add(mailProfile.getMail());
                EmailMetaInfo metaInfo = new EmailMetaInfo(recipients, props);
                dms.sendEnrolmentInvitation(getTemplateName(jitEnrollmentInvitation.getDeviceType(),
                        "jit-enrollment-invitation", "-"), metaInfo);
            }
        } catch (DeviceManagementException ex) {
            String msg = "Error occurred while inviting user to enroll their device";
            log.error(msg, ex);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ConfigurationManagementException ex) {
            String msg = "Error occurred while sending the email invitations. Mail server not configured.";
            log.error(msg, ex);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (NoSuchFileException ex) {
            String msg = "Error occurred while retrieving email template";
            log.error(msg, ex);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
    }

    @POST
    @Path("/validate")
    @Override
    public Response validateUser(Credential credential) {
        try {
            credential.validateRequest();
            RealmService realmService = DeviceMgtAPIUtils.getRealmService();
            String tenant = credential.getTenantDomain();
            int tenantId;
            if (tenant == null || tenant.trim().isEmpty()) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = realmService.getTenantManager().getTenantId(tenant);
            }
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                String msg = "Error occurred while validating the user. Invalid tenant domain " + tenant;
                log.error(msg);
                throw new BadRequestException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                                .build());
            }
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            JsonObject result = new JsonObject();
            if (userRealm.getUserStoreManager().authenticate(credential.getUsername(), credential.getPassword())) {
                result.addProperty("valid", true);
                return Response.status(Response.Status.OK).entity(result).build();
            } else {
                result.addProperty("valid", false);
                return Response.status(Response.Status.OK).entity(result).build();
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving user store to validate user";
            log.error(msg, e);
            return Response.serverError().entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        }
    }

    @GET
    @Override
    @Path("/device/activities")
    public Response getActivities(
            @QueryParam("since") String since,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        long ifModifiedSinceTimestamp;
        long sinceTimestamp;
        long timestamp = 0;
        boolean isIfModifiedSinceSet = false;
        String initiatedBy;
        if (log.isDebugEnabled()) {
            log.debug("getActivities since: " + since + " , offset: " + offset + " ,limit: " + limit + " ,"
                    + "ifModifiedSince: " + ifModifiedSince);
        }
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifSinceDate;
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_SIMPLE_DATE_FORMAT);
            try {
                ifSinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [If-Modified-Since] header";
                return Response.status(400).entity(msg).build();
            }
            ifModifiedSinceTimestamp = ifSinceDate.getTime();
            isIfModifiedSinceSet = true;
            timestamp = ifModifiedSinceTimestamp / 1000;
        } else if (since != null && !since.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_SIMPLE_DATE_FORMAT);
            try {
                sinceDate = format.parse(since);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [since] filter";
                return Response.status(400).entity(msg).build();
            }
            sinceTimestamp = sinceDate.getTime();
            timestamp = sinceTimestamp / 1000;
        }

        if (timestamp == 0) {
            //If timestamp is not sent by the user, a default value is set, that is equal to current time-12 hours.
            long time = System.currentTimeMillis() / 1000;
            timestamp = time - 42300;
        }
        if (log.isDebugEnabled()) {
            log.debug("getActivities final timestamp " + timestamp);
        }

        List<Activity> activities;
        int count;
        ActivityList activityList = new ActivityList();
        DeviceManagementProviderService dmService;

        initiatedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get activities.");
            }
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activities = dmService.getActivitiesUpdatedAfterByUser(timestamp, initiatedBy, limit, offset);
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get activity count with timestamp and user.");
            }
            count = dmService.getActivityCountUpdatedAfterByUser(timestamp, initiatedBy);
            if (log.isDebugEnabled()) {
                log.debug("Activity count: " + count);
            }

            activityList.setList(activities);
            activityList.setCount(count);
            if ((activities == null || activities.isEmpty()) && isIfModifiedSinceSet) {
                return Response.notModified().build();
            }
            return Response.ok().entity(activityList).build();
        } catch (OperationManagementException e) {
            String msg =
                    "Error Response occurred while fetching the activities updated after given time stamp for the user "
                            + initiatedBy + ".";
            log.error(msg, e);
            return Response.serverError().entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build())
                    .build();
        }
    }

    @PUT
    @Override
    @Path("/claims")
    public Response updateUserClaimsForDevices(
            @QueryParam("username") String username, JsonArray deviceList,
            @QueryParam("domain") String domain) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            ClaimMetadataManagementAdminService
                    claimMetadataManagementAdminService = new ClaimMetadataManagementAdminService();
            //Get all available claim URIs
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            //Check they contains a claim attribute for external devices
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                List<ClaimPropertyDTO> claimPropertyDTOList = new ArrayList<>();
                claimPropertyDTOList
                        .add(DeviceMgtAPIUtils.buildClaimPropertyDTO
                                (Constants.ATTRIBUTE_DISPLAY_NAME, Constants.EXTERNAL_DEVICE_CLAIM_DISPLAY_NAME));
                claimPropertyDTOList
                        .add(DeviceMgtAPIUtils.buildClaimPropertyDTO
                                (Constants.ATTRIBUTE_DESCRIPTION, Constants.EXTERNAL_DEVICE_CLAIM_DESCRIPTION));

                LocalClaimDTO localClaimDTO = new LocalClaimDTO();
                localClaimDTO.setLocalClaimURI(Constants.USER_CLAIM_DEVICES);
                localClaimDTO.setClaimProperties(claimPropertyDTOList.toArray(
                        new ClaimPropertyDTO[claimPropertyDTOList.size()]));

                AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
                attributeMappingDTO.setAttributeName(Constants.DEVICES);
                attributeMappingDTO.setUserStoreDomain(domain);
                localClaimDTO.setAttributeMappings(new AttributeMappingDTO[]{attributeMappingDTO});

                claimMetadataManagementAdminService.addLocalClaim(localClaimDTO);
            }
            Map<String, String> userClaims =
                    this.buildExternalDevicesUserClaims(username, domain, deviceList, userStoreManager);
            userStoreManager.setUserClaimValues(username, userClaims, domain);
            return Response.status(Response.Status.OK).entity(userClaims).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while updating external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ClaimMetadataException e) {
            String msg = "Error occurred while adding claim attribute";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/claims")
    public Response getUserClaimsForDevices(
            @QueryParam("username") String username, @QueryParam("domain") String domain) {
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            Map<String, String> claims = new HashMap<>();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                if (log.isDebugEnabled()) {
                    log.debug("Claim attribute for external device doesn't exist.");
                }
                return Response.status(Response.Status.OK).entity(claims).build();
            }
            String[] claimArray = {Constants.USER_CLAIM_DEVICES};
            claims = userStoreManager.getUserClaimValues(username, claimArray, domain);
            return Response.status(Response.Status.OK).entity(claims).build();
        } catch (UserStoreException e) {
            String msg = "Error  occurred while retrieving external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Path("/claims")
    public Response deleteUserClaimsForDevices(
            @QueryParam("username") String username, @QueryParam("domain") String domain) {
        try {
            String[] claimArray = new String[1];
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (domain != null && !domain.isEmpty()) {
                username = domain + Constants.FORWARD_SLASH + username;
            } else {
                RealmConfiguration realmConfiguration = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration();
                domain = realmConfiguration
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (!StringUtils.isBlank(domain)) {
                    username = domain + Constants.FORWARD_SLASH + username;
                }
            }
            if (!userStoreManager.isExistingUser(username)) {
                String msg = "User by username: " + username + " does not exist.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            String[] allUserClaims = userStoreManager.getClaimManager().getAllClaimUris();
            if (!Arrays.asList(allUserClaims).contains(Constants.USER_CLAIM_DEVICES)) {
                if (log.isDebugEnabled()) {
                    log.debug("Claim attribute for external device doesn't exist.");
                }
                return Response.status(Response.Status.OK).entity(claimArray).build();
            }
            claimArray[0] = Constants.USER_CLAIM_DEVICES;
            userStoreManager.deleteUserClaimValues(
                    username,
                    claimArray,
                    domain);
            return Response.status(Response.Status.OK).entity(claimArray).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting external device claims of the user '" + username + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/current-user/permissions")
    public Response getPermissionsOfUser() {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                String message = "User by username: " + username + " does not exist for permission retrieval.";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();
            }
            // Get a list of roles which the user assigned to
            List<String> roles = getFilteredRoles(userStoreManager, username);
            List<String> permissions = new ArrayList<>();
            UserRealm userRealm = DeviceMgtAPIUtils.getUserRealm();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            // Get permissions for each role
            for (String roleName : roles) {
                try {
                    permissions.addAll(getPermissionsListFromRole(roleName, userRealm, tenantId));
                } catch (UserAdminException e) {
                    String message = "Error occurred while retrieving the permissions of role '" + roleName + "'";
                    log.error(message, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                            .build();
                }
            }
            PermissionList permissionList = new PermissionList();
            permissionList.setList(permissions);
            return Response.status(Response.Status.OK).entity(permissionList).build();
        } catch (UserStoreException e) {
            String message = "Error occurred while trying to retrieve roles of the user '" + username + "'";
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(message).build())
                    .build();
        }
    }

    private Map<String, String> buildDefaultUserClaims(String firstName, String lastName, String emailAddress,
                                                       boolean isFresh) {
        Map<String, String> defaultUserClaims = new HashMap<>();
        defaultUserClaims.put(Constants.USER_CLAIM_FIRST_NAME, firstName);
        defaultUserClaims.put(Constants.USER_CLAIM_LAST_NAME, lastName);
        defaultUserClaims.put(Constants.USER_CLAIM_EMAIL_ADDRESS, emailAddress);
        if (isFresh) {
            defaultUserClaims.put(Constants.USER_CLAIM_CREATED, String.valueOf(Instant.now().getEpochSecond()));
        } else {
            defaultUserClaims.put(Constants.USER_CLAIM_MODIFIED, String.valueOf(Instant.now().getEpochSecond()));
        }
        if (log.isDebugEnabled()) {
            log.debug("Default claim map is created for new user: " + defaultUserClaims.toString());
        }
        return defaultUserClaims;
    }

    /**
     * This method is used to build String map for user claims with updated external device details
     *
     * @param username username of the particular user
     * @param domain domain of the particular user
     * @param deviceList Array of external device details
     * @param userStoreManager {@link UserStoreManager} instance
     * @return String map
     * @throws UserStoreException If any error occurs while calling into UserStoreManager service
     */
    private Map<String, String> buildExternalDevicesUserClaims(
            String username,
            String domain,
            JsonArray deviceList,
            UserStoreManager userStoreManager) throws UserStoreException {
        Map<String, String> userClaims;
        String[] claimArray = {
                Constants.USER_CLAIM_FIRST_NAME,
                Constants.USER_CLAIM_LAST_NAME,
                Constants.USER_CLAIM_EMAIL_ADDRESS,
                Constants.USER_CLAIM_MODIFIED
        };
        userClaims = userStoreManager.getUserClaimValues(username, claimArray, domain);
        if (userClaims.containsKey(Constants.USER_CLAIM_DEVICES)) {
            userClaims.replace(Constants.USER_CLAIM_DEVICES, deviceList.toString());
        } else {
            userClaims.put(Constants.USER_CLAIM_DEVICES, deviceList.toString());
        }
        if (log.isDebugEnabled()) {
            log.debug("Claim map is created for user: " + username + ", claims:" + userClaims.toString());
        }
        return userClaims;
    }

    private String generateInitialUserPassword() {
        int passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        String lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numericCharset = "0123456789";
        SecureRandom randomGenerator = new SecureRandom();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword.append(
                    totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }

    private BasicUserInfo getBasicUserInfo(String username) throws UserStoreException, DeviceManagementException {
        BasicUserInfo userInfo = new BasicUserInfo();
        userInfo.setUsername(username);
        userInfo.setEmailAddress(getClaimValue(username, Constants.USER_CLAIM_EMAIL_ADDRESS));
        userInfo.setFirstname(getClaimValue(username, Constants.USER_CLAIM_FIRST_NAME));
        userInfo.setLastname(getClaimValue(username, Constants.USER_CLAIM_LAST_NAME));
        userInfo.setCreatedDate(getClaimValue(username, Constants.USER_CLAIM_CREATED));
        userInfo.setModifiedDate(getClaimValue(username, Constants.USER_CLAIM_MODIFIED));
        userInfo.setRemovable(isUserRemovable(username));
        return userInfo;
    }

    /**
     * Check if the user can be removed or not
     * @param username Username of the user
     * @return True when user can be removed, otherwise false
     * @throws DeviceManagementException Throws when error occurred while getting device count
     */
    private boolean isUserRemovable(String username) throws DeviceManagementException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        return deviceManagementProviderService.getDeviceCount(username.contains("/") ? username.split("/")[1] : username) == 0;
    }

    private String getClaimValue(String username, String claimUri) throws UserStoreException {
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, claimUri, null);
    }

    private String getEnrollmentTemplateName(String deviceType) {
        String templateName = deviceType + "-enrollment-invitation";
        File template = new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "resources" + File.separator + "email-templates" + File.separator + templateName
                + ".vm");
        if (template.exists()) {
            return templateName;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The template that is expected to use is not available. Therefore, using default template.");
            }
        }
        return DeviceManagementConstants.EmailAttributes.DEFAULT_ENROLLMENT_TEMPLATE;
    }

    private String getTemplateName(String deviceType, String prefix, String separator) throws NoSuchFileException {
        String templateName = deviceType + separator + prefix;
        List<String> templatePathSegments =
                Arrays.asList(CarbonUtils.getCarbonHome(), "repository", "resources", "email-templates", templateName + ".vm");
        File template = new File(String.join(File.separator, templatePathSegments));
        if (template.exists()) {
            return templateName;
        }

        String defaultTemplateName = "default" + separator + prefix;
        List<String> defaultTemplatePathSegments =
                Arrays.asList(CarbonUtils.getCarbonHome(), "repository", "resources", "email-templates", defaultTemplateName + ".vm");
        File defaultTemplate = new File(String.join(File.separator, defaultTemplatePathSegments));

        if (defaultTemplate.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("The template that is expected to use is not available. Therefore, using default template.");
            }
            return defaultTemplateName;
        }

        throw new NoSuchFileException("Didn't found template file for " + templateName);
    }

    /**
     * Searches users which matches a given filter based on a claim
     *
     * @param claim the claim value to apply the filter. If <code>null</code> users will be filtered by username.
     * @param filter the search query.
     * @return <code>List<String></code> of users which matches.
     * @throws UserStoreException If unable to search users.
     */
    private ArrayList<String> getUserList(String claim, String filter) throws UserStoreException {
        String defaultFilter = "*";

        org.wso2.carbon.user.core.UserStoreManager userStoreManager =
                (org.wso2.carbon.user.core.UserStoreManager) DeviceMgtAPIUtils.getUserStoreManager();

        String appliedFilter = filter + defaultFilter;

        String[] users;
        if (log.isDebugEnabled()) {
            log.debug("Searching Users - claim: " + claim + " filter: " + appliedFilter);
        }
        if (StringUtils.isEmpty(claim)) {
            users = userStoreManager.listUsers(appliedFilter, -1);
        } else {
            users = userStoreManager.getUserList(claim, appliedFilter, null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Returned user count: " + users.length);
        }

        return new ArrayList<>(Arrays.asList(users));
    }

    /**
     * User search provides an AND search result and if either of the filter returns an empty set of users, there is no
     * need to carry on the search further. This method decides whether to carry on the search or not.
     *
     * @param commonUsers current filtered user list.
     * @return <code>true</code> if further search is needed.
     */
    private boolean skipSearch(List<String> commonUsers) {
        return commonUsers != null && commonUsers.size() == 0;
    }

    /**
     * Returns a list of permissions of a given role
     * @param roleName name of the role
     * @param tenantId the user's tenetId
     * @param userRealm user realm of the tenant
     * @return list of permissions
     * @throws UserAdminException If unable to get the permissions
     */
    private static List<String> getPermissionsListFromRole(String roleName, UserRealm userRealm, int tenantId)
            throws UserAdminException {
        org.wso2.carbon.user.core.UserRealm userRealmCore;
        try {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        } catch (ClassCastException e) {
            String message = "Provided UserRealm object is not an instance of org.wso2.carbon.user.core.UserRealm";
            log.error(message, e);
            throw new UserAdminException(message, e);
        }
        UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
        List<String> permissionsList = new ArrayList<>();
        final UIPermissionNode rolePermissions = userRealmProxy.getRolePermissions(roleName, tenantId);
        DeviceMgtAPIUtils.iteratePermissions(rolePermissions, permissionsList);
        return permissionsList;
    }

    /**
     * Returns a Response with the list of user stores available for a tenant
     * @return list of user stores
     * @throws UserStoreException If unable to search for user stores
     */
    @GET
    @Path("/user-stores")
    @Override
    public Response getUserStores() {
        String domain;
        List<String> userStores = new ArrayList<>();
        UserStoreList userStoreList = new UserStoreList();
        try {
            RealmConfiguration realmConfiguration = DeviceMgtAPIUtils.getUserRealm().getRealmConfiguration();
            userStores.add(realmConfiguration
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));

            while (realmConfiguration != null) {
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
                if (realmConfiguration != null) {
                    domain = realmConfiguration
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    userStores.add(domain);
                } else {
                    break;
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving user stores.";
            log.error(msg, e);
        }
        userStoreList.setList(userStores);
        userStoreList.setCount(userStores.size());
        return Response.status(Response.Status.OK).entity(userStoreList).build();
    }

    /**
     * Iterates through the list of all users and returns a list of users from the specified user store domain
     * @param domain user store domain name
     * @param users list of all users from UserStoreManager
     * @return list of users from specified user store domain
     */
    public List<String> getUsersFromDomain(String domain, List<String> users) {
        List<String> userList = new ArrayList<>();
        for(String username : users) {
            String[] domainName = username.split("/");
            if(domain.equals(Constants.PRIMARY_USER_STORE) && domainName.length == 1) {
                userList.add(username);
            } else if (domainName[0].equals(domain) && domainName.length > 1) {
                userList.add(username);
            }
        }
        return userList;
    }
}
