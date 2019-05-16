/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.ReviewNode;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ReviewDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewDoesNotExistException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.response.Review;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.common.wrapper.ReviewWrapper;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * This class is the default implementation for the Managing the reviews.
 */
public class ReviewManagerImpl implements ReviewManager {

    private static final Log log = LogFactory.getLog(ReviewManagerImpl.class);
    private ReviewDAO reviewDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;

    public ReviewManagerImpl() {
        initDataAccessObjects();
    }

    private void initDataAccessObjects() {
        this.reviewDAO = ApplicationManagementDAOFactory.getCommentDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
    }

    @Override
    public boolean addReview(ReviewWrapper reviewWrapper, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

        if (reviewWrapper == null) {
            String msg = "Request payload is null. Please verify the request payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (reviewWrapper.getRating() < 0) {
            String msg = "You are trying to add invalid rating value as rating. Therefore please verify the request "
                    + "payload.";
            log.error(msg);
            throw new ForbiddenException(msg);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find application release for the application UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (this.reviewDAO.haveUerReviewed(applicationReleaseDTO.getId(), username, tenantId)) {
                String msg =
                        "User " + username + " has already reviewed the application release which has UUID: " + uuid
                                + ". Hence you can't add another review for same application release. But you can update "
                                + "the review that you have already added for ths application release.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            Runnable task = () -> calculateRating(reviewWrapper.getRating(), -12345, uuid, tenantId);
            new Thread(task).start();

            ReviewDTO reviewDTO = reviewWrapperToDO(reviewWrapper);
            reviewDTO.setUsername(username);
            reviewDTO.setRootParentId(-1);
            reviewDTO.setImmediateParentId(-1);
            if (this.reviewDAO.addReview(reviewDTO, applicationReleaseDTO.getId(), tenantId)) {
                ConnectionManagerUtil.commitDBTransaction();
                return true;
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return false;
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs when adding Review for application release with UUID: " + uuid
                    + " is failed";
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "DB transaction error occurred when adding review for application release which has "
                    + "application UUID: " + uuid;
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting application release data for application release UUID:." + uuid;
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when getting review data or adding review data for application release which "
                    + "has UUID: " + uuid;
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean addReplyComment(ReviewWrapper reviewWrapper, String uuid, int parentReviewId)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

        if (reviewWrapper == null) {
            String msg = "Request payload is null. Please verify the request payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find application release for the application UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            ReviewDTO parentReview = this.reviewDAO.getReview(applicationReleaseDTO.getId(), parentReviewId);
            if (parentReview == null) {
                String msg = "Couldn't find an review which has review ID: " + parentReviewId
                        + " for application release which has UUID: " + uuid;
                log.error(msg);
                throw new BadRequestException(msg);
            }
            ReviewDTO replyComment = reviewWrapperToDO(reviewWrapper);
            replyComment.setUsername(username);
            replyComment.setImmediateParentId(parentReview.getId());
            if (parentReview.getRootParentId() == -1) {
                replyComment.setRootParentId(parentReview.getId());
            } else {
                replyComment.setRootParentId(parentReview.getRootParentId());
            }
            if (this.reviewDAO.addReview(replyComment, applicationReleaseDTO.getId(), tenantId)) {
                ConnectionManagerUtil.commitDBTransaction();
                return true;
            }
            return false;
        } catch (DBConnectionException e) {
            throw new ReviewManagementException(
                    "DB Connection error occurs ,ReviewTmp for application release with UUID: " + uuid + " is failed",
                    e);
        } catch (TransactionManagementException e) {
            String msg = "DB transaction error occurred when adding reply comment for comment which has comment id: "
                    + parentReviewId;
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            throw new ReviewManagementException(
                    "Error occured while verifying whether application release is exists or not.", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ReviewDTO reviewWrapperToDO(ReviewWrapper reviewWrapper){
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setContent(reviewWrapper.getContent());
        reviewDTO.setRating(reviewWrapper.getRating());
        return reviewDTO;
    }

    private List<Review> reviewDTOToReview(List<ReviewDTO> reviewDTOs){
        List<Review> reviews = new ArrayList<>();

        for (ReviewDTO reviewDTO : reviewDTOs){
            Review review = new Review();
            review.setId(reviewDTO.getId());
            review.setContent(reviewDTO.getContent());
            review.setRootParentId(reviewDTO.getRootParentId());
            review.setImmediateParentId(reviewDTO.getImmediateParentId());
            review.setCreatedAt(reviewDTO.getCreatedAt());
            review.setModifiedAt(reviewDTO.getModifiedAt());
            review.setReplies(new ArrayList<>());
            reviews.add(review);
        }
        return reviews;
    }

    private Review reviewDTOToReview(ReviewDTO reviewDTO){
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setContent(reviewDTO.getContent());
        review.setRootParentId(reviewDTO.getRootParentId());
        review.setImmediateParentId(reviewDTO.getImmediateParentId());
        review.setCreatedAt(reviewDTO.getCreatedAt());
        review.setModifiedAt(reviewDTO.getModifiedAt());
        review.setReplies(new ArrayList<>());
        return review;
    }

    @Override
    public boolean updateReview(ReviewWrapper updatingReview, int reviewId, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (log.isDebugEnabled()) {
            log.debug("ReviewTmp updating request is received for the reviewTmp id " + reviewId);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't found an application release for UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            ReviewDTO reviewDTO = this.reviewDAO.getReview(applicationReleaseDTO.getId(), reviewId);
            if (reviewDTO == null) {
                String msg =
                        "Couldn't found a review for application release which has UUID: " + uuid + " and review ID: "
                                + reviewId;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            if (!username.equals(reviewDTO.getUsername())) {
                String msg = "You are trying to update a review which is triggered by " + reviewDTO.getUsername()
                        + ". Hence you are not permitted to update the review.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }

            if (reviewDTO.getRootParentId() == -1 && reviewDTO.getImmediateParentId() == -1
                    && updatingReview.getRating() > 0 && updatingReview.getRating() != reviewDTO.getRating()) {
                Runnable task = () -> calculateRating(updatingReview.getRating(), reviewDTO.getRating(), uuid,
                        tenantId);
                new Thread(task).start();
                reviewDTO.setRating(updatingReview.getRating());
            }
            reviewDTO.setContent(updatingReview.getContent());
            return this.reviewDAO.updateReview(reviewDTO, reviewId, tenantId) == 1;
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while  getting reviewTmp with reviewTmp id " + reviewId + ".";
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs updating reviewTmp with reviewTmp id " + reviewId + ".";
            log.error(msg);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured when getting application release data for application release UUID: " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAllReviews(PaginationRequest request, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PaginationResult paginationResult = new PaginationResult();
        TreeMap<Integer, ReviewNode<ReviewDTO>> reviewTree = new TreeMap<>();
        if (log.isDebugEnabled()) {
            log.debug("Get all reviewTmps of the application release uuid: " + uuid);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO releaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (releaseDTO == null){
                String msg = "Couldn't found an application release for UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            List<ReviewDTO> reviewDTOs= this.reviewDAO.getAllReviews(releaseDTO.getId(), request, tenantId);
            for (ReviewDTO reviewDTO : reviewDTOs){
                ReviewNode<ReviewDTO> rootNode = new ReviewNode<>(reviewDTO);
                reviewTree.put(reviewDTO.getId(), rootNode);
                List<ReviewDTO> replyComments = this.reviewDAO.getReplyComments(reviewDTO.getId(), tenantId);
                replyComments.sort(Comparator.comparing(ReviewDTO::getId));
                for (ReviewDTO reply : replyComments){
                    reviewTree.put(reply.getRootParentId(),
                            findAndSetChild(reviewTree.get(reply.getRootParentId()), reply));
                }
            }
            int numOfReviews = reviewTree.size();
            List<Review> results = new ArrayList<>();

            for (ReviewNode<ReviewDTO> reviewNode : reviewTree.values()){
                results.add(constructReviewResponse(null, reviewNode));
            }
            paginationResult.setData(new ArrayList<>(results));
            paginationResult.setRecordsFiltered(numOfReviews);
            paginationResult.setRecordsTotal(numOfReviews);
            return paginationResult;
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException("Error occured while getting all reviewTmps for application uuid: " + uuid,
                    e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementException("Error occured while getting the DB connection.", e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting application release details for application release UUId " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ReviewNode<ReviewDTO> findAndSetChild(ReviewNode<ReviewDTO> node, ReviewDTO reviewDTO) {
        if (node.getData().getId() == reviewDTO.getImmediateParentId()){
            ReviewNode<ReviewDTO> childNode = new ReviewNode<>(reviewDTO);
            node.addChild(childNode);
            return node;
        }
        for (ReviewNode<ReviewDTO> each : node.getChildren()) {
            findAndSetChild(each, reviewDTO);
        }
        return node;
    }

    private Review constructReviewResponse(Review parentReview, ReviewNode<ReviewDTO> node) {
        Review review = reviewDTOToReview(node.getData());
        if (parentReview != null){
            parentReview.getReplies().add(review);
        }
        if (node.getChildren().isEmpty()){
            return review;
        }
        for (ReviewNode<ReviewDTO> reviewDTOReviewNode : node.getChildren()) {
            constructReviewResponse(review, reviewDTOReviewNode);
        }
        return review;
    }

    @Override
    public boolean deleteReview(String uuid, int reviewId)
            throws ReviewManagementException, ReviewDoesNotExistException {
        ReviewDTO existingReview;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.openDBConnection();
            existingReview = this.reviewDAO.getReview(reviewId);
            if (existingReview == null) {
                throw new ReviewDoesNotExistException(
                        "Cannot delete a non-existing review for the application with review id" + reviewId);
            }
            Runnable task = () -> calculateRating(0, existingReview.getRating(), uuid, tenantId);
            new Thread(task).start();
            return isAuthorizedUser(username, existingReview.getUsername(), tenantId)
                    && this.reviewDAO.deleteReview(username, reviewId) == 1;
        } catch (DBConnectionException e) {
            throw new ReviewManagementException(
                    "DB Connection error occurs deleting review with review id " + reviewId + ".", e);
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException("Error occured while deleting review with review id " + reviewId + ".",
                    e);
        } catch (UserStoreException e) {
            throw new ReviewManagementException(
                    "User-store exception while checking whether the user " + username + " of tenant " + tenantId
                            + " has the publisher permission");
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Rating getRating(String appReleaseUuuid) throws ReviewManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            Rating rating = this.applicationReleaseDAO.getRating(appReleaseUuuid, tenantId);
            if (rating == null) {
                throw new ReviewManagementException(
                        "Couldn't find rating for application release UUID: " + appReleaseUuuid
                                + ". Please check the existence of the application release");
            }

            List<Integer> ratingValues = this.reviewDAO.getAllRatingValues(appReleaseUuuid, tenantId);
            TreeMap<Integer, Integer> ratingVariety = new TreeMap<>();
            for (Integer ratingVal : ratingValues) {
                if (ratingVariety.containsKey(ratingVal)) {
                    ratingVariety.replace(ratingVal, ratingVariety.get(ratingVal) + 1);
                } else {
                    ratingVariety.put(ratingVal, 1);
                }
            }
            rating.setRatingVariety(ratingVariety);
            return rating;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ReviewManagementException(
                    "Error occured while getting the rating value of the application release uuid: " + appReleaseUuuid,
                    e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ReviewManagementException(
                    "DB Connection error occured while getting the rating value of the application release uuid: "
                            + appReleaseUuuid, e);
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException(
                    "Error occured while getting all rating values for the application release UUID: "
                            + appReleaseUuuid, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private void calculateRating(int newRatingVal, int oldRatingVal, String uuid, int tenantId) {
        try {
            ConnectionManagerUtil.openDBConnection();
            Rating rating = this.applicationReleaseDAO.getRating(uuid, tenantId);
            if (rating == null) {
                log.error("Couldn't find rating for application release uuid: " + uuid);
            } else {
                double updatedRating;
                double newTotalRating;
                int numOfUsers = rating.getNoOfUsers();
                double currentRating = rating.getRatingValue() * numOfUsers;

                if (oldRatingVal == -12345) {
                    newTotalRating = currentRating + newRatingVal;
                    numOfUsers++;
                } else if (newRatingVal == 0) {
                    newTotalRating = currentRating - newRatingVal;
                    numOfUsers--;
                } else {
                    double tmpVal;
                    tmpVal = currentRating - oldRatingVal;
                    newTotalRating = tmpVal + newRatingVal;
                }

                if (numOfUsers == 0) {
                    updatedRating = 0;
                } else {
                    updatedRating = newTotalRating / numOfUsers;
                }
                this.applicationReleaseDAO.updateRatingValue(uuid, updatedRating, numOfUsers);
            }
        } catch (ApplicationManagementDAOException e) {
            log.error("Error occured while getting the rating value of the application release UUID: " + uuid, e);
        } catch (DBConnectionException e) {
            log.error("DB Connection error occured while updated the rating value of the application release UUID: "
                    + uuid + " can not get.", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * To check whether current user has the permission to do some secured operation.
     *
     * @param username   Name of the User.
     * @param tenantId   ID of the tenant.
     * @param permission Permission that need to be checked.
     * @return true if the current user has the permission, otherwise false.
     * @throws UserStoreException UserStoreException
     */
    private boolean isAdminUser(String username, int tenantId, String permission) throws UserStoreException {
        UserRealm userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        return userRealm != null && userRealm.getAuthorizationManager() != null && userRealm.getAuthorizationManager()
                .isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                        CarbonConstants.UI_PERMISSION_ACTION);
    }


    /**
     * To check whether logged in user is authorized user to do an specific action.
     *
     * @param loggedInUser   username of the loggedin user.
     * @param reviewOwner username of the review owner.
     * @param tenantId   ID of the tenant.
     * @return true if the current user has the permission, otherwise false.
     * @throws UserStoreException UserStoreException
     */
    private boolean isAuthorizedUser(String loggedInUser, String reviewOwner, int tenantId) throws UserStoreException {
        return loggedInUser.equals(reviewOwner) || isAdminUser(loggedInUser, tenantId,
                CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION);
    }

}