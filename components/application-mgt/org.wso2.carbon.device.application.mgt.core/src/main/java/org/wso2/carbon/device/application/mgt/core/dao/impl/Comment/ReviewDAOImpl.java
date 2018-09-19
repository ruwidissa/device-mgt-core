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
package org.wso2.carbon.device.application.mgt.core.dao.impl.Comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.ReviewManagementDAOException;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles ReviewDAO related operations.
 */

public class ReviewDAOImpl extends AbstractDAOImpl implements ReviewDAO {

    private static final Log log = LogFactory.getLog(ReviewDAOImpl.class);
    private String sql;

    @Override
    public boolean addReview(Review review, int appId, int appReleaseId, int tenantId)
            throws ReviewManagementDAOException {
        //todo
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add review for application release. Application id: " + appId
                    + "Application Release id: " + appReleaseId);
        }
        PreparedStatement statement = null;
        ResultSet rs = null;
        sql = "INSERT INTO AP_APP_Review (TENANT_ID, COMMENT, PARENT_ID, USERNAME, AP_APP_RELEASE_ID, AP_APP_ID) "
                + "VALUES (?,?,?,?,(SELECT ID FROM AP_APP_RELEASE WHERE UUID= ?),"
                + "(SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID=?));";
        try {
            Connection conn = this.getDBConnection();
            statement = conn.prepareStatement(sql, new String[] { "id" });
            statement.setInt(1, tenantId);
            statement.setString(2, review.getComment());
            statement.setInt(3, review.getParentId());
            statement.setString(4, review.getUsername());
            statement.setString(5,"");
            statement.setString(6,"");
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            return rs.next();
        }
        catch (DBConnectionException e) {
            throw new ReviewManagementDAOException("Error occurred while obtaining the DB connection while "
                    + "adding review for application UUID:  "+ "Tenant Id: " + tenantId, e);
        }catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occurred while getting application list for the tenant"
                    + " " + tenantId + ". While executing " + sql, e);
        }  finally {
            Util.cleanupResources(statement, rs);
        }
    }

    @Override
    public Review isExistReview(int appId, int appReleaseId, String username, int tenantId)
            throws DBConnectionException, SQLException {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Request received in DAO Layer to check whether review exist or not rein the IoTS APPM. Application id:  "
                            + appId + " Application release id: " + appReleaseId + " comment owner: " + username);
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Review review = null;
        sql = "SELECT ID, COMMENT, CREATED_AT, MODEFIED_AT, USERNAME, PARENT_ID, RATING FROM AP_APP_REVIEW WHERE "
                + "AP_APP_ID = ? AND AP_APP_RELEASE_ID = ? AND USERNAME = ? AND TENANT_ID = ?;";
        try {
            conn = this.getDBConnection();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, appId);
            statement.setInt(2, appReleaseId);
            statement.setString(3, username);
            statement.setInt(4, tenantId);

            rs = statement.executeQuery();
            if (rs.next()){
                review = new Review();
                review.setId(rs.getInt("ID"));
                review.setComment(rs.getString("COMMENT"));
                review.setParentId(rs.getInt("PARENT_ID"));
                review.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                review.setModifiedAt(rs.getTimestamp("MODIFIED_AT"));
                review.setUsername(rs.getString("USERNAME"));
                review.setRating(rs.getInt("RATING"));
            }
            return review;
        } finally {
            Util.cleanupResources(statement, rs);
        }
    }

    @Override
    public boolean updateReview(Review review, int reviewId, int tenantId) throws ReviewManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update the comment with ID (" + reviewId + ")");
        }
        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;
        sql = "UPDATE AP_APP_COMMENT SET COMMENT_TEXT=?, MODEFIED_BY=? WHERE ID=?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
