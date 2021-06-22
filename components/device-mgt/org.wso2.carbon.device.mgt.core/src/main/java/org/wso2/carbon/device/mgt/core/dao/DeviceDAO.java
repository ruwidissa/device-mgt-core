/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.collections.map.SingletonMap;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.Count;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DevicePropertyInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import org.wso2.carbon.device.mgt.common.device.details.DeviceMonitoringData;
import org.wso2.carbon.device.mgt.common.geo.service.GeoQuery;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.common.geo.service.GeoCluster;
import org.wso2.carbon.device.mgt.common.geo.service.GeoCoordinate;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class represents the key operations associated with persisting device related information.
 */
public interface DeviceDAO {

    /**
     * This method is used to get the device count by device-type.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns the device count of given type.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByType(String type, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by user.
     *
     * @param username username of the user.
     * @param tenantId tenant id.
     * @return returns the device count of given user.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by device name (pattern).
     *
     * @param deviceName name of the device.
     * @param tenantId tenant id.
     * @return returns the device count of given user.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByName(String deviceName, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by status.
     *
     * @param status enrollment status.
     * @param tenantId tenant id.
     * @return returns the device count of given status.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByStatus(String status, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by status and type.
     *
     * @param deviceType device type name.
     * @param status enrollment status.
     * @param tenantId tenant id.
     * @return returns the device count of given status.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByStatus(String deviceType, String status, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by ownership.
     *
     * @param ownerShip Ownership of devices.
     * @param tenantId tenant id.
     * @return returns the device count of given ownership.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByOwnership(String ownerShip, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to add a device.
     *
     * @param typeId   device type id.
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of the persisted device record.
     * @throws DeviceManagementDAOException
     */
    int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to update a given device.
     *
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of updated device.
     * @throws DeviceManagementDAOException
     */
    boolean updateDevice(Device device, int tenantId) throws DeviceManagementDAOException;

