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
 * This interface specifies the database access operations performed for comments.
 */

 public interface ReviewDAO {

    /**
     * To add a review to a application.
     *
     * @param tenantId  tenantId of the commented application.
     * @param review   review of the application.
     * @return If review is added successfully, it return true otherwise false
     * @throws ReviewManagementDAOException Exceptions of the review management DAO.
     */
    boolean addReview(Review review, int appId, int appReleaseId, int tenantId) throws ReviewManagementDAOException;


    Review isExistReview(int appId, int appReleaseId, String username, int tenantId)
            throws DBConnectionException, SQLException;

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
    Review getComment(int commentId) throws ReviewManagementException, SQLException, DBConnectionException;

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
    int getCommentCountByApp(String appType, String appName, String version)
            throws ReviewManagementException, DBConnectionException, SQLException;

    /**
     * To delete comment using comment id.
     *
     * @param commentId id of the comment
     * @throws ReviewManagementException Exceptions of the comment management.
     * @throws DBConnectionException      db connection exception.
     * @throws SQLException               sql exception
     */
    void deleteComment(int commentId) throws ReviewManagementException, DBConnectionException, SQLException;

    /**
     * To delete comments using application details.
     *
     * @param appType type of the commented application.
     * @param appName name of the commented application.
     * @param version version of the commented application.
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    void deleteComments(String appType, String appName, String version) throws ReviewManagementException;

    /**
     * To get comment count for pagination
     *
     * @param request
     * @param uuid
     * @return Review count
     * @throws ReviewManagementException
     */
    int getCommentCount(PaginationRequest request, String uuid) throws ReviewManagementException;
}