//            statement.setString(1, updatedComment);
//            statement.setString(2, modifiedBy);
            statement.setInt(3, reviewId);
            statement.executeUpdate();
            rs = statement.executeQuery();
        } finally {
            Util.cleanupResources(statement, rs);
        }
        // todo
        return false;
    }

    @Override
    public Review getComment(int commentId) throws ReviewManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting review with the review id(" + commentId + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        Review review = new Review();
        try {
            conn = this.getDBConnection();
            sql = "SELECT COMMENT_TEXT FROM AP_APP_COMMENT WHERE ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, commentId);
            rs = statement.executeQuery();
            if (rs.next()) {
                review.setId(rs.getInt("ID"));
//                review.setTenantId(rs.getInt("TENANT_ID"));
                review.setComment(rs.getString("COMMENT_TEXT"));
                review.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                review.setUsername(rs.getString("CREATED_BY"));
                review.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                Util.cleanupResources(statement, rs);
                return review;
            }
        } catch (SQLException e) {
            throw new ReviewManagementException(
                    "SQL Error occurred while retrieving information of the review " + commentId, e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving information of the review " + commentId, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews(String uuid, PaginationRequest request, int tenantId)
            throws ReviewManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Review> reviews = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql = "SELECT AP_APP_COMMENT.ID AS ID, AP_APP_COMMENT.COMMENT_TEXT AS COMMENT_TEXT, "
                    + "AP_APP_COMMENT.CREATED_BY AS CREATED_BY, AP_APP_COMMENT.MODIFIED_BY AS MODIFIED_BY, "
                    + "AP_APP_COMMENT.PARENT_ID AS PARENT_ID FROM AP_APP_COMMENT, AP_APP_RELEASE WHERE "
                    + "AP_APP_COMMENT.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID AND AP_APP_RELEASE.UUID =? AND "
                    + "AP_APP_COMMENT.TENANT_ID = ? AND AP_APP_COMMENT.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                    + "LIMIT ? OFFSET ?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setInt(2, tenantId);
            statement.setInt(3, request.getLimit());
            statement.setInt(4, request.getOffSet());
            rs = statement.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getInt("ID"));
                review.setComment(rs.getString("COMMENT_TEXT"));
                review.setUsername(rs.getString("CREATED_BY"));
                reviews.add(review);
            }
        }  catch (DBConnectionException e) {
            throw new ReviewManagementDAOException(
                    "Error occurred while obtaining the DB connection when verifying application existence", e);
        } catch (SQLException e) {
            throw new ReviewManagementDAOException("Error occurred while adding unrestricted roles", e);
        }finally {
            Util.cleanupResources(statement, rs);
        }
        return reviews;
    }

    @Override
    public List<Integer> getAllRatingValues(String uuid)throws SQLException, DBConnectionException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Integer> reviews = new ArrayList<>();
        try {
            conn = this.getDBConnection();
//            todo
            sql = "SELECT AP_APP_COMMENT.ID AS ID, AP_APP_COMMENT.COMMENT_TEXT AS "
                    + "COMMENT_TEXT, AP_APP_COMMENT.CREATED_BY AS CREATED_BY, AP_APP_COMMENT.MODIFIED_BY AS "
                    + "MODIFIED_BY, AP_APP_COMMENT.PARENT_ID AS PARENT_ID FROM AP_APP_COMMENT, AP_APP_RELEASE WHERE "
                    + "AP_APP_COMMENT.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID AND AP_APP_RELEASE.UUID =? AND "
                    + "AP_APP_COMMENT.TENANT_ID = ? AND AP_APP_COMMENT.TENANT_ID = AP_APP_RELEASE.TENANT_ID;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            rs = statement.executeQuery();
            while (rs.next()) {
                reviews.add(rs.getInt("RATING"));
            }
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return reviews;
    }

    @Override
    public int getCommentCount(PaginationRequest request, String uuid) throws ReviewManagementException {

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
                sql = "SELECT COUNT(AP_APP_COMMENT.ID) FROM AP_APP_COMMENT,AP_APP_RELEASE "
                        + "WHERE AP_APP_COMMENT.AP_APP_RELEASE_ID= AP_APP_RELEASE.ID AND "
                        + "AP_APP_COMMENT.AP_APP_ID= AP_APP_RELEASE.AP_APP_ID AND AP_APP_RELEASE.UUID=?;";
                statement = conn.prepareStatement(sql);
                statement.setString(1, uuid);
                rs = statement.executeQuery();
                if (rs.next()) {
                    commentCount = rs.getInt("COMMENTS_COUNT");
                }
            }
        } catch (SQLException e) {
            throw new ReviewManagementException("SQL Error occurred while retrieving count of comments", e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving count of comments", e);
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return commentCount;
    }

    @Override
    public int getCommentCountByApp(String appType, String appName, String version)
            throws ReviewManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql = "SELECT COUNT(ID) AS COMMENT_COUNT FROM AP_APP_COMMENT C, "
                    + "(SELECT ID AS RELEASE_ID, AP_APP_ID AS RELEASE_AP_APP_ID FROM AP_APP_RELEASE R WHERE VERSION=? )R,"
                    + "(SELECT ID AS APP_ID FROM AP_APP P WHERE NAME=? and TYPE=?)P "
                    + "WHERE AP_APP_RELEASE_ID=RELEASE_ID AND RELEASE_AP_APP_ID=APP_ID AND AP_APP_ID=RELEASE_AP_APP_ID;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    @Override
    public void deleteComment(int commentId)
            throws ReviewManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql = "DELETE FROM AP_APP_COMMENT WHERE ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.executeUpdate();
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteComments(String appType, String appName, String version)
            throws ReviewManagementException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
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
            Util.cleanupResources(statement, null);
        }
    }
}
