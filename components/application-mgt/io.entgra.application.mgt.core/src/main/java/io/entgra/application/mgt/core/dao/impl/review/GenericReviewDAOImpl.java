/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.core.dao.impl.review;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.entgra.application.mgt.common.PaginationRequest;
import io.entgra.application.mgt.common.dto.ReviewDTO;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.dao.ReviewDAO;
import io.entgra.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.application.mgt.core.util.DAOUtil;
import io.entgra.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.application.mgt.core.exception.ReviewManagementDAOException;
import io.entgra.application.mgt.core.util.Constants;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringJoiner;

/**
 * This handles ReviewDAO related operations.
 */

public class GenericReviewDAOImpl extends AbstractDAOImpl implements ReviewDAO {

    private static final Log log = LogFactory.getLog(GenericReviewDAOImpl.class);
    private String sql;

    @Override
    public int addReview(ReviewDTO reviewDTO, int appReleaseId, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add review for application release. Application Release UUID: "
                    + appReleaseId);
        }
        sql = "INSERT INTO AP_APP_REVIEW "
                + "(TENANT_ID, "
                + "COMMENT, "
                + "ROOT_PARENT_ID,"
                + "IMMEDIATE_PARENT_ID, "
                + "RATING, "
                + "USERNAME, "
                + "CREATED_AT, "
                + "MODIFIED_AT, "
                + "AP_APP_RELEASE_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";
        try {
            int reviewId = -1;
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql, new String[] { "id" })) {
                statement.setInt(1, tenantId);
                statement.setString(2, reviewDTO.getContent());
                statement.setInt(3, reviewDTO.getRootParentId());
                statement.setInt(4, reviewDTO.getImmediateParentId());
                statement.setInt(5, reviewDTO.getRating());
                statement.setString(6, reviewDTO.getUsername());
                statement.setTimestamp(7, timestamp);
                statement.setTimestamp(8, timestamp);
                statement.setInt(9, appReleaseId);
                statement.executeUpdate();
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        reviewId = rs.getInt(1);
                    }
                }
                return reviewId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to add a review for application release which"
                    + " has ID:  "+ appReleaseId + " and Tenant Id: " + tenantId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL statement to add application review. Application ID: "
                    + appReleaseId + " and tenant " +  tenantId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean hasUerReviewedApp(List<Integer> appReleaseIds, String username, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received to DAO Layer to check whether user have already reviewed or not for the "
                    + "application. Commenting user: " + username + " and tenant-id: " + tenantId);
        }
        try {
            if (appReleaseIds.isEmpty()) {
                return false;
            }
            Connection conn = this.getDBConnection();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT rv.ID FROM AP_APP_REVIEW rv WHERE rv.AP_APP_RELEASE_ID IN (",
                    ") AND rv.USERNAME = ? AND rv.TENANT_ID = ?");
            appReleaseIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                int index = 1;
                for (Integer deviceId : appReleaseIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setString(index++, username);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting the database connection when checking whether user has already "
                    + "commented for the application or not";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing the SQL statement to check whether user has already commented "
                    + "for the application or not";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public ReviewDTO updateReview(ReviewDTO reviewDTO, int reviewId, boolean isActiveReview, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received to DAO Layer to update the Review which has ID " + reviewId);
        }
        sql = "UPDATE "
                + "AP_APP_REVIEW "
                + "SET "
                + "COMMENT = ?, "
                + "RATING = ?, "
                + "MODIFIED_AT = ?, "
                + "ACTIVE_REVIEW = ? "
                + "WHERE ID = ? AND TENANT_ID = ?";
        try {
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, reviewDTO.getContent());
                statement.setInt(2, reviewDTO.getRating());
                statement.setTimestamp(3, timestamp);
                statement.setBoolean(4, isActiveReview);
                statement.setInt(5, reviewId);
                statement.setInt(6, tenantId);
                if (statement.executeUpdate() == 1) {
                    reviewDTO.setModifiedAt(timestamp);
                    return reviewDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting the db connection to update review for review ID: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing review updating query for review ID: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public ReviewDTO getReview(int reviewId, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received to DAO Layer to get review for review ID: " + reviewId);
        }
        sql = "SELECT "
                + "AP_APP_REVIEW.ID AS ID, "
                + "AP_APP_REVIEW.COMMENT AS COMMENT, "
                + "AP_APP_REVIEW.ROOT_PARENT_ID AS ROOT_PARENT_ID, "
                + "AP_APP_REVIEW.IMMEDIATE_PARENT_ID AS IMMEDIATE_PARENT_ID, "
                + "AP_APP_REVIEW.CREATED_AT AS CREATED_AT, "
                + "AP_APP_REVIEW.MODIFIED_AT AS MODIFIED_AT, "
                + "AP_APP_REVIEW.RATING AS RATING, "
                + "AP_APP_REVIEW.USERNAME AS USERNAME, "
                + "AP_APP_RELEASE.UUID AS UUID, "
                + "AP_APP_RELEASE.VERSION AS VERSION "
                + "FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID "
                + "WHERE AP_APP_REVIEW.ID = ? AND AP_APP_REVIEW.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, reviewId);
                statement.setInt(2, tenantId);
                try (ResultSet rs = statement.executeQuery()) {
                    return DAOUtil.loadReview(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "DB Connection Exception occurred while retrieving information of the review for review ID: "
                    + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing SQL statement to get review data for review ID: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one review for review ID: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }


    @Override
    public List<ReviewDTO> getAllReleaseReviews(int releaseId, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all application release reviews for the application release ID: " + releaseId);
        }
        sql = "SELECT "
                + "AP_APP_REVIEW.ID AS ID, "
                + "AP_APP_REVIEW.COMMENT AS COMMENT, "
                + "AP_APP_REVIEW.CREATED_AT AS CREATED_AT, "
                + "AP_APP_REVIEW.MODIFIED_AT AS MODIFIED_AT, "
                + "AP_APP_REVIEW.USERNAME AS USERNAME, "
                + "AP_APP_REVIEW.ROOT_PARENT_ID AS ROOT_PARENT_ID, "
                + "AP_APP_REVIEW.IMMEDIATE_PARENT_ID AS IMMEDIATE_PARENT_ID, "
                + "AP_APP_REVIEW.RATING AS RATING, "
                + "AP_APP_RELEASE.UUID AS UUID, "
                + "AP_APP_RELEASE.VERSION AS VERSION "
                + "FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID "
                + "WHERE AP_APP_REVIEW.AP_APP_RELEASE_ID = ? AND "
                + "AP_APP_REVIEW.ROOT_PARENT_ID = ? AND "
                + "AP_APP_REVIEW.TENANT_ID = ? "
                + "LIMIT ? OFFSET ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, releaseId);
                statement.setInt(2, Constants.REVIEW_PARENT_ID);
                statement.setInt(3, tenantId);
                statement.setInt(4, request.getLimit());
                statement.setInt(5, request.getOffSet());
                try (ResultSet rs = statement.executeQuery()) {
                    return DAOUtil.loadReviews(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get all app release reviews for "
                    + "application release ID: " + releaseId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing the SQL statement to get all app release reviews for "
                    + "application release ID: " + releaseId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ReviewDTO> getAllActiveAppReviews(List<Integer> releaseIds, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to Get all active application reviews.");
        }
        try {
            if (releaseIds.isEmpty()) {
                return new ArrayList<>();
            }
            Connection conn = this.getDBConnection();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT " + "AP_APP_REVIEW.ID AS ID, "
                            + "AP_APP_REVIEW.COMMENT AS COMMENT, "
                            + "AP_APP_REVIEW.CREATED_AT AS CREATED_AT, "
                            + "AP_APP_REVIEW.MODIFIED_AT AS MODIFIED_AT, "
                            + "AP_APP_REVIEW.USERNAME AS USERNAME, "
                            + "AP_APP_REVIEW.ROOT_PARENT_ID AS ROOT_PARENT_ID, "
                            + "AP_APP_REVIEW.IMMEDIATE_PARENT_ID AS IMMEDIATE_PARENT_ID, "
                            + "AP_APP_REVIEW.RATING AS RATING, "
                            + "AP_APP_RELEASE.UUID AS UUID, "
                            + "AP_APP_RELEASE.VERSION AS VERSION "
                            + "FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                            + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID "
                            + "WHERE AP_APP_REVIEW.AP_APP_RELEASE_ID IN (",
                    ") AND AP_APP_REVIEW.ROOT_PARENT_ID = ? AND "
                            + "AP_APP_REVIEW.ACTIVE_REVIEW = true AND "
                            + "AP_APP_REVIEW.TENANT_ID = ? "
                            + "LIMIT ? OFFSET ?");
            releaseIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                int index = 1;
                for (Integer releaseId : releaseIds) {
                    ps.setObject(index++, releaseId);
                }
                ps.setInt(index++, Constants.REVIEW_PARENT_ID);
                ps.setInt(index++, tenantId);
                ps.setInt(index++, request.getLimit());
                ps.setInt(index, request.getOffSet());
                try (ResultSet rs = ps.executeQuery()) {
                    return DAOUtil.loadReviews(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get all active app reviews.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to get all active app reviews.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ReviewDTO> getAllActiveAppReviewsOfUser(List<Integer> releaseIds, PaginationRequest request,
            String username, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to Get all active application reviews of user " + username);
        }
        try {
            if (releaseIds.isEmpty()) {
                return new ArrayList<>();
            }
            Connection conn = this.getDBConnection();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "AP_APP_REVIEW.ID AS ID, "
                            + "AP_APP_REVIEW.COMMENT AS COMMENT, "
                            + "AP_APP_REVIEW.CREATED_AT AS CREATED_AT, "
                            + "AP_APP_REVIEW.MODIFIED_AT AS MODIFIED_AT, "
                            + "AP_APP_REVIEW.USERNAME AS USERNAME, "
                            + "AP_APP_REVIEW.ROOT_PARENT_ID AS ROOT_PARENT_ID, "
                            + "AP_APP_REVIEW.IMMEDIATE_PARENT_ID AS IMMEDIATE_PARENT_ID, "
                            + "AP_APP_REVIEW.RATING AS RATING, "
                            + "AP_APP_RELEASE.UUID AS UUID, "
                            + "AP_APP_RELEASE.VERSION AS VERSION "
                            + "FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                            + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID "
                            + "WHERE AP_APP_REVIEW.AP_APP_RELEASE_ID IN (",
                    ") AND AP_APP_REVIEW.ROOT_PARENT_ID = ? AND "
                            + "AP_APP_REVIEW.ACTIVE_REVIEW = true AND "
                            + "AP_APP_REVIEW.USERNAME = ? AND "
                            + "AP_APP_REVIEW.TENANT_ID = ? "
                            + "LIMIT ? OFFSET ?");
            releaseIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                int index = 1;
                for (Integer releaseId : releaseIds) {
                    ps.setObject(index++, releaseId);
                }
                ps.setInt(index++, Constants.REVIEW_PARENT_ID);
                ps.setString(index++, username);
                ps.setInt(index++, tenantId);
                ps.setInt(index++, request.getLimit());
                ps.setInt(index, request.getOffSet());
                try (ResultSet rs = ps.executeQuery()) {
                    return DAOUtil.loadReviews(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get all active app reviews of user "
                    + username;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to get all active app reviews of user " + username;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ReviewDTO> getReplyComments(int parentId, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all reply comments for review which has review ID: " + parentId);
        }
        try {
            Connection conn = this.getDBConnection();
            sql = "SELECT "
                    + "AP_APP_REVIEW.ID AS ID, "
                    + "AP_APP_REVIEW.COMMENT AS COMMENT, "
                    + "AP_APP_REVIEW.CREATED_AT AS CREATED_AT, "
                    + "AP_APP_REVIEW.MODIFIED_AT AS MODIFIED_AT, "
                    + "AP_APP_REVIEW.USERNAME AS USERNAME, "
                    + "AP_APP_REVIEW.ROOT_PARENT_ID AS ROOT_PARENT_ID, "
                    + "AP_APP_REVIEW.IMMEDIATE_PARENT_ID AS IMMEDIATE_PARENT_ID, "
                    + "AP_APP_REVIEW.RATING AS RATING, "
                    + "AP_APP_RELEASE.UUID AS UUID, "
                    + "AP_APP_RELEASE.VERSION AS VERSION "
                    + "FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                    + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID "
                    + "WHERE "
                    + "AP_APP_REVIEW.ROOT_PARENT_ID = ? AND "
                    + "AP_APP_REVIEW.TENANT_ID = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, parentId);
                statement.setInt(2, tenantId);
                try (ResultSet rs = statement.executeQuery()) {
                    return DAOUtil.loadReviews(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting reply comments for a review "
                    + "which has reviw ID: " + parentId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to ge reply comments for a review which has reviw ID: "
                    + parentId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getAllAppReleaseRatingValues(String uuid, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all app release rating values for app release UUID: " + uuid);
        }
        sql = "SELECT "
                + "AP_APP_REVIEW.RATING AS RATING "
                + "FROM AP_APP_REVIEW, AP_APP_RELEASE "
                + "WHERE "
                + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID AND "
                + "AP_APP_RELEASE.UUID = ? AND "
                + "AP_APP_REVIEW.TENANT_ID = AP_APP_RELEASE.TENANT_ID AND "
                + "AP_APP_REVIEW.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, uuid);
                statement.setInt(2, tenantId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Integer> reviews = new ArrayList<>();
                    while (rs.next()) {
                        reviews.add(rs.getInt("RATING"));
                    }
                    return reviews;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting DB connection to retrieve all rating values for the application "
                    + "release which has UUID:" + uuid;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing SQL to retrieve all rating values for the application release "
                    + "which has UUID:" + uuid;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getAllAppRatingValues(List<String> uuids, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to Get all application rating values of an application.");
        }
        try {
            List<Integer> reviews = new ArrayList<>();
            if (uuids.isEmpty()) {
                return reviews;
            }
            int index = 1;
            Connection conn = this.getDBConnection();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT AP_APP_REVIEW.RATING AS RATING FROM AP_APP_REVIEW INNER JOIN AP_APP_RELEASE ON "
                            + "AP_APP_REVIEW.AP_APP_RELEASE_ID = AP_APP_RELEASE.ID WHERE AP_APP_RELEASE.UUID IN (",
                    ") AND AP_APP_REVIEW.ACTIVE_REVIEW = true AND AP_APP_REVIEW.TENANT_ID = ?");
            uuids.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String uuid : uuids) {
                    ps.setObject(index++, uuid);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        reviews.add(rs.getInt("RATING"));
                    }
                    return reviews;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting DB connection to retrieve all rating values for an application.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing SQL to get all rating values for the application.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteReview(int reviewId, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to delete review which has review ID: " + reviewId);
        }
        try {
            Connection conn = this.getDBConnection();
            sql = "DELETE "
                    + "FROM AP_APP_REVIEW "
                    + "WHERE "
                    + "ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, reviewId);
                statement.setInt(2, tenantId);
                statement.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting the database connection to delete review which has review ID: "
                    + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing SQL to delete review which has review ID: " + reviewId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteReviews(List<Integer> reviewIds, int tenantId) throws ReviewManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to delete reviews for requesting review Ids.");
        }
        try {
            Connection conn = this.getDBConnection();
            sql = "DELETE "
                    + "FROM AP_APP_REVIEW "
                    + "WHERE "
                    + "ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer reviewId : reviewIds) {
                    stmt.setInt(1, reviewId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting the database connection to delete reviews for given review Ids.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete reviews for given review Ids.";
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteAllChildCommentsOfReview(int rootParentId, int tenantId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO request is received to delete child comments of a review.");
        }
        try {
            Connection conn = this.getDBConnection();
            sql = "DELETE "
                    + "FROM AP_APP_REVIEW "
                    + "WHERE "
                    + "ROOT_PARENT_ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)){
                statement.setInt(1, rootParentId);
                statement.setInt(2, tenantId);
                statement.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting the database connection to delete all child comments of a review "
                    + "which has ID: " + rootParentId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured while executing SQL to delete all child comments of a review which has ID: "
                    + rootParentId;
            log.error(msg, e);
            throw new ReviewManagementDAOException(msg, e);
        }
    }
}
