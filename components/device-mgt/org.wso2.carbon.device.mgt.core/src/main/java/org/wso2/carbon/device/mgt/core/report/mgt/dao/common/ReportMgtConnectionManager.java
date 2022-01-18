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
package org.wso2.carbon.device.mgt.core.report.mgt.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require Application management related metadata persistence.
 */
public class ReportMgtConnectionManager {

    private static final Log log = LogFactory.getLog(ReportMgtConnectionManager.class);

    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static DataSource dataSource;

    static {
        String dataSourceName = ReportMgtConfigurationManager.getInstance().getConfiguration().getDatasourceName();
        init(dataSourceName);
    }

    public static void init(String datasourceName) {
        resolveDataSource(datasourceName);
    }

    public static String getDatabaseType() {
        try {
            return dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
        return null;
    }

    /**
     * Resolve the datasource from the datasource definition.
     *
     * @param dataSourceName Name of the datasource
     * @return DataSource resolved by the datasource name
     */
    public static DataSource resolveDataSource(String dataSourceName) {
        try {
            dataSource = InitialContext.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
        return dataSource;
    }

    public static void openDBConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("Database connection has already been obtained.");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBConnectionException("Failed to get a database connection.", e);
        }
        currentConnection.set(conn);
    }

    public static Connection getDBConnection() throws DBConnectionException {
        if (dataSource == null) {
            String dataSourceName = ReportMgtConfigurationManager.getInstance().getConfiguration().getDatasourceName();
            init(dataSourceName);
        }
        Connection conn = currentConnection.get();
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
                currentConnection.set(conn);
            } catch (SQLException e) {
                throw new DBConnectionException("Failed to get database connection.", e);
            }
        }
        return conn;
    }


    public static void closeDBConnection() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.error("Error occurred while closing the connection", e);
        }
        currentConnection.remove();
    }


}
