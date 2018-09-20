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

import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
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
     * To add a review to an application release.
     *
     * @param tenantId  tenantId.
     * @param review   review of the application.
     * @param uuid UUID of the application release
     * @return If review is added successfully, it return true otherwise false
     * @throws ReviewManagementDAOException Exceptions of the review management DAO.
     */
    boolean addReview(Review review, String uuid, int tenantId) throws ReviewManagementDAOException;

   /**
    * To verify whether review is exists or not.
    *
    * @param uuid   UUID of the application release.
    * @param username   username of the logged in user.
    * @param tenantId  tenantId of the commented application.
    * @return If review exists, review returns
    * @throws ReviewManagementDAOException Exceptions of the review management DAO.
    */
    Review haveUerCommented(String uuid, String username, int tenantId) throws ReviewManagementDAOException;

    /**
     * To update already added comment.
     *
     * @return {@link Review}Updated comment
     * @throws ReviewManagementException Exceptions of the comment management.
     * @throws DBConnectionException      db connection exception
     * @throws SQLException               sql exception
     */
    boolean updateReview(Review review, int reviewId, int tenantId)
            throws ReviewManagementException, DBConnectionException, SQLException;


    /**
     * To get the comment with id.
     *
     * @param commentId id of the comment
     * @return {@link Review}Review
     * @throws ReviewManagementException Exceptions of the comment management.
     * @throws DBConnectionException      db connection exception
     * @throws SQLException               sql exception
     */
    Review getReview(int commentId) throws ReviewManagementException, SQLException, DBConnectionException;

    /**
     * To get all the comments
     *
     * @param uuid    uuid of the application
     * @param request {@link PaginationRequest}pagination request with offSet and limit
     * @param tenantId Tenant id
     * @return {@link List}List of all the comments in an application
     * @throws ReviewManagementDAOException      Review management DAO exception
     **/
    List<Review> getAllReviews(String uuid, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException;

    /**
     * To get list of comments using release id and application id.
     *
     * @return {@link List}List of comments
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    List<Integer> getAllRatingValues(String uuid)throws SQLException, DBConnectionException;

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
     * @param username username of the review owner
     * @param reviewId id of the review
     * @return If review is successfully deleted return 1, otherwise returns 0.
     * @throws ReviewManagementDAOException Review management DAO exception.
     */
    int deleteReview(String username, int reviewId) throws ReviewManagementDAOException;

   /**
    * To delete review using review id, in this case, it doesn't check whether user is review owner or not.
    *
    * @param reviewId id of the review
    * @return If review is successfully deleted return 1, otherwise returns 0.
    * @throws ReviewManagementDAOException Review management DAO exception.
    */
   int deleteReviewByAdmin(int reviewId) throws ReviewManagementDAOException;

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
     * To get comment count for pagination
     *
     * @param request pagination request
     * @param uuid uuid of the application release
     * @return Review count
     * @throws ReviewManagementException
     */
    int getReviewCount(PaginationRequest request, String uuid) throws ReviewManagementException;
}