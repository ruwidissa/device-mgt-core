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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.impl;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAO;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.AdditionResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNode;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNodeResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DBConnectionException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.DeviceOrganizationDaoUtil.getDeviceFromResultSet;
import static io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.DeviceOrganizationDaoUtil.loadDeviceOrganization;
import static io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.DeviceOrganizationDaoUtil.loadDeviceOrganizationWithDeviceDetails;

/**
 * Implementation of the DeviceOrganizationDAO interface.
 */
public class DeviceOrganizationMysqlDAOImpl implements DeviceOrganizationDAO {

    private static final Log log = LogFactory.getLog(DeviceOrganizationMysqlDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceNodeResult getChildrenOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice, int tenantID)
            throws DeviceOrganizationMgtDAOException {
        List<DeviceNode> childNodes = new ArrayList<>();
        Set<DeviceOrganization> organizations = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> twiceVisited = new HashSet<>();

        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            DeviceNode deviceNode = getDeviceDetails(deviceId, conn);
            boolean parentAdded = false; // Flag to track whether the parent device has been added
            getChildrenRecursive(
                    deviceNode,
                    maxDepth,
                    visited,
                    twiceVisited,
                    conn,
                    childNodes,
                    includeDevice,
                    parentAdded,
                    tenantID,
                    organizations
            );
            if (!includeDevice
                    && !parentAdded
            ) {
                childNodes.add(deviceNode); // Add the parent device if it hasn't been added and includeDevice is false.
            }
            return new DeviceNodeResult(childNodes, organizations);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve all child devices for " +
                    "parent device ID " + deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve all child devices for " +
                    "parent device ID " + deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    private void getChildrenRecursive(DeviceNode node,
                                      int maxDepth,
                                      Set<Integer> visited,
                                      Set<Integer> twiceVisited,
                                      Connection conn,
                                      List<DeviceNode> childNodes,
                                      boolean includeDevice,
                                      boolean parentAdded,
                                      int tenantID,
                                      Set<DeviceOrganization> organizations
    )
            throws SQLException {
        if (maxDepth <= 0) {
            return;
        }
        if (twiceVisited.contains(node.getDeviceId())) {
            return;
        }

        if (visited.contains(node.getDeviceId())) {
            twiceVisited.add(node.getDeviceId());
        }

        visited.add(node.getDeviceId());

        String sql = "SELECT D.ID, D.NAME, D.DESCRIPTION, D.DEVICE_IDENTIFICATION, DT.NAME AS DEVICE_TYPE_NAME, " +
                "DO.ORGANIZATION_ID, DO.TENANT_ID, DO.DEVICE_ID, DO.PARENT_DEVICE_ID, DO.DEVICE_ORGANIZATION_META ," +
                "DO.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE D " +
                "JOIN DM_DEVICE_ORGANIZATION DO ON D.ID = DO.DEVICE_ID " +
                "JOIN DM_DEVICE_TYPE DT ON D.DEVICE_TYPE_ID = DT.ID " +
                "WHERE DO.TENANT_ID = ? AND DO.PARENT_DEVICE_ID = ? ";

        boolean hasChildren = false;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenantID);
            stmt.setInt(2, node.getDeviceId());

            try (ResultSet rs = stmt.executeQuery()) {
                DeviceNode child;
                DeviceOrganization organization;
                while (rs.next()) {
                    child = getDeviceFromResultSet(rs);
                    node.getChildren().add(child);
                    hasChildren = true;
                    if (includeDevice
                            && !parentAdded
                    ) {
                        childNodes.add(node); // Add the parent device only if includeDevice is true and it hasn't been added.
                        parentAdded = true; // Set the flag to true after adding the parent device.
                    }

                    organization = loadDeviceOrganization(rs);
                    organizations.add(organization);

                    getChildrenRecursive(
                            child,
                            (maxDepth - 1),
                            visited,
                            twiceVisited,
                            conn,
                            childNodes,
                            includeDevice,
                            parentAdded,
                            tenantID,
                            organizations
                    );
                }
            }
        }

