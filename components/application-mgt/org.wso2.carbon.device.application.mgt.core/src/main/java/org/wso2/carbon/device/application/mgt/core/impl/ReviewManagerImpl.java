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

package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.ReviewNode;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ReviewDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.response.Review;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.common.wrapper.ReviewWrapper;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is the default implementation for the Managing the reviews.
 */
public class ReviewManagerImpl implements ReviewManager {

    private static final Log log = LogFactory.getLog(ReviewManagerImpl.class);
    private ReviewDAO reviewDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private ApplicationDAO applicationDAO;

    public ReviewManagerImpl() {
        initDataAccessObjects();
    }

    private void initDataAccessObjects() {
        this.reviewDAO = ApplicationManagementDAOFactory.getCommentDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public Review addReview(ReviewWrapper reviewWrapper, String uuid, boolean allowMultipleReviews)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

        if (reviewWrapper == null) {
            String msg = "Request payload is null. Please verify the request payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (reviewWrapper.getRating() <= 0) {
            String msg = "You are trying to add invalid rating value as rating. Therefore please verify the request "
                    + "payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplication(uuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find an application which has the application release of UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            List<Integer> applicationReleaseIds = new ArrayList<>();
            int associatedAppReleaseId = -1;
            String associatedVersion = null;
            String associatedReleaseUuid = null;
            for (ApplicationReleaseDTO applicationReleaseDTO : applicationDTO.getApplicationReleaseDTOs()) {
                if (applicationReleaseDTO.getUuid().equals(uuid)) {
                    associatedAppReleaseId = applicationReleaseDTO.getId();
                    associatedVersion = applicationReleaseDTO.getVersion();
                    associatedReleaseUuid = applicationReleaseDTO.getUuid();
                }
                Integer id = applicationReleaseDTO.getId();
                applicationReleaseIds.add(id);
            }
            if (!allowMultipleReviews && this.reviewDAO.hasUerReviewedApp(applicationReleaseIds, username, tenantId)) {
                String msg =
                        "User " + username + " has already reviewed the application of app release which has UUID: "
                                + uuid + ". Hence you can't add another review for same application. But if you have "
                                + "added review for same app release you can update the review that you have already "
                                + "added for ths application.";
                log.error(msg);
                throw new ForbiddenException(msg);
            }

            ReviewDTO reviewDTO = reviewWrapperToDTO(reviewWrapper);
            reviewDTO.setUsername(username);
            reviewDTO.setRootParentId(-1);
            reviewDTO.setImmediateParentId(-1);
            reviewDTO.setReleaseVersion(associatedVersion);
            reviewDTO.setReleaseUuid(associatedReleaseUuid);

            int reviewId = this.reviewDAO.addReview(reviewDTO, associatedAppReleaseId, tenantId);
            reviewDTO.setId(reviewId);
            if (reviewId != -1) {
                ConnectionManagerUtil.commitDBTransaction();
                Runnable task = () -> calculateRating(reviewWrapper.getRating(), -12345, uuid, tenantId);
                new Thread(task).start();
                return reviewDTOToReview(reviewDTO);
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return null;
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs when adding Review for application release with UUID: " + uuid
                    + " is failed";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "DB transaction error occurred when adding review for application release which has "
                    + "application UUID: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting application release data for application release UUID:." + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when getting review data or adding review data for application release which "
                    + "has UUID: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    //todo return object
    public boolean addReplyComment(ReviewWrapper reviewWrapper, String uuid, int parentReviewId)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

        if (reviewWrapper == null) {
            String msg = "Request payload is null. Please verify the request payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        ReviewDTO parentReview = getReview(parentReviewId, tenantId);
        if (parentReview == null) {
            String msg = "Couldn't find an review which has review ID: " + parentReviewId
                    + " for application release which has UUID: " + uuid;
            log.error(msg);
            throw new NotFoundException(msg);
        }
        if (!parentReview.getReleaseUuid().equals(uuid)) {
            String msg =
                    "Bad Request. You are trying to add reply comment for application release which has UUID: " + uuid
                            + "," + " but parent review is associated with application release which has UUID: "
                            + parentReview.getReleaseUuid() + ". Hence can't proceed this request further.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        ReviewDTO replyComment = reviewWrapperToDTO(reviewWrapper);
        replyComment.setUsername(username);
        replyComment.setRating(0);
        replyComment.setImmediateParentId(parentReview.getId());
        if (parentReview.getRootParentId() == -1) {
            replyComment.setRootParentId(parentReview.getId());
        } else {
            replyComment.setRootParentId(parentReview.getRootParentId());
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationReleaseDTO applicationReleaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (this.reviewDAO.addReview(replyComment, applicationReleaseDTO.getId(), tenantId) != -1) {
                ConnectionManagerUtil.commitDBTransaction();
                return true;
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return false;
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while adding reply comment for app review of application release"
                    + " which has UUID: " + uuid + " and review Id " + parentReviewId;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "DB transaction error occurred when adding reply comment for comment which has comment id: "
                    + parentReviewId;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while verifying whether application release is exists or not for UUID " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private ReviewDTO reviewWrapperToDTO(ReviewWrapper reviewWrapper) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setContent(reviewWrapper.getContent());
        reviewDTO.setRating(reviewWrapper.getRating());
        return reviewDTO;
    }

    private List<Review> reviewDTOToReview(List<ReviewDTO> reviewDTOs) {
        List<Review> reviews = new ArrayList<>();

        for (ReviewDTO reviewDTO : reviewDTOs) {
            reviews.add(reviewDTOToReview(reviewDTO));
        }
        return reviews;
    }

    private Review reviewDTOToReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setContent(reviewDTO.getContent());
        review.setReleaseUuid(reviewDTO.getReleaseUuid());
        review.setReleaseVersion(reviewDTO.getReleaseVersion());
        review.setCreatedAt(reviewDTO.getCreatedAt());
        review.setModifiedAt(reviewDTO.getModifiedAt());
        review.setRating(reviewDTO.getRating());
        review.setUsername(reviewDTO.getUsername());
        review.setReplies(new ArrayList<>());
        return review;
    }

    private ReviewDTO getReview(int reviewId, int tenantId) throws ReviewManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return this.reviewDAO.getReview(reviewId, tenantId);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs updating reviewTmp with reviewTmp id " + reviewId + ".";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Review updateReview(ReviewWrapper updatingReview, int reviewId, String uuid,
            boolean isPrivilegedUser) throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ReviewDTO reviewDTO = getReview(reviewId, tenantId);
        if (reviewDTO == null) {
            String msg = "Couldn't found a review for review ID: " + reviewId;
            log.error(msg);
            throw new NotFoundException(msg);
        }
        if (!isPrivilegedUser && !username.equals(reviewDTO.getUsername())) {
            String msg = "You are trying to update a review which is created by " + reviewDTO.getUsername()
                    + ". Hence you are not permitted to update the review.";
            log.error(msg);
            throw new ForbiddenException(msg);
        }

        boolean isActiveReview = true;
        //Handle Review
        if (reviewDTO.getRootParentId() == -1 && reviewDTO.getImmediateParentId() == -1) {
            if (!reviewDTO.getReleaseUuid().equals(uuid)) {
                isActiveReview = false;
            } else {
                if (updatingReview.getRating() > 0 && updatingReview.getRating() != reviewDTO.getRating()) {
                    Runnable task = () -> ReviewManagerImpl.this
                            .calculateRating(updatingReview.getRating(), reviewDTO.getRating(), uuid, tenantId);
                    new Thread(task).start();
                    reviewDTO.setRating(updatingReview.getRating());
                }
                if (!reviewDTO.getContent().equals(updatingReview.getContent())) {
                    reviewDTO.setContent(updatingReview.getContent());
                }
            }
        } else {
            if (!reviewDTO.getReleaseUuid().equals(uuid)) {
                String msg = "You are trying to update reply comment, but associated application release UUID and "
                        + "requested app release UUID are mismatched.";
                throw new BadRequestException(msg);
            }
            reviewDTO.setContent(updatingReview.getContent());
        }

        ReviewDTO updatedReviewDTO = updateReviewInDB(reviewDTO, reviewId, isActiveReview, tenantId);
        if (updatedReviewDTO != null) {
            if (!isActiveReview) {
                Review newReview = addReview(updatingReview, uuid, true);
                if (newReview != null) {
                    return newReview;
                } else {
                    ReviewDTO restoringReviewDTO = updateReviewInDB(reviewDTO, reviewId, true, tenantId);
                    if (restoringReviewDTO != null) {
                        String msg = "Review Updating Status: Adding new Review for application release which has"
                                + " UUID: " + uuid + " is failed and the old review is restored.";
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    } else {
                        String msg = "Review Updating Status: Adding new Review for application release which has "
                                + "UUID: " + uuid + " is failed and the old review restoring is also failed.";
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                }
            }
            return reviewDTOToReview(updatedReviewDTO);
        } else {
            String msg = "Review Updating is failed. Hence please contact the administrator.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }
    }

    private ReviewDTO updateReviewInDB(ReviewDTO reviewDTO, int reviewId, boolean isActiveReview, int tenantId)
            throws ReviewManagementException, ApplicationManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ReviewDTO updatedReviewDTO = this.reviewDAO.updateReview(reviewDTO, reviewId, isActiveReview, tenantId);
            if (updatedReviewDTO != null) {
                ConnectionManagerUtil.commitDBTransaction();
                return updatedReviewDTO;
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return null;
        } catch (ReviewManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while  getting reviewTmp with reviewTmp id " + reviewId + ".";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs updating reviewTmp with reviewTmp id " + reviewId + ".";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "DB transaction error occurred when updating comment which has comment id: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAllReleaseReviews(PaginationRequest request, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Get all release reviews of the application release uuid: " + uuid);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO releaseDTO = this.applicationReleaseDAO.getReleaseByUUID(uuid, tenantId);
            if (releaseDTO == null) {
                String msg = "Couldn't found an application release for UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return getReviewTree(this.reviewDAO.getAllReleaseReviews(releaseDTO.getId(), request, tenantId));
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while getting all reviews for application uuid: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg ="Error occured while getting the DB connection to get all reviews for application release which"
                    + " has UUID " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred while getting application release details for application release UUId " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAllAppReviews(PaginationRequest request, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Get all reviews of the application release uuid: " + uuid);
        }
        List<Integer> applicationReleaseIds = getAppReleaseIdsByUUID(uuid, tenantId);
        try {
            ConnectionManagerUtil.openDBConnection();
            return getReviewTree(this.reviewDAO.getAllActiveAppReviews(applicationReleaseIds, request, tenantId));
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while getting all reviews for application which has an "
                    + "application release of uuid: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting the DB connection to get app app reviews.";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAllAppReviewsOfUser(PaginationRequest request, String uuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (log.isDebugEnabled()) {
            log.debug("Get all reviews of the application release uuid: " + uuid);
        }
        List<Integer> applicationReleaseIds = getAppReleaseIdsByUUID(uuid, tenantId);
        try {
            ConnectionManagerUtil.openDBConnection();
            List<ReviewDTO> reviewDtos = this.reviewDAO
                    .getAllActiveAppReviewsOfUser(applicationReleaseIds, request, username, tenantId);
            if (!reviewDtos.isEmpty() && reviewDtos.size() > 1) {
                String msg =
                        "User " + username + " can't have more than active application review for application which"
                                + " has application release of UUID: " + uuid;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            return getReviewTree(reviewDtos);
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while getting all " + username + "'s reviews for application which has an "
                    + "application release of uuid: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg =  "Error occured while getting DB connection to get all " + username + "'s reviews for "
                    + "application which has an application release of uuid: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private List<Integer> getAppReleaseIdsByUUID(String uuid, int tenantId)
            throws ReviewManagementException, ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplication(uuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find an application which has the application release of UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return applicationDTO.getApplicationReleaseDTOs().stream().map(ApplicationReleaseDTO::getId)
                    .collect(Collectors.toList());
        } catch (DBConnectionException e) {
            String msg =
                    "Error occured while getting the DB connection to get application which has application release"
                            + " of UUID: " + uuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting application release details for application which has an "
                    + "application release of UUID " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private PaginationResult getReviewTree(List<ReviewDTO> reviewDTOs) throws ReviewManagementException {
        TreeMap<Integer, ReviewNode<ReviewDTO>> reviewTree = new TreeMap<>();
        PaginationResult paginationResult = new PaginationResult();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            for (ReviewDTO reviewDTO : reviewDTOs) {
                ReviewNode<ReviewDTO> rootNode = new ReviewNode<>(reviewDTO);
                reviewTree.put(reviewDTO.getId(), rootNode);
                List<ReviewDTO> replyComments = this.reviewDAO.getReplyComments(reviewDTO.getId(), tenantId);
                replyComments.sort(Comparator.comparing(ReviewDTO::getId));
                for (ReviewDTO reply : replyComments) {
                    reviewTree.put(reply.getRootParentId(),
                            findAndSetChild(reviewTree.get(reply.getRootParentId()), reply));
                }
            }
            int numOfReviews = reviewTree.size();
            List<Review> results = new ArrayList<>();

            for (ReviewNode<ReviewDTO> reviewNode : reviewTree.values()) {
                results.add(constructReviewResponse(null, reviewNode));
            }
            paginationResult.setData(new ArrayList<>(results));
            paginationResult.setRecordsFiltered(numOfReviews);
            paginationResult.setRecordsTotal(numOfReviews);
            return paginationResult;
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while getting all reply comments for given review list";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        }
    }

    private ReviewNode<ReviewDTO> findAndSetChild(ReviewNode<ReviewDTO> node, ReviewDTO reviewDTO) {
        if (node.getData().getId() == reviewDTO.getImmediateParentId()) {
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
        if (parentReview != null) {
            parentReview.getReplies().add(review);
        }
        if (node.getChildren().isEmpty()) {
            return review;
        }
        for (ReviewNode<ReviewDTO> reviewDTOReviewNode : node.getChildren()) {
            constructReviewResponse(review, reviewDTOReviewNode);
        }
        return review;
    }

    private ReviewNode<ReviewDTO> getReviewNode(ReviewNode<ReviewDTO> node, int reviewId) {
        if (node.getData().getId() == reviewId) {
            return node;
        } else {
            for (ReviewNode<ReviewDTO> each : node.getChildren()) {
                ReviewNode<ReviewDTO> result = getReviewNode(each, reviewId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private List<Integer> getDeletingReviewIds(ReviewNode<ReviewDTO> node, List<Integer> reviewIds) {
        reviewIds.add(node.getData().getId());
        if (node.getChildren().isEmpty()) {
            return reviewIds;
        }
        for (ReviewNode<ReviewDTO> each : node.getChildren()) {
            getDeletingReviewIds(each, reviewIds);
        }
        return reviewIds;
    }

    @Override public void deleteReview(String uuid, int reviewId, boolean isPriviledgedUser)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ReviewDTO existingReview = this.reviewDAO.getReview(reviewId, tenantId);
            if (existingReview == null) {
                String msg = "Cannot delete a non-existing review for the application with review id" + reviewId;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (!existingReview.getReleaseUuid().equals(uuid)) {
                String msg = "You are trying to delete a review which is not associated with application release which "
                        + "has UUID: " + uuid;
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            if (!isPriviledgedUser && !username.equals(existingReview.getUsername())) {
                String msg = "You are trying to delete a comment that is owned by you. Hence you are not permitted to "
                        + "delete the review";
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            if (existingReview.getRootParentId() == Constants.REVIEW_PARENT_ID
                    && existingReview.getImmediateParentId() == Constants.REVIEW_PARENT_ID) {
                this.reviewDAO.deleteAllChildCommentsOfReview(existingReview.getId(), tenantId);
                this.reviewDAO.deleteReview(existingReview.getId(), tenantId);
                ConnectionManagerUtil.commitDBTransaction();
                Runnable task = () -> calculateRating(0, existingReview.getRating(), uuid, tenantId);
                new Thread(task).start();
            } else {
                ReviewDTO rootReview = this.reviewDAO.getReview(existingReview.getRootParentId(), tenantId);
                List<ReviewDTO> replyComments = this.reviewDAO.getReplyComments(rootReview.getId(), tenantId);

                ReviewNode<ReviewDTO> reviewNode = new ReviewNode<>(rootReview);
                replyComments.sort(Comparator.comparing(ReviewDTO::getId));
                for (ReviewDTO reply : replyComments) {
                    reviewNode = findAndSetChild(reviewNode, reply);
                }

                ReviewNode<ReviewDTO> deletingRevieNode = getReviewNode(reviewNode, existingReview.getId());
                List<Integer> deletingReviewIds = getDeletingReviewIds(deletingRevieNode, new ArrayList<>());
                this.reviewDAO.deleteReviews(deletingReviewIds, tenantId);
                ConnectionManagerUtil.commitDBTransaction();
            }
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurs deleting review with review id " + reviewId + ".";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while deleting review with review id " + reviewId + ".";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred when handleing transaction to delete application reviews.";
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Rating getAppReleaseRating(String appReleaseUuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            Rating rating = this.applicationReleaseDAO.getReleaseRating(appReleaseUuid, tenantId);
            if (rating == null) {
                throw new NotFoundException("Couldn't find rating for application release UUID: " + appReleaseUuid
                        + ". Please check the existence of the application release");
            }

            List<Integer> ratingValues = this.reviewDAO.getAllAppReleaseRatingValues(appReleaseUuid, tenantId);
            rating.setRatingVariety(constructRatingVariety(ratingValues));
            return rating;
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occured while getting the rating value of the application release uuid: " + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occured while getting the rating value of the application release uuid: "
                    + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occured while getting all rating values for the application release UUID: "
                    + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public Rating getAppRating(String appReleaseUuid)
            throws ReviewManagementException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getApplication(appReleaseUuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't found an application for application release UUID: " + appReleaseUuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }

            List<String> uuids = applicationDTO.getApplicationReleaseDTOs().stream().map(ApplicationReleaseDTO::getUuid)
                    .collect(Collectors.toList());
            List<Integer> ratingValues = this.reviewDAO.getAllAppRatingValues(uuids, tenantId);

            Rating rating = new Rating();
            rating.setRatingValue(applicationDTO.getAppRating());
            rating.setNoOfUsers(ratingValues.size());
            rating.setRatingVariety(constructRatingVariety(ratingValues));
            return rating;
        } catch (DBConnectionException e) {
            String msg =
                    "DB Connection error occured while getting app rating of the application which has application "
                            + "release for uuid: " + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured while getting the application DTO for the application release uuid: "
                    + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            String msg =
                    "Error occured while getting all rating values of application which has the application release "
                            + "for UUID: " + appReleaseUuid;
            log.error(msg, e);
            throw new ReviewManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private TreeMap<Integer, Integer> constructRatingVariety(List<Integer> ratingValues) {
        TreeMap<Integer, Integer> ratingVariety = new TreeMap<>();
        ratingValues.forEach(ratingVal -> {
            if (ratingVariety.containsKey(ratingVal)) {
                ratingVariety.replace(ratingVal, ratingVariety.get(ratingVal) + 1);
            } else {
                ratingVariety.put(ratingVal, 1);
            }
        });
        IntStream.rangeClosed(1, Constants.MAX_RATING).filter(i -> !ratingVariety.containsKey(i))
                .forEach(i -> ratingVariety.put(i, 0));
        return ratingVariety;
    }

    private void calculateRating(int newRatingVal, int oldRatingVal, String uuid, int tenantId) {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Rating rating = this.applicationReleaseDAO.getReleaseRating(uuid, tenantId);
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
                    newTotalRating = currentRating - oldRatingVal;
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
                updateAppRating(uuid, tenantId);
                ConnectionManagerUtil.commitDBTransaction();
            }
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            log.error("Error occured while getting the rating value of the application release UUID: " + uuid, e);
        } catch (DBConnectionException e) {
            log.error("DB Connection error occured while updated the rating value of the application release UUID: "
                    + uuid + " can not get.", e);
        } catch (TransactionManagementException e) {
            log.error(
                    "Transaction error occured while updated the rating value of the application release UUID: " + uuid
                            + " can not get.", e);
        } catch (ApplicationManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            log.error("Error occured while updating app rating value which has application release for UUID: " + uuid,
                    e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private void updateAppRating(String uuid, int tenantId) throws ApplicationManagementException {
        try {
            ApplicationDTO applicationDTO = this.applicationDAO.getApplication(uuid, tenantId);
            List<String> uuids = applicationDTO.getApplicationReleaseDTOs().stream().map(ApplicationReleaseDTO::getUuid)
                    .collect(Collectors.toList());
            List<Integer> appRatings = this.reviewDAO.getAllAppRatingValues(uuids, tenantId);
            double appAverageRatingValue = appRatings.stream().mapToDouble(x -> x).average().orElse(0.0);
            this.applicationDAO.updateApplicationRating(uuid, appAverageRatingValue, tenantId);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when getting application data or updating application rating value.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ReviewManagementDAOException e) {
            String msg = "Error occurred when getting application rating values";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }
}
