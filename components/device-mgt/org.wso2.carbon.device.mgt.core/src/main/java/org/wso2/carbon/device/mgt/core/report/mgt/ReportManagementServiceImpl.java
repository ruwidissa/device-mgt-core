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
package org.wso2.carbon.device.mgt.core.report.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * This is the service class for reports which calls dao classes and its method which are used for
 * report generation tasks.
 */
public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(ReportManagementServiceImpl.class);

    private DeviceDAO deviceDAO;

    public ReportManagementServiceImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public PaginationResult getDevicesByDuration(PaginationRequest request, List<String> statusList, String fromDate,
                                                 String toDate)
            throws ReportManagementException {
        PaginationResult paginationResult = new PaginationResult();
        try {
            request = DeviceManagerUtil.validateDeviceListPageSize(request);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            List<Device> devices = deviceDAO.getDevicesByDuration(
                    request,
                    statusList,
                    DeviceManagementDAOUtil.getTenantId(),
                    fromDate,
                    toDate
            );
            paginationResult.setData(devices);
            //TODO: Should change the following code to a seperate count method from deviceDAO to get the count
            paginationResult.setRecordsTotal(devices.size());
            return paginationResult;
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection " +
                         "to the data source";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDevicesByDurationCount(List<String> statusList, String ownership, String fromDate, String toDate)
            throws ReportManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesByDurationCount(statusList, ownership, fromDate, toDate, DeviceManagementDAOUtil.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in while retrieving device count by status for " + statusList + "devices.";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new ReportManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
