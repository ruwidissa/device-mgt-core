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
package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;

/**
 * ReviewManager is responsible for handling all the add/update/delete/get operations related with
 */
public interface ReviewManager {

    /**
     * To add a review to a application
     *
     * @param review  review of the application.
     * @param appId     id of the application.
     * @param appReleaseId id of the application release
     * @return {@link Review} Review added
     * @throws ReviewManagementException Exceptions of the review management.
     */
    boolean addReview(Review review,int appId, int appReleaseId) throws ReviewManagementException;

    /**
     * Get all comments to pagination
     *
     * @param request Pagination request {@link PaginationRequest}
     * @param uuid    uuid of the application release
     * @return {@link PaginationResult} pagination result with starting offSet and limit
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    PaginationResult getAllReviews(PaginationRequest request, String uuid) throws ReviewManagementException;

    /**
     * To get the comment with id.
     *
     * @param commentId id of the comment
     * @return {@link Review}Review of the comment id
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    Review getReview(int commentId) throws ReviewManagementException;

    /**
     * To delete review using review id.
     *
     * @param commentId id of the comment
     * @throws ReviewManagementException Exceptions of the comment management
     */
    void deleteReview(String loggedInUser, int commentId) throws ReviewManagementException;

    /**
     * To update a review.
     *
     * @param review   review of the application.
     * @param reviewId id of the review
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the review management
     */
    boolean updateReview(Review review, int reviewId, boolean checkExistence) throws ReviewManagementException;

    /**
     * To get the overall rating for a application release
     *
     * @param appReleaseUuuid   UUID of the application release.
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the review management
     */
    Rating getRating(String appReleaseUuuid) throws ReviewManagementException;
}