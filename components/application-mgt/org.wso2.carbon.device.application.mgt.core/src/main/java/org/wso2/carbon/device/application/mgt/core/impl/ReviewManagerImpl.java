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
import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewDoesNotExistException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
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

    @Override public boolean addReview(Review review, String uuid)
            throws ReviewManagementException, RequestValidatingException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        boolean isSuccess = false;
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Review existingReview = reviewDAO.haveUerCommented(uuid, username, tenantId);
            if (existingReview != null && isAuthorizedUser(username, existingReview.getUsername(), tenantId)
                    && review.getRating() > 0 && review.getRating() != existingReview.getRating()) {
                Runnable task = () -> calculateRating(review.getRating(), existingReview.getRating(), uuid);
                new Thread(task).start();
                isSuccess = updateReview(review, existingReview.getId(), uuid, existingReview);
            } else if (review.getRating() > 0) {
                Runnable task = () -> calculateRating(review.getRating(), -12345, uuid);
                new Thread(task).start();
                review.setUsername(username);
                isSuccess = this.reviewDAO.addReview(review, uuid, tenantId);
            }
            if (isSuccess) {
                ConnectionManagerUtil.commitDBTransaction();
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            return isSuccess;
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ReviewManagementException(
                    "DB Connection error occurs ,Review for application release with UUID: " + uuid +  " is failed", e);
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ReviewManagementException(
                    "Transaction Management Exception occurs,Review for application release with UUID:" + uuid +
                            " is failed ", e);
        } catch (UserStoreException e) {
            throw new ReviewManagementException("Error occured while verifying user's permission to update the review.",
                    e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean updateReview(Review review, int reviewId, String uuid, Review existingReview)
            throws ReviewManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (log.isDebugEnabled()) {
            log.debug("Review updating request is received for the review id " + reviewId);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            if (existingReview == null) {
                existingReview = this.reviewDAO.getReview(reviewId);
                if (existingReview != null && isAuthorizedUser(username, existingReview.getUsername(), tenantId)) {
                    if (review.getRating() > 0 && review.getRating() != existingReview.getRating()) {
                        Review finalExistingReview = existingReview;
                        Runnable task = () -> calculateRating(review.getRating(), finalExistingReview.getRating(),
                                uuid);
                        new Thread(task).start();
                    }
                } else {
                    throw new ReviewManagementException(
                            "Please check the existence of the review, Review-Id: " + reviewId
                                    + " or permission of the " + username + " to update the review.");
                }
            }
            if (review.getComment().isEmpty()) {
                review.setComment(existingReview.getComment());
            }
            if (review.getRating() == 0) {
                review.setRating(existingReview.getRating());
            }
            ConnectionManagerUtil.beginDBTransaction();
            if (this.reviewDAO.updateReview(review, reviewId, username, tenantId) == 1) {
                ConnectionManagerUtil.commitDBTransaction();
                return true;
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return false;
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException("Error occured while  updating review with review id " + reviewId + ".",
                    e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementException(
                    "DB Connection error occurs updating review with review id " + reviewId + ".", e);
        } catch (TransactionManagementException e) {
            throw new ReviewManagementException(
                    "Transaction management error occurs when updating review with review id " + reviewId + ".", e);
        } catch (UserStoreException e) {
            throw new ReviewManagementException(
                    "Error occured while verifying user's permission to update the review. review id: " + reviewId
                            + ".", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAllReviews(PaginationRequest request, String uuid)
            throws ReviewManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PaginationResult paginationResult = new PaginationResult();
        int numOfComments;
        List<Review> reviews;
        TreeMap<Integer, Review> hierarchicalReviewSet = new TreeMap<>();
        if (log.isDebugEnabled()) {
            log.debug("Get all reviews of the application release uuid: " + uuid);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            reviews = this.reviewDAO.getAllReviews(uuid, Util.validateCommentListPageSize(request), tenantId);

            for (Review review : reviews) {
                if (hierarchicalReviewSet.containsKey(review.getParentId())) {
                    Review parentReview = hierarchicalReviewSet.get(review.getParentId());
                    parentReview.setReplyReview(review);
                    hierarchicalReviewSet.replace(review.getParentId(), parentReview);
                } else {
                    hierarchicalReviewSet.put(review.getId(), review);
                }
            }
            numOfComments = hierarchicalReviewSet.size();
            if (numOfComments > 0) {
                paginationResult.setData(new ArrayList<>(hierarchicalReviewSet.values()));
                paginationResult.setRecordsFiltered(numOfComments);
                paginationResult.setRecordsTotal(numOfComments);
            } else {
                paginationResult.setData(new ArrayList<Review>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
            }
            return paginationResult;
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException("Error occured while getting all reviews for application uuid: " + uuid,
                    e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementException("Error occured while getting the DB connection.", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean deleteReview(String uuid, int reviewId)
            throws ReviewManagementException, ReviewDoesNotExistException {
        Review existingReview;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.openDBConnection();
            existingReview = this.reviewDAO.getReview(reviewId);
            if (existingReview == null) {
                throw new ReviewDoesNotExistException(
                        "Cannot delete a non-existing review for the application with review id" + reviewId);
            }
            Runnable task = () -> calculateRating(0, existingReview.getRating(), uuid);
            new Thread(task).start();
            ConnectionManagerUtil.beginDBTransaction();
            if (isAuthorizedUser(username, existingReview.getUsername(), tenantId)
                    && this.reviewDAO.deleteReview(username, reviewId) == 1) {
                ConnectionManagerUtil.commitDBTransaction();
                return true;
            }
            ConnectionManagerUtil.rollbackDBTransaction();
            return false;
        } catch (DBConnectionException e) {
            throw new ReviewManagementException(
                    "DB Connection error occurs deleting review with review id " + reviewId + ".", e);
        } catch (ReviewManagementDAOException e) {
            throw new ReviewManagementException("Error occured while deleting review with review id " + reviewId + ".",
                    e);
        } catch (TransactionManagementException e) {
            throw new ReviewManagementException(
                    "Transaction Management Exception occurs deleting review with review id " + reviewId + ".", e);
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
            TreeMap<Integer, Integer> ratingVariety = rating.getRatingVariety();
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

    private void calculateRating(int newRatingVal, int oldRatingVal, String uuid) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Rating rating = this.applicationReleaseDAO.getRating(uuid, tenantId);
            if (rating == null) {
                log.error("Couldn't find rating for application release uuid: " + uuid);
            } else {
                double updatedRating;
                int numOfUsers = rating.getNoOfUsers();
                double currentRating = rating.getRatingValue() * numOfUsers;
                if (oldRatingVal == -12345) {
                    updatedRating = (currentRating + newRatingVal) / (numOfUsers + 1);
                    this.applicationReleaseDAO.updateRatingValue(uuid, updatedRating, numOfUsers + 1);
                } else if (newRatingVal == 0) {
                    updatedRating = (currentRating - newRatingVal) / (numOfUsers - 1);
                    this.applicationReleaseDAO.updateRatingValue(uuid, updatedRating, numOfUsers - 1);
                } else {
                    double tmpVal;
                    tmpVal = currentRating - oldRatingVal;
                    updatedRating = (tmpVal + newRatingVal) / numOfUsers;
                    this.applicationReleaseDAO.updateRatingValue(uuid, updatedRating, numOfUsers);
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            log.error("Error occured while updated the rating value of the application release UUID: " + uuid);
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            log.error(
                    "Transaction Management Exception occured while updated the rating value of the application release UUID: "
                            + uuid);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            log.error("DB Connection error occured while updated the rating value of the application release UUID: "
                    + uuid + " can not get.");
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