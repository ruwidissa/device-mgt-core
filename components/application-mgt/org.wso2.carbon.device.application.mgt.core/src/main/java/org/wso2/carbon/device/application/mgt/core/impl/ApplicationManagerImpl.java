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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.ApplicationArtifact;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstaller;
import org.wso2.carbon.device.application.mgt.common.Pagination;
import org.wso2.carbon.device.application.mgt.common.config.RatingConfiguration;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionType;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.dto.CategoryDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.LifecycleStateDTO;
import org.wso2.carbon.device.application.mgt.common.dto.TagDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
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
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private LifecycleStateDAO lifecycleStateDAO;
    private LifecycleStateManager lifecycleStateManager;

    public ApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
    }

    /***
     * The responsbility of this method is the creating an application.
     * @param applicationWrapper ApplicationDTO that need to be created.
     * @return {@link ApplicationDTO}
     * @throws RequestValidatingException if application creating request is invalid,
     * @throws ApplicationManagementException Catch all other throwing exceptions and throw {@link ApplicationManagementException}
     */
    @Override
    public Application createApplication(ApplicationWrapper applicationWrapper,
            ApplicationArtifact applicationArtifact) throws RequestValidatingException, ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO applicationDTO;
        Application application;
        if (log.isDebugEnabled()) {
            log.debug("Application create request is received for the tenant : " + tenantId + " From" + " the user : "
                    + userName);
        }
        try {
            applicationDTO = appWrapperToAppDTO(applicationWrapper);
            ApplicationReleaseDTO initialApplicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
            applicationDTO.getApplicationReleaseDTOs().clear();

            ApplicationReleaseDTO applicationReleaseDTO = addApplicationReleaseArtifacts(applicationDTO.getType(),
                    applicationWrapper.getDeviceType(), initialApplicationReleaseDTO, applicationArtifact);
            applicationDTO.getApplicationReleaseDTOs().add(addImageArtifacts(applicationReleaseDTO, applicationArtifact));
        } catch (UnexpectedServerErrorException e) {
            String msg = "Error occurred when getting Device Type data.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }  catch (ResourceManagementException e) {
            String msg = "Error Occured when uploading artifacts of the application: " + applicationWrapper.getName();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }

        try {
            List<ApplicationReleaseDTO> applicationReleaseEntities = new ArrayList<>();
            ApplicationReleaseDTO applicationReleaseDTO;

            Filter filter = new Filter();
            filter.setFullMatch(true);
            filter.setAppName(applicationDTO.getName().trim());
            filter.setOffset(0);
            filter.setLimit(1);

            ConnectionManagerUtil.beginDBTransaction();
            List<ApplicationDTO> applicationList = applicationDAO
                    .getApplications(filter, applicationDTO.getDeviceTypeId(), tenantId);
            if (!applicationList.isEmpty()) {
                String msg =
                        "Already an application registered with same name - " + applicationList.get(0)
                                .getName();
                log.error(msg);
                throw new RequestValidatingException(msg);
            }

            // Insert to application table
            int appId = this.applicationDAO.createApplication(applicationDTO, tenantId);
            if (appId == -1) {
                log.error("ApplicationDTO creation is Failed");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("New ApplicationDTO entry added to AP_APP table. App Id:" + appId);
                }
                //adding application unrestricted roles
                List<String> unrestrictedRoles = applicationWrapper.getUnrestrictedRoles();
                if (!unrestrictedRoles.isEmpty()) {
                    if (!isValidRestrictedRole(unrestrictedRoles)) {
                        ConnectionManagerUtil.rollbackDBTransaction();
                        String msg = "Unrestricted role list contain role/roles which are not in the user store.";
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }

                    if (!hasUserRole(unrestrictedRoles, userName)){
                        ConnectionManagerUtil.rollbackDBTransaction();
                        String msg =
                                "You are trying to restrict the visibility of the application for a role set, but in order to perform the action at least one role should be assigned to user: "
                                        + userName;
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    this.visibilityDAO.addUnrestrictedRoles(unrestrictedRoles, appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New restricted roles to app ID mapping added to AP_UNRESTRICTED_ROLE table."
                                + " App Id:" + appId);
                    }
                }

                List<CategoryDTO> registeredCategories = this.applicationDAO.getAllCategories(tenantId);
                String categoryName = applicationWrapper.getAppCategory();
                Optional<CategoryDTO> category = registeredCategories.stream()
                        .filter(obj -> obj.getCategoryName().equals(categoryName)).findAny();

                if (registeredCategories.isEmpty()) {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    String msg = "Registered application category set is empty category: " + categoryName;
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
                if (!category.isPresent()){
                    ConnectionManagerUtil.rollbackDBTransaction();
                    String msg = "Request contains invalid category: " + categoryName;
                    log.error(msg);
                    throw new ApplicationManagementException(msg);

                }

                /*
                In current flow, allow to add one category for an application. If it is required to add multiple
                categories DAO layer is implemented to match with that requirement. Hence logic is also implemented
                this way.
                */
                List<Integer> categoryIds = new ArrayList<>();
                categoryIds.add(category.get().getId());
                this.applicationDAO.addCategoryMapping(categoryIds,appId,tenantId);


                //adding application tags
                List<String> tags = applicationWrapper.getTags();
                if (!tags.isEmpty()) {
                    List<TagDTO> registeredTags = applicationDAO.getAllTags(tenantId);
                    List<String> registeredTagNames = new ArrayList<>();
                    List<Integer> tagIds = new ArrayList<>();

                    for (TagDTO tagDTO : registeredTags) {
                        registeredTagNames.add(tagDTO.getTagName());
                    }
                    List<String> newTags = getDifference(tags, registeredTagNames);
                    if (!newTags.isEmpty()) {
                        this.applicationDAO.addTags(newTags, tenantId);
                        if (log.isDebugEnabled()) {
                            log.debug("New tags entry added to AP_APP_TAG table. App Id:" + appId);
                        }
                        tagIds = this.applicationDAO.getTagIdsForTagNames(tags, tenantId);
                    } else {
                        for (TagDTO tagDTO : registeredTags) {
                            for (String tagName : tags) {
                                if (tagName.equals(tagDTO.getTagName())) {
                                    tagIds.add(tagDTO.getId());
                                    break;
                                }
                            }
                        }
                    }
                    this.applicationDAO.addTagMapping(tagIds, appId, tenantId);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Creating a new release. App Id:" + appId);
                }
                String initialLifecycleState = lifecycleStateManager.getInitialState();
                applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
                applicationReleaseDTO.setCurrentState(initialLifecycleState);
                applicationReleaseDTO = this.applicationReleaseDAO.createRelease(applicationReleaseDTO, appId, tenantId);
                LifecycleStateDTO lifecycleStateDTO = getLifecycleStateInstance(initialLifecycleState,
                        initialLifecycleState);
                this.lifecycleStateDAO
                        .addLifecycleState(lifecycleStateDTO, appId, applicationReleaseDTO.getUuid(), tenantId);
                applicationReleaseEntities.add(applicationReleaseDTO);
                applicationDTO.setApplicationReleaseDTOs(applicationReleaseEntities);
                application = appDtoToAppResponse(applicationDTO);
                ConnectionManagerUtil.commitDBTransaction();
            }
            return application;
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while adding lifecycle state. application name: " + applicationWrapper.getName()
                    + " application type: is " + applicationWrapper.getType();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while adding application or application release. application name: "
                    + applicationWrapper.getName() + " application type: " + applicationWrapper.getType();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch(LifecycleManagementException e){
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when getting initial lifecycle state. application name: " + applicationWrapper
                    .getName() + " application type: is " + applicationWrapper.getType();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (VisibilityManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while adding unrestricted roles. application name: " + applicationWrapper.getName()
                    + " application type: " + applicationWrapper.getType();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (UserStoreException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when validating the unrestricted roles given for the application";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ApplicationReleaseDTO addApplicationReleaseArtifacts(String applicationType, String deviceType,
            ApplicationReleaseDTO applicationReleaseDTO, ApplicationArtifact applicationArtifact)
            throws ResourceManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();

        String uuid = UUID.randomUUID().toString();
        applicationReleaseDTO.setUuid(uuid);

        // The application executable artifacts such as apks are uploaded.
        if (ApplicationType.ENTERPRISE.toString().equals(applicationType)) {
            try {
                byte[] content = IOUtils.toByteArray(applicationArtifact.getInstallerStream());

                applicationReleaseDTO.setInstallerName(applicationArtifact.getInstallerName());

                try (ByteArrayInputStream binary = new ByteArrayInputStream(content)) {
                    ApplicationInstaller applicationInstaller = applicationStorageManager
                            .getAppInstallerData(binary, deviceType);
                    String packagename = applicationInstaller.getPackageName();

                    ConnectionManagerUtil.getDBConnection();
                    if (applicationReleaseDAO.isActiveReleaseExisitForPackageName(packagename, tenantId,
                            lifecycleStateManager.getEndState())) {
                        String msg = "Application release is already exist for the package name: " + packagename +
                                ". Either you can delete all application releases for package " + packagename + " or "
                                + "you can add this app release as an new application release, under the existing "
                                + "application.";
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                    applicationReleaseDTO.setVersion(applicationInstaller.getVersion());
                    applicationReleaseDTO.setPackageName(packagename);

                    String md5OfApp = StorageManagementUtil.getMD5(new ByteArrayInputStream(content));
                    if (md5OfApp == null) {
                        String msg = "Error occurred while md5sum value retrieving process: application UUID "
                                + applicationReleaseDTO.getUuid();
                        log.error(msg);
                        throw new ApplicationStorageManagementException(msg);
                    }
                    if (this.applicationReleaseDAO
                            .verifyReleaseExistenceByHash(md5OfApp, tenantId)) {
                        throw new BadRequestException(
                                "Application release exists for the uploaded binary file. Application Type: "
                                        + applicationType + " Device Tyep: " + deviceType);
                    }

                    applicationReleaseDTO.setAppHashValue(md5OfApp);

                    try (ByteArrayInputStream binaryDuplicate = new ByteArrayInputStream(content)) {
                        applicationReleaseDTO = applicationStorageManager
                                .uploadReleaseArtifact(applicationReleaseDTO,applicationType,
                                        deviceType, binaryDuplicate);
                    }
                }
            } catch (IOException e) {
                String msg =
                        "Error occurred when getting byte array of binary file. Installer name: " + applicationArtifact
                                .getInstallerName();
                log.error(msg);
                throw new ApplicationStorageManagementException(msg);
            } catch (DBConnectionException e) {
                String msg = "Error occurred when getting database connection for verifying application package existence.";
                log.error(msg);
                throw new ApplicationManagementException(msg, e);
            } catch (ApplicationManagementDAOException e) {
                String msg = "Error occurred when executing the query for verifying application release existence for "
                        + "the package.";
                log.error(msg);
                throw new ApplicationManagementException(msg, e);
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }
        } else if (ApplicationType.WEB_CLIP.toString().equals(applicationType)) {
            applicationReleaseDTO.setVersion(Constants.DEFAULT_VERSION);
            applicationReleaseDTO.setInstallerName(applicationReleaseDTO.getUrl());
            // Since WEB CLIP doesn't have an installer, set uuid as has value for WEB CLIP
            applicationReleaseDTO.setAppHashValue(uuid);
        }
        return applicationReleaseDTO;
    }

    private ApplicationReleaseDTO addImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO,
            ApplicationArtifact applicationArtifact) throws ResourceManagementException {
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();

        applicationReleaseDTO.setIconName(applicationArtifact.getIconName());
        applicationReleaseDTO.setBannerName(applicationArtifact.getBannerName());

        Map<String, InputStream> screenshots = applicationArtifact.getScreenshots();
        List<String> screenshotNames = new ArrayList<>(screenshots.keySet());

        int counter = 1;
        for (String scName : screenshotNames) {
            if (counter == 1) {
                applicationReleaseDTO.setScreenshotName1(scName);
            } else if (counter == 2) {
                applicationReleaseDTO.setScreenshotName2(scName);

            } else if (counter == 3) {
                applicationReleaseDTO.setScreenshotName3(scName);
            }
            counter++;
        }

        // Upload images
        applicationReleaseDTO = applicationStorageManager
                .uploadImageArtifacts(applicationReleaseDTO, applicationArtifact.getIconStream(),
                        applicationArtifact.getBannerStream(), new ArrayList<>(screenshots.values()));
        return applicationReleaseDTO;
    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList = new ApplicationList();
        List<ApplicationDTO> appDTOs;
        List<Application> applications = new ArrayList<>();
        List<ApplicationDTO> filteredApplications = new ArrayList<>();
        DeviceType deviceType = null;

        try {
            //set default values
            if (StringUtils.isEmpty(filter.getSortBy())) {
                filter.setSortBy("ASC");
            }
            if (filter.getLimit() == 0) {
                filter.setLimit(20);
            }
            String deviceTypename = filter.getDeviceType();
            if (!StringUtils.isEmpty(deviceTypename)) {
                deviceType = getDeviceTypeData(deviceTypename);
            }

            ConnectionManagerUtil.openDBConnection();

            validateFilter(filter);
            if (deviceType == null) {
                appDTOs = applicationDAO.getApplications(filter, 0, tenantId);
            } else {
                appDTOs = applicationDAO.getApplications(filter, deviceType.getId(), tenantId);
            }

            for (ApplicationDTO app : appDTOs) {
                boolean isSearchingApp = true;
                List<String> filteringTags = filter.getTags();
                List<String> filteringCategories = filter.getAppCategories();
                List<String> filteringUnrestrictedRoles = filter.getUnrestrictedRoles();

                if (!lifecycleStateManager.getEndState().equals(app.getStatus())) {
                    List<String> appUnrestrictedRoles = visibilityDAO.getUnrestrictedRoles(app.getId(), tenantId);
                    if ((appUnrestrictedRoles.isEmpty() || hasUserRole(appUnrestrictedRoles, userName)) && (
                            filteringUnrestrictedRoles == null || filteringUnrestrictedRoles.isEmpty()
                                    || hasAppUnrestrictedRole(appUnrestrictedRoles, filteringUnrestrictedRoles,
                                    userName))) {
                        if (filteringCategories != null && !filteringCategories.isEmpty()) {
                            List<String> appTagList = applicationDAO.getAppCategories(app.getId(), tenantId);
                            boolean isAppCategory = false;
                            for (String category : filteringCategories) {
                                if (appTagList.contains(category)) {
                                    isAppCategory = true;
                                    break;
                                }
                            }
                            if (!isAppCategory) {
                                isSearchingApp = false;
                            }
                        }
                        if (filteringTags != null && !filteringTags.isEmpty()) {
                            List<String> appTagList = applicationDAO.getAppTags(app.getId(), tenantId);
                            boolean isAppTag = false;
                            for (String tag : filteringTags) {
                                if (appTagList.contains(tag)) {
                                    isAppTag = true;
                                    break;
                                }
                            }
                            if (!isAppTag) {
                                isSearchingApp = false;
                            }
                        }
                        if (isSearchingApp) {
                            filteredApplications.add(app);
                        }
                    }
                }
            }

            for(ApplicationDTO appDTO : filteredApplications){
                applications.add(appDtoToAppResponse(appDTO));
            }
            applicationList.setApplications(applications);
            Pagination pagination = new Pagination();
            applicationList.setPagination(pagination);
            applicationList.getPagination().setSize(filter.getOffset());
            applicationList.getPagination().setCount(applicationList.getApplications().size());
            return applicationList;
        }  catch (UnexpectedServerErrorException e){
            throw new ApplicationManagementException(e.getMessage(), e);
        }catch (UserStoreException e) {
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

    private boolean hasAppUnrestrictedRole(List<String> appUnrestrictedRoles, List<String> filteringUnrestrictedRoles,
            String userName) throws BadRequestException, UserStoreException {
        if (!haveAllUserRoles(filteringUnrestrictedRoles, userName)) {
            String msg =
                    "At least one filtering role is not assigned for the user: " + userName + ". Hence user " + userName
                            + " Can't filter applications by giving these unrestricted role list";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (!appUnrestrictedRoles.isEmpty()) {
            for (String role : filteringUnrestrictedRoles) {
                if (appUnrestrictedRoles.contains(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ApplicationRelease createRelease(int applicationId,
            ApplicationReleaseWrapper applicationReleaseWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationRelease applicationRelease;
        if (log.isDebugEnabled()) {
            log.debug("ApplicationDTO release request is received for the application id: " + applicationId);
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find application for the application Id: " + applicationId;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            DeviceType deviceType = getDeviceTypeData(applicationDTO.getDeviceTypeId());
            ApplicationReleaseDTO applicationReleaseDTO = addApplicationReleaseArtifacts(applicationDTO.getType(),
                    deviceType.getName(), releaseWrapperToReleaseDTO(applicationReleaseWrapper),
                    applicationArtifact);
            applicationReleaseDTO = addImageArtifacts(applicationReleaseDTO, applicationArtifact);

            String initialstate = lifecycleStateManager.getInitialState();
            applicationReleaseDTO.setCurrentState(initialstate);
            LifecycleStateDTO lifecycleState = getLifecycleStateInstance(initialstate, initialstate);
            this.lifecycleStateDAO
                    .addLifecycleState(lifecycleState, applicationId, applicationReleaseDTO.getUuid(), tenantId);
            applicationReleaseDTO = this.applicationReleaseDAO
                    .createRelease(applicationReleaseDTO, applicationDTO.getId(), tenantId);
            applicationRelease = releaseDtoToRelease(applicationReleaseDTO);
            ConnectionManagerUtil.commitDBTransaction();
            return applicationRelease;
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while staring application release creating transaction for application Id: "
                            + applicationId, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while adding application release into IoTS app management ApplicationDTO id of the "
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
        } catch (ResourceManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred while uploading application release artifacts. Application ID: " + applicationId;
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application getApplicationById(int appId, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO applicationDTO;
        boolean isVisibleApp = false;
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationDTO = this.applicationDAO.getApplicationById(appId, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find an application for application Id: " + appId;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            List<ApplicationReleaseDTO> filteredApplicationReleaseDTOs = new ArrayList<>();
            for (ApplicationReleaseDTO applicationReleaseDTO : applicationDTO.getApplicationReleaseDTOs()) {
                if (!applicationReleaseDTO.getCurrentState().equals(lifecycleStateManager.getEndState()) && (
                        state == null || applicationReleaseDTO.getCurrentState().equals(state))) {
                    filteredApplicationReleaseDTOs.add(applicationReleaseDTO);
                }
            }
            applicationDTO.setApplicationReleaseDTOs(filteredApplicationReleaseDTOs);
            if (applicationDTO.getApplicationReleaseDTOs().isEmpty()){
                return null;
            }

            List<String> tags = this.applicationDAO.getAppTags(appId, tenantId);
            List<String> categories = this.applicationDAO.getAppCategories(appId, tenantId);
            applicationDTO.setTags(tags);
            //todo when support to add multiple categories this has to be changed
            if (!categories.isEmpty()){
                applicationDTO.setAppCategory(categories.get(0));
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                return appDtoToAppResponse(applicationDTO);
            }

            List<String> unrestrictedRoles = this.visibilityDAO.getUnrestrictedRoles(appId, tenantId);
            if (!unrestrictedRoles.isEmpty()) {
                if (hasUserRole(unrestrictedRoles, userName)) {
                    isVisibleApp = true;
                }
            } else {
                isVisibleApp = true;
            }

            if (!isVisibleApp) {
                String msg = "You are trying to access visibility restricted application. You don't have required "
                        + "roles to view this application,";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            return appDtoToAppResponse(applicationDTO);
        } catch (LifecycleManagementException e){
            String msg = "Error occurred when getting the last state of the application lifecycle flow";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }catch (UserStoreException e) {
            String msg = "User-store exception while getting application with the application id " + appId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationDTO getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO application;
        boolean isAppAllowed = false;
        List<ApplicationReleaseDTO> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByUUID(uuid, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application release UUID:: " + uuid);
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, state);
                application.setApplicationReleaseDTOs(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (hasUserRole(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }
            applicationReleases = getReleases(application, state);
            application.setApplicationReleaseDTOs(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application release UUID " + uuid);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private boolean hasUserRole(Collection<String> unrestrictedRoleList, String userName) throws UserStoreException {
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

    private boolean haveAllUserRoles(Collection<String> unrestrictedRoleList, String userName)
            throws UserStoreException {
        String[] roleList;
        roleList = getRolesOfUser(userName);
        for (String unrestrictedRole : unrestrictedRoleList) {
            for (String role : roleList) {
                if (!unrestrictedRole.equals(role)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidRestrictedRole(Collection<String> unrestrictedRoleList) throws UserStoreException {
        List<String> roleList = new ArrayList<>(Arrays.asList(getRoleNames()));
        return roleList.containsAll(unrestrictedRoleList);
    }

    private String[] getRoleNames() throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        if (userRealm != null) {
            return userRealm.getUserStoreManager().getRoleNames();
        } else {
            String msg = "User realm is not initiated.";
            log.error(msg);
            throw new UserStoreException(msg);
        }
    }

    private String[] getRolesOfUser(String userName) throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String[] roleList;
        if (userRealm != null) {
            userRealm.getUserStoreManager().getRoleNames();
            roleList = userRealm.getUserStoreManager().getRoleListOfUser(userName);
        } else {
            String msg = "User realm is not initiated. Logged in user: " + userName;
            log.error(msg);
            throw new UserStoreException(msg);
        }
        return roleList;
    }

    public ApplicationDTO getApplication(String appType, String appName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO application;
        boolean isAppAllowed = false;
        List<ApplicationReleaseDTO> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplication(appName, appType, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, null);
                application.setApplicationReleaseDTOs(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (hasUserRole(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }

            applicationReleases = getReleases(application, null);
            application.setApplicationReleaseDTOs(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application name " + appName);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO application;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByRelease(appReleaseUUID, tenantId);

            if (application.getUnrestrictedRoles().isEmpty() || hasUserRole(application.getUnrestrictedRoles(),
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
    private List<ApplicationReleaseDTO> getReleases(ApplicationDTO application, String releaseState)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<ApplicationReleaseDTO> applicationReleases;
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " + application
                    .toString());
        }
        applicationReleases = this.applicationReleaseDAO.getReleases(application.getId(), tenantId);
        for (ApplicationReleaseDTO applicationRelease : applicationReleases) {
            LifecycleStateDTO lifecycleState = null;
            try {
                lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            } catch (LifeCycleManagementDAOException e) {
                throw new ApplicationManagementException(
                        "Error occurred while getting the latest lifecycle state for the application release UUID: "
                                + applicationRelease.getUuid(), e);
            }
            if (lifecycleState != null) {
                log.error("todo");
//                applicationRelease.setLifecycleState(lifecycleState);
            }
        }
        return applicationReleases;
//        return filterAppReleaseByCurrentState(applicationReleases, releaseState);
    }

//    private List<ApplicationReleaseDTO> filterAppReleaseByCurrentState(List<ApplicationReleaseDTO> applicationReleases,
//            String state) {
//        List<ApplicationReleaseDTO> filteredReleases = new ArrayList<>();
//
//        if (state != null && !state.isEmpty()) {
//            for (ApplicationReleaseDTO applicationRelease : applicationReleases) {
//                if (state.equals(applicationRelease.getLifecycleState().getCurrentState())) {
//                    filteredReleases.add(applicationRelease);
//                }
//            }
//
//            if (AppLifecycleState.PUBLISHED.toString().equals(state) && filteredReleases.size() > 1) {
//                log.warn("There are more than one application releases is found which is in PUBLISHED state");
//                filteredReleases.sort((r1, r2) -> {
//                    if (r1.getLifecycleState().getUpdatedAt().after(r2.getLifecycleState().getUpdatedAt())) {
//                        return -1;
//                    } else if (r2.getLifecycleState().getUpdatedAt().after(r1.getLifecycleState().getUpdatedAt())) {
//                        return 1;
//                    }
//                    return 0;
//                });
//            }
//            return filteredReleases;
//        }
//        return applicationReleases;
//    }

    @Override public List<String> deleteApplication(int applicationId) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<String> storedLocations = new ArrayList<>();
        ApplicationDTO application;

        try {
            ConnectionManagerUtil.beginDBTransaction();
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);

            if (application == null) {
                throw new NotFoundException("Couldn't found an application for ApplicationDTO ID: " + applicationId);
            }

            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION) && !application
                    .getUnrestrictedRoles().isEmpty() && hasUserRole(application.getUnrestrictedRoles(), userName)) {
                throw new ForbiddenException(
                        "You don't have permission to delete this application. In order to delete an application you "
                                + "need to have required permission. ApplicationDTO ID: " + applicationId);
            }
            List<ApplicationReleaseDTO> applicationReleases = getReleases(application, null);
            if (log.isDebugEnabled()) {
                log.debug("Request is received to delete applications which are related with the application id "
                        + applicationId);
            }
            for (ApplicationReleaseDTO applicationRelease : applicationReleases) {
                LifecycleStateDTO appLifecycleState = this.lifecycleStateDAO
                        .getLatestLifeCycleState(applicationId, applicationRelease.getUuid());
                LifecycleStateDTO newAppLifecycleState = getLifecycleStateInstance(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                if (lifecycleStateManager.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState(), userName, tenantId)) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                } else {
                    String currentState = appLifecycleState.getCurrentState();
                    List<String> lifecycleFlow = searchLifecycleStateFlow(currentState,
                            AppLifecycleState.REMOVED.toString());
                    for (String nextState : lifecycleFlow) {
                        LifecycleStateDTO lifecycleState = getLifecycleStateInstance(nextState, currentState);
                        if (lifecycleStateManager.isValidStateChange(currentState, nextState, userName, tenantId)) {
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
                Set<String> nextStates = lifecycleStateManager.getNextLifecycleStates(currentNode);
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

    @Override
    public String deleteApplicationRelease(int applicationId, String releaseUuid)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO application;
        try {
            ConnectionManagerUtil.beginDBTransaction();
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application ID: " + applicationId);
            }
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION) && !application
                    .getUnrestrictedRoles().isEmpty() && hasUserRole(application.getUnrestrictedRoles(), userName)) {
                throw new ForbiddenException(
                        "You don't have permission for deleting application release. ApplicationDTO id: " + applicationId
                                + " and release UUID: " + releaseUuid);
            }

            ApplicationReleaseDTO applicationRelease = this.applicationReleaseDAO
                    .getReleaseByIds(applicationId, releaseUuid, tenantId);
            if (applicationRelease == null) {
                throw new NotFoundException("Couldn't find an application release for application ID: " + applicationId
                        + " and release UUID: " + releaseUuid);
            }
            LifecycleStateDTO appLifecycleState = this.lifecycleStateDAO
                    .getLatestLifeCycleState(applicationId, releaseUuid);
            if (appLifecycleState == null) {
                throw new NotFoundException(
                        "Couldn't find an lifecycle sate for application ID: " + applicationId + " and UUID: "
                                + releaseUuid);
            }
            String currentState = appLifecycleState.getCurrentState();
            if (AppLifecycleState.DEPRECATED.toString().equals(currentState) || AppLifecycleState.REJECTED.toString()
                    .equals(currentState) || AppLifecycleState.UNPUBLISHED.toString().equals(currentState)) {
                LifecycleStateDTO newAppLifecycleState = getLifecycleStateInstance(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                if (lifecycleStateManager.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState(), userName, tenantId)) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    List<String> lifecycleFlow = searchLifecycleStateFlow(currentState,
                            AppLifecycleState.REMOVED.toString());
                    for (String nextState : lifecycleFlow) {
                        LifecycleStateDTO lifecycleState = getLifecycleStateInstance(nextState, currentState);
                        if (lifecycleStateManager.isValidStateChange(currentState, nextState, userName, tenantId)) {
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
                    "Error occured when checking permission for executing application release update. ApplicationDTO ID: "
                            + applicationId + " and ApplicationDTO UUID: " + releaseUuid);
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

    /***
     * To verify whether application type is valid one or not
     * @param appType application type {@link ApplicationType}
     * @return true returns if appType is valid on, otherwise returns false
     */
    private boolean isValidAppType(String appType) {
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

    @Override
    //todo with this implementation user has to provide all image artifacts in order to update one image artifact as
    // well. Therefore enhance this later.
    public void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationReleaseDTO applicationReleaseDTO;
        try {

            ConnectionManagerUtil.beginDBTransaction();
            applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Application release image artifact uploading is failed. Doesn't exist a application "
                        + "release for application ID: application UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            String currentState = applicationReleaseDTO.getCurrentState();
            if (!lifecycleStateManager.isUpdatableState(currentState)){
                throw new ForbiddenException(
                        "Can't Update the application release. Application release is in " + currentState
                                + " and it is not an release updatable state. Hence please move the application release"
                                + " into updatable state and retry the operation.");
            }
            applicationStorageManager.deleteImageArtifacts(applicationReleaseDTO);

            applicationReleaseDTO = this.applicationReleaseDAO
                    .updateRelease(addImageArtifacts(applicationReleaseDTO, applicationArtifact), tenantId);
            if (applicationReleaseDTO == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "ApplicationDTO release updating count is 0.  ApplicationDTO release UUID is " + uuid;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException e) {
            String msg =
                    "Error occured when getting DB connection to update image artifacts of the application release of haveing  uuid "
                            + uuid + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occured while getting application release data for updating image artifacts of the application release uuid "
                            + uuid + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ResourceManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while updating image artifacts of the application release uuid " + uuid + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg , e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateApplicationArtifact(String deviceType, String appType, String uuid,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        boolean isValidDeviceType = false;
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = Util.getDeviceManagementService().getDeviceTypes();

            for (DeviceType dt : deviceTypes) {
                if (dt.getName().equals(deviceType)) {
                    isValidDeviceType = true;
                    break;
                }
            }
            if (!isValidDeviceType) {
                String msg = "Invalid request to update application release artifact, invalid application type: "
                        + deviceType + " for application release uuid: " + uuid;
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occured while getting supported device types in IoTS";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't found an application release for UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            String deletingAppHashValue = applicationReleaseDTO.getAppHashValue();
            applicationReleaseDTO = addApplicationReleaseArtifacts(appType, deviceType, applicationReleaseDTO,
                    applicationArtifact);
            applicationStorageManager.copyImageArtifactsAndDeleteInstaller(deletingAppHashValue, applicationReleaseDTO);
            applicationReleaseDTO = this.applicationReleaseDAO.updateRelease(applicationReleaseDTO, tenantId);
            if (applicationReleaseDTO == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationManagementException(
                        "ApplicationDTO release updating count is 0.  ApplicationDTO release UUID: " + uuid);

            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while getting/updating APPM DB for updating application Installer.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg =
                    "Error occured while starting the transaction to update application release artifact of the application uuid "
                            + uuid + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg =
                    "Error occured when getting DB connection to update application release artifact of the application release uuid "
                            + uuid + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationStorageManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException("In order to update the artifact, couldn't find it in the system",
                    e);
        } catch (ResourceManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured when updating application installer.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * To get role restricted application list.
     *
     * @param applicationList list of applications.
     * @param userName        user name
     * @return ApplicationDTO related with the UUID
     */
//    private ApplicationList getRoleRestrictedApplicationList(ApplicationList applicationList, String userName)
//            throws ApplicationManagementException {
//        ApplicationList roleRestrictedApplicationList = new ApplicationList();
//        ArrayList<ApplicationDTO> unRestrictedApplications = new ArrayList<>();
//        for (ApplicationDTO application : applicationList.getApplications()) {
//            if (application.getUnrestrictedRoles().isEmpty()) {
//                unRestrictedApplications.add(application);
//            } else {
//                try {
//                    if (hasUserRole(application.getUnrestrictedRoles(), userName)) {
//                        unRestrictedApplications.add(application);
//                    }
//                } catch (UserStoreException e) {
//                    throw new ApplicationManagementException("Role restriction verifying is failed");
//                }
//            }
//        }
//        roleRestrictedApplicationList.setApplications(unRestrictedApplications);
//        return roleRestrictedApplicationList;
//    }

    /**
     * To validate a app release creating request and app updating request to make sure all the pre-conditions
     * satisfied.
     *
     * @param applicationRelease ApplicationReleaseDTO that need to be created.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    private void validateAppReleasePayload(ApplicationReleaseDTO applicationRelease)
            throws ApplicationManagementException {
        if (applicationRelease.getVersion() == null) {
            throw new ApplicationManagementException("ApplicationReleaseDTO version name is a mandatory parameter for "
                    + "creating release. It cannot be found.");
        }
    }

    @Override
    public LifecycleStateDTO getLifecycleState(int applicationId, String releaseUuid)
            throws ApplicationManagementException {
        LifecycleStateDTO lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (lifecycleState == null) {
                return null;
            }
            lifecycleState.setNextStates(new ArrayList<>(lifecycleStateManager.getNextLifecycleStates(lifecycleState.getCurrentState())));

        } catch (LifeCycleManagementDAOException e) {
            throw new ApplicationManagementException("Failed to get lifecycle state from database", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override
    public void changeLifecycleState(int applicationId, String releaseUuid, LifecycleStateDTO state)
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
            LifecycleStateDTO currentState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (currentState == null) {
                throw new ApplicationManagementException(
                        "Couldn't find latest lifecycle state for the appId: " + applicationId
                                + " and application release UUID: " + releaseUuid);
            }
            state.setPreviousState(currentState.getCurrentState());
            String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            state.setUpdatedBy(userName);

            if (state.getCurrentState() != null && state.getPreviousState() != null) {
                if (lifecycleStateManager.isValidStateChange(state.getPreviousState(), state.getCurrentState(),
                        userName, tenantId)) {
                    //todo if current state of the adding lifecycle state is PUBLISHED, need to check whether is there
                    //todo any other application release in PUBLISHED state for the application( i.e for the appid)
                    this.lifecycleStateDAO.addLifecycleState(state, applicationId, releaseUuid, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    log.error("Invalid lifecycle state transition from '" + state.getPreviousState() + "'" + " to '"
                            + state.getCurrentState() + "'");
                    throw new ApplicationManagementException(
                            "Lifecycle State Validation failed. ApplicationDTO Id: " + applicationId
                                    + " ApplicationDTO release UUID: " + releaseUuid);
                }
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Failed to add lifecycle state. ApplicationDTO Id: " + applicationId + " ApplicationDTO release UUID: "
                            + releaseUuid, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addAplicationCategories(List<String> categories) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            List<CategoryDTO> existingCategories = applicationDAO.getAllCategories(tenantId);
            List<String> existingCategoryNames = existingCategories.stream().map(CategoryDTO::getCategoryName)
                    .collect(Collectors.toList());
            if(!existingCategoryNames.containsAll(categories)){
                List<String> newCategories = getDifference(categories, existingCategoryNames);
                applicationDAO.addCategories(newCategories, tenantId);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured when getting existing categories or when inserting new application categories.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateApplication(int applicationId, ApplicationWrapper applicationWrapper)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO applicationDTO;

        try {
            ConnectionManagerUtil.beginDBTransaction();
            applicationDTO = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (applicationDTO == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Tried to update Application which is not in the publisher. Please verify "
                        + "application details";
                log.error(msg);
                throw new NotFoundException(msg);
            }

            if (!StringUtils.isEmpty(applicationWrapper.getType()) && !applicationDTO.getType()
                    .equals(applicationWrapper.getType())) {
                String msg = "You are trying to change the application type and it is not "
                        + "possible after you create an application. Therefore please remove this application and "
                        + "publish new application with type: " + applicationWrapper.getType();
                log.error(msg);
                throw new BadRequestException(msg);
            }

            String deviceTypeName = applicationWrapper.getDeviceType();
            if (!StringUtils.isEmpty(deviceTypeName)) {
                DeviceType deviceType = getDeviceTypeData(deviceTypeName);
                if (!deviceType.getName().equals(deviceTypeName)){
                    String msg = "You are trying to change the compatible device type of the application type and it is "
                            + "not possible after you create an application for device type. " + deviceTypeName  +
                            "Therefore please remove this application and publish new application with device type: " +
                            deviceTypeName;
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
            }
            if (!StringUtils.isEmpty(applicationWrapper.getName())){
                Filter filter = new Filter();
                filter.setFullMatch(true);
                filter.setAppName(applicationWrapper.getName().trim());
                filter.setOffset(0);
                filter.setLimit(1);

                List<ApplicationDTO> applicationList = applicationDAO
                        .getApplications(filter, applicationDTO.getDeviceTypeId(), tenantId);
                if (!applicationList.isEmpty()) {
                    String msg = "Already an application registered with same name " + applicationWrapper.getName()
                            + ". Hence you can't update the application name from " + applicationDTO.getName() + " to "
                            + applicationWrapper.getName();
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                applicationDTO.setName(applicationWrapper.getName());
            }
            if (!StringUtils.isEmpty(applicationWrapper.getSubType()) && !applicationDTO.getSubType()
                    .equals(applicationWrapper.getSubType())) {
                if (!ApplicationSubscriptionType.PAID.toString().equals(applicationWrapper.getSubType())
                        && !ApplicationSubscriptionType.FREE.toString().equals(applicationWrapper.getSubType())) {
                    String msg = "Invalid application subscription type is found with application updating request "
                            + applicationWrapper.getSubType();
                    log.error(msg);
                    throw new BadRequestException(msg);

                } else if (ApplicationSubscriptionType.FREE.toString().equals(applicationWrapper.getSubType()) && !StringUtils
                        .isEmpty(applicationWrapper.getPaymentCurrency())) {
                    String msg = "If you are going to change Non-Free app as Free app, "
                            + "currency attribute in the application updating payload should be null or \"\"";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                } else if (ApplicationSubscriptionType.PAID.toString().equals(applicationWrapper.getSubType())
                        && StringUtils.isEmpty(applicationWrapper.getPaymentCurrency()) ){
                    String msg = "If you are going to change Free app as Non-Free app, "
                            + "currency attribute in the application payload should not be null or \"\"";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }

                applicationDTO.setSubType(applicationWrapper.getSubType());
                applicationDTO.setPaymentCurrency(applicationWrapper.getPaymentCurrency());
            }

            if (!StringUtils.isEmpty(applicationWrapper.getDescription())){
                applicationDTO.setDescription(applicationWrapper.getDescription());
            }

            List<String> appUnrestrictedRoles = this.visibilityDAO.getUnrestrictedRoles(applicationId, tenantId);

            boolean isExistingAppRestricted = !appUnrestrictedRoles.isEmpty();
            boolean isUpdatingAppRestricted = !applicationWrapper.getUnrestrictedRoles().isEmpty();

            if (isExistingAppRestricted && !isUpdatingAppRestricted) {
                visibilityDAO.deleteUnrestrictedRoles(appUnrestrictedRoles, applicationId, tenantId);
            } else if (isUpdatingAppRestricted) {
                if (!hasUserRole(applicationWrapper.getUnrestrictedRoles(), userName)) {
                    String msg =
                            "You are trying to restrict the visibility of visible application.But you are trying to "
                                    + "restrict the visibility to roles that there isn't at least one role is assigned "
                                    + "to user: " + userName + ". Therefore, it is not allowed and you should have "
                                    + "added at least one role that assigned to " + userName + " user into "
                                    + "restricting role set.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }

                if (!isExistingAppRestricted) {
                    visibilityDAO
                            .addUnrestrictedRoles(applicationWrapper.getUnrestrictedRoles(), applicationId, tenantId);
                } else {
                    List<String> addingRoleList = getDifference(applicationWrapper.getUnrestrictedRoles(),
                            applicationDTO.getUnrestrictedRoles());
                    List<String> removingRoleList = getDifference(applicationDTO.getUnrestrictedRoles(),
                            applicationWrapper.getUnrestrictedRoles());
                    if (!addingRoleList.isEmpty()) {
                        visibilityDAO.addUnrestrictedRoles(addingRoleList, applicationId, tenantId);
                    }
                    if (!removingRoleList.isEmpty()) {
                        visibilityDAO.deleteUnrestrictedRoles(removingRoleList, applicationId, tenantId);
                    }
                }
            }
            applicationDTO.setUnrestrictedRoles(applicationWrapper.getUnrestrictedRoles());

            String updatingAppCategory = applicationWrapper.getAppCategory();
            if ( updatingAppCategory != null){
                List<String> appCategories = this.applicationDAO.getAppCategories(applicationId, tenantId);
                if (!appCategories.contains(updatingAppCategory)){
                    List<CategoryDTO> allCategories = this.applicationDAO.getAllCategories(tenantId);
                    List<Integer> categoryIds = allCategories.stream()
                            .filter(category -> category.getCategoryName().equals(updatingAppCategory))
                            .map(CategoryDTO::getId).collect(Collectors.toList());
                    if (!categoryIds.isEmpty()){
                        String msg =
                                "You are trying to update application category into invalid application category, "
                                        + "it is not registered in the system. Therefore please register the category "
                                        + updatingAppCategory + " and perform the action";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    this.applicationDAO.addCategoryMapping(categoryIds, applicationId, tenantId);
                }
            }

            List<String> updatingAppTags = applicationWrapper.getTags();
            if ( updatingAppTags!= null){
                List<String> appTags = this.applicationDAO.getAppTags(applicationId, tenantId);
                List<String> addingTagList = getDifference(appTags, updatingAppTags);
                List<String> removingTagList = getDifference(updatingAppTags, appTags);
                if (!addingTagList.isEmpty()) {
                    List<TagDTO> allTags = this.applicationDAO.getAllTags(tenantId);
                    List<String> newTags = addingTagList.stream().filter(updatingTagName -> allTags.stream()
                            .noneMatch(tag -> tag.getTagName().equals(updatingTagName))).collect(Collectors.toList());
                    if (!newTags.isEmpty()){
                        this.applicationDAO.addTags(newTags, tenantId);
                    }
                    List<Integer> addingTagIds = this.applicationDAO.getTagIdsForTagNames(addingTagList, tenantId);
                    this.applicationDAO.addTagMapping(addingTagIds, applicationId, tenantId);
                }
                if (!removingTagList.isEmpty()) {
                    List<Integer> removingTagIds = this.applicationDAO.getTagIdsForTagNames(removingTagList, tenantId);
                    this.applicationDAO.deleteTagMapping(removingTagIds, applicationId, tenantId);
                    applicationDAO.deleteTags(removingTagList, applicationId, tenantId);
                }
            }
            //todo
            applicationDAO.editApplication(applicationDTO, tenantId);
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
                    "Error occurred while updating the visibility restriction of the application. ApplicationDTO id  is "
                            + applicationId);
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException(
                    "Error occurred while starting database transaction for application updating. ApplicationDTO id  is "
                            + applicationId);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while getting database connection for application updating. ApplicationDTO id  is "
                            + applicationId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private void validateFilter(Filter filter) throws BadRequestException {
        if (filter == null) {
            String msg = "Filter validation is failed, Filter shouldn't be null, hence please verify the request payload";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        String appType = filter.getAppType();

        if (!StringUtils.isEmpty(appType)) {
            boolean isValidAppType = false;
            for (ApplicationType applicationType : ApplicationType.values()) {
                if (applicationType.toString().equals(appType)) {
                    isValidAppType = true;
                    break;
                }
            }
            if (!isValidAppType) {
                String msg =
                        "Filter validation is failed, Invalid application type is found in filter. Application Type: "
                                + appType + " Please verify the request payload";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        }

        RatingConfiguration ratingConfiguration = ConfigurationManager.getInstance().getConfiguration()
                .getRatingConfiguration();

        int defaultMinRating = ratingConfiguration.getMinRatingValue();
        int defaultMaxRating = ratingConfiguration.getMaxRatingValue();
        int filteringMinRating = filter.getMinimumRating();

        if (filteringMinRating != 0 && (filteringMinRating < defaultMinRating || filteringMinRating > defaultMaxRating))
        {
            String msg = "Filter validation is failed, Minimum rating value: " + filteringMinRating
                    + " is not in the range of default minimum rating value " + defaultMaxRating
                    + " and default maximum rating " + defaultMaxRating;
            log.error(msg);
            throw new BadRequestException(msg);
        }

        String appReleaseState = filter.getAppReleaseState();
        if (!StringUtils.isEmpty(appReleaseState) && !lifecycleStateManager.isStateExist(appReleaseState)) {
            String msg = "Filter validation is failed, Requesting to filter by invalid app release state: "
                    + appReleaseState;
            log.error(msg);
            throw new BadRequestException(msg);
        }

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
     * @return {@link LifecycleStateDTO}
     */
    private LifecycleStateDTO getLifecycleStateInstance(String currentState, String previousState) {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        LifecycleStateDTO lifecycleState = new LifecycleStateDTO();
        lifecycleState.setCurrentState(currentState);
        lifecycleState.setPreviousState(previousState);
        lifecycleState.setUpdatedBy(userName);
        return lifecycleState;
    }

    //todo check whether package names are same
    @Override
    public boolean updateRelease(int applicationId, String releaseUuid, String deviceType,
            ApplicationReleaseDTO updateRelease, InputStream binaryFileStram, InputStream iconFileStream,
            InputStream bannerFileStream, List<InputStream> attachments) throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationReleaseDTO release;
        ApplicationDTO app = null;
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
                        "Couldn't found an application for updating. ApplicationDTO id: " + applicationId);
            }

            if (deviceTypeObj == null || deviceTypeObj.getId() != app.getDeviceTypeId()) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new BadRequestException(
                        "Request to update application release for Invalid device type. Device type: " + deviceType
                                + " application ID " + applicationId + " ApplicationDTO Release UUID " + releaseUuid);
            }
            if (release == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new NotFoundException(
                        "Couldn't found an application realise for updating. ApplicationDTO id: " + applicationId
                                + " and application release UUID: " + releaseUuid);
            }

            String releaseType = updateRelease.getReleaseType();
            Double price = updateRelease.getPrice();
            String metaData = updateRelease.getMetaData();

            if (price < 0.0 || (price == 0.0 && ApplicationSubscriptionType.PAID.toString().equals(app.getSubType()))
                    || (price > 0.0 && ApplicationSubscriptionType.FREE.toString().equals(app.getSubType()))) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new BadRequestException(
                        "Invalid app release payload for updating application release. ApplicationDTO price is " + price
                                + " for " + app.getSubType() + " application. ApplicationDTO ID: " + applicationId
                                + ", ApplicationDTO Release UUID " + releaseUuid + " and supported device type is "
                                + deviceType);
            }
            release.setPrice(price);
            if (releaseType != null) {
                release.setReleaseType(releaseType);
            }
            if (metaData != null) {
                release.setMetaData(metaData);
            }

            List<String> unrestrictedRoles = app.getUnrestrictedRoles();

            String applicationReleaseCreatedUser = lifecycleStateDAO
                    .getAppReleaseCreatedUsername(applicationId, releaseUuid, tenantId);

            if (!isAdminUser && !(!unrestrictedRoles.isEmpty() && hasUserRole(unrestrictedRoles, userName))
                    && !userName.equals(applicationReleaseCreatedUser)) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ForbiddenException("You are not authorized user to update application");
            }

            //todo try to remove this DB call and get it when getting application release
            LifecycleStateDTO lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (!AppLifecycleState.CREATED.toString().equals(lifecycleState.getCurrentState())
                    && !AppLifecycleState.IN_REVIEW.toString().equals(lifecycleState.getCurrentState())
                    && !AppLifecycleState.REJECTED.toString().equals(lifecycleState.getCurrentState())) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ForbiddenException(
                        "You can't update application release which is in " + lifecycleState.getCurrentState()
                                + " State");
            }

//            release = applicationStorageManager
//                    .deleteImageArtifacts(release, iconFileStream, bannerFileStream, attachments);
//            release = applicationStorageManager
//                    .copyImageArtifactsAndDeleteInstaller(release, app.getType(), deviceType, binaryFileStram);
            return applicationReleaseDAO.updateRelease(release, tenantId) != null;
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
                    "Error occured when updating ApplicationDTO release. ApplicationDTO ID " + applicationId
                            + " ApplicationDTO Release UUID: " + releaseUuid, e);
        }
//        catch (ApplicationStorageManagementException e) {
//            ConnectionManagerUtil.rollbackDBTransaction();
//            throw new ApplicationManagementException(
//                    "Error occured when updating application release artifact. ApplicationDTO ID " + applicationId
//                            + " ApplicationDTO release UUID: " + releaseUuid, e);
//        }
//        catch (ResourceManagementException e) {
//            //            updating images
//            ConnectionManagerUtil.rollbackDBTransaction();
//            throw new ApplicationManagementException(
//                    "Error occured when updating image artifact of the application release. ApplicationDTO ID: "
//                            + applicationId + " ApplicationDTO release UUID: " + releaseUuid, e);
//        } catch (RequestValidatingException e) {
//            ConnectionManagerUtil.rollbackDBTransaction();
//            throw new ApplicationManagementException(
//                    "Error occured when validating application release artifact for device type " + deviceType
//                            + " And application type " + app.getType() + ". Applicationn ID: " + applicationId
//                            + " ApplicationDTO release UUID: " + releaseUuid);
//        }
    }


    @Override
    public void validateAppCreatingRequest(ApplicationWrapper applicationWrapper) throws RequestValidatingException {

        String applicationType = applicationWrapper.getType();

        if (StringUtils.isEmpty(applicationWrapper.getName())) {
            String msg = "Application name cannot be empty.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (StringUtils.isEmpty(applicationWrapper.getAppCategory())) {
            String msg = "Application category can't be empty.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (StringUtils.isEmpty(applicationType)) {
            String msg = "Application type can't be empty.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (StringUtils.isEmpty(applicationWrapper.getDeviceType())) {
            String msg = "Device type can't be empty for the application.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }

        List<ApplicationReleaseWrapper> applicationReleaseWrappers;
        applicationReleaseWrappers = applicationWrapper.getApplicationReleaseWrappers();

        if (applicationReleaseWrappers == null || applicationReleaseWrappers.size() != 1) {
            String msg =
                    "Invalid application creating request. ApplicationDTO creating request must have single application "
                            + "release.  ApplicationDTO name:" + applicationWrapper.getName() + " and type: " + applicationWrapper
                            .getType();
            log.error(msg);
            throw new RequestValidatingException(msg);
        }

    }


    @Override
    public void validateReleaseCreatingRequest(ApplicationReleaseWrapper applicationReleaseWrapper,
            String applicationType) throws RequestValidatingException {

        if (applicationReleaseWrapper == null){
            String msg = "Application Release shouldn't be null.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }

        if (ApplicationType.WEB_CLIP.toString().equals(applicationType)) {
            UrlValidator urlValidator = new UrlValidator();
            if (StringUtils
                    .isEmpty(applicationReleaseWrapper.getUrl())){
                String msg = "URL should't be null for the application release creating request for application type WEB_CLIP";
                log.error(msg);
                throw new RequestValidatingException(msg);
            }
            if (!urlValidator.isValid(applicationReleaseWrapper.getUrl())){
                String msg = "Request payload contains an invalid Web Clip URL.";
                log.error(msg);
                throw new RequestValidatingException(msg);
            }
        }
        if (StringUtils.isEmpty(applicationReleaseWrapper.getSupportedOsVersions())){
            String msg = "Supported OS Version shouldn't be null or empty.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
    }


    @Override
    public void validateImageArtifacts(Attachment iconFile, Attachment bannerFile,
            List<Attachment> attachmentList) throws RequestValidatingException {
        if (iconFile == null) {
            String msg = "Icon file is not found with the application release creating request.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (bannerFile == null) {
            String msg = "Banner file is not found with the application release creating request.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (attachmentList == null || attachmentList.isEmpty()) {
            String msg = "Screenshots are not found with the application release creating request.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
    }

    @Override
    public void validateBinaryArtifact(Attachment binaryFile, String applicationType) throws RequestValidatingException {

        if (StringUtils.isEmpty(applicationType)) {
            String msg = "Application type can't be empty.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (!isValidAppType(applicationType)) {
            String msg = "App Type contains in the application creating payload doesn't match with supported app types.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
        if (binaryFile == null && ApplicationType.ENTERPRISE.toString().equals(applicationType)) {
            String msg = "Binary file is not found with the application release creating request. ApplicationDTO type: "
                    + applicationType;
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
    }


    private ApplicationDTO appWrapperToAppDTO(ApplicationWrapper applicationWrapper)
            throws BadRequestException, UnexpectedServerErrorException {

        DeviceType deviceType = getDeviceTypeData(applicationWrapper.getDeviceType());
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setName(applicationWrapper.getName());
        applicationDTO.setDescription(applicationWrapper.getDescription());
        applicationDTO.setAppCategory(applicationWrapper.getAppCategory());
        applicationDTO.setType(applicationWrapper.getType());
        applicationDTO.setSubType(applicationWrapper.getSubType());
        applicationDTO.setPaymentCurrency(applicationWrapper.getPaymentCurrency());
        applicationDTO.setTags(applicationWrapper.getTags());
        applicationDTO.setUnrestrictedRoles(applicationWrapper.getUnrestrictedRoles());
        applicationDTO.setDeviceTypeId(deviceType.getId());
        List<ApplicationReleaseDTO> applicationReleaseEntities = applicationWrapper.getApplicationReleaseWrappers()
                .stream().map(this::releaseWrapperToReleaseDTO).collect(Collectors.toList());
        applicationDTO.setApplicationReleaseDTOs(applicationReleaseEntities);
        return applicationDTO;
    }

    private ApplicationReleaseDTO releaseWrapperToReleaseDTO(ApplicationReleaseWrapper applicationReleaseWrapper){
        ApplicationReleaseDTO applicationReleaseDTO = new ApplicationReleaseDTO();
        applicationReleaseDTO.setDescription(applicationReleaseWrapper.getDescription());
        applicationReleaseDTO.setReleaseType(applicationReleaseWrapper.getReleaseType());
        applicationReleaseDTO.setPrice(applicationReleaseWrapper.getPrice());
        applicationReleaseDTO.setIsSharedWithAllTenants(applicationReleaseWrapper.getIsSharedWithAllTenants());
        applicationReleaseDTO.setMetaData(applicationReleaseWrapper.getMetaData());
        applicationReleaseDTO.setUrl(applicationReleaseWrapper.getUrl());
        applicationReleaseDTO.setSupportedOsVersions(applicationReleaseWrapper.getSupportedOsVersions());
        return applicationReleaseDTO;
    }

    private Application appDtoToAppResponse(ApplicationDTO applicationDTO)
            throws BadRequestException, UnexpectedServerErrorException {

        Application application = new Application();
        DeviceType deviceType = getDeviceTypeData(applicationDTO.getDeviceTypeId());
        application.setId(applicationDTO.getId());
        application.setName(applicationDTO.getName());
        application.setDescription(applicationDTO.getDescription());
        application.setAppCategory(applicationDTO.getAppCategory());
        application.setType(applicationDTO.getType());
        application.setSubType(applicationDTO.getSubType());
        application.setPaymentCurrency(applicationDTO.getPaymentCurrency());
        application.setTags(applicationDTO.getTags());
        application.setUnrestrictedRoles(applicationDTO.getUnrestrictedRoles());
        application.setDeviceType(deviceType.getName());
        List<ApplicationRelease> applicationReleases = applicationDTO.getApplicationReleaseDTOs()
                .stream().map(this::releaseDtoToRelease).collect(Collectors.toList());
        application.setApplicationReleases(applicationReleases);
        return application;
    }

    private ApplicationRelease releaseDtoToRelease(ApplicationReleaseDTO applicationReleaseDTO){
        String artifactDownloadEndpoint = ConfigurationManager.getInstance().getConfiguration()
                .getArtifactDownloadEndpoint();
        String basePath = artifactDownloadEndpoint + Constants.FORWARD_SLASH + applicationReleaseDTO.getUuid();
        ApplicationRelease applicationRelease = new ApplicationRelease();
        applicationRelease.setDescription(applicationReleaseDTO.getDescription());
        applicationRelease.setUuid(applicationReleaseDTO.getUuid());
        applicationRelease.setReleaseType(applicationReleaseDTO.getReleaseType());
        applicationRelease.setPrice(applicationReleaseDTO.getPrice());
        applicationRelease.setIsSharedWithAllTenants(applicationReleaseDTO.getIsSharedWithAllTenants());
        applicationRelease.setMetaData(applicationReleaseDTO.getMetaData());
        applicationRelease.setUrl(applicationReleaseDTO.getUrl());
        applicationRelease.setCurrentStatus(applicationReleaseDTO.getCurrentState());
        applicationRelease.setIsSharedWithAllTenants(applicationReleaseDTO.getIsSharedWithAllTenants());
        applicationRelease.setSupportedOsVersions(applicationReleaseDTO.getSupportedOsVersions());
        applicationRelease
                .setInstallerPath(basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getInstallerName());
        applicationRelease.setIconPath(basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getIconName());
        applicationRelease.setBannerPath(basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getBannerName());

        if (!StringUtils.isEmpty(applicationReleaseDTO.getScreenshotName1())) {
            applicationRelease.setScreenshotPath1(
                    basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getScreenshotName1());
        }
        if (!StringUtils.isEmpty(applicationReleaseDTO.getScreenshotName2())) {
            applicationRelease.setScreenshotPath2(
                    basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getScreenshotName2());
        }
        if (!StringUtils.isEmpty(applicationReleaseDTO.getScreenshotName3())) {
            applicationRelease.setScreenshotPath3(
                    basePath + Constants.FORWARD_SLASH + applicationReleaseDTO.getScreenshotName3());
        }
        return applicationRelease;
    }

    private <T> DeviceType getDeviceTypeData( T deviceTypeAttr)
            throws BadRequestException, UnexpectedServerErrorException {
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = Util.getDeviceManagementService().getDeviceTypes();

            if(deviceTypeAttr instanceof String){
                for (DeviceType dt : deviceTypes) {
                    if (dt.getName().equals(deviceTypeAttr)) {
                        return dt;
                    }
                }
            } else if (deviceTypeAttr instanceof  Integer){
                for (DeviceType dt : deviceTypes) {
                    if (dt.getId() == (Integer) deviceTypeAttr) {
                        return dt;
                    }
                }
            } else {
                String msg = "Invalid device type class is received. Device type class: " + deviceTypeAttr.getClass()
                        .getName();
                log.error(msg);
                throw new BadRequestException(msg);
            }

            String msg =
                    "Invalid device type Attribute is found with the request. Device Type attribute: " + deviceTypeAttr;
            log.error(msg);
            throw new BadRequestException(msg);

        } catch (DeviceManagementException e) {
            String msg = "Error occured when getting device types which are supported by the Entgra IoTS";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
    }
}
