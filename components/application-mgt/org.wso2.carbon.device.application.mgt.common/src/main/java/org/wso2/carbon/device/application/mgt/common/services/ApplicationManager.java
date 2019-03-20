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

import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;

import java.io.InputStream;
import java.util.List;

/**
 * This interface manages the application creation, deletion and editing of the application.
 */
public interface ApplicationManager {

    /**
     * Creates an application.
     *
     * @param application Application that need to be created.
     * @return Created application
     * @throws ApplicationManagementException Application Management Exception
     */
    Application createApplication(Application application)
            throws ApplicationManagementException, RequestValidatingException;

    /**
     * Updates an already existing application.
     *
     * @param application Application that need to be updated.
     * @param applicationId ID of the application
     * @return Updated Application
     * @throws ApplicationManagementException Application Management Exception
     */
    Application updateApplication(int applicationId, Application application) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID for tha application
     * @throws ApplicationManagementException Application Management Exception
     */
    List<String> deleteApplication(int applicationId) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     *
     * @param applicationId ID of tha application
     * @param releaseUuid UUID of tha application release
     * @throws ApplicationManagementException Application Management Exception
     */
    String deleteApplicationRelease(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get the applications based on the search filter.
     *
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException Application Management Exception
     */
    ApplicationList getApplications(Filter filter) throws ApplicationManagementException;

    /**
     * To get the Application for given Id.
     *
     * @param id id of the Application
     * @param state state of the Application
     * @return the Application identified by the ID
     * @throws ApplicationManagementException Application Management Exception.
     */
    Application getApplicationById(int id, String state) throws ApplicationManagementException;

    /**
     * To get the Application for given application relase UUID.
     *
     * @param uuid UUID of the Application
     * @param state state of the Application
     * @return the Application identified by the ID
     * @throws ApplicationManagementException Application Management Exception.
     */
    Application getApplicationByUuid(String uuid, String state) throws ApplicationManagementException;

    /**
     * To get an application associated with the release.
     *
     * @param appReleaseUUID UUID of the app release
     * @return {@link Application} associated with the release
     * @throws ApplicationManagementException If unable to retrieve {@link Application} associated with the given UUID
     */
    Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular Application.
     *
     * @param applicationId ID of the Application .
     * @param releaseUuid UUID of the Application Release.
     * @return the LifecycleState of the Application releases related with the particular Application.
     * @throws ApplicationManagementException Application Management Exception.
     */
    LifecycleState getLifecycleState(int applicationId, String releaseUuid) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular Application.
     *
     * @param applicationId ID of the Application.
     * @param releaseUuid UUID of the Application Release.
     * @param state Lifecycle state to change the app
     * @throws ApplicationManagementException Application Management Exception.
     */
    void changeLifecycleState(int applicationId, String releaseUuid, LifecycleState state)
            throws ApplicationManagementException;

    /**
     * To update release images such as icons, banner and screenshots.
     *
     * @param appId    ID of the Application
     * @param uuid    uuid of the Application
     * @param iconFileStream    icon file of the release
     * @param bannerFileStream    bannerFileStream of the release.
     * @param attachments    screenshot attachments of the release
     * @throws ApplicationManagementException Application Management Exception.
     */
    void updateApplicationImageArtifact(int appId, String uuid, InputStream iconFileStream, InputStream
            bannerFileStream, List<InputStream> attachments) throws ApplicationManagementException;


    /**
     * To update release images.
     *
     * @param appId    ID of the Application
     * @param deviceType   Applicable device type of the application
     * @param uuid    uuid of the Application
     * @param binaryFile    binaryFile of the release.
     * @throws ApplicationManagementException Application Management Exception.
     */
    void updateApplicationArtifact(int appId, String deviceType, String uuid, InputStream binaryFile)
            throws ApplicationManagementException;

    /**
     * To create an application release for an Application.
     *
     * @param applicationId     ID of the Application
     * @param applicationRelease ApplicatonRelease that need to be be created.
     * @return the unique id of the application release, if the application release succeeded else -1
     */
    ApplicationRelease createRelease(int applicationId, ApplicationRelease applicationRelease)
            throws ApplicationManagementException;

    /***
     *
     * @param applicationId ID of the application
     * @param releaseUuid UUID of the application release
     * @param deviceType Supported device type of the application
     * @param applicationRelease {@link ApplicationRelease}
     * @param binaryFileStram {@link InputStream} of the binary file
     * @param iconFileStream {@link InputStream} of the icon
     * @param bannerFileStream {@link InputStream} of the banner
     * @param attachments {@link List} of {@link InputStream} of attachments
     * @return If the application release is updated correctly True returns, otherwise retuen False
     */
    boolean updateRelease(int applicationId, String releaseUuid, String deviceType, ApplicationRelease applicationRelease,
            InputStream binaryFileStram, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> attachments) throws ApplicationManagementException;

}
