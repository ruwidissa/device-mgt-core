/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.application.mgt.common.response.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.dto.ReviewDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;

import java.sql.SQLException;
import java.util.List;

/**
 * This interface specifies the database access operations performed for reviews.
 */

 public interface ReviewDAO {

    /**
     * To add a reviewTmp to an application release.
     *
     * @param tenantId  tenantId.
     * @param reviewDTO   reviewTmp of the application.
     * @param appReleaseId UUID of the application release
     * @return If reviewTmp is added successfully, it return true otherwise false
     * @throws ReviewManagementDAOException Exceptions of the reviewTmp management DAO.
     */
    boolean addReview(ReviewDTO reviewDTO, int appReleaseId, int tenantId) throws ReviewManagementDAOException;

   /**
    * To verify whether user has already commented for the application release or not.
    *
    * @param appReleaseIds List of the application release IDs.
    * @param username   username of the logged in user.
    * @param tenantId  tenantId of the commented application.
    * @return If review exists, review returns
    * @throws ReviewManagementDAOException Exceptions of the review management DAO.
    */
    boolean hasUerReviewedApp(List<Integer> appReleaseIds, String username, int tenantId) throws ReviewManagementDAOException;

    /**
     * To update already added comment.
     *
     * @param reviewDTO Updating reviewTmp
     * @param reviewId id of the updating reviewTmp
     * @param tenantId tenant id
     * @return row count if updating is succeed otherwise 0
     * @throws ReviewManagementDAOException Exceptions of the reviewTmp management.
     */
    int updateReview(ReviewDTO reviewDTO, int reviewId, boolean isActiveReview, int tenantId)
            throws ReviewManagementDAOException;


    /**
     * To get the comment with id.
     *
     * @param reviewId id of the review
     * @return {@link Review}
     * @throws ReviewManagementDAOException Exceptions of the review management DAO.
     */
    ReviewDTO getReview(int reviewId) throws ReviewManagementDAOException;

    ReviewDTO getReview(int appReleaseId, int reviewId) throws ReviewManagementDAOException;


    /**
     * To get all reviews
     *
     * @param releaseId    ID of the application release.
     * @param request {@link PaginationRequest}pagination request with offSet and limit
     * @param tenantId Tenant id
     * @return {@link List}List of all reviews for the application release
     * @throws ReviewManagementDAOException      Review management DAO exception
     **/
    List<ReviewDTO> getAllReleaseReviews(int releaseId, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException;

    List<ReviewDTO> getAllActiveAppReviews(List<Integer> releaseIds, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException;

    List<ReviewDTO> getAllActiveAppReviewsOfUser(List<Integer> releaseIds, PaginationRequest request, String username,
            int tenantId) throws ReviewManagementDAOException;

    List<ReviewDTO> getReplyComments(int parentId, int tenantId)
         throws ReviewManagementDAOException;

    /**
     * To get list of comments using release id and application id.
     * @param uuid UUID of the application release
     * @param tenantId tenant id
     * @return {@link List}List of comments
     * @throws ReviewManagementDAOException Exceptions of the review management DAO.
     */
    List<Integer> getAllAppReleaseRatingValues(String uuid, int tenantId) throws ReviewManagementDAOException;

    List<Integer> getAllAppRatingValues(List<String> uuids, int tenantId) throws ReviewManagementDAOException;


    /**
     * To get count of comments by application details.
     *
     * @param appType type of the commented application.
     * @param appName name of the commented application.
     * @param version version of the commented application.
     * @return Count of the comments
     * @throws ReviewManagementException Exceptions of the comment management.
     * @throws DBConnectionException      db connection exception.
     * @throws SQLException               sql exception
     */
    int getReviewCountByApp(String appType, String appName, String version)
            throws ReviewManagementException, DBConnectionException, SQLException;

    /**
     * To delete review using review id and uuid of the application release.
     *
     * @param reviewId id of the review
     * @return If review is successfully deleted return 1, otherwise returns 0.
     * @throws ReviewManagementDAOException Review management DAO exception.
     */
    void deleteReview(int reviewId, int tenantId) throws ReviewManagementDAOException;

 void deleteReviews(List<Integer> reviewIds, int tenantId) throws ReviewManagementDAOException;


 void deleteAllChildCommentsOfReview(int rootParentId, int tenantId) throws ReviewManagementDAOException;

    /**
     * To delete comments using application details.
     *
     * @param appType type of the commented application.
     * @param appName name of the commented application.
     * @param version version of the commented application.
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    void deleteReviews(String appType, String appName, String version) throws ReviewManagementException;

    /**
     * To get review count for a specific application release
     *
     * @param uuid uuid of the application release
     * @return Review count
     * @throws ReviewManagementDAOException Review management DAO exception
     */
    int getReviewCount(String uuid) throws ReviewManagementDAOException;
}