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
package org.wso2.carbon.device.application.mgt.core.dao.impl.Review;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.dto.ReviewDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

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

public class ReviewDAOImpl extends AbstractDAOImpl implements ReviewDAO {

    private static final Log log = LogFactory.getLog(ReviewDAOImpl.class);
    private String sql;

    @Override
    public boolean addReview(ReviewDTO reviewDTO, int appReleaseId, int tenantId) throws ReviewManagementDAOException {
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
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql, new String[] { "id" });) {
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
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occurred while obtaining the DB connection while "
                    + "adding review for application release which has ID:  "+ appReleaseId + "Tenant Id: " + tenantId, e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while executing SQL statement to add application review. Application ID: "
                            + appReleaseId + " tenant " +  tenantId, e);
        }
    }

    @Override
    public boolean hasUerReviewedApp(List<Integer> appReleaseIds, String username, int tenantId)
            throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to check whether user have already reviewed or not for the "
                    + "application. Commenting user: " + username + " and tenant-id: " + tenantId);
        }
        Connection conn;
        int index = 1;
        try {
            conn = this.getDBConnection();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT rv.ID FROM AP_APP_REVIEW rv WHERE rv.AP_APP_RELEASE_ID IN (",
                    ") AND rv.USERNAME = ? AND rv.TENANT_ID = ?");
            appReleaseIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : appReleaseIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setString(index++, username);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occured while accessing the Database when checking whether "
                    + "user has already commented for the application ro not", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occured while getting the database connection when checking "
                    + "whether user has already commented for the application ro not", e);
        }
    }

    @Override
    public int updateReview(ReviewDTO reviewDTO, int reviewId, boolean isActiveReview, int tenantId)
            throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update the Review with ID (" + reviewId + ")");
        }
        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;
        sql = "UPDATE "
                + "AP_APP_REVIEW "
                + "SET "
                + "COMMENT = ?, "
                + "RATING = ?, "
                + "MODIFIED_AT = ?, "
                + "ACTIVE_REVIEW = ? "
                + "WHERE ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, reviewDTO.getContent());
            statement.setInt(2, reviewDTO.getRating());
            statement.setTimestamp(3, timestamp);
            statement.setBoolean(4, isActiveReview);
            statement.setInt(5, reviewId);
            statement.setInt(6, tenantId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occurred while executing reviewTmp updating query");
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occured while getting the db connection to update reviewTmp");
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
    }

    @Override
    public ReviewDTO getReview(int reviewId) throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting reviewDTO with the review id(" + reviewId + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
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
                    + "WHERE AP_APP_REVIEW.ID = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, reviewId);
            rs = statement.executeQuery();
            return DAOUtil.loadReview(rs);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException(
                    "SQL Error occurred while retrieving information of the reviewTmp " + reviewId, e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "DB Connection Exception occurred while retrieving information of the reviewTmp " + reviewId, e);
        } catch (UnexpectedServerErrorException e) {
            throw new ReviewManagementDAOException("Found more than one review for review ID: " + reviewId, e);
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
    }

    @Override
    public ReviewDTO getReview(int appReleaseId, int reviewId) throws ReviewManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting reviewDTO with the review id(" + reviewId + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            sql = "SELECT "
                    + "ID, "
                    + "COMMENT,"
                    + "ROOT_PARENT_ID,"
                    + "IMMEDIATE_PARENT_ID, "
                    + "CREATED_AT, "
                    + "MODIFIED_AT, "
                    + "RATING, "
                    + "USERNAME "
                    + "FROM AP_APP_REVIEW "
                    + "WHERE ID = ? AND "
                    + "AP_APP_RELEASE_ID = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, reviewId);
            statement.setInt(2, appReleaseId);
            rs = statement.executeQuery();
            return DAOUtil.loadReview(rs);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException(
                    "SQL Error occurred while retrieving information of the reviewTmp " + reviewId, e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "DB Connection Exception occurred while retrieving information of the reviewTmp " + reviewId, e);
        } catch (UnexpectedServerErrorException e) {
            throw new ReviewManagementDAOException("Found more than one review for review ID: " + reviewId, e);
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
    }


    @Override
    public List<ReviewDTO> getAllReleaseReviews(int releaseId, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + releaseId + ") from the database");
        }
        Connection conn;
        List<ReviewDTO> reviewDTOs;
        try {
            conn = this.getDBConnection();
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
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, releaseId);
                statement.setInt(2, Constants.REVIEW_PARENT_ID);
                statement.setInt(3, tenantId);
                statement.setInt(4, request.getLimit());
                statement.setInt(5, request.getOffSet());
                try (ResultSet rs = statement.executeQuery()) {
                    reviewDTOs = DAOUtil.loadReviews(rs);
                }
            }
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while obtaining the DB connection when verifying application existence", e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("DB connection error occurred while getting all reviewTmps", e);
        } return reviewDTOs;
    }

    @Override
    public List<ReviewDTO> getAllActiveAppReviews(List<Integer> releaseIds, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting reviews of the application from the database.");
        }
        Connection conn;
        List<ReviewDTO> reviewDTOs;
        int index = 1;

            try {
                conn = this.getDBConnection();
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
                                + "AP_APP_REVIEW.TENANT_ID = ? "
                                + "LIMIT ? OFFSET ?");
                releaseIds.stream().map(ignored -> "?").forEach(joiner::add);
                String query = joiner.toString();
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    for (Integer releaseId : releaseIds) {
                        ps.setObject(index++, releaseId);
                    }
                    ps.setInt(index++, Constants.REVIEW_PARENT_ID);
                    ps.setInt(index++, tenantId);
                    ps.setInt(index++, request.getLimit());
                    ps.setInt(index, request.getOffSet());
                    try (ResultSet rs = ps.executeQuery()) {
                        reviewDTOs = DAOUtil.loadReviews(rs);
                    }
                }
            }
         catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while obtaining the DB connection when verifying application existence.", e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("DB connection error occurred while getting all reviews.", e);
        } return reviewDTOs;
    }

    @Override
    public List<ReviewDTO> getAllActiveAppReviewsOfUser(List<Integer> releaseIds, PaginationRequest request,
            String username, int tenantId)
            throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting reviews of the application for given user from the database.");
        }
        Connection conn;
        List<ReviewDTO> reviewDTOs;
        int index = 1;

        try {
            conn = this.getDBConnection();
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
                for (Integer releaseId : releaseIds) {
                    ps.setObject(index++, releaseId);
                }
                ps.setInt(index++, Constants.REVIEW_PARENT_ID);
                ps.setString(index++, username);
                ps.setInt(index++, tenantId);
                ps.setInt(index++, request.getLimit());
                ps.setInt(index, request.getOffSet());
                try (ResultSet rs = ps.executeQuery()) {
                    reviewDTOs = DAOUtil.loadReviews(rs);
                }
            }
        }
        catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while obtaining the DB connection when application review of user: " + username , e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("DB connection error occurred while getting application reviews of "
                    + "user:" + username, e);
        } return reviewDTOs;
    }

    @Override
    public List<ReviewDTO> getReplyComments(int parentId, int tenantId) throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + parentId + ") from the database");
        }
        Connection conn;
        List<ReviewDTO> reviewDTOs;
        try {
            conn = this.getDBConnection();
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
                try (ResultSet rs = statement.executeQuery();) {
                    reviewDTOs = DAOUtil.loadReviews(rs);
                }
            }
        }  catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while obtaining the DB connection when getting reply comments for a review.", e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("DB connection error occurred while getting reply comments", e);
        }
        return reviewDTOs;
    }

    @Override
    public List<Integer> getAllAppReleaseRatingValues(String uuid, int tenantId) throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Integer> reviews = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql = "SELECT AP_APP_REVIEW.RATING AS RATING FROM AP_APP_REVIEW, AP_APP_RELEASE WHERE "
                    + "AP_APP_REVIEW.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID AND AP_APP_RELEASE.UUID =? AND "
                    + "AP_APP_REVIEW.TENANT_ID = AP_APP_RELEASE.TENANT_ID AND AP_APP_REVIEW.TENANT_ID = ?";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setInt(2, tenantId);
            rs = statement.executeQuery();
            while (rs.next()) {
                reviews.add(rs.getInt("RATING"));
            }
        } catch (SQLException e) {
            throw new ReviewManagementDAOException(
                    "Error occured while getting all rating values for the application release. App release UUID: "
                            + uuid, e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occured while getting DB connection to retrieve all rating values for the application release. App release UUID: "
                            + uuid, e);
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
        return reviews;
    }

    @Override
    public List<Integer> getAllAppRatingValues(List<String> uuids, int tenantId) throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting rating values of the application from the database");
        }
        Connection conn;
        List<Integer> reviews = new ArrayList<>();
        try {
            int index = 1;
            conn = this.getDBConnection();
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
                    }                }
            }
        } catch (SQLException e) {
            throw new ReviewManagementDAOException(
                    "Error occured while getting all rating values for the application.", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occured while getting DB connection to retrieve all rating values for the application.", e);
        }
        return reviews;
    }

    @Override
    public int getReviewCount(String uuid) throws ReviewManagementDAOException {

        int commentCount = 0;
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean isUuidProvided = false;
        try {
            conn = this.getDBConnection();
            if (uuid != null) {
                isUuidProvided = true;
            }
            if (isUuidProvided) {
                sql = "SELECT COUNT(AP_APP_REVIEW.ID) AS REVIEW_COUNT FROM AP_APP_REVIEW,AP_APP_RELEASE "
                        + "WHERE AP_APP_REVIEW.AP_APP_RELEASE_ID= AP_APP_RELEASE.ID AND "
                        + "AP_APP_REVIEW.AP_APP_ID= AP_APP_RELEASE.AP_APP_ID AND AP_APP_RELEASE.UUID=?;";
                statement = conn.prepareStatement(sql);
                statement.setString(1, uuid);
                rs = statement.executeQuery();
                if (rs.next()) {
                    commentCount = rs.getInt("REVIEW_COUNT");
                }
            }
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("SQL Error occurred while retrieving review counts", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("DB Connection Exception occurred while retrieving review counts", e);
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
        return commentCount;
    }

    @Override
    public int getReviewCountByApp(String appType, String appName, String version)
            throws ReviewManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int commentCount = 0;
        try {
            //todo need to reconstruct the query
            conn = this.getDBConnection();
            sql = "SELECT COUNT(ID) AS COMMENT_COUNT FROM AP_APP_COMMENT C, "
                    + "(SELECT ID AS RELEASE_ID, AP_APP_ID AS RELEASE_AP_APP_ID FROM AP_APP_RELEASE R WHERE VERSION=? )R,"
                    + "(SELECT ID AS APP_ID FROM AP_APP P WHERE NAME=? and TYPE=?)P "
                    + "WHERE AP_APP_RELEASE_ID=RELEASE_ID AND RELEASE_AP_APP_ID=APP_ID AND AP_APP_ID=RELEASE_AP_APP_ID;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            DAOUtil.cleanupResources(statement, rs);
        }
        return commentCount;
    }

    @Override
    public void deleteReview(int reviewId, int tenantId) throws ReviewManagementDAOException {
        Connection conn;
        try {
            conn = this.getDBConnection();
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
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occured while accessing the Database", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occured while getting the database connection", e);

        }
    }

    @Override
    public void deleteReviews(List<Integer> reviewIds, int tenantId) throws ReviewManagementDAOException{
        Connection conn;
        try {
            conn = this.getDBConnection();
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
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occured while accessing the Database", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occured while getting the database connection", e);

        }
    }

    @Override
    public void deleteAllChildCommentsOfReview(int rootParentId, int tenantId) throws ReviewManagementDAOException {
        Connection conn;
        try {
            conn = this.getDBConnection();
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
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occured while accessing the Database", e);
        } catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occured while getting the database connection", e);
        }
    }


    @Override
    public void deleteReviews(String appType, String appName, String version) throws ReviewManagementException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            //todo need to reconstruct the query,
            sql = "DELETE FROM AP_APP_COMMENT WHERE "
                    + "(SELECT AP_APP_RELEASE_ID FROM AP_APP_RELEASE WHERE VERSION=? AND "
                    + "(SELECT AP_APP_ID FROM AP_APP WHERE NAME=? AND TYPE=?)) AND "
                    + "(SELECT AP_APP_ID FROM AP_APP AND NAME=? AND TYPE=?);";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            statement.setString(4, appName);
            statement.setString(5, appType);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while deleting comments", e);
        } catch (SQLException e) {
            throw new ReviewManagementException("SQL Error occurred while deleting comments", e);
        } finally {
            DAOUtil.cleanupResources(statement, null);
        }
    }
}
