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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * This is responsible for ApplicationDTO Release related DAO operations.
 */
public interface ApplicationReleaseDAO {

    /**
     * To create an ApplicationDTO release.
     *
     * @param applicationRelease ApplicationDTO Release that need to be created.
     * @return Unique ID of the relevant release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationReleaseDTO createRelease(ApplicationReleaseDTO applicationRelease, int appId, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To get a release details with the particular version.
     * @param applicationName name of the application to get the release.
     * @param versionName Name of the version
     * @param applicationType Type of the application release
     * @param releaseType type of the release
     * @param tenantId tenantId of the application

     * @return ApplicationReleaseDTO for the particular version of the given application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationReleaseDTO getRelease(String applicationName,String applicationType, String versionName,
            String releaseType, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To get all the releases of a particular application.
     *
     * @param applicationId Id of the application
     * @param tenantId tenant id of the application
     * @return list of the application releases
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    List<ApplicationReleaseDTO> getReleases(int applicationId, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To get the release by state.
     *
     * @param appId Id of the ApplicationDTO
     * @param tenantId tenant id of the application
     * @param state state of the application
     * @return list of the application releases
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    List<ApplicationReleaseDTO> getReleaseByState(int appId, int tenantId,  String state)
            throws ApplicationManagementDAOException;

    /**
     * To update an ApplicationDTO release.
     *
     * @param applicationRelease ApplicationReleaseDTO that need to be updated.
     * @param tenantId           Id of the tenant
     * @return the updated ApplicationDTO Release
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception
     */
    ApplicationReleaseDTO updateRelease(ApplicationReleaseDTO applicationRelease, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To update an ApplicationDTO release.
     * @param uuid UUID of the ApplicationReleaseDTO that need to be updated.
     * @param rating given stars for the application.
     * @param ratedUsers number of users who has rated for the application release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception
     */
    void updateRatingValue(String uuid, double rating, int ratedUsers) throws ApplicationManagementDAOException;

    /**
     * To retrieve rating of an application release.
     *
     * @param uuid UUID of the application Release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    Rating getRating(String uuid, int tenantId) throws ApplicationManagementDAOException;


    /**
     * To delete a particular release.
     *
     * @param id      ID of the ApplicationDTO which the release need to be deleted.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void deleteRelease(int id) throws ApplicationManagementDAOException;

    /**
     * To get release details of a specific application.
     *
     * @param applicationId ID of the application.
     * @param releaseUuid UUID of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationReleaseDTO getReleaseByIds(int applicationId, String releaseUuid, int tenantId) throws
            ApplicationManagementDAOException;

    ApplicationReleaseDTO getReleaseByUUID(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not.
     *
     * @param appId ID of the application.
     * @param uuid UUID of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    boolean verifyReleaseExistence(int appId, String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not for the given app release version.
     *
     * @param hashVal Hash value of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    boolean verifyReleaseExistenceByHash(String hashVal, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not for the given app release version.
     *
     * @param appId ID of the application.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    String getPackageName(int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To verify whether application release exist or not for given application release uuid.
     *
     * @param uuid UUID of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean verifyReleaseExistenceByUuid(String uuid, int tenantId) throws ApplicationManagementDAOException;

    String getReleaseHashValue(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /***
     *
     * @param packageName Application release package name
     * @param tenantId Tenant ID
     * @return True if application release package name already exist in the IoT server, Otherwise returns False.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState) throws ApplicationManagementDAOException;

    }
