/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.core.dao;

import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.Rating;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

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
    Rating getReleaseRating(String uuid, int tenantId) throws ApplicationManagementDAOException;

    List<Double> getReleaseRatings(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To delete a particular release.
     *
     * @param id      ID of the ApplicationDTO which the release need to be deleted.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void deleteRelease(int id) throws ApplicationManagementDAOException;

    void deleteReleases(List<Integer> applicationReleaseIds) throws ApplicationManagementDAOException;

    ApplicationReleaseDTO getReleaseByUUID(String uuid, int tenantId) throws ApplicationManagementDAOException;

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
     * @param releaseUuid ID of the application.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    String getPackageName(String releaseUuid, int tenantId) throws ApplicationManagementDAOException;


    String getReleaseHashValue(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /***
     *
     * @param packageName Application release package name
     * @param tenantId Tenant ID
     * @return True if application release package name already exist in the IoT server, Otherwise returns False.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState)
            throws ApplicationManagementDAOException;

    boolean hasExistInstallableAppRelease(String releaseUuid, String installableStateName, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * This method is responsible to return list of application releases which contains one of the
     * providing package name.
     *
     * @param packages List of package names
     * @param tenantId Tenant Id
     * @return List of application releases {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException if error occurred while getting application releases from the DB.
     */
    List<ApplicationReleaseDTO> getReleaseByPackages(List<String> packages, int tenantId)
            throws ApplicationManagementDAOException;
}
