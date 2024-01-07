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

package io.entgra.device.mgt.core.device.mgt.common.metadata.mgt;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;

import java.util.List;


public interface DeviceStatusManagementService {

    /**
     * This method is useful to create & persist default device status filter for provided tenant if
     * it doesn't exist already
     *
     * @throws MetadataManagementException if error while adding default device status
     */
    void addDefaultDeviceStatusFilterIfNotExist(int tenantId) throws MetadataManagementException;

    /**
     * This method is useful to reset existing device status to default values in xml
     *
     * @throws MetadataManagementException if error while resetting default device status
     */
    void resetToDefaultDeviceStausFilter() throws MetadataManagementException;

    /**
     * This method is useful to update existing allowed device status
     *
     * @throws MetadataManagementException if error while updating existing device status
     */
    void updateDefaultDeviceStatusFilters(int tenantId, String deviceType, List<String> deviceStatus)
            throws MetadataManagementException;

    /**
     * This method is useful to update existing device status check
     *
     * @throws MetadataManagementException if error while updating existing device status
     */
    boolean updateDefaultDeviceStatusCheck(int tenantId, boolean isChecked)
            throws MetadataManagementException;
    /**
     * This method is useful to get existing device status filters
     *
     * @throws MetadataManagementException if error while getting existing device status
     */
    List<AllowedDeviceStatus> getDeviceStatusFilters(int tenantId) throws MetadataManagementException;

    /**
     * This method is useful to get existing device status filters by device type and tenant id
     *
     * @throws MetadataManagementException if error while getting existing device status
     */
    List<String> getDeviceStatusFilters(String deviceType, int tenantId) throws MetadataManagementException;

    /**
     * This method is useful to get existing device status filters
     *
     * @throws MetadataManagementException if error while getting existing device status check
     */
    boolean getDeviceStatusCheck(int tenantId) throws MetadataManagementException;

    /**
     * This method is useful to check status is valid for device type
     *
     * @throws MetadataManagementException if error while getting existing device status check
     */
    boolean isDeviceStatusValid(String deviceType, String deviceStatus, int tenantId) throws MetadataManagementException;
}
