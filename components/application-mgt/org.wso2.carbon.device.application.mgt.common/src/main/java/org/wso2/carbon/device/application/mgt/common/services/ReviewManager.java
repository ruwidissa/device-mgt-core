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

import javassist.NotFoundException;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewDoesNotExistException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;


/**
 * ReviewManager is responsible for handling all the add/update/delete/get operations related with
 */
public interface ReviewManager {

    /**
     * To add a review to an application release
     *
     * @param review  review of the application.
     * @param uuid     uuid of the application release.
     * @return {@link Review} Review added
     * @throws ReviewManagementException Exceptions of the review management.
     */
    boolean addReview(Review review, String uuid)
            throws ReviewManagementException, RequestValidatingException, ApplicationManagementException;

    /**
     * Get all review with pagination
     *
     * @param request Pagination request {@link PaginationRequest}
     * @param uuid    uuid of the application release
     * @return {@link PaginationResult} pagination result with starting offSet and limit
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    PaginationResult getAllReviews(PaginationRequest request, String uuid) throws ReviewManagementException;

    /**
     * To delete review using review id.
     *
     * @param uuid UUID of the application release
     * @param commentId id of the comment
     * @return If review is successfully deleted return true, otherwise returns false
     * @throws ReviewManagementException Exceptions of the comment management
     */
    boolean deleteReview(String uuid, int commentId)
            throws ReviewManagementException, ReviewDoesNotExistException;

    /**
     * To update a review.
     *
     * @param review   review of the application.
     * @param reviewId id of the review
     * @param uuid UUID of the application release
     * @param existingReview Pass existing review when same user adding a review for same application release,
     *                       otherwise pass null
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the review management
     */
    boolean updateReview(Review review, int reviewId, String uuid, Review existingReview)
            throws ReviewManagementException, RequestValidatingException;

    /**
     * To get the overall rating for a application release
     *
     * @param appReleaseUuuid   UUID of the application release.
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the review management
     */
    Rating getRating(String appReleaseUuuid) throws ReviewManagementException;
}