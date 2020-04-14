/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.common.report.mgt;

import com.google.gson.JsonObject;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;

import java.util.List;

/**
 * This is the service class for reports which connects with DAO layer
 */
public interface ReportManagementService {

    /**
     * This method is used to call the getDevicesByDuration method from DeviceDAO
     *
     * @param request  Pagination Request to get a paginated result
     * @param fromDate Start date to filter devices(YYYY-MM-DD)
     * @param toDate   End date to filter devices(YYYY-MM-DD)
     * @return {@link PaginationResult}
     * @throws {@Link DeviceManagementException} When error occurred while validating device list page size
     * @throws {@Link ReportManagementException} When failed to retrieve devices.
     */
    PaginationResult getDevicesByDuration(PaginationRequest request, String fromDate, String toDate)
            throws ReportManagementException;

    int getDevicesByDurationCount(List<String> statusList, String ownership, String fromDate, String toDate)
            throws ReportManagementException;

    JsonObject getCountOfDevicesByDuration(PaginationRequest request, List<String> statusList, String fromDate, String toDate)
            throws ReportManagementException;

    /**
     * Get a list of devices with the count which are older than the given OS version
     *
     * @param request {@link PaginationRequest}
     * @return {@link PaginationResult}
     * @throws ReportManagementException Might occur during the business logic or building database query
     * @throws BadRequestException Might occur if the given os version or the device type doesn't match
     */
    PaginationResult getDevicesExpiredByOSVersion(PaginationRequest request)
            throws ReportManagementException, BadRequestException;

    /**
     * Get a paginated list of devices which is filtered by given encryption status
     *
     * @param request {@link PaginationRequest}
     * @return {@link PaginationResult}
     * @throws ReportManagementException Might occur during the business logic or building database query
     */
    PaginationResult getDevicesByEncryptionStatus(PaginationRequest request, boolean isEncrypted)
            throws ReportManagementException;

    /**
     * This method is used to get devices which have not installed the app with the given package name
     *
     * @param request Request object with device type
     * @param packageName Package name of the application
     * @param version Version of the application
     * @return {@link PaginationResult}
     * @throws ReportManagementException
     * @throws DeviceTypeNotFoundException
     */
    PaginationResult getAppNotInstalledDevices(PaginationRequest request, String packageName, String version)
            throws ReportManagementException, DeviceTypeNotFoundException;

    /**
     * This method is used to get devices which have not assigned to groups.
     *
     * @param paginationRequest Request object with offset and limit
     * @param groupNames default group names that should be omitted when checking the device
     *                  whether they have been assigned to groups
     * @return {@link PaginationResult}
     * @throws ReportManagementException Might occur when opening a connection to the data source.
     * @throws DeviceTypeNotFoundException Might occur when required device type is not found.
     */

    PaginationResult getDeviceNotAssignedToGroups(PaginationRequest paginationRequest,
                                                  List<String> groupNames)
            throws ReportManagementException, DeviceTypeNotFoundException;
}
