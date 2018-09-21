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
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;

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
    Application createApplication(Application application) throws ApplicationManagementException;

    /**
     * Updates an already existing application.
     *
     * @param application Application that need to be updated.
     * @return Updated Application
     * @throws ApplicationManagementException Application Management Exception
     */
    Application updateApplication(Application application) throws ApplicationManagementException;

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
     * To get the applications based on the search filter.
     *
     * @param appId id of the application
     * @return Application release which is published and release of the Application(appId).
     * @throws ApplicationManagementException Application Management Exception
     */
    String getUuidOfLatestRelease(int appId) throws ApplicationManagementException;

    /**
     * To get Application with the given UUID.
     *
     * @param appType type of the Application
     * @param appName name of the Application
     * @return the Application identified by the UUID
     * @throws ApplicationManagementException Application Management Exception.
     */
    Application getApplication(String appType, String appName) throws ApplicationManagementException;

    /**
     * To get an application associated with the release.
     *
     * @param appReleaseUUID UUID of the app release
     * @return {@link Application} associated with the release
     * @throws ApplicationManagementException If unable to retrieve {@link Application} associated with the given UUID
     */
    Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException;

    /**
     * To get Application with the given UUID.
     *
     * @param appId ID of the Application
     * @return the boolean value, whether application exist or not
     * @throws ApplicationManagementException Application Management Exception.
     */
    Boolean verifyApplicationExistenceById(int appId) throws ApplicationManagementException;

    /**
     * To get Application with the given UUID.
     *
     * @return the boolean value, whether user has assigned unrestricted roles to access the application
     * * @throws ApplicationManagementException Application Management Exception.
     */
    Boolean isUserAllowable(List<UnrestrictedRole> unrestrictedRoles, String userName) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular Application.
     *
     * @param applicationId ID of the Application to get all the releases.
     * @return the List of the Application releases related with the particular Application.
     * @throws ApplicationManagementException Application Management Exception.
     */
    List<ApplicationRelease> getReleases(int applicationId) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular Application.
     *
     * @param applicationId ID of the Application .
     * @param applicationUuid UUID of the Application Release.
     * @return the LifecycleState of the Application releases related with the particular Application.
     * @throws ApplicationManagementException Application Management Exception.
     */
    LifecycleState getLifecycleState(int applicationId, String applicationUuid) throws ApplicationManagementException;

    /**
     * To get all the releases of a particular Application.
     *
     * @param applicationId ID of the Application.
     * @param applicationUuid UUID of the Application Release.
     * @throws ApplicationManagementException Application Management Exception.
     */
    void changeLifecycleState(int applicationId, String applicationUuid, LifecycleState state) throws ApplicationManagementException;

    /**
     * Get the application if application is an accessible one.
     *
     * @param applicationId ID of the Application.
     * @throws ApplicationManagementException Application Management Exception.
     */
    Application getApplicationIfAccessible(int applicationId) throws ApplicationManagementException;

    /**
     * Get the application release for given UUID if application release is exists and application id is valid one.
     *
     * @param releaseUuid UUID of the Application Release.
     * @throws ApplicationManagementException Application Management Exception.
     */
    ApplicationRelease getAppReleaseIfExists(int applicationId, String releaseUuid) throws
                                                                                   ApplicationManagementException;

    /**
     * To update with a new release for an Application.
     *
     * @param appId    ID of the Application
     * @param applicationRelease ApplicationRelease
     * @return Updated Application Release.
     * @throws ApplicationManagementException Application Management Exception.
     */
    ApplicationRelease updateRelease(int appId, ApplicationRelease applicationRelease)
            throws ApplicationManagementException;

    /**
     * To verify whether application release is acceptable to update or not.
     *
     * @param appId    ID of the Application
     * @param appReleaseUuid UUID of the ApplicationRelease
     * @return Updated Application Release.
     * @throws ApplicationManagementException Application Management Exception.
     */
    boolean isAcceptableAppReleaseUpdate(int appId, String appReleaseUuid)
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

}
