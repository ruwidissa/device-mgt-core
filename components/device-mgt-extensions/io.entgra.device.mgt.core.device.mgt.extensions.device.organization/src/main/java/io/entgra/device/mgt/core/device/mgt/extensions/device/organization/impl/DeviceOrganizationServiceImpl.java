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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.impl;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAO;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.AdditionResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNodeResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DBConnectionException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtDAOException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtPluginException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.spi.DeviceOrganizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.util.List;

public class DeviceOrganizationServiceImpl implements DeviceOrganizationService {

    private static final Log log = LogFactory.getLog(DeviceOrganizationServiceImpl.class);

    private final DeviceOrganizationDAO deviceOrganizationDao;

    public DeviceOrganizationServiceImpl() {
        this.deviceOrganizationDao = DeviceOrganizationDAOFactory.getDeviceOrganizationDAO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceNodeResult getChildrenOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice)
            throws DeviceOrganizationMgtPluginException {
        if (deviceId <= 0 || maxDepth < 0) {
            String msg = "Invalid input parameters for retrieving child devices : " +
                    "deviceID = " + deviceId + ", maxDepth = " + maxDepth +
                    ", includeDevice = " + includeDevice;
            throw new BadRequestException(msg);
        }
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            //set device details
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getChildrenOfDeviceNode(deviceId, maxDepth, includeDevice, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve child devices : " +
                    "deviceID = " + deviceId + ", maxDepth = " + maxDepth + ", includeDevice = " +
                    includeDevice;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving child devices : " +
                    "deviceID = " + deviceId + ", maxDepth = " + maxDepth + ", includeDevice = " +
                    includeDevice;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceNodeResult getParentsOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice)
            throws DeviceOrganizationMgtPluginException {
        if (deviceId <= 0 || maxDepth <= 0) {
            String msg = "Invalid input parameters for retrieving parent devices. Params : " +
                    "deviceID = " + deviceId + ", maxDepth = " + maxDepth +
                    ", includeDevice = " + includeDevice;
            throw new BadRequestException(msg);
        }
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getParentsOfDeviceNode(deviceId, maxDepth, includeDevice, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve parent devices for : " +
                    "device ID = " + deviceId + ", maxDepth = " + maxDepth + ", includeDevice = " +
                    includeDevice;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving parent devices for : " +
                    "device ID = " + deviceId + ", maxDepth = " + maxDepth + ", includeDevice = " +
                    includeDevice;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getAllDeviceOrganizations() throws DeviceOrganizationMgtPluginException {
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            return deviceOrganizationDao.getAllDeviceOrganizations();
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getDeviceOrganizationRoots(PaginationRequest request) throws DeviceOrganizationMgtPluginException {
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getDeviceOrganizationRoots(request, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceOrganization> getDeviceOrganizationLeafs(PaginationRequest request)
            throws DeviceOrganizationMgtPluginException {
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getDeviceOrganizationLeafs(request, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addAllDevices() throws DeviceOrganizationMgtPluginException {
        try {
            // Open a database connection
//            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceOrganizationDao.addAllDevices(tenantID);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
//            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addOrganizations(int start, int end) throws DeviceOrganizationMgtPluginException {
        try {
            // Open a database connection
//            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceOrganizationDao.addOrganizations(tenantID, start, end);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device organizations.";
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
//            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean addDeviceOrganization(DeviceOrganization deviceOrganization)
            throws DeviceOrganizationMgtPluginException {
        if (deviceOrganization == null || deviceOrganization.getDeviceId() <= 0 ||
                !(deviceOrganization.getParentDeviceId() == null || deviceOrganization.getParentDeviceId() > 0)) {
            throw new BadRequestException("Invalid input parameters for adding deviceOrganizations : " +
                    "deviceOrganization = " + deviceOrganization +
                    ", deviceID = " + "deviceID should be a positive number"
                    + "parentDeviceID = " + "parentDeviceID should be a positive number or null");
        }
        String msg;
        int deviceID = deviceOrganization.getDeviceId();
        Integer parentDeviceID = deviceOrganization.getParentDeviceId();
        boolean exists = isDeviceOrganizationExist(deviceID, parentDeviceID);
        if (exists) {
            log.error("Device Organization already exists");
            return false;
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceOrganization.setTenantID(tenantID);
            AdditionResult result = deviceOrganizationDao.addDeviceOrganization(deviceOrganization);
            if (result.isInserted() && !result.isCyclic()) {
                msg = "Device organization added successfully. Device Organization details : " +
                        "deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device organization failed to add. Device Organization details : " +
                        "deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
                return false;
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to add device organization. " +
                    "Device Organization details : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while adding device organization. "
                    + "Device Organization details : device ID = " + deviceID + ", parent device ID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public boolean deleteDeviceOrganizationByUniqueKey(int deviceID, Integer parentDeviceID)
            throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0 || !(parentDeviceID == null || parentDeviceID > 0)) {
            throw new BadRequestException("Invalid input parameters for deviceOrganization update. : "
                    + ", deviceID = " + deviceID
                    + ", parentDeviceID = " + parentDeviceID);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.deleteDeviceOrganizationByUniqueKey(deviceID, parentDeviceID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve organization. " +
                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred while retrieving device organization for deviceID " +
                    deviceID + " and parentDeviceID " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }


//    private boolean isCyclicRelationshipExist(int deviceID, Integer parentDeviceID) throws DeviceOrganizationMgtPluginException {
//        try {
////            ConnectionManagerUtil.openDBConnection();
//            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//            return deviceOrganizationDao.isCyclicRelationshipExist(deviceID, parentDeviceID, tenantID);
//        }
////        catch (DBConnectionException e) {
////            String msg = "Error occurred while obtaining the database connection to check cyclic relationship. " +
////                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
////            log.error(msg);
////            throw new DeviceOrganizationMgtPluginException(msg, e);
////        }
//        catch (DeviceOrganizationMgtDAOException e) {
//            String msg = "Error occurred in the database level while checking for cyclic relationship. " +
//                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
//            log.error(msg);
//            throw new DeviceOrganizationMgtPluginException(msg, e);
//        } finally {
////            ConnectionManagerUtil.closeDBConnection();
//        }
//    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceOrganizationExist(int deviceID, Integer parentDeviceID) throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0 || !(parentDeviceID == null || parentDeviceID > 0)) {
            throw new BadRequestException("Invalid input parameters for deviceOrganization update. : "
                    + ", deviceID = " + deviceID
                    + ", parentDeviceID = " + parentDeviceID);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.isDeviceOrganizationExist(deviceID, parentDeviceID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to check organization existence. " +
                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while checking organization existence. " +
                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceOrganization getDeviceOrganizationByUniqueKey(int deviceID, Integer parentDeviceID)
            throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0 || !(parentDeviceID == null || parentDeviceID > 0)) {
            throw new BadRequestException("Invalid input parameters for deviceOrganization update. : "
                    + ", deviceID = " + deviceID
                    + ", parentDeviceID = " + parentDeviceID);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getDeviceOrganizationByUniqueKey(deviceID, parentDeviceID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve organization. " +
                    "Params : deviceID = " + deviceID + ", parentDeviceID = " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred while retrieving device organization for deviceID " +
                    deviceID + " and parentDeviceID " + parentDeviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateDeviceOrganization(DeviceOrganization deviceOrganization)
            throws DeviceOrganizationMgtPluginException {

        String msg;
        DeviceOrganization organization = getDeviceOrganizationByID(deviceOrganization.getOrganizationId());
        if (organization == null) {
            msg = "Cannot find device organization for organizationID = " + deviceOrganization.getOrganizationId();
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg);
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceOrganization.setTenantID(tenantID);
            boolean result = deviceOrganizationDao.updateDeviceOrganization(deviceOrganization);
            if (result) {
                msg = "Device organization updated successfully for organizationID = " + deviceOrganization.getOrganizationId();
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device organization failed to update for organizationID = " + deviceOrganization.getOrganizationId();
                throw new DeviceOrganizationMgtPluginException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to update device organization for " +
                    "organizationID = " + organization.getOrganizationId();
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while updating device organization for " +
                    "organizationID = " + organization.getOrganizationId();
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceOrganization getDeviceOrganizationByID(int organizationID)
            throws DeviceOrganizationMgtPluginException {
        if (organizationID <= 0) {
            throw new BadRequestException("organizationID must be a positive number. " +
                    "Invalid input parameters for getting deviceOrganization for : "
                    + "organizationID = " + organizationID);
        }
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.getDeviceOrganizationByID(organizationID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve deviceOrganization for : "
                    + "organizationID = " + organizationID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving deviceOrganization for : "
                    + "organizationID = " + organizationID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeviceOrganizationByID(int organizationID)
            throws DeviceOrganizationMgtPluginException {
        if (organizationID <= 0) {
            throw new BadRequestException("organizationID must be a positive number." +
                    "Invalid input parameters for deviceOrganization Deletion : " +
                    "organizationID = " + organizationID);
        }
        String msg;

        DeviceOrganization deviceOrganization = getDeviceOrganizationByID(organizationID);
        if (deviceOrganization == null) {
            msg = "Cannot find device organization for Deletion : organizationID = " + organizationID;
            log.error(msg);
            return false;
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean result = deviceOrganizationDao.deleteDeviceOrganizationByID(organizationID, tenantID);
            if (result) {
                msg = "Device organization record deleted successfully for organizationID = " + organizationID;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device organization failed to delete for organizationID = " + organizationID;
                throw new DeviceOrganizationMgtPluginException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to delete device organization for " +
                    "organizationID = " + organizationID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while deleting device organization for " +
                    "organizationID = " + organizationID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteDeviceAssociations(int deviceID)
            throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0) {
            throw new BadRequestException("deviceID must be a positive number." +
                    "Invalid input parameters for deviceID = " + deviceID);
        }
        String msg;

        boolean deviceIdExist = isDeviceIdExist(deviceID);
        if (!deviceIdExist) {
            msg = "Cannot find device organizations associated with deviceID = " + deviceID;
            log.error(msg);
            return false;
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean result = deviceOrganizationDao.deleteDeviceAssociations(deviceID, tenantID);
            if (result) {
                msg = "Device organization records associated with deviceID = " + deviceID + " are deleted successfully.";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device organization records associated with deviceID = " + deviceID + " have failed to delete.";
                throw new DeviceOrganizationMgtPluginException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to delete device organizations associated with " +
                    "deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while deleting device organizations associated with " +
                    "deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceIdExist(int deviceID)
            throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0) {
            throw new BadRequestException("deviceID must be a positive number." +
                    "Invalid input parameters for checking deviceID existence " +
                    "in deviceOrganization : deviceID = " + deviceID);
        }
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.isDeviceIdExist(deviceID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to check deviceID existence " +
                    "in deviceOrganization : deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while checking the existence " +
                    "in deviceOrganization : deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChildDeviceIdExist(int deviceID)
            throws DeviceOrganizationMgtPluginException {
        if (deviceID <= 0) {
            throw new BadRequestException("deviceID must be a positive number." +
                    "Invalid input parameters for checking deviceID existence " +
                    "in deviceOrganization : deviceID = " + deviceID);
        }
        try {
            // Open a database connection
            ConnectionManagerUtil.openDBConnection();
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceOrganizationDao.isChildDeviceIdExist(deviceID, tenantID);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to check child deviceID existence " +
                    "in deviceOrganization : deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } catch (DeviceOrganizationMgtDAOException e) {
            String msg = "Error occurred in the database level while checking the child deviceID existence " +
                    "in deviceOrganization : deviceID = " + deviceID;
            log.error(msg);
            throw new DeviceOrganizationMgtPluginException(msg, e);
        } finally {
            // Close the database connection
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
