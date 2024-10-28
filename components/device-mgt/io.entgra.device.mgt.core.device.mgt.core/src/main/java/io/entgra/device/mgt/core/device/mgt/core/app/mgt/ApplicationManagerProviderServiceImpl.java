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

package io.entgra.device.mgt.core.device.mgt.core.app.mgt;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.ReportingPublisherManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceDetailsWrapper;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidDeviceException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.config.AppManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.util.HttpReportingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements Application Manager interface
 */
public class ApplicationManagerProviderServiceImpl implements ApplicationManagementProviderService {

    private ApplicationDAO applicationDAO;

    private static final Log log = LogFactory.getLog(ApplicationManagerProviderServiceImpl.class);

    public ApplicationManagerProviderServiceImpl(AppManagementConfig appManagementConfig) {
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
    }

    ApplicationManagerProviderServiceImpl() {
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public Application[] getApplications(String domain, int pageNumber, int size)
            throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceId, Application application,
                                        String status) throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceId,
                                       Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public Activity installApplicationForDevices(Operation operation, List<DeviceIdentifier> deviceIds)
            throws ApplicationManagementException {
        try {
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIds.size() > 0) {
                type = deviceIds.get(0).getType().toLowerCase();
            }
            Activity activity = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    addOperation(type, operation, deviceIds);
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().notifyOperationToDevices
                    (operation, deviceIds);
            return activity;
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in notify operation at app installation", e);
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
        }
    }

    @Override
    public Activity installApplicationForUsers(Operation operation, List<String> userNameList)
            throws ApplicationManagementException {

        String userName = null;
        try {
            List<Device> deviceList;
            List<DeviceIdentifier> deviceIdentifierList = new ArrayList<>();
            DeviceIdentifier deviceIdentifier;


            for (String user : userNameList) {
                userName = user;
                deviceList = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevicesOfUser
                        (user, false);
                for (Device device : deviceList) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(Integer.toString(device.getId()));
                    deviceIdentifier.setType(device.getType());

                    deviceIdentifierList.add(deviceIdentifier);
                }
            }
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIdentifierList.size() > 0) {
                type = deviceIdentifierList.get(0).getType();
            }

            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .addOperation(type, operation, deviceIdentifierList);
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in get devices for user: " + userName +
                    " in app installation", e);
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);
        }
    }

    @Override
    public Activity installApplicationForUserRoles(Operation operation, List<String> userRoleList)
            throws ApplicationManagementException {

        String userRole = null;
        try {
            List<Device> deviceList;
            List<DeviceIdentifier> deviceIdentifierList = new ArrayList<>();
            DeviceIdentifier deviceIdentifier;

            for (String role : userRoleList) {
                userRole = role;
                deviceList = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                        .getAllDevicesOfRole(userRole, false);
                for (Device device : deviceList) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(Integer.toString(device.getId()));
                    deviceIdentifier.setType(device.getType());

                    deviceIdentifierList.add(deviceIdentifier);
                }
            }
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIdentifierList.size() > 0) {
                type = deviceIdentifierList.get(0).getType();
            }
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().addOperation(type, operation,
                    deviceIdentifierList);
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in get devices for user role " + userRole +
                    " in app installation", e);

        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);
        }
    }

    @Override
    public void updateApplicationListInstalledInDevice(DeviceIdentifier deviceIdentifier,
            List<Application> applications) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating application list for device: " + deviceIdentifier.toString());
        }
        try {
            Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .getDevice(deviceIdentifier, false);
            updateApplicationListInstalledInDevice(device, applications);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred obtaining the device object for device " + deviceIdentifier.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public void updateApplicationListInstalledInDevice(Device device, List<Application> newApplications)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating application list for device: " + device.getDeviceIdentifier());
            log.debug("Apps in device: " + new Gson().toJson(newApplications));
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Application> installedAppList = applicationDAO
                    .getInstalledApplications(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
            if (log.isDebugEnabled()) {
                log.debug("Previous app list: " + new Gson().toJson(installedAppList));
            }

            Map<String, Application> appsToRemove = new HashMap<>();
            Map<String, Application> appsToUpdate = new HashMap<>();
            Map<String, Application> appsToInsert = new HashMap<>();

            Map<String, Application> installedApps = new HashMap<>();
            boolean removable;
            for (Application installedApp: installedAppList) {
                removable = true;
                for (Application newApp : newApplications) {
                    if (newApp.getApplicationIdentifier().equals(installedApp.getApplicationIdentifier())) {
                        removable = false;
                        break;
                    }
                }
                if (removable) {
                    appsToRemove.put(installedApp.getApplicationIdentifier(), installedApp);
                } else {
                    installedApps.put(installedApp.getApplicationIdentifier(), installedApp);
                }
            }

            for (Application newApp : newApplications) {
                if (newApp.getVersion() == null) {
                    newApp.setVersion("N/A");
                } else if (newApp.getVersion().length()
                        > DeviceManagementConstants.OperationAttributes.APPLIST_VERSION_MAX_LENGTH) {
                    newApp.setVersion(StringUtils.abbreviate(newApp.getVersion(),
                            DeviceManagementConstants.OperationAttributes.APPLIST_VERSION_MAX_LENGTH));
                }
                if (installedApps.containsKey(newApp.getApplicationIdentifier())) {
                    Application oldApp = installedApps.get(newApp.getApplicationIdentifier());
                    if (oldApp.isActive() != newApp.isActive() || oldApp.getMemoryUsage() != newApp.getMemoryUsage()
                            || !newApp.getVersion().equals(oldApp.getVersion()) || oldApp.isSystemApp() != newApp.isSystemApp()) {
                        newApp.setId(oldApp.getId());
                        appsToUpdate.put(newApp.getApplicationIdentifier(), newApp);
                    }
                } else {
                    appsToInsert.put(newApp.getApplicationIdentifier(), newApp);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Apps to remove: " + new Gson().toJson(appsToRemove.values()));
                log.debug("Apps to update: " + new Gson().toJson(appsToUpdate.values()));
                log.debug("Apps to insert: " + new Gson().toJson(appsToInsert.values()));
            }
            if (!appsToRemove.isEmpty()) {
                applicationDAO.removeApplications(new ArrayList<>(appsToRemove.values()), device.getId(),
                        device.getEnrolmentInfo().getId(), tenantId);
            }
            if (!appsToUpdate.isEmpty()) {
                applicationDAO.updateApplications(new ArrayList<>(appsToUpdate.values()), device.getId(),
                        device.getEnrolmentInfo().getId(), tenantId);
            }
            if (!appsToInsert.isEmpty()) {
                applicationDAO.addApplications(new ArrayList<>(appsToInsert.values()), device.getId(),
                        device.getEnrolmentInfo().getId(), tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();

            String reportingHost = HttpReportingUtil.getReportingHost();
            if (!StringUtils.isBlank(reportingHost) && HttpReportingUtil.isPublishingEnabledForTenant()) {
                DeviceDetailsWrapper deviceDetailsWrapper = new DeviceDetailsWrapper();
                deviceDetailsWrapper.setTenantId(tenantId);
                deviceDetailsWrapper.setDevice(device);
                deviceDetailsWrapper.setApplications(newApplications);
                ReportingPublisherManager reportingManager = new ReportingPublisherManager();
                reportingManager.publishData(deviceDetailsWrapper, DeviceManagementConstants
                        .Report.APP_USAGE_ENDPOINT);
            }

        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred saving application list of the device " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg =
                    "Error occurred while initializing transaction for saving application list to the device " + device
                            .getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Exception occurred saving application list of the device " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Application> getApplicationListForDevice(DeviceIdentifier deviceId)
            throws ApplicationManagementException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId,
                    false);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error occurred while fetching the device of '" +
                    deviceId.getType() + "' carrying the identifier'" + deviceId.getId(), e);
        }
        if (device == null) {
            if (log.isDebugEnabled()) {
                log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                        "' and type '" + deviceId.getType() + "'. Therefore returning empty app list");
            }
            return new ArrayList<>();
        }
        return getApplicationListForDevice(device);
    }

    @Override
    public List<Application> getApplicationListForDevice(Device device) throws ApplicationManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return applicationDAO.getInstalledApplications(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the Application List of device " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to get application " +
                    "list of the device " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Exception occurred getting application list of the device " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
