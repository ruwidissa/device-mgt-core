/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.core.dao.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.core.dao.CEAPolicyDAO;
import io.entgra.device.mgt.core.cea.mgt.core.dao.factory.CEAPolicyManagementDAOFactory;
import io.entgra.device.mgt.core.cea.mgt.core.dto.CEAPolicyContent;
import io.entgra.device.mgt.core.cea.mgt.core.dto.CEAPolicyDTO;
import io.entgra.device.mgt.core.cea.mgt.core.exception.CEAPolicyManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class AbstractCEAPolicyDAO implements CEAPolicyDAO {
    private static final Log log = LogFactory.getLog(AbstractCEAPolicyDAO.class);
    private static final Gson gson = new Gson();

    @Override
    public CEAPolicy createCEAPolicy(CEAPolicy ceaPolicy) throws CEAPolicyManagementDAOException {
        ceaPolicy.setCreated(new Date());
        ceaPolicy.setLastUpdated(new Date());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ceaPolicy.setTenantId(tenantId);
        CEAPolicyDTO ceaPolicyDTO = toCEAPolicyDTO(ceaPolicy);
        String query = "INSERT INTO DM_CEA_POLICIES " +
                "(POLICY_CONTENT, " +
                "CREATED_TIMESTAMP, " +
                "UPDATED_TIMESTAMP, " +
                "TENANT_ID) VALUES (?, ?, ?, ?)";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, ceaPolicyDTO.getPolicyContent());
            preparedStatement.setTimestamp(2, ceaPolicyDTO.getCreatedTimestamp());
            preparedStatement.setTimestamp(3, ceaPolicyDTO.getUpdatedTimestamp());
            preparedStatement.setInt(4, tenantId);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error occurred while creating CEA policy for tenant id : " + tenantId;
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
        return ceaPolicy;
    }

    @Override
    public CEAPolicy retrieveCEAPolicy() throws CEAPolicyManagementDAOException {
        CEAPolicy ceaPolicy = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = "SELECT POLICY_CONTENT, " +
                "CREATED_TIMESTAMP, " +
                "UPDATED_TIMESTAMP, " +
                "LAST_SYNCED_TIMESTAMP, " +
                "IS_SYNCED " +
                "FROM DM_CEA_POLICIES WHERE TENANT_ID = ?";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                CEAPolicyDTO ceaPolicyDTO;
                while (resultSet.next()) {
                    ceaPolicyDTO = new CEAPolicyDTO();
                    ceaPolicyDTO.setPolicyContent(resultSet.getString("POLICY_CONTENT"));
                    ceaPolicyDTO.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                    ceaPolicyDTO.setUpdatedTimestamp(resultSet.getTimestamp("UPDATED_TIMESTAMP"));
                    ceaPolicyDTO.setLastSyncedTimestamp(resultSet.getTimestamp("LAST_SYNCED_TIMESTAMP"));
                    ceaPolicyDTO.setSynced(resultSet.getBoolean("IS_SYNCED"));
                    ceaPolicyDTO.setTenantId(tenantId);
                    ceaPolicy = toCEAPolicy(ceaPolicyDTO);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving CEA policy for tenant id : " + tenantId;
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
        return ceaPolicy;
    }

    @Override
    public List<CEAPolicy> retrieveAllCEAPolicies() throws CEAPolicyManagementDAOException {
        List<CEAPolicy> ceaPolicies = new ArrayList<>();
        String query = "SELECT POLICY_CONTENT, " +
                "CREATED_TIMESTAMP, " +
                "UPDATED_TIMESTAMP, " +
                "LAST_SYNCED_TIMESTAMP, " +
                "IS_SYNCED, " +
                "TENANT_ID FROM DM_CEA_POLICIES";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                CEAPolicyDTO ceaPolicyDTO;
                while (resultSet.next()) {
                    ceaPolicyDTO = new CEAPolicyDTO();
                    ceaPolicyDTO.setPolicyContent(resultSet.getString("POLICY_CONTENT"));
                    ceaPolicyDTO.setCreatedTimestamp(resultSet.getTimestamp("CREATED_TIMESTAMP"));
                    ceaPolicyDTO.setUpdatedTimestamp(resultSet.getTimestamp("UPDATED_TIMESTAMP"));
                    ceaPolicyDTO.setLastSyncedTimestamp(resultSet.getTimestamp("LAST_SYNCED_TIMESTAMP"));
                    ceaPolicyDTO.setSynced(resultSet.getBoolean("IS_SYNCED"));
                    ceaPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));
                    ceaPolicies.add(toCEAPolicy(ceaPolicyDTO));
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving All CEA policies";
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
        return ceaPolicies;
    }

    @Override
    public CEAPolicy updateCEAPolicy(CEAPolicy existingCEAPolicy, CEAPolicy ceaPolicy) throws CEAPolicyManagementDAOException {
        ceaPolicy.setCreated(existingCEAPolicy.getCreated());
        ceaPolicy.setSynced(existingCEAPolicy.isSynced());
        ceaPolicy.setLastSynced(existingCEAPolicy.getLastSynced());
        ceaPolicy.setLastUpdated(new Date());
        CEAPolicyDTO ceaPolicyDTO = toCEAPolicyDTO(ceaPolicy);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = "UPDATE DM_CEA_POLICIES " +
                "SET POLICY_CONTENT = ?, " +
                "UPDATED_TIMESTAMP = ? " +
                "WHERE TENANT_ID = ?";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, ceaPolicyDTO.getPolicyContent());
            preparedStatement.setTimestamp(2, ceaPolicyDTO.getUpdatedTimestamp());
            preparedStatement.setInt(3, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while updating CEA policy for tenant id : " + tenantId;
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
        return ceaPolicy;
    }

    @Override
    public void updateLastSyncedTime(boolean status, Date syncedTime) throws CEAPolicyManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = "UPDATE DM_CEA_POLICIES " +
                "SET LAST_SYNCED_TIMESTAMP = ?, " +
                "IS_SYNCED = ? " +
                "WHERE TENANT_ID = ?";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setTimestamp(1, new Timestamp(syncedTime.getTime()));
            preparedStatement.setBoolean(2, status);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while updating CEA policy last sync timestamp for tenant id : " + tenantId;
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteCEAPolicy() throws CEAPolicyManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = "DELETE FROM DM_CEA_POLICIES WHERE TENANT_ID = ?";
        Connection connection = CEAPolicyManagementDAOFactory.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.execute();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting CEA policy for tenant id : " + tenantId;
            log.error(msg, e);
            throw new CEAPolicyManagementDAOException(msg, e);
        }
    }

    private CEAPolicyDTO toCEAPolicyDTO(CEAPolicy ceaPolicy) throws CEAPolicyManagementDAOException {
        if (ceaPolicy == null) {
            throw new CEAPolicyManagementDAOException("CEAPolicy can't be null");
        }
        CEAPolicyDTO ceaPolicyDTO = new CEAPolicyDTO();
        CEAPolicyContent ceaPolicyContent = new CEAPolicyContent();
        ActiveSyncServer activeSyncServer = new ActiveSyncServer();
        activeSyncServer.setSecret(Base64.getEncoder().
                encodeToString(ceaPolicy.getActiveSyncServer().getSecret().getBytes(StandardCharsets.UTF_8)));
        activeSyncServer.setClient(ceaPolicy.getActiveSyncServer().getClient());
        activeSyncServer.setKey(ceaPolicy.getActiveSyncServer().getKey());
        activeSyncServer.setGatewayUrl(ceaPolicy.getActiveSyncServer().getGatewayUrl());
        ceaPolicyContent.setAccessPolicy(ceaPolicy.getAccessPolicy());
        ceaPolicyContent.setGracePeriod(ceaPolicy.getGracePeriod());
        ceaPolicyContent.setActiveSyncServer(activeSyncServer);
        ceaPolicyDTO.setPolicyContent(gson.toJson(ceaPolicyContent));
        ceaPolicyDTO.setSynced(ceaPolicy.isSynced());
        ceaPolicyDTO.setCreatedTimestamp(new Timestamp(ceaPolicy.getCreated().getTime()));
        ceaPolicyDTO.setUpdatedTimestamp(new Timestamp(ceaPolicy.getLastUpdated().getTime()));
        ceaPolicyDTO.setTenantId(ceaPolicy.getTenantId());
        if (ceaPolicy.getLastSynced() != null) {
            ceaPolicyDTO.setLastSyncedTimestamp(new Timestamp(ceaPolicy.getLastSynced().getTime()));
        }
        return ceaPolicyDTO;
    }

    private CEAPolicy toCEAPolicy(CEAPolicyDTO ceaPolicyDTO) throws CEAPolicyManagementDAOException{
        if (ceaPolicyDTO == null) {
            throw new CEAPolicyManagementDAOException("CEAPolicyDTO can't be null");
        }
        CEAPolicy ceaPolicy = new CEAPolicy();
        CEAPolicyContent ceaPolicyContent = gson.fromJson(ceaPolicyDTO.getPolicyContent(), CEAPolicyContent.class);
        ActiveSyncServer activeSyncServer = ceaPolicyContent.getActiveSyncServer();
        activeSyncServer.setSecret(new String(Base64.getDecoder().decode(activeSyncServer.getSecret())));
        ceaPolicy.setActiveSyncServer(activeSyncServer);
        ceaPolicy.setAccessPolicy(ceaPolicyContent.getAccessPolicy());
        ceaPolicy.setGracePeriod(ceaPolicyContent.getGracePeriod());
        ceaPolicy.setLastUpdated(new Date(ceaPolicyDTO.getUpdatedTimestamp().getTime()));
        ceaPolicy.setSynced(ceaPolicyDTO.isSynced());
        ceaPolicy.setTenantId(ceaPolicyDTO.getTenantId());
        if (ceaPolicyDTO.getLastSyncedTimestamp() != null) {
            ceaPolicy.setLastSynced(new Date(ceaPolicyDTO.getLastSyncedTimestamp().getTime()));
        }
        ceaPolicy.setCreated(new Date(ceaPolicyDTO.getCreatedTimestamp().getTime()));
        return ceaPolicy;
    }
}
