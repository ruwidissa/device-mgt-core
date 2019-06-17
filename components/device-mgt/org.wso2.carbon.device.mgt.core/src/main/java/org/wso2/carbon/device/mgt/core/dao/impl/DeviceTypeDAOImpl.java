/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.dao.impl;

import com.google.gson.Gson;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeVersion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceTypeDAOImpl implements DeviceTypeDAO {

	@Override
	public void addDeviceType(DeviceType deviceType, int providerTenantId, boolean isSharedWithAllTenants)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO DM_DEVICE_TYPE (NAME,PROVIDER_TENANT_ID,SHARED_WITH_ALL_TENANTS,DEVICE_TYPE_META" +
                            ",LAST_UPDATED_TIMESTAMP) VALUES (?,?,?,?,?)");
			stmt.setString(1, deviceType.getName());
			stmt.setInt(2, providerTenantId);
			stmt.setBoolean(3, isSharedWithAllTenants);
            String deviceMeta = null;
            if (deviceType.getDeviceTypeMetaDefinition() != null) {
                Gson gson = new Gson();
                deviceMeta = gson.toJson(deviceType.getDeviceTypeMetaDefinition());
            }
            stmt.setString(4, deviceMeta);
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
			stmt.execute();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while registering the device type '" + deviceType.getName() + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public void updateDeviceType(DeviceType deviceType, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("UPDATE DM_DEVICE_TYPE SET DEVICE_TYPE_META = ?, LAST_UPDATED_TIMESTAMP = ? " +
                                                 "WHERE NAME = ? AND PROVIDER_TENANT_ID = ?");
            String deviceMeta = null;
            if (deviceType.getDeviceTypeMetaDefinition() != null) {
                Gson gson = new Gson();
                deviceMeta = gson.toJson(deviceType.getDeviceTypeMetaDefinition());
            }
            stmt.setString(1, deviceMeta);
            stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            stmt.setString(3, deviceType.getName());
            stmt.setInt(4, tenantId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating device type'" +
                                                           deviceType.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
	}

	@Override
	public List<DeviceType> getDeviceTypes(int tenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE, DEVICE_TYPE_META,LAST_UPDATED_TIMESTAMP " +
                            "FROM DM_DEVICE_TYPE where PROVIDER_TENANT_ID =? OR SHARED_WITH_ALL_TENANTS = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setBoolean(2, true);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceType deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }
				deviceTypes.add(deviceType);
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<DeviceType> getDeviceTypesByProvider(int tenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT NAME AS DEVICE_TYPE, DEVICE_TYPE_META FROM DM_DEVICE_TYPE where PROVIDER_TENANT_ID =?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			rs = stmt.executeQuery();

			while (rs.next()) {
                DeviceType deviceType = new DeviceType();
                deviceType.setName(rs.getString("DEVICE_TYPE"));
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }
				deviceTypes.add(deviceType);

			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<String> getSharedDeviceTypes() throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE where  " +
							"SHARED_WITH_ALL_TENANTS = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, true);
			rs = stmt.executeQuery();

			while (rs.next()) {
				deviceTypes.add(rs.getString("DEVICE_TYPE"));
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean isDeviceTypeVersionModifiable(int deviceTypeID, String versionName, int tenantId) throws
			DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql =
					"SELECT dt.ID as DEVICE_TYPE_IDENTIFIER, dt.NAME as DEVICE_TYPE_NAME " +
							"FROM DM_DEVICE_TYPE_PLATFORM dv,  DM_DEVICE_TYPE dt " +
							"WHERE dt.ID = dv.DEVICE_TYPE_ID AND  dv.DEVICE_TYPE_ID = ? AND dv.VERSION_NAME = ?" +
							" AND dt.PROVIDER_TENANT_ID = ? AND dt.SHARED_WITH_ALL_TENANTS = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, deviceTypeID);
			stmt.setString(2, versionName);
			stmt.setInt(3, tenantId);
			stmt.setBoolean(4, false);

			rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public DeviceType getDeviceType(int id) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT ID AS DEVICE_TYPE_ID, DEVICE_TYPE_META, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE WHERE ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			DeviceType deviceType = null;
			while (rs.next()) {
				deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }

			}
			return deviceType;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetching the registered device type", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public DeviceType getDeviceType(String type, int tenantId) throws
															   DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DeviceType deviceType = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT ID AS DEVICE_TYPE_ID, DEVICE_TYPE_META FROM DM_DEVICE_TYPE WHERE (PROVIDER_TENANT_ID =? OR " +
							"SHARED_WITH_ALL_TENANTS = ?) AND NAME =?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setBoolean(2, true);
			stmt.setString(3, type);
			rs = stmt.executeQuery();
			if (rs.next()) {
				deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(type);
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }
			}
			return deviceType;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetch device type id for device type '" + type + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public void removeDeviceType(String type, int tenantId) throws DeviceManagementDAOException {

	}

	@Override
	public boolean addDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO DM_DEVICE_TYPE_PLATFORM (DEVICE_TYPE_ID, VERSION_NAME) VALUES (?,?)";
			if (deviceTypeVersion.getVersionStatus() != null) {
				sql = "INSERT INTO DM_DEVICE_TYPE_PLATFORM (DEVICE_TYPE_ID, VERSION_NAME, VERSION_STATUS) " +
						"VALUES (?,?,?)";
			}
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, deviceTypeVersion.getDeviceTypeId());
			stmt.setString(2, deviceTypeVersion.getVersionName());
			if (deviceTypeVersion.getVersionStatus() != null) {
				stmt.setString(3, deviceTypeVersion.getVersionStatus());
			}
			return stmt.execute();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while adding the version: " + deviceTypeVersion.getVersionName()
							+ " to device type: " + deviceTypeVersion.getDeviceTypeId(), e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public List<DeviceTypeVersion> getDeviceTypeVersions(int deviceTypeId, String typeName)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceTypeVersion> deviceTypesVersions = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM DM_DEVICE_TYPE_PLATFORM where DEVICE_TYPE_ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, deviceTypeId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceTypeVersion deviceTypeVersion = new DeviceTypeVersion();
				deviceTypeVersion.setId(rs.getInt("ID"));
				deviceTypeVersion.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
				deviceTypeVersion.setDeviceTypeName(typeName); // Adding this for the sake of completeness of DTO
				deviceTypeVersion.setVersionName(rs.getString("VERSION_NAME"));
				deviceTypeVersion.setVersionStatus(rs.getString("VERSION_STATUS"));
				deviceTypesVersions.add(deviceTypeVersion);
			}
			return deviceTypesVersions;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching device type versions for device " +
					"type: " + deviceTypeId, e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String sql = "UPDATE DM_DEVICE_TYPE_PLATFORM SET " +
					" VERSION_STATUS = ? WHERE DEVICE_TYPE_ID = ? AND VERSION_NAME = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, deviceTypeVersion.getVersionStatus());
			stmt.setInt(2, deviceTypeVersion.getDeviceTypeId());
			stmt.setString(3, deviceTypeVersion.getVersionName());
			return stmt.execute();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while updating details of the version: " + deviceTypeVersion.getVersionName() +
							" and device type: " + deviceTypeVersion.getDeviceTypeId(), e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public DeviceTypeVersion getDeviceTypeVersion(int deviceTypeId, String version)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DeviceTypeVersion deviceTypeVersion = null;
		try {
			conn = this.getConnection();
			String sql =
					"SELECT * FROM DM_DEVICE_TYPE_PLATFORM WHERE DEVICE_TYPE_ID = ? AND VERSION_NAME = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, deviceTypeId);
			stmt.setString(2, version);
			rs = stmt.executeQuery();

			while (rs.next()) {
				deviceTypeVersion = new DeviceTypeVersion();
				deviceTypeVersion.setId(rs.getInt("ID"));
				deviceTypeVersion.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
				deviceTypeVersion.setVersionName(rs.getString("VERSION_NAME"));
				deviceTypeVersion.setVersionStatus(rs.getString("VERSION_STATUS"));
			}
			return deviceTypeVersion;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching device type version for device " +
					"type: " + deviceTypeId + ", and version " + version, e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}

	}

	private Connection getConnection() throws SQLException {
		return DeviceManagementDAOFactory.getConnection();
	}

}
