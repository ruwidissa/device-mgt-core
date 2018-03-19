/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.dao.impl.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.Pagination;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is a ApplicationDAO Implementation specific to Oracle.
 */
public class OracleApplicationDAOImpl extends GenericApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(OracleApplicationDAOImpl.class);

    @Override
    public ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ApplicationList applicationList = new ApplicationList();
        Pagination pagination = new Pagination();
        String sql = "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY"
                + " AS APP_CATEGORY, AP_APP.IS_FREE, AP_APP.RESTRICTED, AP_APP_TAG.TAG AS APP_TAG, " +
                "AP_UNRESTRICTED_ROLES.ROLE AS APP_UNRESTRICTED_ROLES FROM ((AP_APP LEFT JOIN AP_APP_TAG ON " +
                "AP_APP.ID = AP_APP_TAG.AP_APP_ID) LEFT JOIN AP_UNRESTRICTED_ROLES ON " +
                "AP_APP.ID = AP_UNRESTRICTED_ROLES.AP_APP_ID) WHERE AP_APP.TENANT_ID =  ? AND AP_APP.STATUS != ?";


        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        }

        if (filter.getAppType() != null) {
            sql += " AND AP_APP.TYPE ";
            sql += "= ?";
        }
        if (filter.getAppName() != null) {
            sql += " AND LOWER (AP_APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ?";
            } else {
                sql += "LIKE ?";
            }
        }

        sql += " AND rownum <= ? OFFSET ? " + filter.getSortBy() + " APP_ID";

        pagination.setLimit(filter.getLimit());
        pagination.setOffset(filter.getOffset());

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, AppLifecycleState.REMOVED.toString());

            if (filter.getAppType() != null) {
                stmt.setString(3, filter.getAppType());
            }
            if (filter.getAppName() != null) {
                if (filter.isFullMatch()) {
                    stmt.setString(4, filter.getAppName().toLowerCase());
                } else {
                    stmt.setString(4, "%" + filter.getAppName().toLowerCase() + "%");
                }
            }

            stmt.setInt(5, filter.getLimit());
            stmt.setInt(6, filter.getOffset());
            rs = stmt.executeQuery();
            applicationList.setApplications(Util.loadApplications(rs));
            applicationList.setPagination(pagination);
            applicationList.getPagination().setSize(filter.getOffset());
            applicationList.getPagination().setCount(applicationList.getApplications().size());

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application list for the tenant"
                                                                + " " + tenantId + ". While executing " + sql, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection while "
                                                                + "getting application list for the tenant " + tenantId,
                                                        e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON ", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return applicationList;
    }

}
