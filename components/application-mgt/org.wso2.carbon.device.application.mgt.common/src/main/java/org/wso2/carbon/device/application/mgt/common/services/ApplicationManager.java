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
import org.wso2.carbon.device.application.mgt.common.entity.ApplicationEntity;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.entity.ApplicationReleaseEntity;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.entity.LifecycleStateEntity;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This interface manages the application creation, deletion and editing of the application.
 */
public interface ApplicationManager {

    /**
     * Creates an application.
     *
     * @param applicationWrapper ApplicationEntity that need to be created.
     * @return Created application
     * @throws ApplicationManagementException ApplicationEntity Management Exception
     */
    ApplicationEntity createApplication(ApplicationWrapper applicationWrapper, ApplicationArtifact applicationArtifact)
            throws ApplicationManagementException, RequestValidatingException;

    /**
     * Updates an already existing application.
     *
     * @param application ApplicationEntity that need to be updated.
     * @param applicationId ID of the application
     * @return Updated ApplicationEntity
     * @throws ApplicationManagementException ApplicationEntity Management Exception
     */
    ApplicationEntity updateApplication(int applicationId, ApplicationEntity application) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID for tha application
     * @throws ApplicationManagementException ApplicationEntity Management Exception
     */
    List<String> deleteApplication(int applicationId) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID of tha application
     * @param releaseUuid UUID of tha application release
     * @throws ApplicationManagementException ApplicationEntity Management Exception
     */
    String deleteApplicationRelease(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get the applications based on the search filter.
     *
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException ApplicationEntity Management Exception
     */
    ApplicationList getApplications(Filter filter, String deviceTypeName) throws ApplicationManagementException;

    /**
     * To get the ApplicationEntity for given Id.
     *
     * @param id id of the ApplicationEntity
     * @param state state of the ApplicationEntity
     * @return the ApplicationEntity identified by the ID
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    ApplicationEntity getApplicationById(int id, String state) throws ApplicationManagementException;

    /**
     * To get the ApplicationEntity for given application relase UUID.
     *
     * @param uuid UUID of the ApplicationEntity
     * @param state state of the ApplicationEntity
     * @return the ApplicationEntity identified by the ID
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    ApplicationEntity getApplicationByUuid(String uuid, String state) throws ApplicationManagementException;

    /**
     * To get an application associated with the release.
     *
     * @param appReleaseUUID UUID of the app release
     * @return {@link ApplicationEntity} associated with the release
     * @throws ApplicationManagementException If unable to retrieve {@link ApplicationEntity} associated with the given UUID
     */
    ApplicationEntity getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular ApplicationEntity.
     *
     * @param applicationId ID of the ApplicationEntity .
     * @param releaseUuid UUID of the ApplicationEntity Release.
     * @return the LifecycleStateEntity of the ApplicationEntity releases related with the particular ApplicationEntity.
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    LifecycleStateEntity getLifecycleState(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular ApplicationEntity.
     *
     * @param applicationId ID of the ApplicationEntity.
     * @param releaseUuid UUID of the ApplicationEntity Release.
     * @param state Lifecycle state to change the app
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    void changeLifecycleState(int applicationId, String releaseUuid, LifecycleStateEntity state)
            throws ApplicationManagementException;

    /**
     * To update release images such as icons, banner and screenshots.
     *
     * @param appId    ID of the ApplicationEntity
     * @param uuid    uuid of the ApplicationEntity
     * @param iconFileStream    icon file of the release
     * @param bannerFileStream    bannerFileStream of the release.
     * @param attachments    screenshot attachments of the release
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    void updateApplicationImageArtifact(int appId, String uuid, InputStream iconFileStream, InputStream
            bannerFileStream, List<InputStream> attachments) throws ApplicationManagementException;


    /**
     * To update release images.
     *
     * @param appId    ID of the ApplicationEntity
     * @param deviceType   Applicable device type of the application
     * @param uuid    uuid of the ApplicationEntity
     * @param binaryFile    binaryFile of the release.
     * @throws ApplicationManagementException ApplicationEntity Management Exception.
     */
    void updateApplicationArtifact(int appId, String deviceType, String uuid, InputStream binaryFile)
            throws ApplicationManagementException;

    /**
     * To create an application release for an ApplicationEntity.
     *
     * @param applicationId     ID of the ApplicationEntity
     * @param applicationRelease ApplicatonRelease that need to be be created.
     * @return the unique id of the application release, if the application release succeeded else -1
     */
    ApplicationReleaseEntity createRelease(int applicationId, ApplicationReleaseEntity applicationRelease)
            throws ApplicationManagementException;

    /***
     *
     * @param applicationId ID of the application
     * @param releaseUuid UUID of the application release
     * @param deviceType Supported device type of the application
     * @param applicationRelease {@link ApplicationReleaseEntity}
     * @param binaryFileStram {@link InputStream} of the binary file
     * @param iconFileStream {@link InputStream} of the icon
     * @param bannerFileStream {@link InputStream} of the banner
     * @param attachments {@link List} of {@link InputStream} of attachments
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    boolean updateRelease(int applicationId, String releaseUuid, String deviceType, ApplicationReleaseEntity applicationRelease,
            InputStream binaryFileStram, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> attachments) throws ApplicationManagementException;

    void validateAppCreatingRequest(ApplicationWrapper applicationWrapper, Attachment binaryFile, Attachment iconFile,
            Attachment bannerFile, List<Attachment> attachmentList) throws RequestValidatingException;

    void validateReleaseCreatingRequest(ApplicationReleaseWrapper applicationReleaseWrapper, String applicationType,
            Attachment binaryFile, Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList)
            throws RequestValidatingException;

    void addAplicationCategories(List<String> categories) throws ApplicationManagementException;

    }
