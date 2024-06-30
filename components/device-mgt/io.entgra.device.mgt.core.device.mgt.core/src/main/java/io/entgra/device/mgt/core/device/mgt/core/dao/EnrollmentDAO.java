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
package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo.Status;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceDetailsDTO;
import io.entgra.device.mgt.core.device.mgt.core.dto.OwnerWithDeviceDTO;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface EnrollmentDAO {

    EnrolmentInfo addEnrollment(int deviceId, DeviceIdentifier deviceIdentifier,
                                EnrolmentInfo enrolmentInfo, int tenantId) throws DeviceManagementDAOException;

    int updateEnrollment(EnrolmentInfo enrolmentInfo, int tenantId) throws DeviceManagementDAOException;

    boolean updateEnrollmentStatus(List<EnrolmentInfo> enrolmentInfos) throws DeviceManagementDAOException;

    int removeEnrollment(int deviceId, String currentOwner, int tenantId) throws DeviceManagementDAOException;

    @Deprecated
    boolean setStatus(String currentOwner, Status status, int tenantId) throws DeviceManagementDAOException;

    boolean setStatusAllDevices(String currentOwner, Status status, int tenantId) throws DeviceManagementDAOException;

    boolean setStatus(int enrolmentId, Status status, int tenantId) throws DeviceManagementDAOException;

    Status getStatus(int deviceId, String currentOwner, int tenantId) throws DeviceManagementDAOException;

    EnrolmentInfo getEnrollment(int deviceId, String currentUser, int tenantId) throws DeviceManagementDAOException;

    EnrolmentInfo getEnrollment(int deviceId, int tenantId) throws DeviceManagementDAOException;

    List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user, int tenantId) throws
                                                                                             DeviceManagementDAOException;

    /***
     *This method is used to update the owner of the enrollment for given set of devices to given user.
     *
     * @param devices List of devices.
     * @param owner Username of the new device owner.
     * @param tenantId tenant id.
     * @return either (1) true, if device owner updating is succeed or false.
     * @throws DeviceManagementDAOException if an error occurs when updating device owner.
     */
    boolean updateOwnerOfEnrollment(List<Device> devices, String owner, int tenantId)
            throws DeviceManagementDAOException;

    /***
     *This method is used to add the device status of the enrollment for given set of devices to given user.
     *
     * @param currentOwner of device.
     * @param status going to add
     * @param tenantId tenant id.
     * @return either (1) true, if device status is succeeded or false.
     * @throws DeviceManagementDAOException if an error occurs when updating device owner.
     */
    boolean addDeviceStatus(String currentOwner, EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

    /***
     *This method is used to add the device status of the enrollment for given set of devices to given user.
     *
     * @param config of Enrollment.
     * @return either (1) true, if device status is succeeded or false.
     * @throws DeviceManagementDAOException if an error occurs when updating device owner.
     */
    boolean addDeviceStatus(EnrolmentInfo config) throws DeviceManagementDAOException;

    /***
     *This method is used to add the device status of the enrollment for given set of devices to given user.
     *
     * @param enrolmentId of device.
     * @param status going to add
     * @return either (1) true, if device status is succeeded or false.
     * @throws DeviceManagementDAOException if an error occurs when updating device owner.
     */
    boolean addDeviceStatus(int enrolmentId, EnrolmentInfo.Status status) throws DeviceManagementDAOException;

    /**
     * Retrieves owners and the list of device IDs related to an owner.
     *
     * @param owner the owner whose device IDs need to be retrieved
     * @param tenantId the ID of the tenant
     * @return {@link OwnerWithDeviceDTO} which contains a list of devices related to a user
     * @throws DeviceManagementDAOException if an error occurs while fetching the data
     */
    OwnerWithDeviceDTO getOwnersWithDevices(String owner, int tenantId) throws DeviceManagementDAOException;

    /**
     * Retrieves a list of device IDs with owners and device status.
     *
     * @param deviceId the deviceId of the device which user need to be retrieved
     * @param tenantId the ID of the tenant
     * @return {@link OwnerWithDeviceDTO} which contains a list of devices
     * @throws DeviceManagementDAOException if an error occurs while fetching the data
     */
    OwnerWithDeviceDTO getOwnerWithDeviceByDeviceId(int deviceId, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Retrieves owners and the list of device IDs with device status.
     *
     * @param tenantId the ID of the tenant
     * @return {@link OwnerWithDeviceDTO} which contains a list of devices related to a user
     * @throws DeviceManagementDAOException if an error occurs while fetching the data
     */
    List<DeviceDetailsDTO> getDevicesByTenantId(int tenantId) throws DeviceManagementDAOException;
}
