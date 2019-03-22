/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionType;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ValidationException;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManger;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private LifecycleStateDAO lifecycleStateDAO;
    private LifecycleStateManger lifecycleStateManger;

    public ApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManger = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
    }

    /***
     * The responsbility of this method is the creating an application.
     * @param application Application that need to be created.
     * @return {@link Application}
     * @throws RequestValidatingException if application creating request is invalid, returns {@link RequestValidatingException}
     * @throws ApplicationManagementException Catch all other throwing exceptions and returns {@link ApplicationManagementException}
     */
    @Override public Application createApplication(Application application)
            throws RequestValidatingException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        application.setUser(new User(userName, tenantId));
        if (log.isDebugEnabled()) {
            log.debug("Create Application received for the tenant : " + tenantId + " From" + " the user : " + userName);
        }
        validateAppCreatingRequest(application, tenantId);
        //todo throw different exception
        validateAppReleasePayload(application.getApplicationReleases().get(0));
        DeviceType deviceType;
        ApplicationRelease applicationRelease;
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        try {
            // Getting the device type details to get device type ID for internal mappings
            deviceType = Util.getDeviceManagementService().getDeviceType(application.getDeviceType());

            ConnectionManagerUtil.beginDBTransaction();
            if (deviceType == null) {
                log.error("Device type is not matched with application type");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            }
            if (!application.getUnrestrictedRoles().isEmpty()) {
                application.setIsRestricted(true);
            }

            // Insert to application table
            int appId = this.applicationDAO.createApplication(application, deviceType.getId());

            if (appId == -1) {
                log.error("Application creation is Failed");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("New Application entry added to AP_APP table. App Id:" + appId);
                }
                if (!application.getTags().isEmpty()) {
                    this.applicationDAO.addTags(application.getTags(), appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New tags entry added to AP_APP_TAG table. App Id:" + appId);
                    }
                }
                if (application.getIsRestricted()) {
                    this.visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New restricted roles to app ID mapping added to AP_UNRESTRICTED_ROLE table."
                                + " App Id:" + appId);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Creating a new release. App Id:" + appId);
                }
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease = this.applicationReleaseDAO.createRelease(applicationRelease, appId, tenantId);

                if (log.isDebugEnabled()) {
                    log.debug("Changing lifecycle state. App Id:" + appId);
                }
                LifecycleState lifecycleState = getLifecycleStateInstant(AppLifecycleState.CREATED.toString(),
                        AppLifecycleState.CREATED.toString());
                this.lifecycleStateDAO.addLifecycleState(lifecycleState, appId, applicationRelease.getUuid(), tenantId);
                applicationRelease.setLifecycleState(lifecycleState);
                applicationReleases.add(applicationRelease);
                application.setApplicationReleases(applicationReleases);

                ConnectionManagerUtil.commitDBTransaction();
            }
            return application;
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while getting device type id of " + application.getType(), e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding lifecycle state. application name: " + application.getName()
                            + " application type: is " + application.getType(), e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding application or application release. application name: " + application
                            .getName() + " application type: " + application.getType(), e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException("Error occured while getting database connection. ", e);
        } catch (VisibilityManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding unrestricted roles. application name: " + application.getName()
                            + " application type: " + application.getType(), e);
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException("Error occured while disabling AutoCommit. ", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList;
        List<Application> apps;
        List<ApplicationRelease> releases;

        filter = validateFilter(filter);
        if (filter == null) {
            throw new ApplicationManagementException("Filter validation failed, Please verify the request payload");
        }

        try {
            if (filter.getDeviceType() != null ) {
                boolean isValidDeviceType = false;
                String deviceType = filter.getDeviceType().getName();
                List<DeviceType> deviceTypes = Util.getDeviceManagementService().getDeviceTypes();
                for (DeviceType dt : deviceTypes) {
                    if (dt.getName().equals(deviceType)) {
                        filter.getDeviceType().setId(dt.getId());
                        isValidDeviceType = true;
                        break;
                    }
                }
                if (!isValidDeviceType) {
                    throw new BadRequestException("Invalid device type is found, device type is " + deviceType);
                }
            }
            ConnectionManagerUtil.openDBConnection();
//            todo modify this logic, join app n release tables
            applicationList = applicationDAO.getApplications(filter, tenantId);
            apps = applicationList.getApplications();
            for ( Application app : apps){
                if (AppLifecycleState.REMOVED.toString().equals(app.getStatus())){
                    apps.remove(app);
                }
            }
            applicationList.setApplications(apps);
            if (applicationList.getApplications() != null && !applicationList
                    .getApplications().isEmpty()) {
                if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                    applicationList = getRoleRestrictedApplicationList(applicationList, userName);
                }
                for (Application application : applicationList.getApplications()) {
                    releases = getReleases(application, filter.getCurrentAppReleaseState());
                    application.setApplicationReleases(releases);
                }
            }
            return applicationList;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while checking whether the user " + userName + " of tenant " + tenantId
                            + " has the publisher permission", e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "DAO exception while getting applications for the user " + userName + " of tenant " + tenantId, e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException(
                    "Error Occured when getting device type instance for " + filter.getDeviceType());
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public ApplicationRelease createRelease(int applicationId, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        validateAppReleasePayload(applicationRelease);
        if (log.isDebugEnabled()) {
            log.debug("Application release request is received for the application id: " + applicationId);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Application existingApplication = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (existingApplication == null) {
                throw new NotFoundException("Couldn't find application for the application Id: " + applicationId);
            }

            // todo check whether admin or app creator.
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                String msg = "You don't have permission to create application release for the application id: "
                        + applicationId
                        + ". In order to create ann new application realse, you should be either ADMIN user or the application created user.";
                throw new ForbiddenException(msg);
            }
            if (!existingApplication.getUnrestrictedRoles().isEmpty() && !isRoleExists(
                    existingApplication.getUnrestrictedRoles(), userName)) {
                String msg = "Application is visible to limited roles and those roles are not assigned to " + userName;
                throw new ApplicationManagementException(msg);
            }
            if (this.applicationReleaseDAO
                    .verifyReleaseExistenceByHash(applicationId, applicationRelease.getAppHashValue(), tenantId)) {
                throw new BadRequestException("Application release exists for the application Id: " + applicationId
                        + " and uploaded binary file");
            }
            String packageName = this.applicationReleaseDAO.getPackageName(applicationId, tenantId);
            if (packageName != null && !packageName.equals(applicationRelease.getPackageName())) {
                throw new BadRequestException(
                        "Package name in the payload is different from the existing package name of other application releases.");
            }
            applicationRelease = this.applicationReleaseDAO
                    .createRelease(applicationRelease, existingApplication.getId(), tenantId);
            LifecycleState lifecycleState = getLifecycleStateInstant(AppLifecycleState.CREATED.toString(),
                    AppLifecycleState.CREATED.toString());
            this.lifecycleStateDAO
                    .addLifecycleState(lifecycleState, applicationId, applicationRelease.getUuid(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
            return applicationRelease;
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while staring application release creating transaction for application Id: "
                            + applicationId, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while adding application release into IoTS app management Application id of the "
                            + "application release: " + applicationId, e);

        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while adding new application release lifecycle state to the application release: "
                            + applicationId, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while adding new application release for application " + applicationId, e);
        } catch (UserStoreException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred whecn checking whether user is admin user or not. Application release: "
                            + applicationId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Application getApplicationById(int appId, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationById(appId, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application Id: " + appId);
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, state);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }
            applicationReleases = getReleases(application, state);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application id " + appId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Application getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByUUID(uuid, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application release UUID:: " + uuid);
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, state);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }
            applicationReleases = getReleases(application, state);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application release UUID " + uuid);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private boolean isRoleExists(Collection<String> unrestrictedRoleList, String userName) throws UserStoreException {
        String[] roleList;
        roleList = getRolesOfUser(userName);
        for (String unrestrictedRole : unrestrictedRoleList) {
            for (String role : roleList) {
                if (unrestrictedRole.equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getRolesOfUser(String userName) throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String[] roleList = {};
        if (userRealm != null) {
            roleList = userRealm.getUserStoreManager().getRoleListOfUser(userName);
        } else {
            log.error("role list is empty of user :" + userName);
        }
        return roleList;
    }

    public Application getApplication(String appType, String appName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplication(appName, appType, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, null);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }

            applicationReleases = getReleases(application, null);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application name " + appName);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByRelease(appReleaseUUID, tenantId);

            if (application.getUnrestrictedRoles().isEmpty() || isRoleExists(application.getUnrestrictedRoles(),
                    userName)) {
                return application;
            }
            return null;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application UUID " + appReleaseUUID);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    //    todo rethink about this method
    private List<ApplicationRelease> getReleases(Application application, String releaseState)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<ApplicationRelease> applicationReleases;
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " + application
                    .toString());
        }
        applicationReleases = this.applicationReleaseDAO.getReleases(application.getId(), tenantId);
        for (ApplicationRelease applicationRelease : applicationReleases) {
            LifecycleState lifecycleState = null;
            try {
                lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            } catch (LifeCycleManagementDAOException e) {
                throw new ApplicationManagementException(
                        "Error occurred while getting the latest lifecycle state for the application release UUID: "
                                + applicationRelease.getUuid(), e);
            }
            if (lifecycleState != null) {
                applicationRelease.setLifecycleState(lifecycleState);
            }
        }
        return filterAppReleaseByCurrentState(applicationReleases, releaseState);
    }

    private List<ApplicationRelease> filterAppReleaseByCurrentState(List<ApplicationRelease> applicationReleases,
            String state) {
        List<ApplicationRelease> filteredReleases = new ArrayList<>();

        if (state != null && !state.isEmpty()) {
            for (ApplicationRelease applicationRelease : applicationReleases) {
                if (state.equals(applicationRelease.getLifecycleState().getCurrentState())) {
                    filteredReleases.add(applicationRelease);
                }
            }

            if (AppLifecycleState.PUBLISHED.toString().equals(state) && filteredReleases.size() > 1) {
                log.warn("There are more than one application releases is found which is in PUBLISHED state");
                filteredReleases.sort((r1, r2) -> {
                    if (r1.getLifecycleState().getUpdatedAt().after(r2.getLifecycleState().getUpdatedAt())) {
                        return -1;
                    } else if (r2.getLifecycleState().getUpdatedAt().after(r1.getLifecycleState().getUpdatedAt())) {
                        return 1;
                    }
                    return 0;
                });
            }
            return filteredReleases;
        }
        return applicationReleases;
    }

    @Override public List<String> deleteApplication(int applicationId) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<String> storedLocations = new ArrayList<>();
        Application application;

        try {
            ConnectionManagerUtil.beginDBTransaction();
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);

            if (application == null) {
                throw new NotFoundException("Couldn't found an application for Application ID: " + applicationId);
            }

            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION) && !application
                    .getUnrestrictedRoles().isEmpty() && isRoleExists(application.getUnrestrictedRoles(), userName)) {
                throw new ForbiddenException(
                        "You don't have permission to delete this application. In order to delete an application you "
                                + "need to have required permission. Application ID: " + applicationId);
            }
            List<ApplicationRelease> applicationReleases = getReleases(application, null);
            if (log.isDebugEnabled()) {
                log.debug("Request is received to delete applications which are related with the application id "
                        + applicationId);
            }
            for (ApplicationRelease applicationRelease : applicationReleases) {
                LifecycleState appLifecycleState = this.lifecycleStateDAO
                        .getLatestLifeCycleState(applicationId, applicationRelease.getUuid());
                LifecycleState newAppLifecycleState = getLifecycleStateInstant(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                if (lifecycleStateManger.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState())) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                } else {
                    String currentState = appLifecycleState.getCurrentState();
                    List<String> lifecycleFlow = searchLifecycleStateFlow(currentState,
                            AppLifecycleState.REMOVED.toString());
                    for (String nextState : lifecycleFlow) {
                        LifecycleState lifecycleState = getLifecycleStateInstant(nextState, currentState);
                        if (lifecycleStateManger.isValidStateChange(currentState, nextState)) {
                            this.lifecycleStateDAO
                                    .addLifecycleState(lifecycleState, applicationId, applicationRelease.getUuid(),
                                            tenantId);
                        } else {
                            ConnectionManagerUtil.rollbackDBTransaction();
                            throw new ApplicationManagementException(
                                    "Can't delete application release which has the UUID:" + applicationRelease
                                            .getUuid()
                                            + " and its belongs to the  application which has application ID:"
                                            + applicationId + " You have to move the lifecycle state from "
                                            + currentState + " to acceptable state");
                        }
                        currentState = nextState;
                    }
                }
                storedLocations.add(applicationRelease.getAppHashValue());
            }
            this.applicationDAO.deleteApplication(applicationId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (UserStoreException e) {
            String msg = "Error occured while check whether current user has the permission to delete an application";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while changing the application lifecycle state into REMOVED state.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return storedLocations;
    }

    private List<String> searchLifecycleStateFlow(String start, String finish) throws ApplicationManagementException {
        Map<String, String> nextNodeMap = new HashMap<>();
        List<String> directions = new LinkedList<>();
        Queue<String> queue = new LinkedList<>();

        String currentNode = start;
        queue.add(currentNode);

        Set<String> visitedNodes = new HashSet<>();
        visitedNodes.add(currentNode);
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            if (currentNode.equals(finish)) {
                break;
            } else {
                Set<String> nextStates = lifecycleStateManger.getNextLifecycleStates(currentNode);
                if (nextStates.contains(finish)) {
                    queue = new LinkedList<>();
                    queue.add(finish);
                    nextNodeMap.put(currentNode, finish);
                } else {
                    for (String node : nextStates) {
                        if (!visitedNodes.contains(node)) {
                            queue.add(node);
                            visitedNodes.add(node);
                            nextNodeMap.put(currentNode, node);
                        }
                    }
                }
            }
        }

        //If all nodes are explored and the destination node hasn't been found.
        if (!currentNode.equals(finish)) {
            String errorMsg = "can't found a feasible path from " + start + " to " + finish;
            throw new ApplicationManagementException(errorMsg);
        }

        //Reconstruct path
        for (String node = start; node != null; node = nextNodeMap.get(node)) {
            if (!node.equals(start)) {
                directions.add(node);
            }
        }
        return directions;
    }

    @Override public String deleteApplicationRelease(int applicationId, String releaseUuid)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        try {
            ConnectionManagerUtil.beginDBTransaction();
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application ID: " + applicationId);
            }
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION) && !application
                    .getUnrestrictedRoles().isEmpty() && isRoleExists(application.getUnrestrictedRoles(), userName)) {
                throw new ForbiddenException(
                        "You don't have permission for deleting application release. Application id: " + applicationId
                                + " and release UUID: " + releaseUuid);
            }

            ApplicationRelease applicationRelease = this.applicationReleaseDAO
                    .getReleaseByIds(applicationId, releaseUuid, tenantId);
            if (applicationRelease == null) {
                throw new NotFoundException("Couldn't find an application release for application ID: " + applicationId
                        + " and release UUID: " + releaseUuid);
            }
            LifecycleState appLifecycleState = this.lifecycleStateDAO
                    .getLatestLifeCycleState(applicationId, releaseUuid);
            if (appLifecycleState == null) {
                throw new NotFoundException(
                        "Couldn't find an lifecycle sate for application ID: " + applicationId + " and UUID: "
                                + releaseUuid);
            }
            String currentState = appLifecycleState.getCurrentState();
            if (AppLifecycleState.DEPRECATED.toString().equals(currentState) || AppLifecycleState.REJECTED.toString()
                    .equals(currentState) || AppLifecycleState.UNPUBLISHED.toString().equals(currentState)) {
                LifecycleState newAppLifecycleState = getLifecycleStateInstant(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                if (lifecycleStateManger.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState())) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    List<String> lifecycleFlow = searchLifecycleStateFlow(currentState,
                            AppLifecycleState.REMOVED.toString());
                    for (String nextState : lifecycleFlow) {
                        LifecycleState lifecycleState = getLifecycleStateInstant(nextState, currentState);
                        if (lifecycleStateManger.isValidStateChange(currentState, nextState)) {
                            this.lifecycleStateDAO
                                    .addLifecycleState(lifecycleState, applicationId, applicationRelease.getUuid(),
                                            tenantId);
                        } else {
                            ConnectionManagerUtil.rollbackDBTransaction();
                            throw new ApplicationManagementException(
                                    "Can't delete the application release, You have to move the "
                                            + "lifecycle state from " + currentState + " to " + nextState);
                        }
                        currentState = nextState;
                    }
                }
            } else {
                throw new ApplicationManagementException(
                        "Can't delete the application release, You have to move the " + "lifecycle state from "
                                + currentState + " to acceptable " + "state");
            }
            return applicationRelease.getAppHashValue();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementDAOException(
                    "Error ocured when getting application data or application release data for application id of "
                            + applicationId + " application release UUID of the " + releaseUuid);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured when deleting application release for application ID of " + applicationId
                            + " and application release UUID of " + releaseUuid, e);
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "Error occured when checking permission for executing application release update. Application ID: "
                            + applicationId + " and Application UUID: " + releaseUuid);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * To check whether current user has the permission to do some secured operation.
     *
     * @param username   Name of the User.
     * @param tenantId   ID of the tenant.
     * @param permission Permission that need to be checked.
     * @return true if the current user has the permission, otherwise false.
     * @throws UserStoreException UserStoreException
     */
    private boolean isAdminUser(String username, int tenantId, String permission) throws UserStoreException {
        UserRealm userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        return userRealm != null && userRealm.getAuthorizationManager() != null && userRealm.getAuthorizationManager()
                .isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                        CarbonConstants.UI_PERMISSION_ACTION);
    }

    /**
     * To validate the application creating request
     *
     * @param application Application that need to be created
     * @throws RequestValidatingException Validation Exception
     */
    private void validateAppCreatingRequest(Application application, int tenantId) throws RequestValidatingException {

        Boolean isValidApplicationType;
        Filter filter = new Filter();
        try {
            filter.setFullMatch(true);
            filter.setAppName(application.getName().trim());
            filter.setOffset(0);
            filter.setLimit(1);
            if (application.getName() == null) {
                throw new RequestValidatingException("Application name cannot be empty");
            }
            if (application.getUser() == null || application.getUser().getUserName() == null
                    || application.getUser().getTenantId() == -1) {
                throw new RequestValidatingException("Username and tenant Id cannot be empty");
            }
            if (application.getAppCategory() == null) {
                throw new RequestValidatingException("Application category can't be empty");
            }

            isValidApplicationType = isValidAppType(application.getType());

            if (!isValidApplicationType) {
                throw new RequestValidatingException(
                        "App Type contains in the application creating payload doesn't match with supported app types");
            }

            if (application.getApplicationReleases().size() > 1) {
                throw new RequestValidatingException(
                        "Invalid payload. Application creating payload should contains one application release, but "
                                + "the payload contains more than one");
            }

            //Check whether application is already existing one or not
            ConnectionManagerUtil.openDBConnection();
            ApplicationList applicationList = applicationDAO.getApplications(filter, tenantId);
            if (applicationList != null && applicationList.getApplications() != null && !applicationList
                    .getApplications().isEmpty()) {
                throw new RequestValidatingException(
                        "Already an application registered with same name - " + applicationList.getApplications().get(0)
                                .getName());
            }
        } catch (ApplicationManagementDAOException e) {
            throw new RequestValidatingException(
                    "Error occured while getting existing applications for application name: " + application.getName()
                            + " and application type " + application.getType() + ". Tenant ID is " + tenantId, e);
        } catch (DBConnectionException e) {
            throw new RequestValidatingException(
                    "Error occured while getting database connection to get existing applications for application name: "
                            + application.getName() + " and application type: " + application.getType()
                            + ". Tenant id is " + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /***
     * To verify whether application type is valid one or not
     * @param appType application type {@link ApplicationType}
     * @return true returns if appType is valid on, otherwise returns false
     */
    private Boolean isValidAppType(String appType) {
        if (appType == null) {
            return false;
        }
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.toString().equals(appType)) {
                return true;
            }
        }
        return false;
    }

    //todo check whether user is whether admin user or application owner, otherwise throw an exception
    @Override public void updateApplicationImageArtifact(int appId, String uuid, InputStream iconFileStream,
            InputStream bannerFileStream, List<InputStream> attachments) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationRelease applicationRelease;
        try {
            if (appId <= 0) {
                throw new ValidationException(
                        "Application id could,t be a negative integer. Hence please add valid application id.");
            }
            ConnectionManagerUtil.beginDBTransaction();
            applicationRelease = this.applicationReleaseDAO.getReleaseByIds(appId, uuid, tenantId);
            if (applicationRelease == null) {
                throw new NotFoundException(
                        "Doesn't exist a application release for application ID:  " + appId + "and application UUID: "
                                + uuid);
            }
            LifecycleState lifecycleState = this.lifecycleStateDAO
                    .getLatestLifeCycleState(appId, applicationRelease.getUuid());
            if (AppLifecycleState.PUBLISHED.toString().equals(lifecycleState.getCurrentState())
                    || AppLifecycleState.DEPRECATED.toString().equals(lifecycleState.getCurrentState())) {
                throw new ForbiddenException("Can't Update the application release in PUBLISHED or DEPRECATED state. "
                        + "Hence please demote the application and update the application release");
            }
            applicationRelease = applicationStorageManager
                    .updateImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);
            applicationRelease = this.applicationReleaseDAO.updateRelease(appId, applicationRelease, tenantId);
            if (applicationRelease == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationManagementException(
                        "Application release updating count is 0. Application id: " + appId
                                + " Application release UUID: " + uuid);

            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occured when getting DB connection to update image artifacts of the application, appid: "
                            + appId + " and uuid " + uuid + ".", e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while getting application release data for updating image artifacts of the application, appid: "
                            + appId + " and uuid " + uuid + ".", e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while getting latest lifecycle state for updating image artifacts of the application, appid: "
                            + appId + " and uuid " + uuid + ".", e);
        } catch (ResourceManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while updating image artifacts of the application, appid: " + appId + " and uuid "
                            + uuid + " to the system.", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    //todo check whether package names are same
    @Override public void updateApplicationArtifact(int appId, String deviceType, String uuid, InputStream binaryFile)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationRelease applicationRelease;
        try {
            boolean isValidDeviceType = false;
            List<DeviceType> deviceTypes = Util.getDeviceManagementService().getDeviceTypes();
            for (DeviceType dt : deviceTypes) {
                if (dt.getName().equals(deviceType)) {
                    isValidDeviceType = true;
                    break;
                }
            }
            if (!isValidDeviceType) {
                throw new ValidationException(
                        "Invalid request to update application release artifact, invalid application type: "
                                + deviceType + " for Application id: " + appId + " application release uuid: " + uuid);
            }
            if (appId <= 0) {
                throw new ValidationException(
                        "Application id could,t be a negative integer. Hence please add valid application id. application type: "
                                + deviceType + " Application id: " + appId + " UUID: " + uuid);
            }

            ConnectionManagerUtil.beginDBTransaction();
            Application application = this.applicationDAO.getApplicationById(appId, tenantId);
            if (application == null) {
                throw new NotFoundException("Doesn't exist a application for the application ID:  " + appId);
            }
            applicationRelease = this.applicationReleaseDAO.getReleaseByIds(appId, uuid, tenantId);
            if (applicationRelease == null) {
                throw new NotFoundException(
                        "Doesn't exist a application release for application ID:  " + appId + "and application UUID: "
                                + uuid);
            }
            applicationRelease = applicationStorageManager
                    .updateReleaseArtifacts(applicationRelease, application.getType(), application.getDeviceType(),
                            binaryFile);
            applicationRelease = this.applicationReleaseDAO.updateRelease(appId, applicationRelease, tenantId);
            if (applicationRelease == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationManagementException(
                        "Application release updating count is 0. Application id: " + appId
                                + " Application release UUID: " + uuid);

            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException("Error occured while getting/updating APPM DB.", e);
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException(
                    "Error occured while starting the transaction to update application release artifact of the application, appid: "
                            + appId + " and uuid " + uuid + ".", e);
        } catch (RequestValidatingException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException("Error occured while updating application artifact.", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error occured while getting supported device types in IoTS", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occured when getting DB connection to update application release artifact of the application, appid: "
                            + appId + " and uuid " + uuid + ".", e);
        } catch (ApplicationStorageManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException("In order to update the artifact, couldn't find it in the system",
                    e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * To get role restricted application list.
     *
     * @param applicationList list of applications.
     * @param userName        user name
     * @return Application related with the UUID
     */
    private ApplicationList getRoleRestrictedApplicationList(ApplicationList applicationList, String userName)
            throws ApplicationManagementException {
        ApplicationList roleRestrictedApplicationList = new ApplicationList();
        ArrayList<Application> unRestrictedApplications = new ArrayList<>();
        for (Application application : applicationList.getApplications()) {
            if (application.getUnrestrictedRoles().isEmpty()) {
                unRestrictedApplications.add(application);
            } else {
                try {
                    if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                        unRestrictedApplications.add(application);
                    }
                } catch (UserStoreException e) {
                    throw new ApplicationManagementException("Role restriction verifying is failed");
                }
            }
        }
        roleRestrictedApplicationList.setApplications(unRestrictedApplications);
        return roleRestrictedApplicationList;
    }

    /**
     * To validate a app release creating request and app updating request to make sure all the pre-conditions satisfied.
     *
     * @param applicationRelease ApplicationRelease that need to be created.
     * @throws ApplicationManagementException Application Management Exception.
     */
    private void validateAppReleasePayload(ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        if (applicationRelease.getVersion() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                    + "creating release. It cannot be found.");
        }
    }

    @Override public LifecycleState getLifecycleState(int applicationId, String releaseUuid)
            throws ApplicationManagementException {
        LifecycleState lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (lifecycleState == null) {
                return null;
            }
            lifecycleState.setNextStates(
                    new ArrayList<>(lifecycleStateManger.getNextLifecycleStates(lifecycleState.getCurrentState())));

        } catch (LifeCycleManagementDAOException e) {
            throw new ApplicationManagementException("Failed to get lifecycle state from database", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override public void changeLifecycleState(int applicationId, String releaseUuid, LifecycleState state)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (!this.applicationDAO.verifyApplicationExistenceById(applicationId, tenantId)) {
                throw new NotFoundException("Couldn't find application for the application Id: " + applicationId);
            }
            if (!this.applicationReleaseDAO.verifyReleaseExistence(applicationId, releaseUuid, tenantId)) {
                throw new NotFoundException("Couldn't find application release for the application Id: " + applicationId
                        + " application release uuid: " + releaseUuid);
            }
            LifecycleState currentState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (currentState == null) {
                throw new ApplicationManagementException(
                        "Couldn't find latest lifecycle state for the appId: " + applicationId
                                + " and application release UUID: " + releaseUuid);
            }
            state.setPreviousState(currentState.getCurrentState());

            String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            state.setUpdatedBy(userName);

            if (state.getCurrentState() != null && state.getPreviousState() != null) {
                if (lifecycleStateManger.isValidStateChange(state.getPreviousState(), state.getCurrentState())) {
                    //todo if current state of the adding lifecycle state is PUBLISHED, need to check whether is there
                    //todo any other application release in PUBLISHED state for the application( i.e for the appid)
                    this.lifecycleStateDAO.addLifecycleState(state, applicationId, releaseUuid, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    log.error("Invalid lifecycle state transition from '" + state.getPreviousState() + "'" + " to '"
                            + state.getCurrentState() + "'");
                    throw new ApplicationManagementException(
                            "Lifecycle State Validation failed. Application Id: " + applicationId
                                    + " Application release UUID: " + releaseUuid);
                }
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Failed to add lifecycle state. Application Id: " + applicationId + " Application release UUID: "
                            + releaseUuid, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Application updateApplication(int applicationId, Application application)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application existingApplication;
        boolean isExistingAppRestricted = false;
        List<String> addingRoleList;
        List<String> removingRoleList;
        List<String> addingTags;
        List<String> removingTags;

        try {
            ConnectionManagerUtil.beginDBTransaction();
            existingApplication = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (existingApplication == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new NotFoundException("Tried to update Application which is not in the publisher, "
                        + "Please verify application details");
            }
            if (!existingApplication.getUnrestrictedRoles().isEmpty()) {
                isExistingAppRestricted = true;
            }

            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION) && !(
                    isExistingAppRestricted && isRoleExists(existingApplication.getUnrestrictedRoles(), userName))) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ForbiddenException("You are not authorized user to update application");
            }

            if (!existingApplication.getType().equals(application.getType())) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationManagementException("You are trying to change the application type and it is not "
                        + "possible after you create an application. Therefore please remove this application and "
                        + "publish new application with type: " + application.getType());
            }
            if (!existingApplication.getSubType().equals(application.getSubType())) {
                if (ApplicationSubscriptionType.PAID.toString().equals(existingApplication.getSubType()) && (
                        !"".equals(application.getPaymentCurrency()) || application.getPaymentCurrency() != null)) {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    throw new ApplicationManagementException("If you are going to change Non-Free app as Free app, "
                            + "currency attribute in the application updating payload should be null or \"\"");
                } else if (ApplicationSubscriptionType.FREE.toString().equals(existingApplication.getSubType()) && (
                        application.getPaymentCurrency() == null || "".equals(application.getPaymentCurrency()))) {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    throw new ApplicationManagementException("If you are going to change Free app as Non-Free app, "
                            + "currency attribute in the application payload should not be null or \"\"");
                }
            }
            if (isExistingAppRestricted && application.getUnrestrictedRoles().isEmpty()) {
                visibilityDAO.deleteUnrestrictedRoles(existingApplication.getUnrestrictedRoles(), application.getId(),
                        tenantId);
            } else if (!isExistingAppRestricted && !application.getUnrestrictedRoles().isEmpty()) {
                visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), application.getId(), tenantId);
            } else if (isExistingAppRestricted && !application.getUnrestrictedRoles().isEmpty()) {
                addingRoleList = getDifference(application.getUnrestrictedRoles(),
                        existingApplication.getUnrestrictedRoles());
                removingRoleList = getDifference(existingApplication.getUnrestrictedRoles(),
                        application.getUnrestrictedRoles());
                if (!addingRoleList.isEmpty()) {
                    visibilityDAO.addUnrestrictedRoles(addingRoleList, application.getId(), tenantId);
                }
                if (!removingRoleList.isEmpty()) {
                    visibilityDAO.deleteUnrestrictedRoles(removingRoleList, application.getId(), tenantId);
                }
            }

            addingTags = getDifference(existingApplication.getTags(), application.getTags());
            removingTags = getDifference(application.getTags(), existingApplication.getTags());
            if (!addingTags.isEmpty()) {
                applicationDAO.addTags(addingTags, application.getId(), tenantId);
            }
            if (!removingTags.isEmpty()) {
                applicationDAO.deleteTags(removingTags, application.getId(), tenantId);
            }
            return applicationDAO.editApplication(application, tenantId);
        } catch (UserStoreException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while checking whether logged in user is ADMIN or not when updating application of application id: "
                            + applicationId);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while updating the application, application id: " + applicationId);
        } catch (VisibilityManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while updating the visibility restriction of the application. Application id  is "
                            + applicationId);
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while starting database transaction for application updating. Application id  is "
                            + applicationId);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while getting database connection for application updating. Application id  is "
                            + applicationId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private Filter validateFilter(Filter filter) {
        if (filter != null && filter.getAppType() != null) {
            boolean isValidRequest = false;
            for (ApplicationType applicationType : ApplicationType.values()) {
                if (applicationType.toString().equals(filter.getAppType())) {
                    isValidRequest = true;
                    break;
                }
            }
            if (!isValidRequest) {
                return null;
            }
        }
        return filter;
    }

    private <T> List<T> getDifference(List<T> list1, Collection<T> list2) {
        List<T> list = new ArrayList<>();
        for (T t : list1) {
            if (!list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /***
     * By invoking the method, it returns Lifecycle State Instance.
     * @param currentState Current state of the lifecycle
     * @param previousState Previouse state of the Lifecycle
     * @return {@link LifecycleState}
     */
    private LifecycleState getLifecycleStateInstant(String currentState, String previousState) {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setCurrentState(currentState);
        lifecycleState.setPreviousState(previousState);
        lifecycleState.setUpdatedBy(userName);
        return lifecycleState;
    }

    //todo check whether package names are same
    @Override public boolean updateRelease(int applicationId, String releaseUuid, String deviceType,
            ApplicationRelease updateRelease, InputStream binaryFileStram, InputStream iconFileStream,
            InputStream bannerFileStream, List<InputStream> attachments) throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationRelease release;
        Application app = null;
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        DeviceType deviceTypeObj;
        boolean isAdminUser;

        try {
            // Getting the device type details to get device type ID for internal mappings
            deviceTypeObj = Util.getDeviceManagementService().getDeviceType(deviceType);
            isAdminUser = isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION);

            ConnectionManagerUtil.beginDBTransaction();
            app = this.applicationDAO.getApplicationById(applicationId, tenantId);
            release = this.applicationReleaseDAO.getReleaseByIds(applicationId, releaseUuid, tenantId);

            if (app == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new NotFoundException(
                        "Couldn't found an application for updating. Application id: " + applicationId);
            }

            if (deviceTypeObj == null || deviceTypeObj.getId() != app.getDeviceTypeId()) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new BadRequestException(
                        "Request to update application release for Invalid device type. Device type: " + deviceType
                                + " application ID " + applicationId + " Application Release UUID " + releaseUuid);
            }
            if (release == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new NotFoundException(
                        "Couldn't found an application realise for updating. Application id: " + applicationId
                                + " and application release UUID: " + releaseUuid);
            }

            String releaseType = updateRelease.getReleaseType();
            Double price = updateRelease.getPrice();
            int isSharedWithTenants = updateRelease.getIsSharedWithAllTenants();
            String metaData = updateRelease.getMetaData();

            if (price < 0.0 || (price == 0.0 && ApplicationSubscriptionType.PAID.toString().equals(app.getSubType()))
                    || (price > 0.0 && ApplicationSubscriptionType.FREE.toString().equals(app.getSubType()))) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new BadRequestException(
                        "Invalid app release payload for updating application release. Application price is " + price
                                + " for " + app.getSubType() + " application. Application ID: " + applicationId
                                + ", Application Release UUID " + releaseUuid + " and supported device type is "
                                + deviceType);
            }
            release.setPrice(price);
            if (releaseType != null) {
                release.setReleaseType(releaseType);
            }
            //todo change this when isShared with field change to boolean
            if (isSharedWithTenants == 0 || isSharedWithTenants == 1) {
                release.setIsSharedWithAllTenants(isSharedWithTenants);
            }
            if (metaData != null) {
                release.setMetaData(metaData);
            }

            List<String> unrestrictedRoles = app.getUnrestrictedRoles();

            String applicationReleaseCreatedUser = lifecycleStateDAO
                    .getAppReleaseCreatedUsername(applicationId, releaseUuid, tenantId);

            if (!isAdminUser && !(!unrestrictedRoles.isEmpty() && isRoleExists(unrestrictedRoles, userName))
                    && !userName.equals(applicationReleaseCreatedUser)) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ForbiddenException("You are not authorized user to update application");
            }

            //todo try to remove this DB call and get it when getting application release
            LifecycleState lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (!AppLifecycleState.CREATED.toString().equals(lifecycleState.getCurrentState())
                    && !AppLifecycleState.IN_REVIEW.toString().equals(lifecycleState.getCurrentState())
                    && !AppLifecycleState.REJECTED.toString().equals(lifecycleState.getCurrentState())) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ForbiddenException(
                        "You can't update application release which is in " + lifecycleState.getCurrentState()
                                + " State");
            }

            release = applicationStorageManager
                    .updateImageArtifacts(release, iconFileStream, bannerFileStream, attachments);
            release = applicationStorageManager
                    .updateReleaseArtifacts(release, app.getType(), deviceType, binaryFileStram);
            return applicationReleaseDAO.updateRelease(applicationId, release, tenantId) != null;
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error occured when validating the device type " + deviceType, e);
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "Error occured while verifying whether user is admin user or not. Username " + userName
                            + " tenant id " + tenantId, e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error Occured when getting lifecycle state of the application release of application UUID: "
                            + releaseUuid, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured when updating Application release. Application ID " + applicationId
                            + " Application Release UUID: " + releaseUuid, e);
        } catch (ApplicationStorageManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured when updating application release artifact. Application ID " + applicationId
                            + " Application release UUID: " + releaseUuid, e);
        } catch (ResourceManagementException e) {
            //            updating images
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured when updating image artifact of the application release. Application ID: "
                            + applicationId + " Application release UUID: " + releaseUuid, e);
        } catch (RequestValidatingException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured when validating application release artifact for device type " + deviceType
                            + " And application type " + app.getType() + ". Applicationn ID: " + applicationId
                            + " Application release UUID: " + releaseUuid);
        }
    }
}
