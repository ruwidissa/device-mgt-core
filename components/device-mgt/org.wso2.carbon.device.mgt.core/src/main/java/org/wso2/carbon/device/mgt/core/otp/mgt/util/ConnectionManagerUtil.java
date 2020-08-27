/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.otp.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.IllegalTransactionStateException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionManagerUtil is responsible for handling all the datasource connections utilities.
 */
public class ConnectionManagerUtil {

    private static final Log log = LogFactory.getLog(ConnectionManagerUtil.class);
    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static DataSource dataSource;

    public static void openDBConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            String msg = "Database connection has already been obtained.";
            log.error(msg);
            throw new IllegalTransactionStateException(msg);
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            String msg = "Failed to get a database connection.";
            log.error(msg, e);
            throw new DBConnectionException(msg, e);
        }
        currentConnection.set(conn);
    }

    public static Connection getDBConnection() throws DBConnectionException {
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

    public static void beginDBTransaction() throws TransactionManagementException, DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            conn = getDBConnection();
        } else if (inTransaction(conn)) {
            String msg = "Transaction has already been started.";
            log.error(msg);
            throw new IllegalTransactionStateException(msg);
        }

        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            String msg = "Error occurred while starting a database transaction.";
            log.error(msg, e);
            throw new TransactionManagementException(msg, e);
        }
    }

    public static void endDBTransaction() throws TransactionManagementException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while ending database transaction.", e);
        }
    }

    public static void commitDBTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            log.error("Error occurred while committing the transaction", e);
        }
    }

    public static void rollbackDBTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.rollback();
        } catch (SQLException e) {
            log.warn("Error occurred while roll-backing the transaction", e);
        }
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

    private static boolean inTransaction(Connection conn) {
        boolean inTransaction = true;
        try {
            if (conn.getAutoCommit()) {
                inTransaction = false;
            }
        } catch (SQLException e) {
            throw new IllegalTransactionStateException("Failed to get transaction state.");
        }
        return inTransaction;
    }

    public static boolean isTransactionStarted() throws DBConnectionException {
        Connection connection = getDBConnection();
        return inTransaction(connection);
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

    public static String getDatabaseType() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
        return null;
    }

    /**
     * To check whether particular database that is used for application management supports batch query execution.
     *
     * @return true if batch query is supported, otherwise false.
     */
    public static boolean isBatchQuerySupported() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().supportsBatchUpdates();
        } catch (SQLException e) {
            log.error("Error occurred while checking whether database supports batch updates", e);
        }
        return false;
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }
}
