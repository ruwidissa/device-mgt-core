/*
 *   Copyright (c) 2019, Entgra (Pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.extensions.device.type.template.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeMgtPluginException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This component handles the connections
 */
public class DeviceTypeDAOHandler {

    private static final Log log = LogFactory.getLog(DeviceTypeDAOHandler.class);

    private DataSource dataSource;
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public DeviceTypeDAOHandler(String datasourceName) {
        initDAO(datasourceName);
    }

    public void initDAO(String datasourceName) {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(datasourceName);
        } catch (NamingException e) {
            String msg = "Error while looking up the data source: " + datasourceName;
            log.error(msg, e);
            throw new DeviceTypeDeployerPayloadException(msg, e);
        }
    }

    public void openConnection() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                String msg = "Database connection has already been obtained.";
                log.error(msg);
                throw new IllegalTransactionStateException(msg);
            }
            conn = dataSource.getConnection();
            currentConnection.set(conn);
        } catch (SQLException e) {
            String msg = "Failed to get a database connection.";
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        }
    }

    public void beginTransaction() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving datasource connection";
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        }
    }

    public Connection getConnection() throws DeviceTypeMgtPluginException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                String msg = "Error occurred while retrieving data source connection";
                log.error(msg, e);
                throw new DeviceTypeMgtPluginException(msg, e);
            }
        }
        return currentConnection.get();
    }

    public void commitTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            String msg = "No connection is associated with the current transaction. This might have ideally been " +
                         "caused by not properly initiating the transaction via " +
                         "'beginTransaction'/'openConnection' methods";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while committing the transaction.";
            log.error(msg, e);
        }
    }

    public void closeConnection() {
        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                String msg = "Error occurred while close the connection";
                log.error(msg, e);
            }
        }
        currentConnection.remove();
    }

    public void rollbackTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            String msg = "No connection is associated with the current transaction. This might have ideally been " +
                         "caused by not properly initiating the transaction via " +
                         "'beginTransaction'/'openConnection' methods";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            String msg = "Error occurred while roll-backing the transaction.";
            log.error(msg, e);
        }
    }
}
