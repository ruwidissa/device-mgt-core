/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationArtifact;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstaller;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.LifecycleChanger;
import org.wso2.carbon.device.application.mgt.common.Pagination;
import org.wso2.carbon.device.application.mgt.common.config.RatingConfiguration;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionType;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.dto.CategoryDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
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
import org.wso2.carbon.device.application.mgt.common.response.Category;
import org.wso2.carbon.device.application.mgt.common.response.Tag;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.PublicAppReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.PublicAppWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.WebAppReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.WebAppWrapper;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
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
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private LifecycleStateDAO lifecycleStateDAO;
    private SubscriptionDAO subscriptionDAO;
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
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
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
        if (log.isDebugEnabled()) {
            log.debug("Application create request is received for the tenant : " + tenantId + " From" + " the user : "
                    + userName);
        }

        ApplicationDTO applicationDTO = APIUtil.convertToAppDTO(applicationWrapper);

        //uploading application artifacts
        try {
            ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
            applicationReleaseDTO = addApplicationReleaseArtifacts(applicationDTO.getType(),
                    applicationWrapper.getDeviceType(), applicationReleaseDTO, applicationArtifact, false);
            applicationReleaseDTO = addImageArtifacts(applicationReleaseDTO, applicationArtifact);
            applicationDTO.getApplicationReleaseDTOs().clear();
            applicationDTO.getApplicationReleaseDTOs().add(applicationReleaseDTO);
        } catch (ResourceManagementException e) {
            String msg = "Error Occured when uploading artifacts of the application: " + applicationWrapper.getName();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
        return addAppDataIntoDB(applicationDTO, tenantId);
    }

    private void deleteApplicationArtifacts(List<String> directoryPaths) throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();

        try {
            applicationStorageManager.deleteAllApplicationReleaseArtifacts(directoryPaths);
        } catch (ApplicationStorageManagementException e) {
            String errorLog = "Error occurred when deleting application artifacts. directory paths: ." + directoryPaths
                    .toString();
            log.error(errorLog);
            throw new ApplicationManagementException(errorLog, e);
        }
    }

    private ApplicationReleaseDTO addApplicationReleaseArtifacts(String applicationType, String deviceType,
            ApplicationReleaseDTO applicationReleaseDTO, ApplicationArtifact applicationArtifact, boolean isNewRelease)
            throws ResourceManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();

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
                    if (!isNewRelease && applicationReleaseDAO.isActiveReleaseExisitForPackageName(packagename, tenantId,
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
                                        + applicationType + " Device Type: " + deviceType);
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

    private ApplicationReleaseDTO updateApplicationReleaseArtifacts(String applicationType, String deviceType,
            ApplicationReleaseDTO applicationReleaseDTO, ApplicationArtifact applicationArtifact)
            throws ResourceManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();

        // The application executable artifacts such as apks are uploaded.
        if (ApplicationType.ENTERPRISE.toString().equals(applicationType)) {
            try {
                byte[] content = IOUtils.toByteArray(applicationArtifact.getInstallerStream());

                try(ByteArrayInputStream binaryClone = new ByteArrayInputStream(content)){
                    String md5OfApp = StorageManagementUtil.getMD5(binaryClone);

                    if (md5OfApp == null) {
                        String msg = "Error occurred while md5sum value retrieving process: application UUID "
                                + applicationReleaseDTO.getUuid();
                        log.error(msg);
                        throw new ApplicationStorageManagementException(msg);
                    }
                    if (!applicationReleaseDTO.getAppHashValue().equals(md5OfApp)){
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

                            if (this.applicationReleaseDAO
                                    .verifyReleaseExistenceByHash(md5OfApp, tenantId)) {
                                throw new BadRequestException(
                                        "Application release exists for the uploaded binary file. Application Type: "
                                                + applicationType + " Device Tyep: " + deviceType);
                            }

                            applicationReleaseDTO.setAppHashValue(md5OfApp);
                            String deletingAppHashValue = applicationReleaseDTO.getAppHashValue();
                            try (ByteArrayInputStream binaryDuplicate = new ByteArrayInputStream(content)) {
                                applicationReleaseDTO = applicationStorageManager
                                        .uploadReleaseArtifact(applicationReleaseDTO,applicationType,
                                                deviceType, binaryDuplicate);
                                applicationStorageManager.copyImageArtifactsAndDeleteInstaller(deletingAppHashValue,
                                        applicationReleaseDTO);
                            }
                        }
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
        }
        return applicationReleaseDTO;
    }

    private ApplicationReleaseDTO addImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO,
            ApplicationArtifact applicationArtifact) throws ResourceManagementException {
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();

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

    private ApplicationReleaseDTO updateImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO,
            ApplicationArtifact applicationArtifact) throws ResourceManagementException{
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();

        applicationStorageManager.deleteImageArtifacts(applicationReleaseDTO);

        applicationReleaseDTO.setIconName(applicationArtifact.getIconName());
        applicationReleaseDTO.setBannerName(applicationArtifact.getBannerName());
        applicationReleaseDTO.setScreenshotName1(null);
        applicationReleaseDTO.setScreenshotName2(null);
        applicationReleaseDTO.setScreenshotName3(null);

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
    public Application createWebClip(WebAppWrapper webAppWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (log.isDebugEnabled()) {
            log.debug("Web clip create request is received for the tenant : " + tenantId + " From" + " the user : "
                    + userName);
        }

        ApplicationDTO applicationDTO = APIUtil.convertToAppDTO(webAppWrapper);
        ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
        String uuid = UUID.randomUUID().toString();
        //todo check installer name exists or not, do it in the validation method
        String md5 = DigestUtils.md5Hex(applicationReleaseDTO.getInstallerName());
        applicationReleaseDTO.setUuid(uuid);
        applicationReleaseDTO.setAppHashValue(md5);

        //uploading application artifacts
        try {
            applicationDTO.getApplicationReleaseDTOs().clear();
            applicationDTO.getApplicationReleaseDTOs().add(addImageArtifacts(applicationReleaseDTO, applicationArtifact));
        } catch (ResourceManagementException e) {
            String msg = "Error Occured when uploading artifacts of the web clip: " + webAppWrapper.getName();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }

        //insert application data into database
        return addAppDataIntoDB(applicationDTO, tenantId);
    }

    @Override
    public Application createPublicApp(PublicAppWrapper publicAppWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String publicAppStorePath = "";
        if (log.isDebugEnabled()) {
            log.debug("Public app creating request is received for the tenant : " + tenantId + " From" + " the user : "
                    + userName);
        }

        if (DeviceTypes.ANDROID.toString().equals(publicAppWrapper.getDeviceType())) {
            publicAppStorePath = Constants.GOOGLE_PLAY_STORE_URL;
        } else if (DeviceTypes.IOS.toString().equals(publicAppWrapper.getDeviceType())) {
            publicAppStorePath = Constants.APPLE_STORE_URL;
        }

        ApplicationDTO applicationDTO = APIUtil.convertToAppDTO(publicAppWrapper);
        ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
        String uuid = UUID.randomUUID().toString();
        String appInstallerUrl = publicAppStorePath + applicationReleaseDTO.getPackageName();
        //todo check app package name exist or not, do it in validation method
        applicationReleaseDTO.setInstallerName(appInstallerUrl);
        String md5 = DigestUtils.md5Hex(appInstallerUrl);
        applicationReleaseDTO.setUuid(uuid);
        applicationReleaseDTO.setAppHashValue(md5);

        //uploading application artifacts
        try {
            applicationDTO.getApplicationReleaseDTOs().clear();
            applicationDTO.getApplicationReleaseDTOs()
                    .add(addImageArtifacts(applicationReleaseDTO, applicationArtifact));
        } catch (ResourceManagementException e) {
            String msg = "Error Occured when uploading artifacts of the public app: " + publicAppWrapper.getName();
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }

        //insert application data into database
        return addAppDataIntoDB(applicationDTO, tenantId);
    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList = new ApplicationList();
        List<ApplicationDTO> appDTOs;
        List<Application> applications = new ArrayList<>();
        List<ApplicationDTO> filteredApplications = new ArrayList<>();
        DeviceType deviceType = null;

        //set default values
        if (!StringUtils.isEmpty(filter.getDeviceType())) {
            deviceType = getDeviceTypeData(filter.getDeviceType());
        }
        if (filter.getLimit() == 0) {
            filter.setLimit(20);
        }

        if (deviceType == null) {
            deviceType = new DeviceType();
            deviceType.setId(-1);
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            validateFilter(filter);
            appDTOs = applicationDAO.getApplications(filter, deviceType.getId(), tenantId);
            for (ApplicationDTO applicationDTO : appDTOs) {
                boolean isSearchingApp = true;
                List<String> filteringTags = filter.getTags();
                List<String> filteringCategories = filter.getAppCategories();
                List<String> filteringUnrestrictedRoles = filter.getUnrestrictedRoles();

                if (!lifecycleStateManager.getEndState().equals(applicationDTO.getStatus())) {
                    //get application categories, tags and unrestricted roles.
                    List<String> appUnrestrictedRoles = visibilityDAO
                            .getUnrestrictedRoles(applicationDTO.getId(), tenantId);
                    List<String> appCategoryList = applicationDAO.getAppCategories(applicationDTO.getId(), tenantId);
                    List<String> appTagList = applicationDAO.getAppTags(applicationDTO.getId(), tenantId);

                    //Set application categories, tags and unrestricted roles to the application DTO.
                    applicationDTO.setUnrestrictedRoles(appUnrestrictedRoles);
                    applicationDTO.setAppCategories(appCategoryList);
                    applicationDTO.setTags(appTagList);

                    if ((appUnrestrictedRoles.isEmpty() || hasUserRole(appUnrestrictedRoles, userName)) && (
                            filteringUnrestrictedRoles == null || filteringUnrestrictedRoles.isEmpty()
                                    || hasAppUnrestrictedRole(appUnrestrictedRoles, filteringUnrestrictedRoles,
                                    userName))) {
                        if (filteringCategories != null && !filteringCategories.isEmpty()) {
                            isSearchingApp = filteringCategories.stream().anyMatch(appCategoryList::contains);
                        }
                        if (filteringTags != null && !filteringTags.isEmpty() && isSearchingApp) {
                            isSearchingApp = filteringTags.stream().anyMatch(appTagList::contains);
                        }
                        if (isSearchingApp) {
                            filteredApplications.add(applicationDTO);
                        }
                    }
                }

                List<ApplicationReleaseDTO> filteredApplicationReleaseDTOs = new ArrayList<>();
                for (ApplicationReleaseDTO applicationReleaseDTO : applicationDTO.getApplicationReleaseDTOs()) {
                    if (!applicationReleaseDTO.getCurrentState().equals(lifecycleStateManager.getEndState())) {
                        filteredApplicationReleaseDTOs.add(applicationReleaseDTO);
                    }
                }
                applicationDTO.setApplicationReleaseDTOs(filteredApplicationReleaseDTOs);
            }

            for (ApplicationDTO appDTO : filteredApplications) {
                applications.add(APIUtil.appDtoToAppResponse(appDTO));
            }

            Pagination pagination = new Pagination();
            pagination.setCount(applications.size());
            pagination.setSize(applications.size());
            pagination.setOffset(filter.getOffset());
            pagination.setLimit(filter.getLimit());

            applicationList.setApplications(applications);
            applicationList.setPagination(pagination);
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

    /***
     * This method is responsible to add application data into APPM database. However, before call this method it is
     * required to do the validation of request and check the existence of application releaseDTO.
     *
     * @param applicationDTO Application DTO object.
     * @param tenantId Tenant Id
     * @return {@link Application}
     * @throws ApplicationManagementException which throws if error occurs while during application management.
     */
    private Application addAppDataIntoDB(ApplicationDTO applicationDTO, int tenantId)
            throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();
        List<String> unrestrictedRoles = applicationDTO.getUnrestrictedRoles();
        ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
        List<String> categories = applicationDTO.getAppCategories();
        List<String> tags = applicationDTO.getTags();
        List<ApplicationReleaseDTO> applicationReleaseEntities = new ArrayList<>();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            // Insert to application table
            int appId = this.applicationDAO.createApplication(applicationDTO, tenantId);
            if (appId == -1) {
                log.error("Application data storing is Failed.");
                ConnectionManagerUtil.rollbackDBTransaction();
                deleteApplicationArtifacts(Collections.singletonList(applicationReleaseDTO.getAppHashValue()));
                return null;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("New ApplicationDTO entry added to AP_APP table. App Id:" + appId);
                }
                //add application categories

                List<Integer> categoryIds = applicationDAO.getCategoryIdsForCategoryNames(categories, tenantId);
                this.applicationDAO.addCategoryMapping(categoryIds, appId, tenantId);

                //adding application unrestricted roles
                if (unrestrictedRoles != null && !unrestrictedRoles.isEmpty()) {
                    this.visibilityDAO.addUnrestrictedRoles(unrestrictedRoles, appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New restricted roles to app ID mapping added to AP_UNRESTRICTED_ROLE table."
                                + " App Id:" + appId);
                    }
                }

                //adding application tags
                if (tags != null && !tags.isEmpty()) {
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
                applicationReleaseDTO.setCurrentState(initialLifecycleState);
                applicationReleaseDTO = this.applicationReleaseDAO
                        .createRelease(applicationReleaseDTO, appId, tenantId);
                LifecycleState lifecycleState = getLifecycleStateInstance(initialLifecycleState, initialLifecycleState);
                this.lifecycleStateDAO.addLifecycleState(lifecycleState, applicationReleaseDTO.getId(), tenantId);
                applicationReleaseEntities.add(applicationReleaseDTO);
                applicationDTO.setApplicationReleaseDTOs(applicationReleaseEntities);
                Application application = APIUtil.appDtoToAppResponse(applicationDTO);
                ConnectionManagerUtil.commitDBTransaction();
                return application;
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred while adding lifecycle state. application name: " + applicationDTO.getName() + ".";
            log.error(msg);
            try {
                applicationStorageManager.deleteAllApplicationReleaseArtifacts(
                        Collections.singletonList(applicationReleaseDTO.getAppHashValue()));
            } catch (ApplicationStorageManagementException ex) {
                String errorLog =
                        "Error occurred when deleting application artifacts. Application artifacts are tried to "
                                + "delete because of lifecycle state adding issue in the application creating operation.";
                log.error(errorLog);
                throw new ApplicationManagementException(errorLog, e);
            }
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while adding application or application release. application name: "
                    + applicationDTO.getName() + ".";
            log.error(msg);
            deleteApplicationArtifacts(Collections.singletonList(applicationReleaseDTO.getAppHashValue()));
            throw new ApplicationManagementException(msg, e);
        } catch (LifecycleManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred when getting initial lifecycle state. application name: " + applicationDTO.getName()
                            + ".";
            log.error(msg);
            deleteApplicationArtifacts(Collections.singletonList(applicationReleaseDTO.getAppHashValue()));
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (VisibilityManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while adding unrestricted roles. application name: " + applicationDTO.getName()
                    + ".";
            log.error(msg);
            deleteApplicationArtifacts(Collections.singletonList(applicationReleaseDTO.getAppHashValue()));
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease createEntAppRelease(int applicationId, ApplicationReleaseWrapper applicationReleaseWrapper,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationRelease applicationRelease;
        if (log.isDebugEnabled()) {
            log.debug("Application release creating request is received for the application id: " + applicationId);
        }

        ApplicationDTO applicationDTO = getApplication(applicationId);
        try {
            if (!ApplicationType.ENTERPRISE.toString().equals(applicationDTO.getType())) {
                String msg =
                        "It is possible to add new application release for " + ApplicationType.ENTERPRISE.toString()
                                + " app type. But you are requesting to add new application release for "
                                + applicationDTO.getType() + " app type.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            ApplicationReleaseDTO applicationReleaseDTO = uploadReleaseArtifacts(applicationReleaseWrapper,
                    applicationDTO, applicationArtifact);
            ConnectionManagerUtil.beginDBTransaction();
            String initialstate = lifecycleStateManager.getInitialState();
            applicationReleaseDTO.setCurrentState(initialstate);
            LifecycleState lifecycleState = getLifecycleStateInstance(initialstate, initialstate);
            applicationReleaseDTO = this.applicationReleaseDAO
                    .createRelease(applicationReleaseDTO, applicationDTO.getId(), tenantId);
            this.lifecycleStateDAO
                    .addLifecycleState(lifecycleState, applicationReleaseDTO.getId(), tenantId);
            applicationRelease = APIUtil.releaseDtoToRelease(applicationReleaseDTO);
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
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ApplicationDTO getApplication(int applicationId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find application for the application Id: " + applicationId;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return applicationDTO;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while obtaining the database connection for getting application for the application ID: "
                            + applicationId, e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "Error occurred while getting application data for application ID: " + applicationId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ApplicationReleaseDTO uploadReleaseArtifacts(ApplicationReleaseWrapper applicationReleaseWrapper,
            ApplicationDTO applicationDTO, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        try {
            DeviceType deviceType = getDeviceTypeData(applicationDTO.getDeviceTypeId());
            ApplicationReleaseDTO applicationReleaseDTO = addApplicationReleaseArtifacts(applicationDTO.getType(),
                    deviceType.getName(), APIUtil.releaseWrapperToReleaseDTO(applicationReleaseWrapper), applicationArtifact,
                    true);
            return addImageArtifacts(applicationReleaseDTO, applicationArtifact);
        } catch (ResourceManagementException e) {
            String msg =
                    "Error occurred while uploading application release artifacts. Application ID: " + applicationDTO
                            .getId();
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public Application getApplicationById(int appId, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        boolean isVisibleApp = false;
        ApplicationDTO applicationDTO = getApplication(appId);

        try {
            ConnectionManagerUtil.openDBConnection();
            List<ApplicationReleaseDTO> filteredApplicationReleaseDTOs = new ArrayList<>();
            for (ApplicationReleaseDTO applicationReleaseDTO : applicationDTO.getApplicationReleaseDTOs()) {
                if (!applicationReleaseDTO.getCurrentState().equals(lifecycleStateManager.getEndState()) && (
                        state == null || applicationReleaseDTO.getCurrentState().equals(state))) {
                    filteredApplicationReleaseDTOs.add(applicationReleaseDTO);
                }
            }
            if (state != null && filteredApplicationReleaseDTOs.isEmpty()) {
                return null;
            }
            applicationDTO.setApplicationReleaseDTOs(filteredApplicationReleaseDTOs);

            List<String> tags = this.applicationDAO.getAppTags(appId, tenantId);
            List<String> categories = this.applicationDAO.getAppCategories(appId, tenantId);
            applicationDTO.setTags(tags);
            if (!categories.isEmpty()){
                applicationDTO.setAppCategories(categories);
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
            return APIUtil.appDtoToAppResponse(applicationDTO);
        } catch (LifecycleManagementException e){
            String msg = "Error occurred when getting the last state of the application lifecycle flow";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }catch (UserStoreException e) {
            String msg = "User-store exception while getting application with the application id " + appId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured when getting, either application tags or application categories";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease getApplicationReleaseByUUID(String uuid) throws ApplicationManagementException{
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        boolean isVisibleAppRelease = false;
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for the UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (applicationReleaseDTO.getCurrentState().equals(lifecycleStateManager.getEndState())) {
                return null;
            }

            List<String> unrestrictedRoles = this.visibilityDAO.getUnrestrictedRolesByUUID(uuid, tenantId);
            if (!unrestrictedRoles.isEmpty()) {
                if (hasUserRole(unrestrictedRoles, userName)) {
                    isVisibleAppRelease = true;
                }
            } else {
                isVisibleAppRelease = true;
            }

            if (!isVisibleAppRelease) {
                String msg = "You are trying to access release of visibility restricted application. You don't have "
                        + "required roles to view this application,";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            return APIUtil.releaseDtoToRelease(applicationReleaseDTO);
        } catch (LifecycleManagementException e) {
            String msg = "Error occurred when getting the end state of the application lifecycle flow";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "User-store exception while getting application with the application release UUID: " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }


    @Override
    public Application getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        boolean isVisibleApp = false;

        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = applicationDAO.getApplicationByUUID(uuid, tenantId);

            if (applicationDTO == null) {
                String msg = "Couldn't found an application for application release UUID: " + uuid;
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
            if (state != null && filteredApplicationReleaseDTOs.isEmpty()) {
                return null;
            }
            applicationDTO.setApplicationReleaseDTOs(filteredApplicationReleaseDTOs);

            List<String> tags = this.applicationDAO.getAppTags(applicationDTO.getId(), tenantId);
            List<String> categories = this.applicationDAO.getAppCategories(applicationDTO.getId(), tenantId);
            applicationDTO.setTags(tags);
            applicationDTO.setAppCategories(categories);

            List<String> unrestrictedRoles = this.visibilityDAO.getUnrestrictedRoles(applicationDTO.getId(), tenantId);
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
            return APIUtil.appDtoToAppResponse(applicationDTO);
        } catch (LifecycleManagementException e) {
            String msg = "Error occurred when getting the last state of the application lifecycle flow";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "User-store exception while getting application with the application release UUID " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting, application data.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
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
        //todo check role by role
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

    //todo no usage
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
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
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
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
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
        //todo
        applicationReleases = null;
        try {
            applicationReleases = this.applicationReleaseDAO.getReleases(application.getId(), tenantId);
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
        }
        for (ApplicationReleaseDTO applicationRelease : applicationReleases) {
            LifecycleState lifecycleState = null;
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

    @Override
    public void deleteApplication(int applicationId) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Request is received to delete applications which are related with the application id "
                    + applicationId);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();
        ApplicationDTO applicationDTO = getApplication(applicationId);
        List<ApplicationReleaseDTO> applicationReleaseDTOs = applicationDTO.getApplicationReleaseDTOs();
        for (ApplicationReleaseDTO applicationReleaseDTO : applicationReleaseDTOs) {
            if (!lifecycleStateManager.isDeletableState(applicationReleaseDTO.getCurrentState())){
                String msg = "Application release which has application release UUID: " +
                        applicationReleaseDTO.getUuid() + " is not in a deletable state. Therefore Application "
                        + "deletion is not permitted. In order to delete the application, all application releases "
                        + "of the application has to be in a deletable state.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            List<Integer> deletingAppReleaseIds = new ArrayList<>();
            for (ApplicationReleaseDTO applicationReleaseDTO : applicationReleaseDTOs) {
                List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                        .getDeviceSubscriptions(applicationReleaseDTO.getId(), tenantId);
                if (!deviceSubscriptionDTOS.isEmpty()){
                    String msg = "Application release which has UUID: " + applicationReleaseDTO.getUuid() +
                            " either subscribed to device/s or it had subscribed to device/s. Therefore you are not "
                            + "permitted to delete the application release.";
                    log.error(msg);
                    throw new ForbiddenException(msg);
                }
                applicationStorageManager.deleteApplicationReleaseArtifacts(applicationReleaseDTO.getAppHashValue());
                deletingAppReleaseIds.add(applicationReleaseDTO.getId());
            }
            this.lifecycleStateDAO.deleteLifecycleStates(deletingAppReleaseIds);
            this.applicationReleaseDAO.deleteReleases(deletingAppReleaseIds);
            this.applicationDAO.deleteApplicationTags(applicationId, tenantId);
            this.applicationDAO.deleteCategoryMapping(applicationId, tenantId);
            this.applicationDAO.deleteApplication(applicationId, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when getting application data for application id: " + applicationId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when deleting application artifacts in the file system. Application id: "
                    + applicationId;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while deleting life-cycle state data of application releases of the application"
                    + " which has application ID: " + applicationId;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void retireApplication(int applicationId) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Request is received to delete applications which are related with the application id "
                    + applicationId);
        }
        ApplicationDTO applicationDTO = getApplication(applicationId);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            List<ApplicationReleaseDTO> applicationReleaseDTOs = applicationDTO.getApplicationReleaseDTOs();
            List<ApplicationReleaseDTO> activeApplicationReleaseDTOs = new ArrayList<>();
            for (ApplicationReleaseDTO applicationReleaseDTO : applicationReleaseDTOs) {
                if (!applicationReleaseDTO.getCurrentState().equals(lifecycleStateManager.getEndState())) {
                    activeApplicationReleaseDTOs.add(applicationReleaseDTO);
                }
            }
            if (!activeApplicationReleaseDTOs.isEmpty()) {
                String msg = "There are application releases which are not in the state " + lifecycleStateManager
                        .getEndState() + ". Hence you are not allowed to delete the application";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            this.applicationDAO.retireApplication(applicationId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting application data for application id: " + applicationId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteApplicationRelease(String releaseUuid)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO
                    .getReleaseByUUID(releaseUuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " + releaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            if (!lifecycleStateManager.isDeletableState(applicationReleaseDTO.getCurrentState())) {
                String msg = "Application state is not in the deletable state. Therefore you are not permitted to "
                        + "delete the application release.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                    .getDeviceSubscriptions(applicationReleaseDTO.getId(), tenantId);
            if (!deviceSubscriptionDTOS.isEmpty()){
                String msg = "Application release which has UUID: " + applicationReleaseDTO.getUuid() +
                        " either subscribed to device/s or it had subscribed to device/s. Therefore you are not "
                        + "permitted to delete the application release.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            applicationStorageManager.deleteApplicationReleaseArtifacts(applicationReleaseDTO.getAppHashValue());
            lifecycleStateDAO.deleteLifecycleStateByReleaseId(applicationReleaseDTO.getId());
            applicationReleaseDAO.deleteRelease(applicationReleaseDTO.getId());
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when application release data for application release UUID: " + releaseUuid;
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when deleting the application release artifact from the file system. "
                    + "Application release UUID: " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when dleting lifecycle data for application release UUID: " + releaseUuid;
            throw new ApplicationManagementException(msg, e);
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
    public void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
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
        boolean isValidDeviceType = false;
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = DAOUtil.getDeviceManagementService().getDeviceTypes();

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

            applicationReleaseDTO = updateApplicationReleaseArtifacts(appType, deviceType, applicationReleaseDTO,
                    applicationArtifact);
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

    @Override
    public List<LifecycleState> getLifecycleStateChangeFlow(String releaseUuid) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO
                    .getReleaseByUUID(releaseUuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't found an application release for application release UUID: " + releaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return this.lifecycleStateDAO.getLifecycleStates(applicationReleaseDTO.getId(), tenantId);
        } catch (LifeCycleManagementDAOException e) {
            String msg = "Failed to get lifecycle state for application release uuid " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred while getting application release for application release UUID: " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease changeLifecycleState(String releaseUuid, LifecycleChanger lifecycleChanger)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (lifecycleChanger == null || StringUtils.isEmpty(lifecycleChanger.getAction())) {
            String msg = "The Action is null or empty. Please verify the request.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO
                    .getReleaseByUUID(releaseUuid, tenantId);

            if (applicationReleaseDTO == null) {
                String msg = "Couldn't found an application release for the UUID: " + releaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (lifecycleStateManager
                    .isValidStateChange(applicationReleaseDTO.getCurrentState(), lifecycleChanger.getAction(), userName,
                            tenantId)) {
                if (lifecycleStateManager.isInstallableState(lifecycleChanger.getAction()) && applicationReleaseDAO
                        .hasExistInstallableAppRelease(applicationReleaseDTO.getUuid(),
                                lifecycleStateManager.getInstallableState(), tenantId)) {
                    String msg = "Installable application release is already registered for the application. "
                            + "Therefore it is not permitted to change the lifecycle state from "
                            + applicationReleaseDTO.getCurrentState() + " to " + lifecycleChanger.getAction();
                    log.error(msg);
                    throw new ForbiddenException(msg);
                }
                LifecycleState lifecycleState = new LifecycleState();
                lifecycleState.setCurrentState(lifecycleChanger.getAction());
                lifecycleState.setPreviousState(applicationReleaseDTO.getCurrentState());
                lifecycleState.setUpdatedBy(userName);
                lifecycleState.setResonForChange(lifecycleChanger.getReason());
                applicationReleaseDTO.setCurrentState(lifecycleChanger.getAction());
                if (this.applicationReleaseDAO.updateRelease(applicationReleaseDTO, tenantId) == null) {
                    String msg = "Application release updating is failed/.";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
                this.lifecycleStateDAO.addLifecycleState(lifecycleState, applicationReleaseDTO.getId(), tenantId);
                ConnectionManagerUtil.commitDBTransaction();
                return APIUtil.releaseDtoToRelease(applicationReleaseDTO);
            } else {
                String msg = "Invalid lifecycle state transition from '" + applicationReleaseDTO.getCurrentState() + "'"
                        + " to '" + lifecycleChanger.getAction() + "'";
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Failed to add lifecycle state for Application release UUID: " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when accessing application release data of application release which has the "
                    + "application release UUID: " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addApplicationCategories(List<String> categories) throws ApplicationManagementException {
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
    public void updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationDTO applicationDTO = getApplication(applicationId);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (!StringUtils.isEmpty(applicationUpdateWrapper.getName())){
                Filter filter = new Filter();
                filter.setFullMatch(true);
                filter.setAppName(applicationUpdateWrapper.getName().trim());
                filter.setOffset(0);
                filter.setLimit(1);

                List<ApplicationDTO> applicationList = applicationDAO
                        .getApplications(filter, applicationDTO.getDeviceTypeId(), tenantId);
                if (!applicationList.isEmpty()) {
                    String msg = "Already an application registered with same name " + applicationUpdateWrapper.getName()
                            + ". Hence you can't update the application name from " + applicationDTO.getName() + " to "
                            + applicationUpdateWrapper.getName();
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                applicationDTO.setName(applicationUpdateWrapper.getName());
            }
            if (!StringUtils.isEmpty(applicationUpdateWrapper.getSubType()) && !applicationDTO.getSubType()
                    .equals(applicationUpdateWrapper.getSubType())) {
                if (!ApplicationSubscriptionType.PAID.toString().equals(applicationUpdateWrapper.getSubType())
                        && !ApplicationSubscriptionType.FREE.toString().equals(applicationUpdateWrapper.getSubType())) {
                    String msg = "Invalid application subscription type is found with application updating request "
                            + applicationUpdateWrapper.getSubType();
                    log.error(msg);
                    throw new BadRequestException(msg);

                } else if (ApplicationSubscriptionType.FREE.toString().equals(applicationUpdateWrapper.getSubType())
                        && !StringUtils.isEmpty(applicationUpdateWrapper.getPaymentCurrency())) {
                    String msg = "If you are going to change Non-Free app as Free app, "
                            + "currency attribute in the application updating payload should be null or \"\"";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                } else if (ApplicationSubscriptionType.PAID.toString().equals(applicationUpdateWrapper.getSubType())
                        && StringUtils.isEmpty(applicationUpdateWrapper.getPaymentCurrency()) ){
                    String msg = "If you are going to change Free app as Non-Free app, "
                            + "currency attribute in the application payload should not be null or \"\"";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
                applicationDTO.setSubType(applicationUpdateWrapper.getSubType());
                applicationDTO.setPaymentCurrency(applicationUpdateWrapper.getPaymentCurrency());
            }

            if (!StringUtils.isEmpty(applicationUpdateWrapper.getDescription())){
                applicationDTO.setDescription(applicationUpdateWrapper.getDescription());
            }

            List<String> appUnrestrictedRoles = this.visibilityDAO.getUnrestrictedRoles(applicationId, tenantId);

            boolean isExistingAppRestricted = !appUnrestrictedRoles.isEmpty();
            boolean isUpdatingAppRestricted = false;
            if (applicationUpdateWrapper.getUnrestrictedRoles() != null && !applicationUpdateWrapper
                    .getUnrestrictedRoles().isEmpty()) {
                isUpdatingAppRestricted = true;
            }

            if (isExistingAppRestricted && !isUpdatingAppRestricted) {
                visibilityDAO.deleteUnrestrictedRoles(appUnrestrictedRoles, applicationId, tenantId);
            } else if (isUpdatingAppRestricted) {
                if (!hasUserRole(applicationUpdateWrapper.getUnrestrictedRoles(), userName)) {
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
                            .addUnrestrictedRoles(applicationUpdateWrapper.getUnrestrictedRoles(), applicationId, tenantId);
                } else {
                    List<String> addingRoleList = getDifference(applicationUpdateWrapper.getUnrestrictedRoles(),
                            applicationDTO.getUnrestrictedRoles());
                    List<String> removingRoleList = getDifference(applicationDTO.getUnrestrictedRoles(),
                            applicationUpdateWrapper.getUnrestrictedRoles());
                    if (!addingRoleList.isEmpty()) {
                        visibilityDAO.addUnrestrictedRoles(addingRoleList, applicationId, tenantId);
                    }
                    if (!removingRoleList.isEmpty()) {
                        visibilityDAO.deleteUnrestrictedRoles(removingRoleList, applicationId, tenantId);
                    }
                }
            }
            applicationDTO.setUnrestrictedRoles(applicationUpdateWrapper.getUnrestrictedRoles());

            String updatingAppCategory = applicationUpdateWrapper.getAppCategory();
            if ( updatingAppCategory != null){
                List<String> appCategories = this.applicationDAO.getAppCategories(applicationId, tenantId);
                if (!appCategories.contains(updatingAppCategory)){
                    List<CategoryDTO> allCategories = this.applicationDAO.getAllCategories(tenantId);
                    List<Integer> categoryIds = allCategories.stream()
                            .filter(category -> category.getCategoryName().equals(updatingAppCategory))
                            .map(CategoryDTO::getId).collect(Collectors.toList());
                    if (categoryIds.isEmpty()){
                        ConnectionManagerUtil.rollbackDBTransaction();
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

            List<String> updatingAppTags = applicationUpdateWrapper.getTags();
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
                    this.applicationDAO.deleteApplicationTags(removingTagIds, applicationId, tenantId);
                    applicationDAO.deleteTags(removingTagList, applicationId, tenantId);
                }
            }
            if (!applicationDAO.updateApplication(applicationDTO, tenantId)){
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Any application is not updated for the application ID: " + applicationId;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
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

    @Override
    public List<Tag> getRegisteredTags() throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            List<TagDTO> tags = applicationDAO.getAllTags(tenantId);
            List<Integer> mappedTagIds = applicationDAO.getDistinctTagIdsInTagMapping();
            List<Tag> responseTagList = new ArrayList<>();
            tags.forEach(tag -> {
                Tag responseTag = new Tag();
                if (!mappedTagIds.contains(tag.getId())) {
                    responseTag.setTagDeletable(true);
                }
                responseTag.setTagName(tag.getTagName());
                responseTagList.add(responseTag);
            });
            return responseTagList;
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting registered tags from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<Category> getRegisteredCategories() throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            List<CategoryDTO> categories = applicationDAO.getAllCategories(tenantId);
            List<Integer> mappedCategoryIds = applicationDAO.getDistinctCategoryIdsInCategoryMapping();
            List<Category> responseCategoryList = new ArrayList<>();
            categories.forEach(category -> {
                Category responseCategory = new Category();
                if (!mappedCategoryIds.contains(category.getId())) {
                    responseCategory.setCategoryDeletable(true);
                }
                responseCategory.setCategoryName(category.getCategoryName());
                responseCategoryList.add(responseCategory);
            });
            return responseCategoryList;
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting registered tags from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteApplicationTag(int appId, String tagName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ApplicationDTO applicationDTO = getApplication(appId);
            ConnectionManagerUtil.beginDBTransaction();
            TagDTO tag = applicationDAO.getTagForTagName(tagName, tenantId);
            if (tag == null){
                String msg = "Couldn't found a tag for tag name " + tagName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (applicationDAO.hasTagMapping(tag.getId(), applicationDTO.getId(), tenantId)){
                applicationDAO.deleteApplicationTags(tag.getId(), applicationDTO.getId(), tenantId);
                ConnectionManagerUtil.commitDBTransaction();
            } else {
                String msg = "Tag " + tagName + " is not an application tag. Application ID: " + appId;
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting tag Id or deleting tag mapping from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteTag(String tagName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            TagDTO tag = applicationDAO.getTagForTagName(tagName, tenantId);
            if (tag == null){
                String msg = "Couldn't found a tag for tag name " + tagName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (applicationDAO.hasTagMapping(tag.getId(), tenantId)){
                applicationDAO.deleteTagMapping(tag.getId(), tenantId);
            }
            applicationDAO.deleteTag(tag.getId(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting tag Id or deleting the tag from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteUnusedTag(String tagName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            TagDTO tag = applicationDAO.getTagForTagName(tagName, tenantId);
            if (tag == null){
                String msg = "Couldn't found a tag for tag name " + tagName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (applicationDAO.hasTagMapping(tag.getId(), tenantId)){
                String msg =
                        "Tag " + tagName + " is used for applications. Hence it is not permitted to delete the tag "
                                + tagName;
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            applicationDAO.deleteTag(tag.getId(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting tag Ids or deleting the tag from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateTag(String oldTagName, String newTagName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (StringUtils.isEmpty(oldTagName) || StringUtils.isEmpty(newTagName)) {
            String msg = "Either old tag name or new tag name contains empty/null value. Hence please verify the "
                    + "request.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (applicationDAO.getTagForTagName(newTagName, tenantId) != null){
                String msg =
                        "You are trying to modify tag name into existing tag. Therefore you can't modify tag name from "
                                + oldTagName + " to new tag name " + newTagName;
                log.error(msg);
                throw new BadRequestException(msg);

            }
            TagDTO tag = applicationDAO.getTagForTagName(oldTagName, tenantId);
            if (tag == null){
                String msg = "Couldn't found a tag for tag name " + oldTagName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            tag.setTagName(newTagName);
            applicationDAO.updateTag(tag, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting tag Ids or deleting the tag from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<String> addTags(List<String> tags) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            if (tags != null && !tags.isEmpty()) {
                ConnectionManagerUtil.beginDBTransaction();
                List<TagDTO> registeredTags = applicationDAO.getAllTags(tenantId);
                List<String> registeredTagNames = registeredTags.stream().map(TagDTO::getTagName)
                        .collect(Collectors.toList());

                List<String> newTags = getDifference(tags, registeredTagNames);
                if (!newTags.isEmpty()) {
                    this.applicationDAO.addTags(newTags, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                    if (log.isDebugEnabled()) {
                        log.debug("New tags are added to the AP_APP_TAG table.");
                    }
                }
                return Stream.concat(registeredTagNames.stream(), newTags.stream()).collect(Collectors.toList());
            } else{
                String msg = "Tag list is either null of empty. In order to add new tags, tag list should be a list of "
                        + "Stings. Therefore please verify the payload.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred either getting registered tags or adding new tags.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public List<String> addApplicationTags(int appId, List<String> tags) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ApplicationDTO applicationDTO = getApplication(appId);
            if (tags != null && !tags.isEmpty()) {
                ConnectionManagerUtil.beginDBTransaction();
                List<TagDTO> registeredTags = applicationDAO.getAllTags(tenantId);
                List<String> registeredTagNames = registeredTags.stream().map(TagDTO::getTagName)
                        .collect(Collectors.toList());

                List<String> newTags = getDifference(tags, registeredTagNames);
                if (!newTags.isEmpty()) {
                    this.applicationDAO.addTags(newTags, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New tags entries are added to AP_APP_TAG table. App Id:" + applicationDTO.getId());
                    }
                }

                List<String> applicationTags = this.applicationDAO.getAppTags(applicationDTO.getId(), tenantId);
                List<String> newApplicationTags = getDifference(tags, applicationTags);
                if (!newApplicationTags.isEmpty()) {
                    List<Integer> newTagIds = this.applicationDAO.getTagIdsForTagNames(newApplicationTags, tenantId);
                    this.applicationDAO.addTagMapping(newTagIds, applicationDTO.getId(), tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                }
                return Stream.concat(applicationTags.stream(), newApplicationTags.stream())
                        .collect(Collectors.toList());
            } else {
                String msg = "Tag list is either null or empty. In order to add new tags for application which has "
                        + "application ID: " + appId +", tag list should be a list of Stings. Therefore please "
                        + "verify the payload.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while accessing application tags. Application ID: " + appId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public List<String> addCategories(List<String> categories) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            if (categories != null && !categories.isEmpty()) {
                ConnectionManagerUtil.beginDBTransaction();
                List<CategoryDTO> registeredCategories = applicationDAO.getAllCategories(tenantId);
                List<String> registeredCategoryNames = registeredCategories.stream().map(CategoryDTO::getCategoryName)
                        .collect(Collectors.toList());

                List<String> newCategories = getDifference(categories, registeredCategoryNames);
                if (!newCategories.isEmpty()) {
                    this.applicationDAO.addCategories(newCategories, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                    if (log.isDebugEnabled()) {
                        log.debug("New categories are added to the AP_APP_TAG table.");
                    }
                }
                return Stream.concat(registeredCategoryNames.stream(), newCategories.stream())
                        .collect(Collectors.toList());
            } else{
                String msg = "Category list is either null of empty. In order to add new categories, category list "
                        + "should be a list of Stings. Therefore please verify the payload.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred either getting registered categories or adding new categories.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteCategory(String tagName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            CategoryDTO category = applicationDAO.getCategoryForCategoryName(tagName, tenantId);
            if (category == null){
                String msg = "Couldn't found a category for category name " + tagName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (applicationDAO.hasCategoryMapping(category.getId(), tenantId)){
                String msg = "Category " + category.getCategoryName()
                        + " is used by some applications. Therefore it is not permitted to delete the application category.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            applicationDAO.deleteCategory(category.getId(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting category Id or deleting the category from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateCategory(String oldCategoryName, String newCategoryName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            CategoryDTO category = applicationDAO.getCategoryForCategoryName(oldCategoryName, tenantId);
            if (category == null){
                String msg = "Couldn't found a category for tag name " + oldCategoryName + ".";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            category.setCategoryName(newCategoryName);
            applicationDAO.updateCategory(category, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting tag Ids or deleting the category from the system.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public String getInstallableLifecycleState() throws ApplicationManagementException {
        if (lifecycleStateManager == null) {
            String msg = "Application lifecycle manager is not initialed. Please contact the administrator.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }
        return lifecycleStateManager.getInstallableState();
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
                if (applicationType.toString().equalsIgnoreCase(appType)) {
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
     * @return {@link LifecycleState}
     */
    private LifecycleState getLifecycleStateInstance(String currentState, String previousState) {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setCurrentState(currentState);
        lifecycleState.setPreviousState(previousState);
        lifecycleState.setUpdatedBy(userName);
        return lifecycleState;
    }

    @Override
    public boolean updateRelease(String deviceType, String applicationType, String releaseUuid,
            ApplicationReleaseWrapper applicationReleaseWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO
                    .getReleaseByUUID(releaseUuid, tenantId);

            if (applicationReleaseDTO == null) {
                String msg = "Couldn't found an application release for updating. Application release UUID: " + releaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            if (!lifecycleStateManager.isUpdatableState(applicationReleaseDTO.getCurrentState())) {
                String msg = "Application release in " + applicationReleaseDTO.getCurrentState()
                        + " state. Therefore you are not allowed to update the application release. Hence, "
                        + "please move application release from " + applicationReleaseDTO.getCurrentState()
                        + " to updatable state.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }

            DeviceType deviceTypeObj = getDeviceTypeData(deviceType);
            Double price = applicationReleaseWrapper.getPrice();

            String applicationSubType = this.applicationDAO.getApplicationSubTypeByUUID(releaseUuid, tenantId);
            if (applicationSubType == null) {
                String msg = "Couldn't find an application subscription type for the application release UUID: " + releaseUuid;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }

            if (price < 0.0 || (price == 0.0 && ApplicationSubscriptionType.PAID.toString().equals(applicationSubType))
                    || (price > 0.0 && ApplicationSubscriptionType.FREE.toString().equals(applicationSubType))) {
                throw new BadRequestException(
                        "Invalid app release payload for updating application release. ApplicationDTO price is " + price
                                + " for " + applicationSubType + " application., Application Release UUID "
                                + releaseUuid + " and supported device type is " + deviceType);
            }
            applicationReleaseDTO.setPrice(price);
            applicationReleaseDTO.setIsSharedWithAllTenants(applicationReleaseDTO.getIsSharedWithAllTenants());

            String supportedOSVersions = applicationReleaseWrapper.getSupportedOsVersions();
            if (!StringUtils.isEmpty(supportedOSVersions)) {
                //todo check OS versions are supported or not
                applicationReleaseDTO.setSupportedOsVersions(supportedOSVersions);
            }
            if (!StringUtils.isEmpty(applicationReleaseWrapper.getDescription())) {
                applicationReleaseDTO.setDescription(applicationReleaseWrapper.getDescription());
            }
            if (!StringUtils.isEmpty(applicationReleaseWrapper.getReleaseType())) {
                applicationReleaseDTO.setReleaseType(applicationReleaseWrapper.getReleaseType());
            }
            if (!StringUtils.isEmpty(applicationReleaseWrapper.getMetaData())) {
                applicationReleaseDTO.setMetaData(applicationReleaseWrapper.getMetaData());
            }

            applicationReleaseDTO = updateApplicationReleaseArtifacts(applicationType, deviceTypeObj.getName(),
                    applicationReleaseDTO, applicationArtifact);
            applicationReleaseDTO = updateImageArtifacts(applicationReleaseDTO, applicationArtifact);

            boolean updateStatus = applicationReleaseDAO.updateRelease(applicationReleaseDTO, tenantId) != null;
            if (!updateStatus) {
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            ConnectionManagerUtil.commitDBTransaction();
            return updateStatus;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured when updating Application release. ApplicationDTO ID ApplicationDTO Release "
                    + "UUID: " + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ResourceManagementException e) {
            String msg = "Error occured when updating application release artifact in the file system. Application "
                    + "release UUID:" + releaseUuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }


    @Override
    public <T> void validateAppCreatingRequest(T param) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int deviceTypeId = -1;
        String appName;
        List<String> appCategories;
        List<String> unrestrictedRoles;

        if (param instanceof ApplicationWrapper) {
            ApplicationWrapper applicationWrapper = (ApplicationWrapper) param;
            appName = applicationWrapper.getName();
            if (StringUtils.isEmpty(appName)) {
                String msg = "Application name cannot be empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            appCategories = applicationWrapper.getAppCategories();
            if (appCategories == null) {
                String msg = "Application category can't be null.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (appCategories.isEmpty()) {
                String msg = "Application category can't be empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(applicationWrapper.getDeviceType())) {
                String msg = "Device type can't be empty for the application.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            DeviceType deviceType = getDeviceTypeData(applicationWrapper.getDeviceType());
            deviceTypeId = deviceType.getId();

            List<ApplicationReleaseWrapper> applicationReleaseWrappers;
            applicationReleaseWrappers = applicationWrapper.getApplicationReleaseWrappers();

            if (applicationReleaseWrappers == null || applicationReleaseWrappers.size() != 1) {
                String msg = "Invalid application creating request. Application creating request must have single "
                        + "application release.  Application name:" + applicationWrapper.getName() + ".";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            unrestrictedRoles = applicationWrapper.getUnrestrictedRoles();
        } else if (param instanceof WebAppWrapper) {
            WebAppWrapper webAppWrapper = (WebAppWrapper) param;
            appName = webAppWrapper.getName();
            if (StringUtils.isEmpty(appName)) {
                String msg = "Web Clip name cannot be empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            appCategories = webAppWrapper.getCategories();
            if (appCategories == null) {
                String msg = "Web Clip category can't be null.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (appCategories.isEmpty()) {
                String msg = "Web clip category can't be empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(webAppWrapper.getType()) || (!ApplicationType.WEB_CLIP.toString()
                    .equals(webAppWrapper.getType()) && !ApplicationType.WEB_APP.toString()
                    .equals(webAppWrapper.getType()))) {
                String msg = "Web app wrapper contains invalid application type with the request. Hence please verify "
                        + "the request payload..";
                log.error(msg);
                throw new BadRequestException(msg);
            }

            List<WebAppReleaseWrapper> webAppReleaseWrappers;
            webAppReleaseWrappers = webAppWrapper.getWebAppReleaseWrappers();

            if (webAppReleaseWrappers == null || webAppReleaseWrappers.size() != 1) {
                String msg = "Invalid web clip creating request. Web clip creating request must have single "
                        + "web clip release. Web clip name:" + webAppWrapper.getName() + ".";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            unrestrictedRoles = webAppWrapper.getUnrestrictedRoles();
        } else if (param instanceof PublicAppWrapper) {
            PublicAppWrapper publicAppWrapper = (PublicAppWrapper) param;
            appName = publicAppWrapper.getName();
            if (StringUtils.isEmpty(appName)) {
                String msg = "Application name cannot be empty for public app.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            appCategories = publicAppWrapper.getCategories();
            if (appCategories == null) {
                String msg = "Application category can't be null.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (appCategories.isEmpty()) {
                String msg = "Application category can't be empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(publicAppWrapper.getDeviceType())) {
                String msg = "Device type can't be empty for the public application.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            DeviceType deviceType = getDeviceTypeData(publicAppWrapper.getDeviceType());
            deviceTypeId = deviceType.getId();

            List<PublicAppReleaseWrapper> publicAppReleaseWrappers;
            publicAppReleaseWrappers = publicAppWrapper.getPublicAppReleaseWrappers();

            if (publicAppReleaseWrappers == null || publicAppReleaseWrappers.size() != 1) {
                String msg = "Invalid public app creating request. Request must have single release. Application name:"
                        + publicAppWrapper.getName() + ".";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            unrestrictedRoles = publicAppWrapper.getUnrestrictedRoles();
        } else {
            String msg = "Invalid payload found with the request. Hence verify the request payload object.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            if (unrestrictedRoles != null && !unrestrictedRoles.isEmpty()) {
                if (!isValidRestrictedRole(unrestrictedRoles)) {
                    String msg = "Unrestricted role list contain role/roles which are not in the user store.";
                    log.error(msg);
                    throw new ApplicationManagementException(msg);
                }
                if (!hasUserRole(unrestrictedRoles, userName)) {
                    String msg = "You are trying to restrict the visibility of the application for a role set, but "
                            + "in order to perform the action at least one role should be assigned to user: "
                            + userName;
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
            }

            Filter filter = new Filter();
            filter.setFullMatch(true);
            filter.setAppName(appName);
            filter.setOffset(0);
            filter.setLimit(1);
            List<ApplicationDTO> applicationList = applicationDAO.getApplications(filter, deviceTypeId, tenantId);
            if (!applicationList.isEmpty()) {
                String msg =
                        "Already an application registered with same name - " + applicationList.get(0).getName() + ".";
                log.error(msg);
                throw new BadRequestException(msg);
            }

            List<CategoryDTO> registeredCategories = this.applicationDAO.getAllCategories(tenantId);

            if (registeredCategories.isEmpty()) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Registered application category set is empty. Since it is mandatory to add application "
                        + "category when adding new application, registered application category list shouldn't be null.";
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            for (String cat : appCategories) {
                boolean isValidCategory = false;
                for (CategoryDTO obj : registeredCategories) {
                    if (cat.equals(obj.getCategoryName())) {
                        isValidCategory = true;
                        break;
                    }
                }
                if (!isValidCategory) {
                    String msg = "Application Creating request contains invalid categories. Hence please verify the "
                            + "application creating payload.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred while getting data which is related to web clip. web clip name: " + appName + ".";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (UserStoreException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when validating the unrestricted roles given for the web clip";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public <T> void validateReleaseCreatingRequest(T param) throws ApplicationManagementException {
        if (param == null) {
            String msg = "In order to validate release creating request param shouldn't be null.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        if (param instanceof ApplicationReleaseWrapper) {
            ApplicationReleaseWrapper applicationReleaseWrapper = (ApplicationReleaseWrapper) param;
            if (StringUtils.isEmpty(applicationReleaseWrapper.getSupportedOsVersions())) {
                String msg = "Supported OS Version shouldn't be null or empty.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } else if (param instanceof WebAppReleaseWrapper) {
            WebAppReleaseWrapper webAppReleaseWrapper = (WebAppReleaseWrapper) param;
            UrlValidator urlValidator = new UrlValidator();
            if (StringUtils.isEmpty(webAppReleaseWrapper.getVersion())) {
                String msg = "Version shouldn't be empty or null for the WEB CLIP release creating request.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(webAppReleaseWrapper.getUrl())) {
                String msg = "URL should't be null for the application release creating request for application type "
                        + "WEB_CLIP";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (!urlValidator.isValid(webAppReleaseWrapper.getUrl())) {
                String msg = "Request payload contains an invalid Web Clip URL.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } else if (param instanceof PublicAppReleaseWrapper) {
            PublicAppReleaseWrapper publicAppReleaseWrapper = (PublicAppReleaseWrapper) param;
            if (StringUtils.isEmpty(publicAppReleaseWrapper.getSupportedOsVersions())) {
                String msg = "Supported OS Version shouldn't be null or empty for public app release creating request.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(publicAppReleaseWrapper.getVersion())) {
                String msg = "Version shouldn't be empty or null for the Public App release creating request.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            if (StringUtils.isEmpty(publicAppReleaseWrapper.getPackageName())) {
                String msg = "Package name shouldn't be empty or null for the Public App release creating request.";
                log.error(msg);
                throw new BadRequestException(msg);
            }

        } else {
            String msg = "Invalid payload found with the release creating request. Hence verify the release creating "
                    + "request payload object.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
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
        //todo remove this check, because banner is not mandatory to have
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
    public void validateBinaryArtifact(Attachment binaryFile) throws RequestValidatingException {

        if (binaryFile == null) {
            String msg = "Binary file is not found with the application release creating request for ENTERPRISE app "
                    + "creating request.";
            log.error(msg);
            throw new RequestValidatingException(msg);
        }
    }

    private <T> DeviceType getDeviceTypeData( T deviceTypeAttr)
            throws BadRequestException, UnexpectedServerErrorException {
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = DAOUtil.getDeviceManagementService().getDeviceTypes();

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

    public String getPlistArtifact(String releaseUuid) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplicationByUUID(releaseUuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find application for the release UUID: " + releaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
            String artifactDownloadEndpoint = ConfigurationManager.getInstance().getConfiguration()
                    .getArtifactDownloadEndpoint();
            String artifactDownloadURL = artifactDownloadEndpoint + Constants.FORWARD_SLASH + applicationReleaseDTO.getUuid()
                              + Constants.FORWARD_SLASH + applicationReleaseDTO.getInstallerName();
            String plistContent = "&lt;!DOCTYPE plist PUBLIC &quot;-//Apple//DTDPLIST1.0//EN&quot; &quot;" +
                                  "http://www.apple.com/DTDs/PropertyList-1.0.dtd&quot;&gt;&lt;plist version=&quot;" +
                                  "1.0&quot;&gt;&lt;dict&gt;&lt;key&gt;items&lt;/key&gt;&lt;array&gt;&lt;dict&gt;&lt;" +
                                  "key&gt;assets&lt;/key&gt;&lt;array&gt;&lt;dict&gt;&lt;key&gt;kind&lt;/key&gt;&lt;" +
                                  "string&gt;software-package&lt;/string&gt;&lt;key&gt;url&lt;/key&gt;&lt;string&gt;" +
                                  "$downloadURL&lt;/string&gt;&lt;/dict&gt;&lt;/array&gt;&lt;key&gt;metadata&lt;" +
                                  "/key&gt;&lt;dict&gt;&lt;key&gt;bundle-identifier&lt;/key&gt;&lt;string&gt;" +
                                  "$packageName&lt;/string&gt;&lt;key&gt;bundle-version&lt;/key&gt;&lt;string&gt;" +
                                  "$bundleVersion&lt;/string&gt;&lt;key&gt;kind&lt;/key&gt;&lt;string&gt;" +
                                  "software&lt;/string&gt;&lt;key&gt;title&lt;/key&gt;&lt;string&gt;$appName&lt;" +
                                  "/string&gt;&lt;/dict&gt;&lt;/dict&gt;&lt;/array&gt;&lt;/dict&gt;&lt;/plist&gt;";
            plistContent = plistContent.replace("$downloadURL", artifactDownloadURL)
                    .replace("$packageName", applicationReleaseDTO.getPackageName())
                    .replace("$bundleVersion", applicationReleaseDTO.getVersion())
                    .replace("$appName", applicationDTO.getName());
            return StringEscapeUtils.unescapeXml(plistContent);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException(
                    "Error occurred while obtaining the database connection for getting application for the release UUID: "
                    + releaseUuid, e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "Error occurred while getting application data for release UUID: " + releaseUuid, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
