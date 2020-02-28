/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(ApplicationDAOImpl.class);

    @Override
    public void addApplications(List<Application> applications, int deviceId, int enrolmentId,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_APPLICATION (NAME, PLATFORM, " +
                    "CATEGORY, VERSION, TYPE, LOCATION_URL, IMAGE_URL, TENANT_ID, " +
                    "APP_IDENTIFIER, MEMORY_USAGE, IS_ACTIVE, DEVICE_ID, ENROLMENT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            for (Application application : applications) {
                stmt.setString(1, application.getName());
                stmt.setString(2, application.getPlatform());
                stmt.setString(3, application.getCategory());
                stmt.setString(4, application.getVersion());
                stmt.setString(5, application.getType());
                stmt.setString(6, application.getLocationUrl());
                stmt.setString(7, application.getImageUrl());
                stmt.setInt(8, tenantId);
                stmt.setString(9, application.getApplicationIdentifier());
                stmt.setInt(10, application.getMemoryUsage());
                stmt.setBoolean(11, application.isActive());
                stmt.setInt(12, deviceId);
                stmt.setInt(13, enrolmentId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding bulk application list", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateApplications(List<Application> applications, int deviceId, int enrolmentId,
                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("UPDATE DM_APPLICATION SET NAME = ?, PLATFORM = ?, CATEGORY = ?, " +
                    "VERSION = ?, TYPE = ?, LOCATION_URL = ?, IMAGE_URL = ?, MEMORY_USAGE = ?, IS_ACTIVE = ? " +
                    "WHERE APP_IDENTIFIER = ? AND DEVICE_ID = ? AND ENROLMENT_ID = ? AND TENANT_ID = ?");

            for (Application application : applications) {
                stmt.setString(1, application.getName());
                stmt.setString(2, application.getPlatform());
                stmt.setString(3, application.getCategory());
                stmt.setString(4, application.getVersion());
                stmt.setString(5, application.getType());
                stmt.setString(6, application.getLocationUrl());
                stmt.setString(7, application.getImageUrl());
                stmt.setInt(8, application.getMemoryUsage());
                stmt.setBoolean(9, application.isActive());
                stmt.setString(10, application.getApplicationIdentifier());
                stmt.setInt(11, deviceId);
                stmt.setInt(12, enrolmentId);
                stmt.setInt(13, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding bulk application list", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeApplications(List<Application> apps, int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("DELETE FROM DM_APPLICATION WHERE APP_IDENTIFIER = ? AND DEVICE_ID = ? " +
                    "AND ENROLMENT_ID = ? AND TENANT_ID = ?");

            for (Application app : apps) {
                stmt.setString(1, app.getApplicationIdentifier());
                stmt.setInt(2, deviceId);
                stmt.setInt(3, enrolmentId);
                stmt.setInt(4, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementDAOException("Error occurred while removing bulk application list", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Application getApplication(String identifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Application application = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT ID, NAME, APP_IDENTIFIER, PLATFORM, CATEGORY, VERSION, TYPE, " +
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, TENANT_ID FROM " +
                    "DM_APPLICATION WHERE APP_IDENTIFIER = ? AND TENANT_ID = ?");
            stmt.setString(1, identifier);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                application = this.loadApplication(rs);
            }
            return application;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving application application '" +
                    identifier + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Application getApplication(String identifier, String version, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Application application = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT ID, NAME, APP_IDENTIFIER, PLATFORM, CATEGORY, VERSION, TYPE, " +
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, TENANT_ID FROM " +
                    "DM_APPLICATION WHERE APP_IDENTIFIER = ? AND VERSION = ?  AND TENANT_ID = ?");
            stmt.setString(1, identifier);
            stmt.setString(2, version);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                application = this.loadApplication(rs);
            }
            return application;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving application application '" +
                    identifier + "' and version '" + version + "'.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Application getApplication(String identifier, String version, int deviceId,  int enrolmentId,  int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Application application = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT ID,  NAME, APP_IDENTIFIER, PLATFORM, CATEGORY, VERSION, TYPE, " +
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, TENANT_ID " +
                    "FROM DM_APPLICATION WHERE DEVICE_ID = ? AND ENROLMENT_ID = ? AND APP_IDENTIFIER = ? AND " +
                    "VERSION = ? AND TENANT_ID = ?");
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.setString(3, identifier);
            stmt.setString(4, version);
            stmt.setInt(5, tenantId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                application = this.loadApplication(rs);
            }
            return application;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving application application '" +
                    identifier + "' and version '" + version + "'.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    @Override
    public List<Application> getInstalledApplications(int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Application> applications = new ArrayList<>();
        Application application;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT ID, NAME, APP_IDENTIFIER, PLATFORM, CATEGORY, VERSION, TYPE, " +
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, TENANT_ID FROM DM_APPLICATION " +
                    "WHERE DEVICE_ID = ? AND ENROLMENT_ID = ? AND TENANT_ID = ?");

            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                application = loadApplication(rs);
                applications.add(application);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("SQL Error occurred while retrieving the list of Applications " +
                    "installed in device id '" + deviceId, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return applications;
    }

    @Override
    public List<Application> getApplications(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        List<Application> applications = new ArrayList<>();
        Application application;
        String sql = "Select " +
                        "ID," +
                        " NAME, " +
                        "APP_IDENTIFIER, " +
                        "PLATFORM, " +
                        "CATEGORY, " +
                        "VERSION, " +
                        "TYPE, " +
                        "LOCATION_URL, " +
                        "IMAGE_URL, " +
                        "APP_PROPERTIES, " +
                        "MEMORY_USAGE, " +
                        "IS_ACTIVE, " +
                        "TENANT_ID " +
                     "From DM_APPLICATION " +
                     "WHERE PLATFORM = ? " +
                     "AND TENANT_ID = ? LIMIT ? OFFSET ?";
        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, request.getDeviceType());
                stmt.setInt(2, tenantId);
                stmt.setInt(3, request.getRowCount());
                stmt.setInt(4, request.getStartIndex());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        application = loadApplication(rs);
                        applications.add(application);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "SQL Error occurred while retrieving the list of Applications " +
                    "installed in all enrolled devices for device type " + request.getDeviceType() +
                    " under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return applications;
    }

    @Override
    public List<String> getAppVersions(int tenantId, String packageName) throws DeviceManagementDAOException {
        String sql = "SELECT " +
                        "VERSION " +
                     "FROM DM_APPLICATION " +
                     "WHERE TENANT_ID=? " +
                     "AND APP_IDENTIFIER=?";
        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, packageName);

                try (ResultSet rs = stmt.executeQuery()) {
                    List<String> versions = new ArrayList<>();
                    while (rs.next()) {
                        versions.add(rs.getString("VERSION"));
                    }
                    return versions;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered apps under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private Application loadApplication(ResultSet rs) throws DeviceManagementDAOException {
        ByteArrayInputStream bais;
        ObjectInputStream ois;
        Properties properties;

        Application application = new Application();
        try {
            application.setId(rs.getInt("ID"));
            application.setName(rs.getString("NAME"));
            application.setType(rs.getString("TYPE"));

            if (rs.getBytes("APP_PROPERTIES") != null) {
                byte[] appProperties = rs.getBytes("APP_PROPERTIES");
                bais = new ByteArrayInputStream(appProperties);

                ois = new ObjectInputStream(bais);
                properties = (Properties) ois.readObject();
                application.setAppProperties(properties);
            }
            application.setCategory(rs.getString("CATEGORY"));
            application.setImageUrl(rs.getString("IMAGE_URL"));
            application.setLocationUrl(rs.getString("LOCATION_URL"));
            application.setPlatform(rs.getString("PLATFORM"));
            application.setVersion(rs.getString("VERSION"));
            application.setMemoryUsage(rs.getInt("MEMORY_USAGE"));
            application.setActive(rs.getBoolean("IS_ACTIVE"));
            application.setApplicationIdentifier(rs.getString("APP_IDENTIFIER"));

        } catch (IOException e) {
            throw new DeviceManagementDAOException("IO error occurred fetch at app properties", e);
        } catch (ClassNotFoundException e) {
            throw new DeviceManagementDAOException("Class not found error occurred fetch at app properties", e);
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("SQL error occurred fetch at application", e);
        }

        return application;
    }

}
