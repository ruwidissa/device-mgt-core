/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.metadata.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.util.MetadataDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implementation of MetadataDAO which includes the methods to do CRUD operations on metadata.
 */
public abstract class AbstractMetadataDAOImpl implements MetadataDAO {

    private static final Log log = LogFactory.getLog(AbstractMetadataDAOImpl.class);

    @Override
    public int addMetadata(int tenantId, Metadata metadata) throws MetadataManagementDAOException {
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_METADATA (DATA_TYPE, METADATA_KEY, METADATA_VALUE, TENANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, metadata.getDataType().toString());
                stmt.setString(2, metadata.getMetaKey());
                stmt.setString(3, metadata.getMetaValue());
                stmt.setInt(4, tenantId);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the " + "Metadata for metadataKey : " + metadata.getMetaKey();
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
    }

    @Override
    public Metadata getMetadata(int tenantId, String metaKey) throws MetadataManagementDAOException {
        Metadata metadata = null;
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String sql = "SELECT DATA_TYPE, METADATA_KEY, METADATA_VALUE " +
                    "FROM DM_METADATA " +
                    "WHERE TENANT_ID = ? " +
                    "AND METADATA_KEY = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, metaKey);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        metadata = MetadataDAOUtil.getMetadata(rs);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving data for metadataKey : " + metaKey;
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
        return metadata;
    }

    @Override
    public boolean updateMetadata(int tenantId, Metadata metadata) throws MetadataManagementDAOException {
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_METADATA " +
                    "SET DATA_TYPE = ?, METADATA_VALUE = ? " +
                    "WHERE METADATA_KEY = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, metadata.getDataType().toString());
                stmt.setString(2, metadata.getMetaValue());
                stmt.setString(3, metadata.getMetaKey());
                stmt.setInt(4, tenantId);
                int rows = stmt.executeUpdate();
                return rows == 1;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating data of metadataKey:" + metadata.getMetaKey();
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean deleteMetadata(int tenantId, String key) throws MetadataManagementDAOException {
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String query = "DELETE FROM DM_METADATA WHERE METADATA_KEY = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, key);
                stmt.setInt(2, tenantId);
                int rows = stmt.executeUpdate();
                return rows == 1;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting metadata for specified metadataKey:" + key;
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
    }

    @Override
    public int getMetadataCount(int tenantId) throws MetadataManagementDAOException {
        int metadataCount = 0;
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String sql =
                    "SELECT COUNT(*) AS METADATA_COUNT FROM DM_METADATA WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        metadataCount = rs.getInt("METADATA_COUNT");
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while counting metadata";
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
        return metadataCount;
    }

    @Override
    public boolean isExist(int tenantId, String metaKey) throws MetadataManagementDAOException {
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(*) AS COUNT FROM DM_METADATA " +
                    "WHERE TENANT_ID = ? " +
                    "AND METADATA_KEY = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, metaKey);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) != 0;
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while checking the existence of Metadata entry for metadataKey:" + metaKey;
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
        return false;
    }

}
