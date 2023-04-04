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
package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.mgt.common.Base64File;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.exception.ResourceManagementException;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import io.entgra.application.mgt.common.ApplicationArtifact;
import io.entgra.application.mgt.common.LifecycleChanger;
import io.entgra.application.mgt.common.ApplicationList;
import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.Filter;
import io.entgra.application.mgt.common.LifecycleState;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.response.ApplicationRelease;
import io.entgra.application.mgt.common.response.Category;
import io.entgra.application.mgt.common.response.Tag;
import io.entgra.application.mgt.common.wrapper.CustomAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.EntAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppReleaseWrapper;
import java.util.List;

/**
 * This interface manages the application creation, deletion and editing of the application.
 */
public interface ApplicationManager {

    /**
     * This method is responsible for handling application creation
     *
     * @param appId application id of the application to which the release should be created
     * @param releaseWrapper {@link EntAppReleaseWrapper} of the release to be created
     * @param isPublished If the app should be added in PUBLISHED state instead of initial state
     * @return Created application release bean
     * @throws ApplicationManagementException if any error occurred while creating the application
     */
    ApplicationRelease createEntAppRelease(int appId, EntAppReleaseWrapper releaseWrapper, boolean isPublished)
            throws ApplicationManagementException;

    /**
     * This method is responsible for handling application creation
     *
     * @param appId application id of the application to which the release should be created
     * @param releaseWrapper {@link WebAppReleaseWrapper} of the release to be created
     * @param isPublished If the app should be added in PUBLISHED state instead of initial state
     * @return Created application release bean
     * @throws ApplicationManagementException if any error occurred while creating the application
     */
    ApplicationRelease createWebAppRelease(int appId, WebAppReleaseWrapper releaseWrapper, boolean isPublished)
            throws ApplicationManagementException, ResourceManagementException;

    /**
     * This method is responsible for handling application creation
     *
     * @param appId application id of the application to which the release should be created
     * @param releaseWrapper {@link PublicAppReleaseWrapper} of the release to be created
     * @param isPublished If the app should be added in PUBLISHED state instead of initial state
     * @return Created application release bean
     * @throws ApplicationManagementException if any error occurred while creating the application
     */
    ApplicationRelease createPubAppRelease(int appId, PublicAppReleaseWrapper releaseWrapper, boolean isPublished)
            throws ApplicationManagementException, ResourceManagementException;

    /**
     * This method is responsible for handling application creation
     *
     * @param appId application id of the application to which the release should be created
     * @param releaseWrapper {@link CustomAppReleaseWrapper} of the release to be created
     * @param isPublished If the app should be added in PUBLISHED state instead of initial state
     * @return Created application release bean
     * @throws ApplicationManagementException if any error occurred while creating the application
     */
    ApplicationRelease createCustomAppRelease(int appId, CustomAppReleaseWrapper releaseWrapper, boolean isPublished)
            throws ResourceManagementException, ApplicationManagementException;

    /**
     * Checks if release is available for a given application wrapper, and if exists it uploads
     * the artifacts of the release
     *
     * @param app Application wrapper bean of the application
     * @param <T> Application Wrapper class
     * @return constructed ApplicationDTO after uploading the release artifacts if exist
     * @throws ApplicationManagementException if any error occurred while uploading release artifacts
     */
    <T> ApplicationDTO uploadReleaseArtifactIfExist(T app) throws ApplicationManagementException;

    /**
     * Check if public app release packageName is valid (I.E invalid if packageName already exists)
     *
     * @param packageName name of the application release package
     * @throws ApplicationManagementException if package name is invalid
     */
    void validatePublicAppReleasePackageName(String packageName) throws ApplicationManagementException;

    /**
     * This method is responsible to add application data into APPM database. However, before call this method it is
     * required to do the validation of request and check the existence of application releaseDTO.
     *
     * @param applicationDTO Application DTO object.
     * @param isPublished Wether the app should be added in PUBLISHED state instead of initial state
     * @return {@link Application}
     * @throws ApplicationManagementException which throws if error occurs while during application management.
     */
    Application addAppDataIntoDB(ApplicationDTO applicationDTO, boolean isPublished) throws
            ApplicationManagementException;

