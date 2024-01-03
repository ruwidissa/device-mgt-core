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

package io.entgra.device.mgt.core.application.mgt.core.dao.impl.vpp;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssetDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssociationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.dao.VppApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.device.mgt.core.application.mgt.core.util.DAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.List;

public class GenericVppApplicationDAOImpl  extends AbstractDAOImpl implements VppApplicationDAO {
    private static final Log log = LogFactory.getLog(GenericVppApplicationDAOImpl.class);

    @Override
    public int addVppUser(VppUserDTO userDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int vppUserId = -1;
        String sql = "INSERT INTO "
                + "AP_VPP_USER("
                + "CLIENT_USER_ID, "
                + "DM_USERNAME, "
                + "TENANT_ID, "
                + "EMAIL, "
                + "INVITE_CODE, "
                + "STATUS,"
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "MANAGED_ID,"
                + "TEMP_PASSWORD) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                long currentTime = DeviceManagementDAOUtil.getCurrentUTCTime();
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, tenantId);
                stmt.setString(4, userDTO.getEmail());
                stmt.setString(5, userDTO.getInviteCode());
                stmt.setString(6, userDTO.getStatus());
                stmt.setLong(7, currentTime);
                stmt.setLong(8, currentTime);
                stmt.setString(9, userDTO.getManagedId());
                stmt.setString(10, userDTO.getTmpPassword());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        vppUserId = rs.getInt(1);
                    }
                }
                return vppUserId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add  the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppUserDTO updateVppUser(VppUserDTO userDTO, int tenantId)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE AP_VPP_USER SET ";
        if (userDTO.getClientUserId() != null && !userDTO.getClientUserId().isEmpty()) {
            sql += "CLIENT_USER_ID = ?,";
        }
        if (userDTO.getDmUsername() != null && !userDTO.getDmUsername().isEmpty()) {
            sql += "DM_USERNAME = ?,";
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            sql += "EMAIL = ?,";
        }
        if (userDTO.getInviteCode() != null && !userDTO.getInviteCode().isEmpty()) {
            sql += "INVITE_CODE = ?,";
        }
        if (userDTO.getStatus() != null && !userDTO.getStatus().isEmpty()) {
            sql += "STATUS = ?,";
        }
        if (userDTO.getManagedId() != null && !userDTO.getManagedId().isEmpty()) {
            sql += "MANAGED_ID = ?,";
        }
        if (userDTO.getTmpPassword() != null && !userDTO.getTmpPassword().isEmpty()) {
            sql += "TEMP_PASSWORD = ?,";
        }

        sql += " TENANT_ID = ?, LAST_UPDATED_TIME = ? WHERE ID = ?";

        try {
            Connection conn = this.getDBConnection();
            long updatedTime = DeviceManagementDAOUtil.getCurrentUTCTime();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int x = 0;

                if (userDTO.getClientUserId() != null && !userDTO.getClientUserId().isEmpty()) {
                    stmt.setString(++x, userDTO.getClientUserId());
                }
                if (userDTO.getDmUsername() != null && !userDTO.getDmUsername().isEmpty()) {
                    stmt.setString(++x, userDTO.getDmUsername());
                }
                if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
                    stmt.setString(++x, userDTO.getEmail());
                }
                if (userDTO.getInviteCode() != null && !userDTO.getInviteCode().isEmpty()) {
                    stmt.setString(++x, userDTO.getInviteCode());
                }
                if (userDTO.getStatus() != null && !userDTO.getStatus().isEmpty()) {
                    stmt.setString(++x, userDTO.getStatus());
                }
                if (userDTO.getManagedId() != null && !userDTO.getManagedId().isEmpty()) {
                    stmt.setString(++x, userDTO.getManagedId());
                }
                if (userDTO.getTmpPassword() != null && !userDTO.getTmpPassword().isEmpty()) {
                    stmt.setString(++x, userDTO.getTmpPassword());
                }
                stmt.setInt(++x, tenantId);
                stmt.setLong(++x, updatedTime);
                stmt.setInt(++x, userDTO.getId());
                if (stmt.executeUpdate() == 1) {
                    return userDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when updating the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to updating the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppUserDTO getUserByDMUsername(String emmUsername, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "ID, "
                + "CLIENT_USER_ID, "
                + "TENANT_ID, "
                + "EMAIL, "
                + "INVITE_CODE, "
                + "STATUS, "
                + "CREATED_TIME, "
                + "LAST_UPDATED_TIME, "
                + "MANAGED_ID, "
                + "TEMP_PASSWORD, "
                + "DM_USERNAME "
                + "FROM AP_VPP_USER "
                + "WHERE DM_USERNAME = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, emmUsername);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadVppUser(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when retrieving vpp user by EMM Username.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve vpp user by EMM Username.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one user for: " + emmUsername;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssetDTO getAssetByAppId(int appId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "ID, "
                + "APP_ID, "
                + "TENANT_ID, "
                + "CREATED_TIME, "
                + "LAST_UPDATED_TIME, "
                + "ADAM_ID, "
                + "ASSIGNED_COUNT, "
                + "DEVICE_ASSIGNABLE, "
                + "PRICING_PARAMS, "
                + "PRODUCT_TYPE, "
                + "RETIRED_COUNT, "
                + "REVOCABLE "
//                + "SUPPORTED_PLATFORMS "
                + "FROM AP_ASSETS "
                + "WHERE APP_ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadAsset(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when retrieving asset data of app id "+ appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve asset by app id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }  catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one app for app id: " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int addAsset(VppAssetDTO vppAssetDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int assetId = -1;
        String sql = "INSERT INTO "
                + "AP_ASSETS("
                + "APP_ID, "
                + "TENANT_ID, "
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "ADAM_ID,"
                + "ASSIGNED_COUNT,"
                + "DEVICE_ASSIGNABLE,"
                + "PRICING_PARAMS,"
                + "PRODUCT_TYPE,"
                + "RETIRED_COUNT,"
                + "REVOCABLE, "
                + "SUPPORTED_PLATFORMS) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                long currentTime = System.currentTimeMillis();
                stmt.setInt(1, vppAssetDTO.getAppId());
                stmt.setInt(2, tenantId);
                stmt.setLong(3, currentTime);
                stmt.setLong(4, currentTime);
                stmt.setString(5, vppAssetDTO.getAdamId());
                stmt.setString(6, vppAssetDTO.getAssignedCount());
                stmt.setString(7, vppAssetDTO.getDeviceAssignable());
                stmt.setString(8, vppAssetDTO.getPricingParam());
                stmt.setString(9, vppAssetDTO.getProductType());
                stmt.setString(10, vppAssetDTO.getRetiredCount());
                stmt.setString(11, vppAssetDTO.getRevocable());
                List<String> platformList =  vppAssetDTO.getSupportedPlatforms();
                String platformString = String.join(",", platformList);
                stmt.setString(12, platformString);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        assetId = rs.getInt(1);
                    }
                }
                return assetId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssetDTO updateAsset(VppAssetDTO vppAssetDTO, int tenantId)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE AP_ASSETS SET ";

        if (vppAssetDTO.getAdamId() != null && !vppAssetDTO.getAdamId().isEmpty()) {
            sql += "ADAM_ID = ?, ";
        }
        if (vppAssetDTO.getAssignedCount() != null && !vppAssetDTO.getAssignedCount().isEmpty()) {
            sql += "ASSIGNED_COUNT = ?, ";
        }
        if (vppAssetDTO.getDeviceAssignable() != null && !vppAssetDTO.getDeviceAssignable().isEmpty()) {
            sql += "DEVICE_ASSIGNABLE = ?, ";
        }
        if (vppAssetDTO.getPricingParam() != null && !vppAssetDTO.getPricingParam().isEmpty()) {
            sql += "PRICING_PARAMS = ?, ";
        }
        if (vppAssetDTO.getProductType() != null && !vppAssetDTO.getProductType().isEmpty()) {
            sql += "PRODUCT_TYPE = ?, ";
        }
        if (vppAssetDTO.getRetiredCount() != null && !vppAssetDTO.getRetiredCount().isEmpty()) {
            sql += "RETIRED_COUNT = ?, ";
        }
        if (vppAssetDTO.getRevocable() != null && !vppAssetDTO.getRevocable().isEmpty()) {
            sql += "REVOCABLE = ?, ";
        }
        if (vppAssetDTO.getSupportedPlatforms() != null && !vppAssetDTO.getSupportedPlatforms().isEmpty()) {
            sql += "SUPPORTED_PLATFORMS = ?,";
        }
        sql += "APP_ID = ?, LAST_UPDATED_TIME = ? WHERE ID = ? AND TENANT_ID = ?";

        try {
            Connection conn = this.getDBConnection();
            long updatedTime = System.currentTimeMillis();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int x = 0;

                if (vppAssetDTO.getAdamId() != null && !vppAssetDTO.getAdamId().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getAdamId());
                }
                if (vppAssetDTO.getAssignedCount() != null && !vppAssetDTO.getAssignedCount().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getAssignedCount());
                }
                if (vppAssetDTO.getDeviceAssignable() != null && !vppAssetDTO.getDeviceAssignable().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getDeviceAssignable());
                }
                if (vppAssetDTO.getPricingParam() != null && !vppAssetDTO.getPricingParam().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getPricingParam());
                }
                if (vppAssetDTO.getProductType() != null && !vppAssetDTO.getProductType().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getProductType());
                }
                if (vppAssetDTO.getRetiredCount() != null && !vppAssetDTO.getRetiredCount().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getRetiredCount());
                }
                if (vppAssetDTO.getRevocable() != null && !vppAssetDTO.getRevocable().isEmpty()) {
                    stmt.setString(++x, vppAssetDTO.getRevocable());
                }
                if (vppAssetDTO.getSupportedPlatforms() != null && !vppAssetDTO.getSupportedPlatforms().isEmpty()) {
                    List<String> platformList =  vppAssetDTO.getSupportedPlatforms();
                    String platformString = String.join(",", platformList);
                    stmt.setString(++x, platformString);
                }

                stmt.setInt(++x, vppAssetDTO.getAppId());
                stmt.setLong(++x, updatedTime);
                stmt.setInt(++x, vppAssetDTO.getId());
                stmt.setLong(++x, tenantId);
                if (stmt.executeUpdate() == 1) {
                    return vppAssetDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when updating the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to updating the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssociationDTO getAssociation(int assetId, int userId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "ID, "
                + "ASSOCIATION_TYPE, "
                + "CREATED_TIME, "
                + "LAST_UPDATED_TIME, "
                + "PRICING_PARAMS "
                + "FROM AP_VPP_ASSOCIATION "
                + "WHERE ASSET_ID = ? AND USER_ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, assetId);
                stmt.setInt(2, userId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadAssignment(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when retrieving assignment data of user with id "+ userId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve assignment by asset id and user id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }  catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one assignment for user id: " + userId + " and asset id: " + assetId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int addAssociation(VppAssociationDTO vppAssociationDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int associationId = -1;
        String sql = "INSERT INTO "
                + "AP_VPP_ASSOCIATION("
                + "ASSET_ID, "
                + "USER_ID, "
                + "TENANT_ID, "
                + "ASSOCIATION_TYPE,"
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "PRICING_PARAMS) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                long currentTime = System.currentTimeMillis();
                stmt.setInt(1, vppAssociationDTO.getAssetId());
                stmt.setInt(2, vppAssociationDTO.getClientId());
                stmt.setInt(3, tenantId);
                stmt.setString(4, vppAssociationDTO.getAssociationType());
                stmt.setLong(5, currentTime);
                stmt.setLong(6, currentTime);
                stmt.setString(7, vppAssociationDTO.getPricingParam());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        associationId = rs.getInt(1);
                    }
                }
                return associationId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add the asset.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public VppAssociationDTO updateAssociation(VppAssociationDTO vppAssociationDTO, int tenantId)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE "
                + "AP_VPP_ASSOCIATION "
                + "SET "
                + "ASSET_ID = ?,"
                + "USER_ID = ?, "
                + "ASSOCIATION_TYPE = ?, "
                + "LAST_UPDATED_TIME = ?, "
                + "PRICING_PARAMS = ? "
                + "WHERE ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            long updatedTime = System.currentTimeMillis();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vppAssociationDTO.getAssetId());
                stmt.setInt(2, vppAssociationDTO.getClientId());
                stmt.setString(3, vppAssociationDTO.getAssociationType());
                stmt.setLong(4, updatedTime);
                stmt.setString(5, vppAssociationDTO.getPricingParam());
                stmt.setInt(6, vppAssociationDTO.getId());
                stmt.setLong(7, tenantId);
                if (stmt.executeUpdate() == 1) {
                    return vppAssociationDTO;
                }
                return null;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when updating the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to updating the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
