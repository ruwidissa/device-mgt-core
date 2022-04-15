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

import io.entgra.application.mgt.common.*;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.dto.CategoryDTO;
import io.entgra.application.mgt.common.dto.TagDTO;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * ApplicationDAO is responsible for handling all the Database related operations related with Application Management.
 */
public interface ApplicationDAO {

    /**
     * Use to create an application for given application data and tenant.
     *
     * @param applicationDTO ApplicationDTO that need to be created.
     * @return Primary key of the created application.
     * @throws ApplicationManagementDAOException if error occurred wile executing query to inser app data into database.
     */
    int createApplication(ApplicationDTO applicationDTO, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Use to add an application to favourites for given appId, username and tenantId
     *
     * @param appId id of the application
     * @param userName currently logged-in user
     * @throws ApplicationManagementDAOException if error occurred wile executing query to insert data into database.
     */
    void addAppToFavourite(int appId, String userName, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Use to remove an application from favourites for given appId, username and tenantId
     *
     * @param appId id of the application
     * @param userName currently logged-in username
     * @throws ApplicationManagementDAOException if error occurred wile executing query to delete data from database.
     */
    void removeAppFromFavourite(int appId, String userName, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Use to check if an app is favourite for given username and tenantId
     * @param appId id of the application
     * @param userName currently logged-in username
     * @return If application is favourite
     * @throws ApplicationManagementDAOException if error occurred wile executing query to check if application is a favourite
     */
    boolean isFavouriteApp(int appId, String userName, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To add tags for a particular application.
     *
     * @param tags tags that need to be added for a application.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void addTags(List<String> tags, int tenantId) throws ApplicationManagementDAOException;

    List<TagDTO> getAllTags(int tenantId) throws ApplicationManagementDAOException;

    List<Integer> getTagIdsForTagNames (List<String> tagNames, int tenantId) throws ApplicationManagementDAOException;

    TagDTO getTagForTagName(String tagName, int tenantId) throws ApplicationManagementDAOException;

    List<Integer> getDistinctTagIdsInTagMapping() throws ApplicationManagementDAOException;

    void addTagMapping (List<Integer>  tagIds, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    List<String> getAppTags(int appId, int tenantId) throws ApplicationManagementDAOException;

    boolean hasTagMapping(int tagId, int appId, int tenantId) throws ApplicationManagementDAOException;

    boolean hasTagMapping(int tagId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplicationTags(List<Integer> tagIds, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplicationTag(Integer tagId, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplicationTags(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteTagMapping(int tagId, int tenantId) throws ApplicationManagementDAOException;

    void deleteTag(int tagId, int tenantId) throws ApplicationManagementDAOException;

    void updateTag(TagDTO tagDTO, int tenantId) throws ApplicationManagementDAOException;

    List<String> getAppCategories (int appId, int tenantId) throws ApplicationManagementDAOException;

    boolean hasCategoryMapping(int categoryId, int tenantId) throws ApplicationManagementDAOException;

    List<CategoryDTO> getAllCategories(int tenantId) throws ApplicationManagementDAOException;

    List<Integer> getCategoryIdsForCategoryNames(List<String> categoryNames, int tenantId)
            throws ApplicationManagementDAOException;

    List<Integer> getDistinctCategoryIdsInCategoryMapping() throws ApplicationManagementDAOException;

    CategoryDTO getCategoryForCategoryName(String categoryName, int tenantId) throws ApplicationManagementDAOException;

    void addCategories(List<String> categories, int tenantId) throws ApplicationManagementDAOException;

    void addCategoryMapping(List<Integer> categoryIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException;

    void deleteAppCategories(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteAppCategories(List<Integer> categoryIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException;

    void deleteCategory(int categoryId, int tenantId) throws ApplicationManagementDAOException;

    void updateCategory(CategoryDTO categoryDTO, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the applications that satisfy the given criteria.
     *
     * @param filter   Filter criteria.
     * @param deviceTypeId ID of the device type
     * @param tenantId Id of the tenant.
     * @return ApplicationDTO list
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    List<ApplicationDTO> getApplications(Filter filter, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given id
     *
     * @param applicationId Id of the application to be retrieved.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplication(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param releaseUuid UUID of the application release.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplication(String releaseUuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get application with a specific application release which has given application release UUID
     *
     * @param releaseUuid Applicatioon Release UUID
     * @param tenantId Tenant Id
     * @return {@link ApplicationDTO}
     * @throws ApplicationManagementDAOException if error occurred while getting application and
     * application release data from the DB.
     */
    ApplicationDTO getAppWithRelatedRelease(String releaseUuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get list of applications with a specific application releases which has given package names.
     *
     * @param packageNames List of package names
     * @param tenantId Tenant Id
     * @return List of {@link ApplicationDTO}
     * @throws ApplicationManagementDAOException if error occurred while getting application and
     * application release data from the DB.
     */
    List<ApplicationDTO> getAppWithRelatedReleases(List<String> packageNames, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * Verify whether application exist for given application name and device type. Because a name and device type is
     * unique for an application.
     *
     * @param appName     name of the application.
     * @param deviceTypeId  ID of the device type.
     * @param tenantId ID of the tenant.
     * @return ID of the ApplicationDTO.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isExistingAppName(String appName, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To edit the given application.
     *
     * @param application ApplicationDTO that need to be edited.
     * @param tenantId    Tenant ID of the ApplicationDTO.
     * @return Updated ApplicationDTO.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    boolean updateApplication(ApplicationDTO application, int tenantId) throws ApplicationManagementDAOException;

    void updateApplicationRating(String uuid, double rating, int tenantId) throws ApplicationManagementDAOException;


    /**
     * To delete the application
     *
     * @param appId     ID of the application.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void retireApplication(int appId) throws ApplicationManagementDAOException;

    /**
     * To get the application count that satisfies gives search query.
     *
     * @param filter ApplicationDTO Filter.
     * @param tenantId Id of the tenant
     * @return count of the applications
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    int getApplicationCount(Filter filter, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplication(int appId, int tenantId) throws ApplicationManagementDAOException;
}
