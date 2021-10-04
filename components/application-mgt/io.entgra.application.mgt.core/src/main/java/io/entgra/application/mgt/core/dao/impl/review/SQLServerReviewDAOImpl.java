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

package io.entgra.application.mgt.core.dao.impl.review;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.PaginationRequest;
import io.entgra.application.mgt.common.dto.ReviewDTO;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.exception.ReviewManagementDAOException;
import io.entgra.application.mgt.core.util.Constants;
import io.entgra.application.mgt.core.util.DAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * This handles Application Review handling operations which are specific to MsSQL.
 */
public class SQLServerReviewDAOImpl extends GenericReviewDAOImpl {

    private static final Log log = LogFactory.getLog(SQLServerReviewDAOImpl.class);
    private String sql;

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
                + "ORDER BY AP_APP_REVIEW.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, releaseId);
                statement.setInt(2, Constants.REVIEW_PARENT_ID);
                statement.setInt(3, tenantId);
                statement.setInt(4, request.getOffSet());
                statement.setInt(5, request.getLimit());
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
                            + "AP_APP_REVIEW.ACTIVE_REVIEW = 'true' AND "
                            + "AP_APP_REVIEW.TENANT_ID = ? "
                            + "ORDER BY AP_APP_REVIEW.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            releaseIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                int index = 1;
                for (Integer releaseId : releaseIds) {
                    ps.setObject(index++, releaseId);
                }
                ps.setInt(index++, Constants.REVIEW_PARENT_ID);
                ps.setInt(index++, tenantId);
                ps.setInt(index++, request.getOffSet());
                ps.setInt(index, request.getLimit());
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
                            + "AP_APP_REVIEW.ACTIVE_REVIEW = 'true' AND "
                            + "AP_APP_REVIEW.USERNAME = ? AND "
                            + "AP_APP_REVIEW.TENANT_ID = ? "
                            + "ORDER BY AP_APP_REVIEW.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
                ps.setInt(index++, request.getOffSet());
                ps.setInt(index, request.getLimit());
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
                    ") AND AP_APP_REVIEW.ACTIVE_REVIEW = 'true' AND AP_APP_REVIEW.TENANT_ID = ?");
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
}
