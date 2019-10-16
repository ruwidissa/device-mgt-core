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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This handles Application operations which are specific to Oracle.
 */
public class OracleApplicationDAOImpl extends GenericApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(OracleApplicationDAOImpl.class);

    @Override
    public List<ApplicationDTO> getApplications(Filter filter,int deviceTypeId, int tenantId) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
        }
        int paramIndex = 1;
        String sql = "SELECT "
                + "AP_APP.ID AS APP_ID, "
                + "AP_APP.NAME AS APP_NAME, "
                + "AP_APP.DESCRIPTION AS APP_DESCRIPTION, "
                + "AP_APP.TYPE AS APP_TYPE, "
                + "AP_APP.STATUS AS APP_STATUS, "
                + "AP_APP.SUB_TYPE AS APP_SUB_TYPE, "
                + "AP_APP.CURRENCY AS APP_CURRENCY, "
                + "AP_APP.RATING AS APP_RATING, "
                + "AP_APP.DEVICE_TYPE_ID AS APP_DEVICE_TYPE_ID, "
                + "AP_APP_RELEASE.ID AS RELEASE_ID, "
                + "AP_APP_RELEASE.DESCRIPTION AS RELEASE_DESCRIPTION, "
                + "AP_APP_RELEASE.VERSION AS RELEASE_VERSION, "
                + "AP_APP_RELEASE.UUID AS RELEASE_UUID, "
                + "AP_APP_RELEASE.RELEASE_TYPE AS RELEASE_TYPE, "
                + "AP_APP_RELEASE.INSTALLER_LOCATION AS AP_RELEASE_STORED_LOC, "
                + "AP_APP_RELEASE.ICON_LOCATION AS AP_RELEASE_ICON_LOC, "
                + "AP_APP_RELEASE.BANNER_LOCATION AS AP_RELEASE_BANNER_LOC, "
                + "AP_APP_RELEASE.SC_1_LOCATION AS AP_RELEASE_SC1, "
                + "AP_APP_RELEASE.SC_2_LOCATION AS AP_RELEASE_SC2, "
                + "AP_APP_RELEASE.SC_3_LOCATION AS AP_RELEASE_SC3, "
                + "AP_APP_RELEASE.APP_HASH_VALUE AS RELEASE_HASH_VALUE, "
                + "AP_APP_RELEASE.APP_PRICE AS RELEASE_PRICE, "
                + "AP_APP_RELEASE.APP_META_INFO AS RELEASE_META_INFO, "
                + "AP_APP_RELEASE.PACKAGE_NAME AS PACKAGE_NAME, "
                + "AP_APP_RELEASE.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                + "AP_APP_RELEASE.RATING AS RELEASE_RATING, "
                + "AP_APP_RELEASE.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                + "AP_APP_RELEASE.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP "
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID "
                + "INNER JOIN (SELECT ID FROM AP_APP ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY) AS app_data ON app_data.ID = AP_APP.ID "
                + "WHERE AP_APP.TENANT_ID = ?";

        if (filter == null) {
            String msg = "Filter is not instantiated.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg);
        }

        if (!StringUtils.isEmpty(filter.getAppType())) {
            sql += " AND AP_APP.TYPE = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppName())) {
            sql += " AND LOWER (AP_APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ?";
            } else {
                sql += "LIKE ?";
            }
        }
        if (!StringUtils.isEmpty(filter.getSubscriptionType())) {
            sql += " AND AP_APP.SUB_TYPE = ?";
        }
        if (filter.getMinimumRating() > 0) {
            sql += " AND AP_APP.RATING >= ?";
        }
        if (!StringUtils.isEmpty(filter.getVersion())) {
            sql += " AND AP_APP_RELEASE.VERSION = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseType())) {
            sql += " AND AP_APP_RELEASE.RELEASE_TYPE = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseState())) {
            sql += " AND AP_APP_RELEASE.CURRENT_STATE = ?";
        }
        if (deviceTypeId != -1) {
            sql += " AND AP_APP.DEVICE_TYPE_ID = ?";
        }

        if (filter.getLimit() == -1) {
            sql = sql.replace("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY", "");
        }

        String sortingOrder = "ASC";
        if (!StringUtils.isEmpty(filter.getSortBy() )) {
            sortingOrder = filter.getSortBy();
        }
        sql += " ORDER BY APP_ID " + sortingOrder;

        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
            ){
                if (filter.getLimit() != -1) {
                    stmt.setInt(paramIndex++, filter.getOffset());
                    if (filter.getLimit() == 0) {
                        stmt.setInt(paramIndex++, 100);
                    } else {
                        stmt.setInt(paramIndex++, filter.getLimit());
                    }
                }
                stmt.setInt(paramIndex++, tenantId);

                if (filter.getAppType() != null && !filter.getAppType().isEmpty()) {
                    stmt.setString(paramIndex++, filter.getAppType());
                }
                if (filter.getAppName() != null && !filter.getAppName().isEmpty()) {
                    if (filter.isFullMatch()) {
                        stmt.setString(paramIndex++, filter.getAppName().toLowerCase());
                    } else {
                        stmt.setString(paramIndex++, "%" + filter.getAppName().toLowerCase() + "%");
                    }
                }
                if (!StringUtils.isEmpty(filter.getSubscriptionType())) {
                    stmt.setString(paramIndex++, filter.getSubscriptionType());
                }
                if (filter.getMinimumRating() > 0) {
                    stmt.setInt(paramIndex++, filter.getMinimumRating());
                }
                if (!StringUtils.isEmpty(filter.getVersion())) {
                    stmt.setString(paramIndex++, filter.getVersion());
                }
                if (!StringUtils.isEmpty(filter.getAppReleaseType())) {
                    stmt.setString(paramIndex++, filter.getAppReleaseType());
                }
                if (!StringUtils.isEmpty(filter.getAppReleaseState())) {
                    stmt.setString(paramIndex++, filter.getAppReleaseState());
                }
                if (deviceTypeId > 0 ) {
                    stmt.setInt(paramIndex, deviceTypeId);
                }
                try (ResultSet rs = stmt.executeQuery() ) {
                    return DAOUtil.loadApplications(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection while getting application list for the "
                    + "tenant " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application list for the tenant " + tenantId + ". While "
                    + "executing " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