        // Add the parent node if it doesn't have children and includeDevice is true
        if (!hasChildren && includeDevice && !parentAdded) {
            childNodes.add(node);
            parentAdded = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceNodeResult getParentsOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice, int tenantID)
            throws DeviceOrganizationMgtDAOException {

        List<DeviceNode> parentNodes = new ArrayList<>();
        Set<DeviceOrganization> organizations = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> twiceVisited = new HashSet<>();
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            DeviceNode deviceNode = getDeviceDetails(deviceId, conn);
            boolean childAdded = false;
            getParentsRecursive(
                    deviceNode,
                    maxDepth,
                    visited,
                    twiceVisited,
                    conn,
                    parentNodes,
                    includeDevice,
                    childAdded,
                    tenantID,
                    organizations);
            if (!includeDevice && !childAdded) {
                parentNodes.add(deviceNode);
            }

            return new DeviceNodeResult(parentNodes, organizations);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve parent devices for " +
                    "device ID " + deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve parent devices for " +
                    "device ID " + deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    private void getParentsRecursive(DeviceNode node,
                                     int maxDepth,
                                     Set<Integer> visited,
                                     Set<Integer> twiceVisited,
                                     Connection conn,
                                     List<DeviceNode> parentNodes,
                                     boolean includeDevice,
                                     boolean childAdded,
                                     int tenantID,
                                     Set<DeviceOrganization> organizations)
            throws SQLException {
        if (maxDepth <= 0) {
            return;
        }
        if (twiceVisited.contains(node.getDeviceId())) {
            return;
        }

        if (visited.contains(node.getDeviceId())) {
            twiceVisited.add(node.getDeviceId());
        }

        visited.add(node.getDeviceId());

        String sql = "SELECT D.ID, D.NAME, D.DESCRIPTION, D.DEVICE_IDENTIFICATION, DT.NAME AS DEVICE_TYPE_NAME, " +
                "DO.ORGANIZATION_ID,  DO.TENANT_ID, DO.DEVICE_ID, DO.PARENT_DEVICE_ID, DO.DEVICE_ORGANIZATION_META ," +
                "DO.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE D " +
                "JOIN DM_DEVICE_ORGANIZATION DO ON D.ID = DO.PARENT_DEVICE_ID " +
                "JOIN DM_DEVICE_TYPE DT ON D.DEVICE_TYPE_ID = DT.ID " +
                "WHERE DO.TENANT_ID = ? AND DO.DEVICE_ID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tenantID);
            stmt.setInt(2, node.getDeviceId());
            try (ResultSet rs = stmt.executeQuery()) {
                DeviceNode parent;
                DeviceOrganization organization;
                while (rs.next()) {
                    parent = getDeviceFromResultSet(rs);
                    node.getParents().add(parent);
                    if (includeDevice && !childAdded) {
                        parentNodes.add(node);
                        childAdded = true;
                    }

                    organization = loadDeviceOrganization(rs);
                    organizations.add(organization);

                    getParentsRecursive(
                            parent,
                            (maxDepth - 1),
                            visited,
                            twiceVisited,
                            conn,
                            parentNodes,
                            includeDevice,
                            childAdded,
                            tenantID,
                            organizations);
                }
            }
        }
    }