    /**
     * This method is responsible for handling application creation
     *
     * @param app Application wrapper object which depends on the application type
     * @param isPublished If the app should be created in PUBLISHED state
     * @param <T> Application wrapper class which depends on the application type
     * @return Created application bean
     * @throws ApplicationManagementException if any error occurred while creating the application
     */
   <T> Application createApplication(T app, boolean isPublished) throws ApplicationManagementException;
    /**
     * Add an application to favourites
     * @param appId id of the application
     * @throws ApplicationManagementException Catch all other throwing exceptions and throw {@link ApplicationManagementException}
     */
    void addAppToFavourites(int appId) throws ApplicationManagementException;

    /**
     * Remove an application from favourites
     * @param appId id of the application
     * @throws ApplicationManagementException Catch all other throwing exceptions and throw {@link ApplicationManagementException}
     */
    void removeAppFromFavourites(int appId) throws ApplicationManagementException;

    /**
     * Check if an application is a favourite app
     * @param appId id of the application
     * @throws ApplicationManagementException Catch all other throwing exceptions and throw {@link ApplicationManagementException}
     */
    boolean isFavouriteApp(int appId) throws ApplicationManagementException;

    /**
     * Check the existence of an application for given application name and the device type.
     *
     * @param appName Application name
     * @param deviceTypeName Device Type name
     * @return True if application exists for given application name and the device type, otherwise returns False
     * @throws ApplicationManagementException if error occured while checking the application existence for given
     * application name and device type or request with invalid device type data.
     */
    boolean isExistingAppName(String appName, String deviceTypeName) throws ApplicationManagementException;

    /**
     * Updates an already existing application.
     *
     * @param applicationId ID of the application
     * @param applicationUpdateWrapper Application data that need to be updated.
     * @return Updated Application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    Application updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper)
            throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID for tha application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    void deleteApplication(int applicationId) throws ApplicationManagementException;

    /**
     * Retire an application identified by the unique ID.
     *
     * @param applicationId ID for tha application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    void retireApplication(int applicationId) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param releaseUuid UUID of tha application release
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    void deleteApplicationRelease(String releaseUuid) throws ApplicationManagementException;

    ApplicationList getFavouriteApplications(Filter filter) throws ApplicationManagementException;

    /**
     * Use to delete application artifact files (For example this is useful to delete application release artifacts
     * (I.E screenshots) when an application is deleted)
     *
     * @param directoryPaths directory paths that contains release artifacts (I.E screenshots)
     * @throws ApplicationManagementException if error occurred while deleting artifacts
     */
    void deleteApplicationArtifacts(List<String> directoryPaths) throws ApplicationManagementException;

    /**
     * To get the applications based on the search filter.
     *
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    ApplicationList getApplications(Filter filter) throws ApplicationManagementException;

    /**
     *
     * @param applicationReleaseDTOs application releases of the application
     * @return if application is hide-able
     * @throws ApplicationManagementException if any error occurred while checking if hide-able
     */
    boolean isHideableApp(List<ApplicationReleaseDTO> applicationReleaseDTOs)
            throws ApplicationManagementException;

    /**
     *
     * @param applicationReleaseDTOs application releases of the application
     * @return if application is deletable
     * @throws ApplicationManagementException if any error occurred while checking if deletable
     */
    boolean isDeletableApp(List<ApplicationReleaseDTO> applicationReleaseDTOs)
            throws ApplicationManagementException;

    /**
     * To get list of applications that application releases has given package names.
     *
     * @param packageNames List of package names.
     * @return List of applications {@link Application}
     * @throws ApplicationManagementException if error occurred while getting application data from DB or error
     * occurred while accessing user store.
     */
    List<Application> getApplications(List<String> packageNames) throws ApplicationManagementException;

    /**
     * To create an application release for an ApplicationDTO.
     *
     * @param applicationDTO     ApplicationDTO of the release
     * @param applicationReleaseDTO ApplicatonRelease that need to be be created.
     * @param type {@link ApplicationType}
     * @param isPublished if the app should be added in PUBLISHED state instead of initial state
     * @return the unique id of the application release, if the application release succeeded else -1
     */
    <T> ApplicationRelease createRelease(ApplicationDTO applicationDTO, ApplicationReleaseDTO applicationReleaseDTO,
                                         ApplicationType type, boolean isPublished)
            throws ApplicationManagementException;

