package io.entgra.device.mgt.core.application.mgt.core.dao.impl.vpp;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.dao.VppApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.device.mgt.core.application.mgt.core.util.DAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

public class GenericVppApplicationDAOImpl  extends AbstractDAOImpl implements VppApplicationDAO {
    private static final Log log = LogFactory.getLog(GenericVppApplicationDAOImpl.class);

    @Override
    public int addVppUser(VppUserDTO userDTO)
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
                long currentTime = System.currentTimeMillis();
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, userDTO.getTenantId());
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

    public VppUserDTO updateVppUser(VppUserDTO userDTO)
            throws ApplicationManagementDAOException {

        String sql = "UPDATE "
                + "AP_VPP_USER "
                + "SET "
                + "CLIENT_USER_ID = ?,"
                + "DM_USERNAME = ?, "
                + "TENANT_ID = ?, "
                + "EMAIL = ?, "
                + "INVITE_CODE = ?, "
                + "STATUS = ?, "
                + "LAST_UPDATED_TIME = ?, "
                + "MANAGED_ID = ?, "
                + "TEMP_PASSWORD = ? "
                + "WHERE ID = ?";
        try {
            Connection conn = this.getDBConnection();
            long updatedTime = System.currentTimeMillis();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, userDTO.getTenantId());
                stmt.setString(4, userDTO.getEmail());
                stmt.setString(5, userDTO.getInviteCode());
                stmt.setString(6, userDTO.getStatus());
                stmt.setLong(7, updatedTime);
                stmt.setString(8, userDTO.getManagedId());
                stmt.setString(9, userDTO.getTmpPassword());
                stmt.setInt(10, userDTO.getId());
                stmt.executeUpdate();
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

    public VppUserDTO getUserByDMUsername(String emmUsername)
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
                + "TEMP_PASSWORD "
                + "FROM AP_VPP_USER "
                + "WHERE DM_USERNAME = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, emmUsername);
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
}
