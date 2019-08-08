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
            throw new DeviceTypeDeployerPayloadException("Error while looking up the data source: " + datasourceName, e);
        }
    }

    public void openConnection() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                throw new IllegalTransactionStateException("Database connection has already been obtained.");
            }
            conn = dataSource.getConnection();
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new DeviceTypeMgtPluginException("Failed to get a database connection.", e);
        }
    }

    public void beginTransaction() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while retrieving datasource connection", e);
        }
    }

    public Connection getConnection() throws DeviceTypeMgtPluginException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new DeviceTypeMgtPluginException("Error occurred while retrieving data source connection", e);
            }
        }
        return currentConnection.get();
    }

    public void commitTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalStateException("No connection is associated with the current transaction. " +
                                            "This might have ideally been caused by not properly initiating the " +
                                            "transaction via 'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            log.error("Error occurred while committing the transaction.", e);
        }
    }

    public void closeConnection() {

        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection");
            }
        }
        currentConnection.remove();
    }

    public void rollbackTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalStateException("No connection is associated with the current transaction. " +
                                            "This might have ideally been caused by not properly initiating the " +
                                            "transaction via 'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("Error occurred while roll-backing the transaction.", e);
        }
    }
}