    /**
     * Get application and all application releases associated to the application for the given application Id
     *
     * @param applicationId Application Id
     * @return {@link ApplicationDTO}
     * @throws ApplicationManagementException if error occurred application data from the database.
     */
    ApplicationDTO getApplication(int applicationId) throws ApplicationManagementException;

    /**
     * To get the Application for given Id.
     *
     * @param id id of the ApplicationDTO
     * @param state state of the ApplicationDTO
     * @return the ApplicationDTO identified by the ID
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    Application getApplicationById(int id, String state) throws ApplicationManagementException;

    /**
     * To get the Application Release for given uuid.
     *
     * @param uuid uuid of the ApplicationDTO
     * @return the Application Release identified by the UUID
     * @throws ApplicationManagementException Application Management Exception.
     */
    Application getApplicationByUuid(String uuid) throws ApplicationManagementException;

    /**
     * To get the ApplicationDTO for given application relase UUID.
     *
     * @param uuid UUID of the ApplicationDTO
     * @param state state of the ApplicationDTO
     * @return the ApplicationDTO identified by the ID
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    Application getApplicationByUuid(String uuid, String state) throws ApplicationManagementException;

    /**
     * To get lifecycle state change flow of a particular Application Release.
     *
     * @param releaseUuid UUID of the Application Release.
     * @return the List of LifecycleStates which represent the lifecycle change flow of the application releases.
     * @throws ApplicationManagementException Application Management Exception.
     */
    List<LifecycleState> getLifecycleStateChangeFlow(String releaseUuid) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular ApplicationDTO.
     *
     * @param releaseUuid UUID of the ApplicationDTO Release.
     * @param lifecycleChanger Lifecycle changer that contains the action and the reson for the change.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     * @return
     */
    ApplicationRelease changeLifecycleState(String releaseUuid, LifecycleChanger lifecycleChanger)
            throws ApplicationManagementException;
    
    /**
     * To get all the releases of a particular ApplicationDTO.
     *
     * @param applicationReleaseDTO  of the ApplicationDTO Release.
     * @param lifecycleChanger Lifecycle changer that contains the action and the reason for the change.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     * @return
     */
    ApplicationRelease changeLifecycleState(ApplicationReleaseDTO applicationReleaseDTO, LifecycleChanger lifecycleChanger)
            throws ApplicationManagementException;
    
    /**
     * To update release images such as icons, banner and screenshots.
     *
     * @param uuid    uuid of the ApplicationDTO
     * @param applicationArtifact Application artifact that contains names and input streams of the application artifacts.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException;

    /**
     * To update release images.
     *
     * @param deviceType Application artifact compatible device type name.
     * @param uuid    uuid of the ApplicationDTO
     * @param  applicationArtifact Application artifact that contains names and input streams of the application artifacts.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    void updateApplicationArtifact(String deviceType, String uuid,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /**
     * Use to update existing enterprise app release
     *
     * @param releaseUuid UUID of the application release.
     * @param entAppReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationArtifact {@link ApplicationArtifact}
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    ApplicationRelease updateEntAppRelease(String releaseUuid, EntAppReleaseWrapper entAppReleaseWrapper,
                                           ApplicationArtifact applicationArtifact) throws ApplicationManagementException;


    /**
     * Use to update existing public app release
     *
     * @param releaseUuid UUID of the application release.
     * @param publicAppReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationArtifact {@link ApplicationArtifact}
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    ApplicationRelease updatePubAppRelease(String releaseUuid, PublicAppReleaseWrapper publicAppReleaseWrapper,
                                           ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /**
     * Use to update existing web app release
     *
     * @param releaseUuid UUID of the application release.
     * @param webAppReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationArtifact {@link ApplicationArtifact}
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    ApplicationRelease updateWebAppRelease(String releaseUuid, WebAppReleaseWrapper webAppReleaseWrapper,
                                           ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /**
     * Use to update existing custom app release
     *
     * @param releaseUuid UUID of the application release.
     * @param customAppReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationArtifact {@link ApplicationArtifact}
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    ApplicationRelease updateCustomAppRelease(String releaseUuid, CustomAppReleaseWrapper customAppReleaseWrapper,
                                              ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /**
     * To validate the application creating request
     *
     */
    <T> void validateAppCreatingRequest(T param) throws ApplicationManagementException, RequestValidatingException;

