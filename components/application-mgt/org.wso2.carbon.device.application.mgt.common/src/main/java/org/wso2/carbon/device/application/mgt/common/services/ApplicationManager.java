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
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.LifecycleStateDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;

import java.io.InputStream;
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
            throws ApplicationManagementException, RequestValidatingException;

    /**
     * Updates an already existing application.
     *
     * @param applicationWrapper Application that need to be updated.
     * @param applicationId ID of the application
     * @return Updated Application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    void updateApplication(int applicationId, ApplicationWrapper applicationWrapper) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID for tha application
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    List<String> deleteApplication(int applicationId) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID of tha application
     * @param releaseUuid UUID of tha application release
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    String deleteApplicationRelease(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get the applications based on the search filter.
     *
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException ApplicationDTO Management Exception
     */
    ApplicationList getApplications(Filter filter) throws ApplicationManagementException;

    /**
     * To get the ApplicationDTO for given Id.
     *
     * @param id id of the ApplicationDTO
     * @param state state of the ApplicationDTO
     * @return the ApplicationDTO identified by the ID
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    Application getApplicationById(int id, String state) throws ApplicationManagementException;

    /**
     * To get the ApplicationDTO for given application relase UUID.
     *
     * @param uuid UUID of the ApplicationDTO
     * @param state state of the ApplicationDTO
     * @return the ApplicationDTO identified by the ID
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    ApplicationDTO getApplicationByUuid(String uuid, String state) throws ApplicationManagementException;

    /**
     * To get an application associated with the release.
     *
     * @param appReleaseUUID UUID of the app release
     * @return {@link ApplicationDTO} associated with the release
     * @throws ApplicationManagementException If unable to retrieve {@link ApplicationDTO} associated with the given UUID
     */
    ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular ApplicationDTO.
     *
     * @param applicationId ID of the ApplicationDTO .
     * @param releaseUuid UUID of the ApplicationDTO Release.
     * @return the LifecycleStateDTO of the ApplicationDTO releases related with the particular ApplicationDTO.
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    LifecycleStateDTO getLifecycleState(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular ApplicationDTO.
     *
     * @param applicationId ID of the ApplicationDTO.
     * @param releaseUuid UUID of the ApplicationDTO Release.
     * @param state Lifecycle state to change the app
     * @throws ApplicationManagementException ApplicationDTO Management Exception.
     */
    void changeLifecycleState(int applicationId, String releaseUuid, LifecycleStateDTO state)
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
    ApplicationRelease createRelease(int applicationId, ApplicationReleaseWrapper applicationReleaseWrapper,
            ApplicationArtifact applicationArtifact) throws ApplicationManagementException;

    /***
     *
     * @param applicationId ID of the application
     * @param releaseUuid UUID of the application release
     * @param deviceType Supported device type of the application
     * @param applicationRelease {@link ApplicationReleaseDTO}
     * @param binaryFileStram {@link InputStream} of the binary file
     * @param iconFileStream {@link InputStream} of the icon
     * @param bannerFileStream {@link InputStream} of the banner
     * @param attachments {@link List} of {@link InputStream} of attachments
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    boolean updateRelease(int applicationId, String releaseUuid, String deviceType, ApplicationReleaseDTO applicationRelease,
            InputStream binaryFileStram, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> attachments) throws ApplicationManagementException;

    /***
     * To validate the application creating request
     *
     * @param applicationWrapper {@link ApplicationDTO}
     * @throws RequestValidatingException if the payload contains invalid inputs.
     */
    void validateAppCreatingRequest(ApplicationWrapper applicationWrapper) throws RequestValidatingException;

    /***
     *
     * @param applicationReleaseWrapper {@link ApplicationReleaseDTO}
     * @param applicationType Type of the application
     * @throws RequestValidatingException throws if payload does not satisfy requrements.
     */
    void validateReleaseCreatingRequest(ApplicationReleaseWrapper applicationReleaseWrapper, String applicationType)
            throws RequestValidatingException;

    /***
     *
     * @param iconFile Icon file for the application.
     * @param bannerFile Banner file for the application.
     * @param attachmentList Screenshot list.
     * @throws RequestValidatingException If request doesn't contains required attachments.
     */
    void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList)
            throws RequestValidatingException;

    void validateBinaryArtifact(Attachment binaryFile, String applicationType) throws RequestValidatingException;


    void addAplicationCategories(List<String> categories) throws ApplicationManagementException;

    }