    Device getDevice(DeviceData deviceData, int tenantId) throws DeviceManagementDAOException;


    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, int tenantId) throws DeviceManagementDAOException;


    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(String deviceIdentifier, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and owner and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param owner username of the owner.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, String owner, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id which modified
     * later than the ifModifiedSince param.
     *
     * @param deviceIdentifier device id.
     * @param ifModifiedSince last modified time.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, Date ifModifiedSince, int tenantId) throws
                                                                                            DeviceManagementDAOException;

    /**
     * Retrieves a list of devices based on a given criteria of properties
     * @param deviceProps properties by which devices need to be filtered
     * @param tenantId tenant id
     * @return list of devices with properties
     * @throws DeviceManagementDAOException
     */
    List<Device> getDeviceBasedOnDeviceProperties(Map<String, String> deviceProps, int tenantId) throws DeviceManagementDAOException;

    /**
     * Retrieves a list of devices based on a given criteria of properties
     * This will ignores the tenant and it will return devices registered under every tenants
     * @param deviceProps properties by which devices need to be filtered
     * @return list of devices with properties
     * @throws DeviceManagementDAOException if the SQL query has failed to be executed
     */
    List<DevicePropertyInfo> getDeviceBasedOnDeviceProperties(Map<String, String> deviceProps)
            throws DeviceManagementDAOException;

    /**
     * Retrieves properties of given device identifier
     *
     * @param deviceId identifier of device that need to be retrieved
     * @param tenantId tenant ID
     * @return list of devices with properties
     * @throws DeviceManagementDAOException
     */
    Device getDeviceProps(String deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id which modified
     * later than the ifModifiedSince param.
     *
     * @param deviceIdentifier device id.
     * @param ifModifiedSince last modified time.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(String deviceIdentifier, Date ifModifiedSince, int tenantId) throws
            DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and owner and tenant-id which modified
     * later than the ifModifiedSince param.
     *
     * @param deviceIdentifier device id.
     * @param owner username of the owner.
     * @param ifModifiedSince last modified time.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, String owner, Date ifModifiedSince, int tenantId) throws
            DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier, enrollment status and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param status enrollment status.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given identifier with it's tenant id
     *
     * @param deviceIdentifier device id.
     * @return {@link SingletonMap} with device and corresponding tenant id
     * @throws DeviceManagementDAOException will be thrown in case of a {@link SQLException}
     */
    SingletonMap getDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given tenant id.
     *
     * @param deviceId device id.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(int deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve all the devices of a given tenant.
     *
     * @param tenantId tenant id.
     * @return returns a list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of a given tenant as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @param tenantId tenant id.
     * @return returns paginated list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of a given tenant as a paginated result, along the lines of
     * activeServerCount and serverIndex
     *
     * @param request
     * @param tenantId
     * @param activeServerCount
     * @param serverIndex
     * @return
     */
    List<Device> getAllocatedDevices(PaginationRequest request, int tenantId, int activeServerCount, int serverIndex) throws DeviceManagementDAOException;

    /**
     * This method is used to search for devices within a specific group.
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @param tenantId tenant id.
     * @return returns paginated list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> searchDevicesInGroup(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get device count within a specific group.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param tenantId tenant id
     * @return Device count
     * @throws DeviceManagementDAOException
     */
    int getCountOfDevicesInGroup(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve all the devices of a given tenant and device type.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns list of devices of provided type.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the list of devices attributed to a specific node
     * when using dynamic partitioning to allocate tasks given the tenant and device type
     * along with activeServerCount and serverIndex
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns list of devices of provided type.
     * @throws DeviceManagementDAOException
     */
    List<Device> getAllocatedDevices(String type, int tenantId, int activeServerCount, int serverIndex) throws DeviceManagementDAOException;

    List<Device> getDevices(long timestamp, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given user.
     *
     * @param username user name.
     * @param tenantId tenant id.
     * @return returns list of devices of given user.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of given user of given device type.
     * @param username user name.
     * @param type device type.
     * @param tenantId tenant id.
     * @return List of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(String username, String type, int tenantId) throws DeviceManagementDAOException;


    /**
     * This method is used to retrieve devices of a given user as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and search data.
     * @param tenantId tenant id.
     * @return returns paginated list of devices in which owner matches (search) with given username.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is using to retrieve devices of a given user of given device statues
     *
     * @param username Username
     * @param tenantId Tenant Id
     * @param deviceStatuses Device Statuses
     * @returnList of devices
     * @throws DeviceManagementDAOException if error ccured while getting devices from the database
     */
    List<Device> getDevicesOfUser(String username, int tenantId, List<String> deviceStatuses)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant.
     *
     * @param username user name.
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(String username, int tenantId) throws DeviceManagementDAOException;

    int getDeviceCount(String type, String status, int tenantId) throws DeviceManagementDAOException;

    List<String> getDeviceIdentifiers(String type, String status, int tenantId) throws DeviceManagementDAOException;

    boolean setEnrolmentStatusInBulk(String deviceType, String status, int tenantId, List<String> devices) throws DeviceManagementDAOException;
    /**
     * This method is used to retrieve the device count of a given tenant.
     *
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant for the given search terms.
     *
     * @param request paginated request used to search devices.
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the available device types of a given tenant.
     *
     * @return returns list of device types.
     * @throws DeviceManagementDAOException
     */
    List<DeviceType> getDeviceTypes() throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given device name.
     *
     * @param deviceName device name.
     * @param tenantId   tenant id.
     * @return returns list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByNameAndType(String deviceName, String type, int tenantId, int offset, int limit)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given device name as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and device search info.
     * @param tenantId   tenant id.
     * @return returns paginated list of devices which name matches (search) given device-name.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByName(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to add an enrollment information of a given device.
     *
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of the enrollment.
     * @throws DeviceManagementDAOException
     */
    int addEnrollment(Device device, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to set the current enrollment status of given device and user.
     *
     * @param deviceId     device id.
     * @param currentOwner current user name.
     * @param status       device status.
     * @param tenantId     tenant id.
     * @return returns true if success.
     * @throws DeviceManagementDAOException
     */
    boolean setEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner, Status status,
                               int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the status of current enrollment of a given user and device.
     *
     * @param deviceId     device id.
     * @param currentOwner device owner.
     * @param tenantId     tenant id.
     * @return returns current enrollment status.
     * @throws DeviceManagementDAOException
     */
    Status getEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner,
                              int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve current enrollment of a given device and user.
     *
     * @param deviceId    device id.
     * @param request     {@link PaginationRequest}
     * @param tenantId    tenant id.
     * @return returns EnrolmentInfo object.
     * @throws DeviceManagementDAOException if SQL error occurred while processing the query.
     */
    EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve current active enrollment of a given device and tenant id.
     *
     * @param deviceId    device id.
     * @param tenantId    tenant id.
     * @return returns EnrolmentInfo object.
     * @throws DeviceManagementDAOException
     */
    EnrolmentInfo getActiveEnrolment(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given enrollment status.
     *
     * @param status   enrollment status.
     * @param tenantId tenant id.
     * @return returns list of devices of given status.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given ownership as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and device search.
     * @param tenantId tenant id.
     * @return returns list of devices of given ownership.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByOwnership(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given enrollment status as a paginated result
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @param tenantId tenant id.
     * @return returns paginated list of devices of given status.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByStatus(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;


    List<Integer> getDeviceEnrolledTenants() throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the details of geoclusters formed relatively to the zoom level and map
     * boundaries.
     *
     * @param geoQuery  the query to determine the geo data.
     * @param tenantId  tenant id.
     * @return returns a list of enrolment info objects.
     */
    List<GeoCluster> findGeoClusters(GeoQuery geoQuery, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to identify whether given device ids are exist or not.
     *
     * @param deviceIdentifiers List of device identifiers.
     * @param tenantId tenant id.
     * @return returns list of device ids that matches with device identifiers.
     * @throws DeviceManagementDAOException throws {@link DeviceManagementDAOException} if connections establishment
     * fails.
     */
    List<Device> getDevicesByIdentifiers(List<String> deviceIdentifiers, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices with specified device identifiers filtered with statuses.
     *
     * @param deviceIdentifiers List of device identifiers.
     * @param tenantId tenant id.
     * @return returns list of device ids that matches with device identifiers.
     * @throws DeviceManagementDAOException throws {@link DeviceManagementDAOException} if connections establishment
     * fails.
     */
    List<Device> getDevicesByIdentifiersAndStatuses(List<String> deviceIdentifiers, List<EnrolmentInfo.Status> statuses, int tenantId)
            throws DeviceManagementDAOException;

    /***
     * This method is used to permanently delete devices and their related details
     * @param deviceIdentifiers List of device identifiers.
     * @param deviceIds list of device ids (primary keys).
     * @param enrollmentIds list of enrollment ids.
     * @throws DeviceManagementDAOException when no enrolments are found for the given device.
     */
    void deleteDevices(List<String> deviceIdentifiers, List<Integer> deviceIds, List<Integer> enrollmentIds) throws DeviceManagementDAOException;

    boolean transferDevice(String deviceType, String deviceId, String owner, int destinationTenantId)
            throws DeviceManagementDAOException, SQLException;

    /**
     * This method is used to get a device list which enrolled within a specific time period
     *
     * @param request  Pagination request to get paginated result
     * @param tenantId ID of the current tenant
     * @param fromDate Start date to filter devices(YYYY-MM-DD)
     * @param toDate   End date to filter devices(YYYY-MM-DD)
     * @return returns a list of Device objects
     * @throws {@Link DeviceManagementDAOException} If failed to retrieve devices
     *
     */
    List<Device> getDevicesByDuration(PaginationRequest request,
                                      int tenantId,
                                      String fromDate,
                                      String toDate) throws DeviceManagementDAOException;

    int getDevicesByDurationCount(
            List<String> statusList, String ownership, String fromDate, String toDate, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count to generate the report graph within a specific time periode
     *
     * @param request Pagination request to get paginated result
     * @param statusList Status list to filter data
     * @param tenantId ID of the current tenant
     * @param fromDate Start date to filter devices(YYYY-MM-DD)
     * @param toDate End date to filter devices(YYYY-MM-DD)
     * @return returns a list of Count objects
     * @throws DeviceManagementDAOException
     */
    List<Count> getCountOfDevicesByDuration(PaginationRequest request,
                                            List<String> statusList,
                                            int tenantId,
                                            String fromDate,
                                            String toDate) throws DeviceManagementDAOException;

    /**
     * Retrieve device location information
     * @param deviceIdentifier Device Identifier object
     * @param from Specified start timestamp
     * @param to Specified end timestamp
     * @return
     * @throws DeviceManagementDAOException
     */
    List<DeviceLocationHistorySnapshot> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from, long to)
            throws DeviceManagementDAOException;

    /**
     * This method is used to get the details of subscribed devices.
     *
     * @param deviceIds device ids of the subscribed devices.
     * @param tenantId  Id of the current tenant.
     * @param request   paginated request object.
     * @return devices - subscribed device details list
     * @throws DeviceManagementDAOException if connections establishment fails.
     */
    List<Device> getSubscribedDevices(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * @param deviceIds device ids of the subscribed devices.
     * @param tenantId  tenant id
     * @param status    current status of the device. (e.g ACTIVE, REMOVED, etc)
     * @return number of subscribed device count.
     * @throws DeviceManagementDAOException if error occurred while processing the SQL statement.
     */
    int getSubscribedDeviceCount(List<Integer> deviceIds, int tenantId, List<String> status)
            throws DeviceManagementDAOException;

    /**
     * Get a list of devices older than the given OS version of a device type
     *
     * @param request  Object with device type and OS version info
     * @param tenantId Id of the current tenant.
     * @return {@link List<Device>}
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    List<Device> getDevicesExpiredByOSVersion(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Count the number of devices older than the given OS version of a device type
     *
     * @param deviceType Device type name
     * @param osValue Generated value for the OS version
     * @param tenantId Id of the current tenant.
     * @return {@link Integer}
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    int getCountOfDeviceExpiredByOSVersion(String deviceType, Long osValue, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Get All devices for monitoring
     * @param deviceTypeId device type identifier
     * @param deviceTypeName name of the type. (android, ios ...)
     * @param activeServerCount Number of available servers
     * @param serverHashIndex server index number
     * @return device object
     * @throws DeviceManagementDAOException
     */
    List<DeviceMonitoringData> getAllDevicesForMonitoring(int deviceTypeId, String deviceTypeName,
                                                          int activeServerCount, int serverHashIndex)
            throws DeviceManagementDAOException;

    /**
     * Get a paginated list of devices filtered by given encryption status
     *
     * @param request  Object with device type and OS version info
     * @param tenantId Id of the current tenant.
     * @param isEncrypted Encryption status to be filtered.
     * @return {@link List<Device>}
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    List<Device> getDevicesByEncryptionStatus(PaginationRequest request, int tenantId, boolean isEncrypted)
            throws DeviceManagementDAOException;

    /**
     * Count the number of devices devices in the given encryption status
     *
     * @param tenantId Id of the current tenant.
     * @param isEncrypted Encryption status to be filtered.
     * @return {@link Integer}
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    int getCountOfDevicesByEncryptionStatus(int tenantId, boolean isEncrypted)
            throws DeviceManagementDAOException;

    /**
     * This method is used to get devices which have not installed the app with the given package name
     *
     * @param request Request object with device type
     * @param tenantId ID of the current tenant
     * @param packageName Package name of the application
     * @param version Version of the application
     * @return A list of device objects
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    List<Device> getAppNotInstalledDevices(PaginationRequest request,
                                      int tenantId,
                                      String packageName,
                                      String version) throws DeviceManagementDAOException;

    /**
     * This method is used to get count if devices which have not installed the app with the given package name
     *
     * @param request Request object with device type
     * @param tenantId ID of the current tenant
     * @param packageName Package name of the application
     * @param version Version of the application
     * @return Device count
     * @throws DeviceManagementDAOException Thrown if error occurs while database transactions
     */
    int getCountOfAppNotInstalledDevices(PaginationRequest request,
                                           int tenantId,
                                           String packageName,
                                           String version) throws DeviceManagementDAOException;

    int getFunctioningDevicesInSystem() throws DeviceManagementDAOException;
}