    /**
     *
     * @throws ApplicationManagementException throws if payload does not satisfy requirements.
     */
    <T> void validateReleaseCreatingRequest(T releases, String deviceType) throws ApplicationManagementException;

    /**
     * Validate enterprise application release
     *
     */
    void validateEntAppReleaseCreatingRequest(EntAppReleaseWrapper releaseWrapper, String deviceType)
            throws RequestValidatingException, ApplicationManagementException;

    /**
     * Validate custom application release
     *
     */
    void validateCustomAppReleaseCreatingRequest(CustomAppReleaseWrapper releaseWrapper, String deviceType)
            throws RequestValidatingException, ApplicationManagementException;

    /**
     * Validate web application release
     *
     */
    void validateWebAppReleaseCreatingRequest(WebAppReleaseWrapper releaseWrapper)
            throws RequestValidatingException, ApplicationManagementException;

    /**
     * Validate public application release
     *
     */
    void validatePublicAppReleaseCreatingRequest(PublicAppReleaseWrapper releaseWrapper, String deviceType)
            throws RequestValidatingException, ApplicationManagementException;

    /**
     * Validates image files of the application release
     *
     * @param iconFile icon of the application release
     * @param screenshots screenshots of the application release
     * @throws RequestValidatingException if any image is invalid
     */
    void validateImageArtifacts(Base64File iconFile, List<Base64File> screenshots) throws RequestValidatingException;

    /**
     * Validates any base64 files, for example a base64file may an empty file name which is invalid
     *
     * @param file Base64 File to be validated
     * @throws RequestValidatingException if the file is invalid
     */
    void validateBase64File(Base64File file) throws RequestValidatingException;

 /***
     *
     * @param iconFile Icon file for the application.
     * @param bannerFile Banner file for the application.
     * @param attachmentList Screenshot list.
     * @throws RequestValidatingException If request doesn't contains required attachments.
     */
    void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList)
            throws RequestValidatingException;

    /**
     * Validates binary file of the application release
     *
     * @param binaryFile binary file of the application release
     * @throws RequestValidatingException if binary file is invalid
     */
    void validateBinaryArtifact(Base64File binaryFile) throws RequestValidatingException;

    void validateBinaryArtifact(Attachment binaryFile) throws RequestValidatingException;

    void addApplicationCategories(List<String> categories) throws ApplicationManagementException;

    List<Tag> getRegisteredTags() throws ApplicationManagementException;

    List<Category> getRegisteredCategories() throws ApplicationManagementException;

    void deleteApplicationTag(int appId, String tagName) throws ApplicationManagementException;

    void deleteTag(String tagName) throws ApplicationManagementException;

    void deleteUnusedTag(String tagName) throws ApplicationManagementException;

    void updateTag(String oldTagName, String newTagName) throws ApplicationManagementException;

    List<String> addTags(List<String> tags) throws ApplicationManagementException;

    List<String> addApplicationTags(int appId, List<String> tags) throws ApplicationManagementException;

    List<String> addCategories(List<String> categories) throws ApplicationManagementException;

    void deleteCategory(String categoryName) throws ApplicationManagementException;

    void updateCategory(String oldCategoryName, String newCategoryName) throws ApplicationManagementException;

    String getInstallableLifecycleState() throws ApplicationManagementException;

    /**
     * Check if there are subscription devices for operations
     *
     * @param operationId Id of the operation
     * @param deviceId  deviceId of the relevant device
     * @return boolean value either true or false according to the situation
     * @throws ApplicationManagementException
     */
    boolean checkSubDeviceIdsForOperations(int operationId, int deviceId) throws ApplicationManagementException;

    void updateSubStatus(int deviceId, List<Integer> operationId, String status) throws ApplicationManagementException;

    void updateSubsStatus(int deviceId, int operationId, String status) throws ApplicationManagementException;

    /**
     * Get plist content to download and install the application.
     *
     * @param uuid Release UUID of the application.
     * @return plist string
     * @throws ApplicationManagementException Application management exception
     */
    String getPlistArtifact(String uuid) throws ApplicationManagementException;

    List<ApplicationReleaseDTO> getReleaseByPackageNames(List<String> packageIds) throws ApplicationManagementException;
}
