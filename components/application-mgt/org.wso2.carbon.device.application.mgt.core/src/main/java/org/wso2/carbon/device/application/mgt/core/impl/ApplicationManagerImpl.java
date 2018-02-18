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
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.common.SortingOrder;
import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ValidationException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int DEFAULT_OFFSET = 10;
    private DeviceTypeDAO deviceTypeDAO;
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;

    public ApplicationManagerImpl() {
        initDataAccessObjects();
    }

    private void initDataAccessObjects() {
        this.deviceTypeDAO = ApplicationManagementDAOFactory.getDeviceTypeDAO();
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public Application createApplication(Application application) throws ApplicationManagementException {

        User loggedInUser = new User(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername(),
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
        application.setUser(loggedInUser);
        if (log.isDebugEnabled()) {
            log.debug("Create Application received for the tenant : " + application.getUser().getTenantId() + " From"
                    + " the user : " + application.getUser().getUserName());
        }

        validateAppCreatingRequest(application);
        validateReleaseCreatingRequest(application.getApplicationReleases());
        DeviceType deviceType;
        ApplicationRelease applicationRelease;
        try {
            ConnectionManagerUtil.beginDBTransaction();
            int tenantId = application.getUser().getTenantId();
            deviceType = this.deviceTypeDAO.getDeviceType(application.getType(), application.getUser().getTenantId());

            if (deviceType == null) {
                log.error("Device type is not matched with application type");
                return null;
            }
            application.setDevicetype(deviceType);
            int appId = this.applicationDAO.createApplication(application, deviceType.getId());

            if (appId != -1) {
                log.error("Application creation Failed");
                ConnectionManagerUtil.rollbackDBTransaction();
            } else {
                if (!application.getTags().isEmpty()) {
                    this.applicationDAO.addTags(application.getTags(), appId, tenantId);
                }
                if (application.getIsRestricted() == 1 && !application.getUnrestrictedRoles().isEmpty()) {
                    this.visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), appId, tenantId);
                } else {
                    application.setIsRestricted(0);
                }
                ConnectionManagerUtil.commitDBTransaction();
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease.setCreatedAt((Timestamp) new Date());
                applicationRelease = ApplicationManagementDAOFactory.getApplicationReleaseDAO().
                        createRelease(applicationRelease, application.getId());
                //todo add lifecycle and add this into application
            }

            return application;

        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting device type id of " + application.getType();
            log.error(msg, e);
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding application";
            log.error(msg, e);
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList;
        List<ApplicationRelease> applicationReleases;

        filter = validateFilter(filter);
        if (filter == null) {
            throw new ApplicationManagementException("Filter validation failed, Please verify the request payload");
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            applicationList = applicationDAO.getApplications(filter, tenantId);
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationList = getRoleRestrictedApplicationList(applicationList, userName);
            }

            for (Application application : applicationList.getApplications()) {
                applicationReleases = getReleases(application.getId());
                application.setApplicationReleases(applicationReleases);
            }

            return applicationList;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while checking whether the user " + userName + " of tenant " + tenantId
                            + " has the publisher permission", e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "DAO exception while getting applications for the user " + userName + " of tenant " + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public String getUuidOfLatestRelease(int appId) throws ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return applicationDAO.getUuidOfLatestRelease(appId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    private boolean isRoleExists(List<UnrestrictedRole> unrestrictedRoleList, String userName)
            throws UserStoreException {
        String[] roleList;
        roleList = getRolesOfUser(userName);
        for (UnrestrictedRole unrestrictedRole : unrestrictedRoleList) {
            for (String role : roleList) {
                if (unrestrictedRole.getRole().equals(role)) {
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

    @Override
    public Application getApplication(String appType, String appName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = ApplicationManagementDAOFactory.getApplicationDAO()
                    .getApplication(appType, appName, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
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

            applicationReleases = getReleases(application.getId());
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application name " + appName);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application getApplicationById(int applicationId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = ApplicationManagementDAOFactory.getApplicationDAO()
                    .getApplicationById(applicationId, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
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
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application id " + applicationId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = ApplicationManagementDAOFactory.getApplicationDAO()
                    .getApplicationByRelease(appReleaseUUID, tenantId);

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

    public Boolean verifyApplicationExistenceById(int appId) throws ApplicationManagementException {
        try {
            Boolean isAppExist;
            ConnectionManagerUtil.openDBConnection();
            isAppExist = ApplicationManagementDAOFactory.getApplicationDAO().verifyApplicationExistenceById(appId);
            return isAppExist;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public Boolean isUserAllowable(List<UnrestrictedRole> unrestrictedRoles, String userName)
            throws ApplicationManagementException {
        try {
            return isRoleExists(unrestrictedRoles, userName);
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while verifying whether user have assigned" + "unrestricted roles or not", e);
        }
    }

    @Override
    public List<ApplicationRelease> getReleases(int applicationId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        Application application = validateApplication(applicationId);
        List<ApplicationRelease> applicationReleases;
        List<ApplicationRelease> filteredApplicationReleases = new ArrayList<>();
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " + application
                    .toString());
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationReleases = ApplicationManagementDAOFactory.getApplicationReleaseDAO()
                    .getApplicationReleases(application.getName(), application.getType(), tenantId);
            for (ApplicationRelease applicationRelease : applicationReleases) {
                if (!AppLifecycleState.REMOVED.toString().equals(ApplicationManagementDAOFactory.getLifecycleStateDAO().
                        getLatestLifeCycleStateByReleaseID(applicationRelease.getId()).getCurrentState())) {
                    filteredApplicationReleases.add(applicationRelease);
                }
            }
            return filteredApplicationReleases;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<String> deleteApplication(int applicationId) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        if (validateApplication(applicationId) == null) {
            throw new ApplicationManagementException("Invalid Application");
        }
        List<ApplicationRelease> applicationReleases = getReleases(applicationId);
        List<String> storedLocations = new ArrayList<>();
        if (log.isDebugEnabled()) {
            log.debug("Request is received to delete applications which are related with the application id " +
                    applicationId);
        }
        for (ApplicationRelease applicationRelease : applicationReleases) {
            LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
            LifecycleState newAppLifecycleState = new LifecycleState();
            newAppLifecycleState.setPreviousState(appLifecycleState.getCurrentState());
            newAppLifecycleState.setCurrentState(AppLifecycleState.REMOVED.toString());
            newAppLifecycleState.setTenantId(tenantId);
            newAppLifecycleState.setUpdatedBy(userName);
            addLifecycleState(applicationId, applicationRelease.getUuid(), newAppLifecycleState);
            storedLocations.add(applicationRelease.getAppHashValue());
        }
        //todo add column into application and move application into removed state
        return storedLocations;
    }

    @Override
    public String deleteApplicationRelease(int applicationId, String releaseUuid) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = validateApplication(applicationId);

        if (application == null) {
            throw new ApplicationManagementException("Invalid Application ID is received");
        }
        ApplicationRelease applicationRelease = validateApplicationRelease(releaseUuid);
        if (applicationRelease == null) {
            throw new ApplicationManagementException("Invalid Application Release UUID is received");
        }

        LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
        LifecycleState newAppLifecycleState = new LifecycleState();
        newAppLifecycleState.setPreviousState(appLifecycleState.getCurrentState());
        newAppLifecycleState.setCurrentState(AppLifecycleState.REMOVED.toString());
        newAppLifecycleState.setTenantId(tenantId);
        newAppLifecycleState.setUpdatedBy(userName);
        addLifecycleState(applicationId, applicationRelease.getUuid(), newAppLifecycleState);
        return applicationRelease.getAppHashValue();
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
     * To validate the application
     *
     * @param application Application that need to be created
     * @throws ValidationException Validation Exception
     */
    private void validateAppCreatingRequest(Application application) throws ValidationException {

        Boolean isValidApplicationType;
        try {
            if (application.getName() == null) {
                throw new ValidationException("Application name cannot be empty");
            }
            if (application.getUser() == null || application.getUser().getUserName() == null
                    || application.getUser().getTenantId() == -1) {
                throw new ValidationException("Username and tenant Id cannot be empty");
            }
            if (application.getAppCategory() == null) {
                throw new ValidationException("Username and tenant Id cannot be empty");
            }

            isValidApplicationType = isValidAppType(application);

            if (!isValidApplicationType) {
                throw new ValidationException("App Type contains in the application creating payload doesn't match with " +
                        "supported app types");
            }

            validateApplicationExistence(application);
        } catch (ApplicationManagementException e) {
            throw new ValidationException("Error occured while validating whether there is already an application "
                    + "registered with same name.", e);
        }
    }

    private Boolean isValidAppType(Application application) {
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.toString().equals(application.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * To validate the application existence
     *
     * @param application Application that need to be validated
     * @throws ValidationException Validation Exception
     */
    private void validateApplicationExistence(Application application) throws ApplicationManagementException {
        Filter filter = new Filter();
        filter.setFullMatch(true);
        filter.setAppName(application.getName().trim());
        filter.setOffset(0);
        filter.setLimit(1);

        ApplicationList applicationList = getApplications(filter);
        if (applicationList != null && applicationList.getApplications() != null && !applicationList.getApplications()
                .isEmpty()) {
            throw new ApplicationManagementException(
                    "Already an application registered with same name - " + applicationList.getApplications().get(0)
                            .getName());
        }
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationID ID of the Application.
     * @return Application related with the UUID
     */
    public Application validateApplication(int applicationID) throws ApplicationManagementException {
        if (applicationID <= 0) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplicationById(applicationID);
        if (application == null) {
            throw new NotFoundException("Application of the " + applicationID + " does not exist.");
        }
        return application;
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    public ApplicationRelease validateApplicationRelease(String applicationUuid) throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        ApplicationRelease applicationRelease = DataHolder.getInstance().getApplicationReleaseManager()
                .getReleaseByUuid(applicationUuid);
        if (applicationRelease == null) {
            throw new ApplicationManagementException(
                    "Application with UUID " + applicationUuid + " does not exist.");
        }
        return applicationRelease;
    }

    @Override
    public ApplicationRelease updateRelease(int appId, ApplicationRelease applicationRelease) throws
            ApplicationManagementException {
        return null;
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
     * To validate a create release request to make sure all the pre-conditions satisfied.
     *
     * @param applicationReleases ApplicationRelease that need to be created.
     * @throws ApplicationManagementException Application Management Exception.
     */
    private void validateReleaseCreatingRequest(List<ApplicationRelease> applicationReleases)
            throws ApplicationManagementException {

        if (applicationReleases.isEmpty() || applicationReleases.size() > 1) {
            throw new ApplicationManagementException("ApplicationRelease size is grater than minimal release size or "
                    + "request doesn't contains application release");
        }
        if (applicationReleases.get(0).getVersion() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                    + "creating release. It cannot be found.");
        }
        //todo
        //        if (getRelease(applicationReleases.get(0).getUuid(), applicationReleases.get(0).getVersion(),
        //                applicationReleases.get(0).getReleaseType()) != null) {
        //            throw new ApplicationManagementException( "Application Release for the Application UUID " +
        //                    applicationReleases.get(0).getUuid() + " " + "with the version "
        //                    + applicationReleases.get(0).getVersion() + " already exists. Cannot create an " +
        //                    "application release with the same version.");
        //        }
    }

    @Override
    public LifecycleState getLifecycleState(int applicationId, String applicationUuid) throws
            ApplicationManagementException {
        LifecycleState lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            LifecycleStateDAO lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
            Application application = validateApplication(applicationId);
            //todo applicationUuid and applicationId should be passed and util method has to be changed
            ApplicationRelease applicationRelease = validateApplicationRelease(applicationUuid);
            lifecycleState = lifecycleStateDAO.getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            lifecycleState.setNextStates(getNextLifecycleStates(lifecycleState.getCurrentState()));
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException("Failed to get lifecycle state", e);
        } catch (ApplicationManagementException e) {
            throw new ApplicationManagementException("Failed to get application and application management", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override
    public void addLifecycleState(int applicationId, String applicationUuid, LifecycleState state) throws
            ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            Application application = validateApplication(applicationId);
            //todo applicationUuid and applicationId should be passed and util method has to be changed
            ApplicationRelease applicationRelease = validateApplicationRelease(applicationUuid);
            LifecycleStateDAO lifecycleStateDAO;

            if (application != null) {
                state.setAppId(applicationId);
            }
            if (applicationRelease != null) {
                state.setReleaseId(applicationRelease.getId());
            }
            if (state.getCurrentState() != null && state.getPreviousState() != null && state.getUpdatedBy() != null) {
                validateLifecycleState(state);
                lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
                lifecycleStateDAO.addLifecycleState(state);
            }
        } catch (LifeCycleManagementDAOException | DBConnectionException e) {
            throw new ApplicationManagementException("Failed to add lifecycle state", e);
        } catch (ApplicationManagementException e) {
            throw new ApplicationManagementException("Lifecycle State Validation failed", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private List<String> getNextLifecycleStates(String currentLifecycleState) {
        List<String> nextLifecycleStates = new ArrayList<>();
        if (AppLifecycleState.CREATED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.IN_REVIEW.toString());
        }
        if (AppLifecycleState.IN_REVIEW.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.APPROVED.toString());
            nextLifecycleStates.add(AppLifecycleState.REJECTED.toString());
        }
        if (AppLifecycleState.REJECTED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.IN_REVIEW.toString());
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        if (AppLifecycleState.APPROVED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.PUBLISHED.toString());
        }
        if (AppLifecycleState.PUBLISHED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.UNPUBLISHED.toString());
            nextLifecycleStates.add(AppLifecycleState.DEPRECATED.toString());
        }
        if (AppLifecycleState.UNPUBLISHED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.PUBLISHED.toString());
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        if (AppLifecycleState.DEPRECATED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        return nextLifecycleStates;
    }

    private void validateLifecycleState(LifecycleState state) throws LifecycleManagementException {

        if (AppLifecycleState.CREATED.toString().equals(state.getCurrentState())) {
            throw new LifecycleManagementException("Current State Couldn't be " + state.getCurrentState());
        }
        if (AppLifecycleState.IN_REVIEW.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.CREATED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.REJECTED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.CREATED.toString() + " or " +
                        AppLifecycleState.REJECTED.toString());
            }
        }
        if (AppLifecycleState.APPROVED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.IN_REVIEW.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.IN_REVIEW.toString());
            }
        }
        if (AppLifecycleState.PUBLISHED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.APPROVED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.UNPUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.APPROVED.toString() + " or " +
                        AppLifecycleState.UNPUBLISHED.toString());
            }
        }
        if (AppLifecycleState.UNPUBLISHED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.PUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.PUBLISHED.toString());
            }
        }
        if (AppLifecycleState.REJECTED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.IN_REVIEW.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.IN_REVIEW.toString());
            }
        }
        if (AppLifecycleState.DEPRECATED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.PUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.PUBLISHED.toString());
            }
        }
        if (AppLifecycleState.REMOVED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.DEPRECATED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.REJECTED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.UNPUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.DEPRECATED.toString() + " or " +
                        AppLifecycleState.REJECTED.toString() + " or " + AppLifecycleState.UNPUBLISHED.toString());
            }
        }
    }

    @Override
    public Application updateApplication(Application application) throws ApplicationManagementException {
        Application existingApplication = validateApplication(application.getId());
        if (existingApplication == null) {
            throw new NotFoundException("Tried to update Application which is not in the publisher, " +
                    "Please verify application details");
        }
        if (!existingApplication.getType().equals(application.getType())) {
            throw new ApplicationManagementException("You are trying to change the application type and it is not " +
                    "possible after you create an application. Therefore please remove this application and publish " +
                    "new application with type: " + application.getType());
        }
        if (existingApplication.getIsFree() != application.getIsFree()) {
            if (existingApplication.getIsFree() == 1) {
                if (application.getPaymentCurrency() != null || !application.getPaymentCurrency().equals("")) {
                    throw new ApplicationManagementException("If you are going to change Non-Free app as Free app, " +
                            "currency attribute in the application updating payload should be null or \"\"");
                }
            } else if (existingApplication.getIsFree() == 0) {
                if (application.getPaymentCurrency() == null || application.getPaymentCurrency().equals("")) {
                    throw new ApplicationManagementException("If you are going to change Free app as Non-Free app, " +
                            "currency attribute in the application payload should not be null or \"\"");
                }
            }
        }
        //todo get restricted roles and assign for application
        if (existingApplication.getIsRestricted() != application.getIsRestricted()) {
            if (existingApplication.getIsRestricted() == 1) {
                if (application.getUnrestrictedRoles() == null || application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to add role restriction for non role " +
                            "restricted Application, Unrestricted role list won't be empty or null");
                }
            } else if (existingApplication.getIsRestricted() == 0) {
                if (application.getUnrestrictedRoles() != null || !application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to remove role restriction from role " +
                            "restricted Application, Unrestricted role list should be empty or null");
                }
            }
            //todo update role restriction
        }
        //todo get tags and assign for application verify
        //todo update application
        return application;
    }

    private Filter validateFilter(Filter filter) {
        if (filter != null) {
            if (filter.getLimit() == 0) {
                filter.setLimit(DEFAULT_LIMIT);
            }
            if (filter.getOffset() == 0) {
                filter.setOffset(DEFAULT_OFFSET);
            }
            if (!SortingOrder.ASC.toString().equals(filter.getSortBy()) &&
                    !SortingOrder.DESC.toString().equals(filter.getSortBy())) {
                return null;
            }
            if (filter.getAppType() != null) {
                Boolean isValidRequest = false;
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
        }
        return filter;
    }
}