    private DeviceNode getDeviceDetails(int deviceId, Connection conn) throws SQLException {
        String sql = "SELECT D.ID, D.NAME, D.DESCRIPTION, D.DEVICE_IDENTIFICATION, DT.NAME AS DEVICE_TYPE_NAME " +
                "FROM DM_DEVICE D " +
                "JOIN DM_DEVICE_TYPE DT ON D.DEVICE_TYPE_ID = DT.ID " +
                "WHERE D.ID = ?";
        DeviceNode node = new DeviceNode();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    node = getDeviceFromResultSet(rs);
                }
            }
        }
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getAllDeviceOrganizations() throws DeviceOrganizationMgtDAOException {
        List<DeviceOrganization> deviceOrganizations = new ArrayList<>();
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT * FROM DM_DEVICE_ORGANIZATION";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    DeviceOrganization deviceOrganization;
                    while (rs.next()) {
                        deviceOrganization = loadDeviceOrganization(rs);
                        deviceOrganizations.add(deviceOrganization);
                    }
                }
            }
            return deviceOrganizations;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieving all device organizations details.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getDeviceOrganizationRoots(PaginationRequest request, int tenantID)
            throws DeviceOrganizationMgtDAOException {
        List<DeviceOrganization> deviceOrganizations = new ArrayList<>();
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT D.ID, D.NAME, D.DESCRIPTION, D.DEVICE_IDENTIFICATION, DT.NAME AS DEVICE_TYPE_NAME, " +
                    "DO.ORGANIZATION_ID,  DO.TENANT_ID, DO.DEVICE_ID, DO.PARENT_DEVICE_ID, DO.DEVICE_ORGANIZATION_META ," +
                    "DO.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE_ORGANIZATION DO JOIN DM_DEVICE D ON D.ID = DO.DEVICE_ID " +
                    "JOIN DM_DEVICE_TYPE DT ON D.DEVICE_TYPE_ID = DT.ID " +
                    "WHERE DO.TENANT_ID = ? AND (DO.PARENT_DEVICE_ID IS NULL AND " +
                    "DO.DEVICE_ID NOT IN " +
                    "(SELECT DEVICE_ID FROM DM_DEVICE_ORGANIZATION " +
                    "WHERE PARENT_DEVICE_ID IS NOT NULL)) " +
                    "LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, request.getLimit());
                stmt.setInt(3, request.getOffSet());
                try (ResultSet rs = stmt.executeQuery()) {
                    DeviceOrganization deviceOrganization;
                    while (rs.next()) {
                        deviceOrganization = loadDeviceOrganizationWithDeviceDetails(rs);
                        deviceOrganizations.add(deviceOrganization);
                    }
                }
            }
            return deviceOrganizations;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieving device organization root details.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieving device organization roots.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getDeviceOrganizationLeafs(PaginationRequest request, int tenantID) throws DeviceOrganizationMgtDAOException {
        List<DeviceOrganization> deviceOrganizations = new ArrayList<>();
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT * FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID NOT IN " +
                    "(SELECT DISTINCT PARENT_DEVICE_ID FROM DM_DEVICE_ORGANIZATION WHERE PARENT_DEVICE_ID IS NOT NULL ) " +
                    "LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, request.getLimit());
                stmt.setInt(3, request.getOffSet());
                try (ResultSet rs = stmt.executeQuery()) {
                    DeviceOrganization deviceOrganization;
                    while (rs.next()) {
                        deviceOrganization = loadDeviceOrganization(rs);
                        deviceOrganizations.add(deviceOrganization);
                    }
                }
            }
            return deviceOrganizations;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieving all device organizations details.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AdditionResult addDeviceOrganization(DeviceOrganization deviceOrganization)
            throws DeviceOrganizationMgtDAOException {

        AdditionResult result = new AdditionResult();
        int inserted = 0;
        try {
            String sql = "INSERT INTO DM_DEVICE_ORGANIZATION (TENANT_ID, DEVICE_ID, PARENT_DEVICE_ID, " +
                    "DEVICE_ORGANIZATION_META, LAST_UPDATED_TIMESTAMP)" +
                    " VALUES (?, ?, ?, ?, ?)";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceOrganization.getTenantID());
                stmt.setInt(2, deviceOrganization.getDeviceId());
                if (deviceOrganization.getParentDeviceId() != null) {
                    stmt.setInt(3, deviceOrganization.getParentDeviceId());
                } else {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
                if (deviceOrganization.getDeviceOrganizationMeta() != null) {
                    stmt.setString(4, deviceOrganization.getDeviceOrganizationMeta());
                } else {
                    stmt.setString(4, "");
                }

                stmt.setTimestamp(5, timestamp);
                inserted = stmt.executeUpdate();

                if (deviceOrganization.isCheckCyclicRelationship()) {
                    boolean isCyclic = isCyclicRelationshipExist(conn,
                            deviceOrganization.getParentDeviceId(), deviceOrganization.getTenantID());
                    if (isCyclic) {
                        result.setCyclic(true);
                        return result;
                    } else {
                        if (inserted > 0) {
                            result.setInserted(true);
                            return result;
                        }
                    }
                }
                if (inserted > 0) {
                    result.setInserted(true);
                }
                return result;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert device organization for " +
                    deviceOrganization.getDeviceId();
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to insert device organization for " +
                    deviceOrganization.getDeviceId();
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }
    /**
     * Implementation for a test
     * @param tenantID
     * @throws DeviceOrganizationMgtDAOException
     */

    @Override
    public void addAllDevices(int tenantID) throws DeviceOrganizationMgtDAOException{

        try {
            ConnectionManagerUtil.beginDBTransaction();

            // Add devices from 1 to 500
            for (int i = 21; i <= 500; i++) {
                String deviceName = "Meter_" + String.format("%04d", i);
                String deviceIdentification = String.format("%04d", i);

                String query = "INSERT INTO DM_DEVICE (DESCRIPTION, NAME, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION," +
                        " LAST_UPDATED_TIMESTAMP, TENANT_ID) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP(), ?)";

                Connection connection = ConnectionManagerUtil.getDBConnection();
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, "Test Device " + i);
                    statement.setString(2, deviceName);
                    statement.setInt(3, 1); // Assuming DEVICE_TYPE_ID is 1
                    statement.setString(4, deviceIdentification);
                    statement.setInt(5, tenantID);

                    statement.executeUpdate();
                }
            }

            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException | SQLException e) {
            // Handle exceptions or log errors as needed
            e.printStackTrace();
//            Assert.fail("Exception occurred during device addition: " + e.getMessage());
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addOrganizations(int tenantID, int start, int end)  throws DeviceOrganizationMgtDAOException {

        for (int i = 2; i <= 500; i++) {
            DeviceOrganization organization = new DeviceOrganization();
            organization.setDeviceId(i);
            organization.setParentDeviceId(i - 1);
            organization.setUpdateTime(new Date(System.currentTimeMillis()));
            organization.setTenantID(tenantID);

            try {
                ConnectionManagerUtil.beginDBTransaction();
                addDeviceOrganization(organization);
                ConnectionManagerUtil.commitDBTransaction();
            } catch (DBConnectionException | DeviceOrganizationMgtDAOException e) {
                // Handle exceptions or log errors as needed
                e.printStackTrace();
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
    }

    private boolean isCyclicRelationshipExist(Connection conn, Integer parentDeviceID, int tenantID)
            throws DeviceOrganizationMgtDAOException {
        try {
            // Check for cyclic relationship
            if (hasCyclicRelationship(conn, parentDeviceID, tenantID)){
                log.error("cyclic relationship detected. Insertion not allowed.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to check cyclic relationship for " +
                    "parentDeviceID " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    private boolean hasCyclicRelationship(Connection connection, Integer parentDeviceID, int tenantID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "WITH RECURSIVE all_paths AS\n" +
                        "(" +
                        "SELECT DEVICE_ID AS DEVICE_ID,\n" +
                        "CAST(DEVICE_ID AS CHAR(500)) AS path, 0 AS is_cycle\n" +
                        "FROM DM_DEVICE_ORGANIZATION\n" +
                        "WHERE PARENT_DEVICE_ID=? AND TENANT_ID = ?\n" +
                        "UNION ALL\n" +
                        "SELECT r.DEVICE_ID,\n" +
                        "CONCAT(d.path, ',', r.DEVICE_ID),\n" +
                        "FIND_IN_SET(r.DEVICE_ID, d.path)!=0\n" +
                        "FROM DM_DEVICE_ORGANIZATION r, all_paths d\n" +
                        "WHERE r.PARENT_DEVICE_ID=d.DEVICE_ID\n" +
                        "AND is_cycle=0" +
                        ")\n" +
                        "SELECT\n" +
                        "  DEVICE_ID,\n" +
                        "  path,\n" +
                        "  is_cycle\n" +
                        "FROM\n" +
                        "  all_paths\n" +
                        "WHERE\n" +
                        "  is_cycle = 1;")) {
            statement.setInt(1, parentDeviceID);
            statement.setInt(2, tenantID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceOrganizationExist(int deviceId, Integer parentDeviceId, int tenantID)
            throws DeviceOrganizationMgtDAOException {
        try {
            String sql;
            Connection conn = ConnectionManagerUtil.getDBConnection();

            if (parentDeviceId != null) {
                sql = "SELECT * FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ? AND PARENT_DEVICE_ID = ?";
            } else {
                sql = "SELECT * FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ? AND PARENT_DEVICE_ID IS NULL";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, deviceId);
                if (parentDeviceId != null) {
                    stmt.setInt(3, parentDeviceId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Returns true if a match is found, false otherwise
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to check organization existence for deviceId " +
                    deviceId + " and parentDeviceId " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to check organization existence for deviceId " +
                    deviceId + " and parentDeviceId " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public DeviceOrganization getDeviceOrganizationByUniqueKey(int deviceId, Integer parentDeviceId, int tenantID)
            throws DeviceOrganizationMgtDAOException {
        try {
            String sql;
            Connection conn = ConnectionManagerUtil.getDBConnection();

            if (parentDeviceId != null) {
                sql = "SELECT * FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ? AND PARENT_DEVICE_ID = ?";
            } else {
                sql = "SELECT * FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ? AND PARENT_DEVICE_ID IS NULL";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, deviceId);
                if (parentDeviceId != null) {
                    stmt.setInt(3, parentDeviceId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return loadDeviceOrganization(rs);
                    }
                }
            }
            return null; // No matching device organization found.
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to get device organization for DEVICE_ID " +
                    deviceId + " and PARENT_DEVICE_ID " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to get device organization for DEVICE_ID " +
                    deviceId + " and PARENT_DEVICE_ID " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDeviceOrganization(DeviceOrganization deviceOrganization)
            throws DeviceOrganizationMgtDAOException {
        String msg;
        DeviceOrganization organization = getDeviceOrganizationByID(deviceOrganization.getOrganizationId(), deviceOrganization.getTenantID());

        if (organization == null) {
            msg = "Device Organization does not exist for organization ID = " + deviceOrganization.getOrganizationId();
            throw new DeviceOrganizationMgtDAOException(msg);
        }

        try {
            String sql = "UPDATE DM_DEVICE_ORGANIZATION SET ";
            if ((organization.getDeviceId() != deviceOrganization.getDeviceId()) && deviceOrganization.getDeviceId() > 0) {
                sql += "DEVICE_ID = ? , ";
            }
            if ((deviceOrganization.getParentDeviceId() == null || deviceOrganization.getParentDeviceId() > 0) &&
                    !Objects.equals(organization.getParentDeviceId(), deviceOrganization.getParentDeviceId())) {
                sql += "PARENT_DEVICE_ID = ? ,";
            }
            if (!Objects.equals(organization.getDeviceOrganizationMeta(), deviceOrganization.getDeviceOrganizationMeta())) {
                sql += "DEVICE_ORGANIZATION_META = ? ,";
            }
            sql += "LAST_UPDATED_TIMESTAMP = ? WHERE ORGANIZATION_ID = ? ";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int x = 0;

                if ((organization.getDeviceId() != deviceOrganization.getDeviceId()) && deviceOrganization.getDeviceId() > 0) {
                    stmt.setInt(++x, deviceOrganization.getDeviceId());
                }
                if (!Objects.equals(organization.getParentDeviceId(), deviceOrganization.getParentDeviceId())) {
                    stmt.setInt(++x, deviceOrganization.getParentDeviceId());
                }
                if (!Objects.equals(organization.getDeviceOrganizationMeta(), deviceOrganization.getDeviceOrganizationMeta())) {
                    stmt.setString(++x, deviceOrganization.getDeviceOrganizationMeta());
                }
                stmt.setTimestamp(++x, timestamp);
                stmt.setInt(++x, deviceOrganization.getOrganizationId());
                return stmt.executeUpdate() > 0;
            }

        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining DB connection to update device organization for " +
                    deviceOrganization.getOrganizationId();
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            msg = "Error occurred while processing SQL to update device organization for " +
                    deviceOrganization.getOrganizationId();
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceOrganization getDeviceOrganizationByID(int organizationId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT * FROM DM_DEVICE_ORGANIZATION DO WHERE DO.TENANT_ID = ? AND DO.ORGANIZATION_ID = ? ";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, organizationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return loadDeviceOrganization(rs);
                    }
                    log.info("No Device Organization found for retrieving for organizationID = " + organizationId);
                    return null;
                }
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to get device organization details for " +
                    "organizationID = " + organizationId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to get device organization details for " +
                    "organizationID = " + organizationId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeviceOrganizationByUniqueKey(int deviceId, Integer parentDeviceId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String deleteOrganizationSql = "DELETE FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ? AND PARENT_DEVICE_ID = ?";

            try (PreparedStatement deleteOrgStmt = conn.prepareStatement(deleteOrganizationSql)) {

                // Delete the organization
                deleteOrgStmt.setInt(1, tenantID);
                deleteOrgStmt.setInt(2, deviceId);
                deleteOrgStmt.setInt(3, parentDeviceId);
                return deleteOrgStmt.executeUpdate() > 0;
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to delete device organization for " +
                    "tenantID = " + tenantID +", deviceId = " + deviceId + "and parentDeviceId = " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while obtaining DB connection to delete device organization for " +
                    "tenantID = " + tenantID +", deviceId = " + deviceId + "and parentDeviceId = " + parentDeviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeviceOrganizationByID(int organizationId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String deleteOrganizationSql = "DELETE FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND ORGANIZATION_ID = ?";

            try (PreparedStatement deleteOrgStmt = conn.prepareStatement(deleteOrganizationSql)) {

                // Delete the organization
                deleteOrgStmt.setInt(1, tenantID);
                deleteOrgStmt.setInt(2, organizationId);
                return deleteOrgStmt.executeUpdate() > 0;
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to delete device organization for " +
                    organizationId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to delete device organization details for " +
                    organizationId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeviceAssociations(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String deleteByDeviceIdSql = "DELETE FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND DEVICE_ID = ?";
            String deleteByParentDeviceIdSql = "DELETE FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ? AND PARENT_DEVICE_ID = ?";

            try (PreparedStatement deleteByDeviceIdStmt = conn.prepareStatement(deleteByDeviceIdSql);
                 PreparedStatement deleteByParentDeviceIdStmt = conn.prepareStatement(deleteByParentDeviceIdSql)) {

                // Delete device organizations where the device is the device_id
                deleteByDeviceIdStmt.setInt(1, tenantID);
                deleteByDeviceIdStmt.setInt(2, deviceId);

                // Delete device organizations where the device is the parent_device_id
                deleteByParentDeviceIdStmt.setInt(1, tenantID);
                deleteByParentDeviceIdStmt.setInt(2, deviceId);

                return deleteByDeviceIdStmt.executeUpdate() > 0 | deleteByParentDeviceIdStmt.executeUpdate() > 0;

            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to delete device organization for device ID" +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to delete device organization details for device ID " +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceIdExist(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT 1 " +
                    "FROM DM_DEVICE_ORGANIZATION " +
                    "WHERE TENANT_ID = ? AND (device_id = ? OR parent_device_id = ?) " +
                    "LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, deviceId);
                stmt.setInt(3, deviceId);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Returns true if a match is found, false otherwise
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to query device organization for device ID" +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to query device organization details for device ID " +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChildDeviceIdExist(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException {
        try {
            Connection conn = ConnectionManagerUtil.getDBConnection();
            String sql = "SELECT 1 " +
                    "FROM DM_DEVICE_ORGANIZATION " +
                    "WHERE TENANT_ID = ? AND device_id = ? " +
                    "LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantID);
                stmt.setInt(2, deviceId);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Returns true if a match is found, false otherwise
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to query device organization for device ID" +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to query device organization details for device ID " +
                    deviceId;
            log.error(msg);
            throw new DeviceOrganizationMgtDAOException(msg, e);
        }
    }


}
