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
package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationFilter;
import io.entgra.device.mgt.core.device.mgt.core.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;
import io.entgra.device.mgt.core.device.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
                    "APP_IDENTIFIER, MEMORY_USAGE, IS_ACTIVE, DEVICE_ID, ENROLMENT_ID, IS_SYSTEM_APP) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
                stmt.setInt(14, application.isSystemApp());
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
                    "VERSION = ?, TYPE = ?, LOCATION_URL = ?, IMAGE_URL = ?, MEMORY_USAGE = ?, IS_ACTIVE = ?, IS_SYSTEM_APP = ? " +
                    "WHERE ID = ?");

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
                stmt.setInt(10, application.isSystemApp());
                stmt.setInt(11, application.getId());
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
            stmt = conn.prepareStatement("DELETE FROM DM_APPLICATION WHERE ID = ?");

            for (Application app : apps) {
                stmt.setInt(1, app.getId());
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
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, IS_SYSTEM_APP, TENANT_ID " +
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
                    "LOCATION_URL, IMAGE_URL, APP_PROPERTIES, MEMORY_USAGE, IS_ACTIVE, IS_SYSTEM_APP, TENANT_ID FROM DM_APPLICATION " +
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
        String sql = "SELECT " +
                        "ID, " +
                        "NAME, " +
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
                        "IS_SYSTEM_APP, " +
                        "TENANT_ID " +
                    "FROM DM_APPLICATION " +
                    "WHERE PLATFORM = ? AND " +
                    "TENANT_ID = ? AND " +
                    "NOT EXISTS (SELECT ID " +
                    "FROM DM_APPLICATION A " +
                    "WHERE A.NAME = DM_APPLICATION.NAME " +
                    "AND A.ID < DM_APPLICATION.ID AND " +
                    "PLATFORM = ? AND TENANT_ID = ?) ";

        try {
            ApplicationFilter applicationFilter = new Gson().fromJson(request.getFilter(), ApplicationFilter.class);
            boolean isAppNameFilterProvided = false;
            if (null != applicationFilter.getAppName()) {
                sql = sql + "AND NAME LIKE ? ";
                applicationFilter.setAppName(Constants.QUERY_WILDCARD.concat(applicationFilter.getAppName())
                        .concat(Constants.QUERY_WILDCARD));
                isAppNameFilterProvided = true;
            }

            boolean isPackageFilterProvided = false;
            if (null != applicationFilter.getPackageName()) {
                sql = sql + "AND APP_IDENTIFIER LIKE ? ";
                applicationFilter.setPackageName(Constants.QUERY_WILDCARD.concat(applicationFilter.getPackageName())
                        .concat(Constants.QUERY_WILDCARD));
                isPackageFilterProvided = true;
            }

            boolean isLimitPresent = false;
            if (request != null && request.getRowCount() != -1) {
                sql = sql + "LIMIT ? OFFSET ?";
                isLimitPresent = true;
            }
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setString(paramIdx++, request.getDeviceType());
                stmt.setInt(paramIdx++, tenantId);
                stmt.setString(paramIdx++, request.getDeviceType());
                stmt.setInt(paramIdx++, tenantId);
                if (isAppNameFilterProvided){
                    stmt.setString(paramIdx++, applicationFilter.getAppName());
                }
                if (isPackageFilterProvided){
                    stmt.setString(paramIdx++, applicationFilter.getPackageName());
                }
                if (isLimitPresent) {
                    stmt.setInt(paramIdx++, request.getRowCount());
                    stmt.setInt(paramIdx, request.getStartIndex());
                }
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
        String sql = "SELECT DISTINCT " +
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
            application.setSystemApp(rs.getInt("IS_SYSTEM_APP"));
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

    @Override
    public void saveApplicationIcon(String iconPath, String packageName, String version, int tenantId)
            throws DeviceManagementDAOException{
        Connection conn;
        String sql = "INSERT INTO DM_APP_ICONS " +
                "(ICON_PATH, " +
                "PACKAGE_NAME, " +
                "VERSION, " +
                "CREATED_TIMESTAMP, " +
                "TENANT_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1,iconPath);
                stmt.setString(2,packageName);
                stmt.setString(3,version);
                stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
                stmt.setInt(5, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while saving application icon details";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getApplicationPackageCount(String packageName) throws DeviceManagementDAOException{
        Connection conn;
        String sql = "SELECT " +
                "COUNT(*) AS APP_PACKAGE_COUNT " +
                "FROM DM_APP_ICONS " +
                "WHERE PACKAGE_NAME = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("APP_PACKAGE_COUNT");
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting application icon details";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateApplicationIcon(String iconPath, String oldPackageName, String newPackageName, String version)
            throws DeviceManagementDAOException{
        Connection conn;
        String sql = "UPDATE DM_APP_ICONS " +
                "SET " +
                "ICON_PATH= ?, " +
                "PACKAGE_NAME = ?, " +
                "VERSION = ? " +
                "WHERE PACKAGE_NAME = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1,iconPath);
                stmt.setString(2,newPackageName);
                stmt.setString(3,version);
                stmt.setString(4,oldPackageName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating application icon details";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplicationIcon(String packageName) throws DeviceManagementDAOException {
        Connection conn;
        String sql = "DELETE " +
                "FROM DM_APP_ICONS " +
                "WHERE PACKAGE_NAME = ?";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting application icon details";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public String getIconPath(String applicationIdentifier) throws DeviceManagementDAOException{
        Connection conn;
        String sql = "SELECT " +
                "ICON_PATH " +
                "FROM DM_APP_ICONS " +
                "WHERE PACKAGE_NAME = ?";
        String iconPath = null;
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1,applicationIdentifier);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        iconPath = rs.getString("ICON_PATH");
                    }
                    return iconPath;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving app icon path of the application";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Application> getInstalledApplicationListOnDevice(int deviceId, int enrolmentId, int offset, int limit, int tenantId, int isSystemApp)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Application> applicationList = new ArrayList<>();
        Application application;
        String sql = "SELECT " +
                "ID, " +
                "NAME, " +
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
                "IS_SYSTEM_APP, " +
                "TENANT_ID " +
                "FROM DM_APPLICATION " +
                "WHERE DEVICE_ID = ? AND " +
                "ENROLMENT_ID = ? AND " +
                "TENANT_ID = ? " +
                (isSystemApp != 0 ? "AND IS_SYSTEM_APP = ? " : "") +
                "LIMIT ? " +
                "OFFSET ? ";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                stmt.setInt(paramIndex++, deviceId);
                stmt.setInt(paramIndex++, enrolmentId);
                stmt.setInt(paramIndex++, tenantId);
                if (isSystemApp != 0) {
                    stmt.setInt(paramIndex++, isSystemApp);
                }
                stmt.setInt(paramIndex++, limit);
                stmt.setInt(paramIndex, offset);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        application = loadApplication(rs);
                        applicationList.add(application);
                    }
                }
            }

        } catch (SQLException e) {
            String msg = "SQL Error occurred while retrieving the list of Applications " +
                    "installed on device id '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return applicationList;
    }

    public List<Application> getInstalledApplicationListOnDevice(int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Application> applicationList = new ArrayList<>();
        Application application;
        String sql = "SELECT " +
                "ID, " +
                "NAME, " +
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
                "IS_SYSTEM_APP, " +
                "TENANT_ID " +
                "FROM DM_APPLICATION " +
                "WHERE DEVICE_ID = ? AND " +
                "ENROLMENT_ID = ? AND " +
                "TENANT_ID = ? ";
        try {
            conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, enrolmentId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        application = loadApplication(rs);
                        applicationList.add(application);
                    }
                }
            }

        } catch (SQLException e) {
            String msg = "SQL Error occurred while retrieving the list of Applications " +
                    "installed in device id '" + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return applicationList;
    }
}
