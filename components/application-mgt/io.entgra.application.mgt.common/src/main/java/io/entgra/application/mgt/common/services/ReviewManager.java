/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.Rating;
import io.entgra.application.mgt.common.response.Review;
import io.entgra.application.mgt.common.PaginationRequest;
import io.entgra.application.mgt.common.PaginationResult;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.ReviewManagementException;
import io.entgra.application.mgt.common.wrapper.ReviewWrapper;

/**
 * ReviewManager is responsible for handling all the add/update/delete/get operations related with
 */
public interface ReviewManager {

    /**
     * To add a reviewTmp to an application release
     *
     * @param reviewWrapper  reviewTmp of the application.
     * @param uuid     uuid of the application release.
     * @return {@link Review} Added review
     * @throws ReviewManagementException Exceptions of the reviewTmp management.
     */
    Review addReview(ReviewWrapper reviewWrapper, String uuid, boolean allowMultipleReviews)
            throws ReviewManagementException, ApplicationManagementException;

    boolean addReplyComment(ReviewWrapper reviewWrapper, String uuid, int parentReviewId)
            throws ReviewManagementException, ApplicationManagementException;

    /**
     * Get all review with pagination
     *
     * @param request Pagination request {@link PaginationRequest}
     * @param uuid    uuid of the application release
     * @return {@link PaginationResult} pagination result with starting offSet and limit
     * @throws ReviewManagementException Exceptions of the comment management.
     */
    PaginationResult getAllReleaseReviews(PaginationRequest request, String uuid)
            throws ReviewManagementException, ApplicationManagementException;

    PaginationResult getAllAppReviews(PaginationRequest request, String uuid) throws ReviewManagementException,
            ApplicationManagementException;

    PaginationResult getAllAppReviewsOfUser(PaginationRequest request, String uuid) throws ReviewManagementException,
            ApplicationManagementException;

    /**
     * To delete review using review id.
     *
     * @param uuid UUID of the application release
     * @throws ReviewManagementException Exceptions of the comment management
     */
    void deleteReview(String uuid, int reviewId, boolean isPriviledgedUser)
            throws ReviewManagementException, ApplicationManagementException;

    /**
     * To update a reviewTmp.
     *
     * @param reviewId id of the reviewTmp
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the reviewTmp management
     */
    Review updateReview(ReviewWrapper updatingReview, int reviewId, String uuid, boolean isPrivilegedUser)
            throws ReviewManagementException, ApplicationManagementException;

    /**
     * To get the overall rating for a application release
     *
     * @param appReleaseUuid   UUID of the application release.
     * @return {@link Review}updated review
     * @throws ReviewManagementException Exceptions of the review management
     */
    Rating getAppReleaseRating(String appReleaseUuid) throws ReviewManagementException, ApplicationManagementException;

    Rating getAppRating(String appReleaseUuid) throws ReviewManagementException, ApplicationManagementException;

}