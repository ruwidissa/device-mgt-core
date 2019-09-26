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

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;

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
}
