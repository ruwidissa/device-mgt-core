package io.entgra.device.mgt.core.device.mgt.core.dao;

public interface TenantDAO {

    /**
     * Delete device certificates of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceCertificateByTenantId(int tenantId)throws DeviceManagementDAOException;

    /**
     * Delete groups of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteGroupByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete role-group mapping data of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteRoleGroupMapByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete devices of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device properties of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDevicePropertiesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete group properties of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteGroupPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device-group mapping details of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceGroupMapByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete operations of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteOperationByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete enrolments of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteEnrolmentByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device statuses of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceStatusByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete enrolment mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteEnrolmentOpMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device operation responses of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceOperationResponseByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete large-device operations responses of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceOperationResponseLargeByTenantId(int tenantId) throws DeviceManagementDAOException;

    // Delete policy related tables

    /**
     * Delete applications of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteApplicationByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy compliance features of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyComplianceFeaturesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy change management data of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyChangeManagementByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy compliance statuses of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyComplianceStatusByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy criteria properties of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyCriteriaPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy criteria of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyCriteriaByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete role policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteRolePolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete user policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteUserPolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDevicePolicyAppliedByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete criteria of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteCriteriaByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device type properties of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceTypePolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDevicePolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete profile features of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteProfileFeaturesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete policy corrective actions of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deletePolicyCorrectiveActionByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete profiles of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteProfileByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete app icons of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteAppIconsByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device group policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceGroupPolicyByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete notifications of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteNotificationByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device information of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceInfoByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device location of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceLocationByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device history of last seven days of a tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceHistoryLastSevenDaysByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device details of a tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceDetailByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete metadata of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteMetadataByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete OTP data of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteOTPDataByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete geo fences of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteGeofenceByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete geo fence group mapping data of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteGeofenceGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device events of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceEventByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device event group mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceEventGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete geo fence event mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteGeofenceEventMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete external group mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteExternalGroupMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete External device mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteExternalDeviceMappingByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete external permission mapping of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteExternalPermissionMapping(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete dynamic tasks of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDynamicTaskByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete dynamic task properties of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDynamicTaskPropertiesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device subtypes of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceSubTypeByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete traccar unsynced devices of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteTraccarUnsyncedDevicesByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete sub operation templates of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteSubOperationTemplate(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete device organizations of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteDeviceOrganizationByTenantId(int tenantId) throws DeviceManagementDAOException;

    /**
     * Delete CEA policies of tenant
     *
     * @param tenantId Tenant ID
     * @throws DeviceManagementDAOException thrown if there is an error when deleting data
     */
    void deleteCEAPoliciesByTenantId(int tenantId) throws DeviceManagementDAOException;

}
