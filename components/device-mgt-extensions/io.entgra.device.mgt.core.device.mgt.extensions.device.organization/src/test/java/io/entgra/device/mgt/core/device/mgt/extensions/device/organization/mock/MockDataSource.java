/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.mock;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is the mock data source implementation that will be used in the test cases.
 */
public class MockDataSource implements DataSource {
    private final List<Connection> connections = new ArrayList<>();
    private final String url;
    private boolean throwException = false;
    private int connectionCounter = 0;

    public MockDataSource(String url) {
        this.url = url;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (throwException) {
            throw new SQLException("Cannot created test connection.");
        } else {
            if (!connections.isEmpty()) {
                if (this.connectionCounter < this.connections.size()) {
                    Connection connection = this.connections.get(this.connectionCounter);
                    this.connectionCounter++;
                    return connection;
                } else {
                    return new MockConnection(url);
                }
            }
            return new MockConnection(url);
        }
    }

    public void setConnection(Connection connection) {
        this.connections.add(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    public void reset() {
        this.throwException = false;
        this.connections.clear();
        this.connectionCounter = 0;
    }

    public String getUrl() {
        return this.url;
    }

    public MockConnection getConnection(int id) {
        return (MockConnection) this.connections.get(id);
    }

}
