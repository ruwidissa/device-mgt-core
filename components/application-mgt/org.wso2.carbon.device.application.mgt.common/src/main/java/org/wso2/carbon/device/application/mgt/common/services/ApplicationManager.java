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
package org.wso2.carbon.device.application.mgt.common.services;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.device.application.mgt.common.ApplicationArtifact;
import org.wso2.carbon.device.application.mgt.common.LifecycleChanger;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.response.Category;
import org.wso2.carbon.device.application.mgt.common.response.Tag;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.PublicAppWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.WebAppWrapper;

import java.util.List;

/**
 * This interface manages the application creation, deletion and editing of the application.
 */
public interface ApplicationManager {

    /**
     * Creates an application.
     *
     * @param applicationWrapper Application that need to be created.
     * @return Created application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    Application createApplication(ApplicationWrapper applicationWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException;

    Application createWebClip(WebAppWrapper webAppWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException;

    Application createPublicApp(PublicAppWrapper publicAppWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException;

    /**
     * Updates an already existing application.
     *
     * @param applicationUpdateWrapper Application data that need to be updated.
     * @param applicationId ID of the application
     * @return Updated Application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    void updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper)
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

    /**
     * To get the applications based on the search filter.
     *
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    ApplicationList getApplications(Filter filter) throws ApplicationManagementException;

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
     * To get an application associated with the release.
     *
     * @param appReleaseUUID UUID of the app release
     * @return {@link ApplicationDTO} associated with the release
     * @throws ApplicationManagementException If unable to retrieve {@link ApplicationDTO} associated with the given UUID
     */
    ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException;

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
     * To update release images such as icons, banner and screenshots.
     *
     * @param uuid    uuid of the ApplicationDTO
     * @param applicationArtifact Application artifact that contains names and input streams of the application artifacts.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException;


    /**
     * To update release images.
     *
     * @param deviceType Application artifact compatible device type name.
     * @param appType Type of the application.
     * @param uuid    uuid of the ApplicationDTO
     * @param  applicationArtifact Application artifact that contains names and input streams of the application artifacts.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    void updateApplicationArtifact(String deviceType, String appType, String uuid,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /**
     * To create an application release for an ApplicationDTO.
     *
     * @param applicationId     ID of the ApplicationDTO
     * @param applicationReleaseWrapper ApplicatonRelease that need to be be created.
     * @return the unique id of the application release, if the application release succeeded else -1
     */
    ApplicationRelease createEntAppRelease(int applicationId, ApplicationReleaseWrapper applicationReleaseWrapper,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /***
     *
     * @param deviceType Device type which is supported for the Application.
     * @param applicationType Application Type
     * @param releaseUuid UUID of the application release.
     * @param applicationReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationArtifact {@link ApplicationArtifact}
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    boolean updateRelease(String deviceType, String applicationType, String releaseUuid,
            ApplicationReleaseWrapper applicationReleaseWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException;

    /***
     * To validate the application creating request
     *
     */
    <T> void validateAppCreatingRequest(T param) throws ApplicationManagementException;

    /***
     *
     * @throws RequestValidatingException throws if payload does not satisfy requrements.
     */
    <T> void validateReleaseCreatingRequest(T param) throws ApplicationManagementException;

    /***
     *
     * @param iconFile Icon file for the application.
     * @param bannerFile Banner file for the application.
     * @param attachmentList Screenshot list.
     * @throws RequestValidatingException If request doesn't contains required attachments.
     */
    void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList)
            throws RequestValidatingException;

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
     * Get plist content to download and install the application.
     *
     * @param uuid Release UUID of the application.
     * @return plist string
     * @throws ApplicationManagementException Application management exception
     */
    String getPlistArtifact(String uuid) throws ApplicationManagementException;

}
