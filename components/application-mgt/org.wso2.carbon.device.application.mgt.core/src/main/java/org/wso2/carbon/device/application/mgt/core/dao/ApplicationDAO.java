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

import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.CategoryDTO;
import org.wso2.carbon.device.application.mgt.common.dto.TagDTO;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * ApplicationDAO is responsible for handling all the Database related operations related with ApplicationDTO Management.
 */
public interface ApplicationDAO {

    /**
     * To create an application.
     *
     * @param application ApplicationDTO that need to be created.
     * @return Created Application.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    int createApplication(ApplicationDTO application, int tenantId) throws ApplicationManagementDAOException;

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

    void deleteApplicationTags(Integer tagId, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplicationTags(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteTagMapping(int tagId, int tenantId) throws ApplicationManagementDAOException;

    void deleteTag(int tagId, int tenantId) throws ApplicationManagementDAOException;

    void updateTag(TagDTO tagDTO, int tenantId) throws ApplicationManagementDAOException;

    List<String> getAppCategories (int appId, int tenantId) throws ApplicationManagementDAOException;

    boolean hasCategoryMapping(int categoryId, int tenantId) throws ApplicationManagementDAOException;

    List<CategoryDTO> getAllCategories(int tenantId) throws ApplicationManagementDAOException;

    List<Integer> getCategoryIdsForCategoryNames(List<String> CatgeoryNames, int tenantId)
            throws ApplicationManagementDAOException;

    List<Integer> getDistinctCategoryIdsInCategoryMapping() throws ApplicationManagementDAOException;

    CategoryDTO getCategoryForCategoryName(String categoryName, int tenantId) throws ApplicationManagementDAOException;

    void addCategories(List<String> categories, int tenantId) throws ApplicationManagementDAOException;

    void addCategoryMapping(List<Integer> categoryIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException;

    void deleteCategoryMapping (int applicationId, int tenantId) throws ApplicationManagementDAOException;

    void deleteCategory(int categoryId, int tenantId) throws ApplicationManagementDAOException;

    void updateCategory(CategoryDTO categoryDTO, int tenantId) throws ApplicationManagementDAOException;



    /**
     * To check application existence.
     *
     * @param appName appName that need to identify application.
     * @param type type that need to identify application.
     * @param tenantId tenantId that need to identify application.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
     boolean isExistApplication(String appName, String type, int tenantId) throws ApplicationManagementDAOException;

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
     * To get the UUID of latest app release that satisfy the given criteria.
     *
     * @param appId   application id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    String getUuidOfLatestRelease(int appId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param appName     name of the application to be retrieved.
     * @param tenantId ID of the tenant.
     * @param appType Type of the application.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplication(String appName, String appType, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given id
     *
     * @param id ID of the application.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplicationById(String id, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given id
     *
     * @param applicationId Id of the application to be retrieved.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplicationById(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param releaseUuid UUID of the application release.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    ApplicationDTO getApplicationByUUID(String releaseUuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param appId ID of the application
     * @param tenantId Tenant Id
     * @return the boolean value
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    boolean verifyApplicationExistenceById(int appId, int tenantId) throws ApplicationManagementDAOException;

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
    boolean isValidAppName(String appName, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException;

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
    int getApplicationCount(Filter filter, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To delete the tags of a application.
     *
     * @param tags Tags which are going to delete.
     * @param applicationId ID of the application to delete the tags.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    void deleteTags(List<String> tags, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get an {@link ApplicationDTO} associated with the given release
     *
     * @param appReleaseUUID UUID of the {@link ApplicationReleaseDTO}
     * @param tenantId ID of the tenant
     * @return {@link ApplicationDTO} associated with the given release UUID
     * @throws ApplicationManagementDAOException if unable to fetch the ApplicationDTO from the data store.
     */
    ApplicationDTO getApplicationByRelease(String appReleaseUUID, int tenantId) throws ApplicationManagementDAOException;

    String getApplicationSubTypeByUUID(String uuid, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplication(int appId, int tenantId) throws ApplicationManagementDAOException;

}

