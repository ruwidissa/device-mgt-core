package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.TenantDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TenantDAOImpl implements TenantDAO {

    private static final Log log = LogFactory.getLog(TenantDAOImpl.class);


    @Override
    public void deleteDeviceCertificateByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_CERTIFICATE WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting certificates for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteGroupByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting groups for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteRoleGroupMapByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting role group mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting devices for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDevicePropertiesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_PROPERTIES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device properties for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteGroupPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM GROUP_PROPERTIES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group properties for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceGroupMapByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device group mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteOperationByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_OPERATION WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting operations for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteEnrolmentByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting enrolment for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceStatusByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_STATUS WHERE ENROLMENT_ID IN " +
                    "(SELECT ID FROM DM_ENROLMENT WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device status for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteEnrolmentOpMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting enrolment op mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceOperationResponseByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE WHERE EN_OP_MAP_ID IN " +
                    "(SELECT ID FROM DM_ENROLMENT_OP_MAPPING WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device operation response for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceOperationResponseLargeByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE_LARGE WHERE EN_OP_MAP_ID IN "+
                    "(SELECT ID FROM DM_ENROLMENT_OP_MAPPING WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device operation response large for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteApplicationByTenantId(int tenantId) throws DeviceManagementDAOException{
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_APPLICATION WHERE DEVICE_ID IN " +
                    "(SELECT ID FROM DM_DEVICE WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting applications for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }


    }

    @Override
    public void deletePolicyComplianceFeaturesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_COMPLIANCE_FEATURES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy compliance features for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyChangeManagementByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_CHANGE_MGT WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy change management for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyComplianceStatusByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_COMPLIANCE_STATUS WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy compliance status for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyCriteriaPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_CRITERIA_PROPERTIES WHERE POLICY_CRITERION_ID IN " +
                    "(SELECT ID FROM DM_POLICY_CRITERIA WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?))";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy criteria properties for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyCriteriaByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_CRITERIA WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy criteria for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteRolePolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_POLICY WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting role policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteUserPolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_USER_POLICY WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting user policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDevicePolicyAppliedByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_POLICY_APPLIED WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy applied for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteCriteriaByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_CRITERIA WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting criteria for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceTypePolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_TYPE_POLICY WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device type policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteDevicePolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_POLICY WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteProfileFeaturesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_PROFILE_FEATURES WHERE PROFILE_ID IN " +
                    "(SELECT ID FROM DM_PROFILE WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting profile features for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deletePolicyCorrectiveActionByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_POLICY_CORRECTIVE_ACTION WHERE POLICY_ID IN " +
                    "(SELECT ID FROM DM_POLICY WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting policy corrective action for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteProfileByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_PROFILE WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting profile for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteAppIconsByTenantId(int tenantId) throws DeviceManagementDAOException{
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_APP_ICONS WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting App Icons for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceGroupPolicyByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_GROUP_POLICY WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device group policy for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteNotificationByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_NOTIFICATION WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting notifications for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceInfoByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_INFO WHERE ENROLMENT_ID IN " +
                    "(SELECT ID FROM DM_ENROLMENT WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device info for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceLocationByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE ENROLMENT_ID IN " +
                    "(SELECT ID FROM DM_ENROLMENT WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting  device location for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceHistoryLastSevenDaysByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_HISTORY_LAST_SEVEN_DAYS WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device history for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceDetailByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE ENROLMENT_ID IN " +
                    "(SELECT ID FROM DM_ENROLMENT WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device detail for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteMetadataByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_METADATA WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting  metadata for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteOTPDataByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_OTP_DATA WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting OTP data for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteGeofenceByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting geo fence for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteGeofenceGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE_GROUP_MAPPING WHERE FENCE_ID IN " +
                    "(SELECT ID FROM DM_GEOFENCE WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting geo fence group mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceEventByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_EVENT WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting  device event for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceEventGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_EVENT_GROUP_MAPPING WHERE EVENT_ID IN " +
                    "(SELECT ID FROM DM_DEVICE_EVENT WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device event group mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteGeofenceEventMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE_EVENT_MAPPING WHERE FENCE_ID IN " +
                    "(SELECT ID FROM DM_GEOFENCE WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting geo fence event mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteExternalGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_GROUP_MAPPING WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting external group mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteExternalDeviceMappingByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_DEVICE_MAPPING WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting external device mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteExternalPermissionMapping(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_PERMISSION_MAPPING WHERE TRACCAR_USER_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting ext permission mapping for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }


    }

    @Override
    public void deleteDynamicTaskByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DYNAMIC_TASK WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting dynamic task for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDynamicTaskPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DYNAMIC_TASK_PROPERTIES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting dynamic task properties for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceSubTypeByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_SUB_TYPE WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device sub types for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteTraccarUnsyncedDevicesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_TRACCAR_UNSYNCED_DEVICES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Traccar unsynced devices for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteSubOperationTemplate(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM SUB_OPERATION_TEMPLATE WHERE SUB_TYPE_ID IN " +
                    "(SELECT SUB_TYPE_ID FROM DM_DEVICE_SUB_TYPE WHERE TENANT_ID = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting sub operation template for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteDeviceOrganizationByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_ORGANIZATION WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device organization for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteCEAPoliciesByTenantId(int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = DeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_CEA_POLICIES WHERE TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting CEA policies for Tenant ID " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }
}
