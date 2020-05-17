/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.device.details.mgt.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceDetailsWrapper;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.EventPublishingException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.core.util.HttpReportingUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInformationManagerImpl implements DeviceInformationManager {

    private final DeviceDetailsDAO deviceDetailsDAO;
    private DeviceDAO deviceDAO;
    private static final Log log = LogFactory.getLog(DeviceInformationManagerImpl.class);
    private static final String LOCATION_EVENT_STREAM_DEFINITION = "org.wso2.iot.LocationStream";
    private static final String DEVICE_INFO_EVENT_STREAM_DEFINITION = "org.wso2.iot.DeviceInfoStream";

    public DeviceInformationManagerImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceDetailsDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
    }

    @Override
    public void addDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo) throws DeviceDetailsMgtException {
        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceId, false);
            addDeviceInfo(device, deviceInfo);
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while retrieving the device information.", e);
        }
    }

    @Override
    public void addDeviceInfo(Device device, DeviceInfo deviceInfo) throws DeviceDetailsMgtException {
        try {

            DeviceDetailsWrapper deviceDetailsWrapper = new DeviceDetailsWrapper();
            deviceDetailsWrapper.setDeviceInfo(deviceInfo);
            publishEvents(device, deviceDetailsWrapper, DeviceManagementConstants.Report.DEVICE_INFO_PARAM);

            DeviceManagementDAOFactory.beginTransaction();
            DeviceInfo newDeviceInfo;
            DeviceInfo previousDeviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId(),
                    device.getEnrolmentInfo().getId());
            Map<String, String> previousDeviceProperties = deviceDetailsDAO.getDeviceProperties(device.getId(),
                    device.getEnrolmentInfo().getId());
            if (previousDeviceInfo != null) {
                previousDeviceInfo.setDeviceDetailsMap(new HashMap<>());
                newDeviceInfo = processDeviceInfo(previousDeviceInfo, deviceInfo);
                deviceDetailsDAO.updateDeviceInformation(device.getId(), device.getEnrolmentInfo().getId(),
                        newDeviceInfo);
            } else {
                deviceDetailsDAO.addDeviceInformation(device.getId(), device.getEnrolmentInfo().getId(), deviceInfo);
                newDeviceInfo = deviceInfo;
            }
            if (previousDeviceProperties.isEmpty()) {
                deviceDetailsDAO.addDeviceProperties(newDeviceInfo.getDeviceDetailsMap(), device.getId(),
                        device.getEnrolmentInfo().getId());
            } else {
                Map<String, String> updatableProps = new HashMap<>();
                Map<String, String> injectableProps = new HashMap<>();
                // generate a default value depending on the devices OS version
                addOSVersionValue(device, newDeviceInfo);
                for (String key : newDeviceInfo.getDeviceDetailsMap().keySet()) {
                    if (previousDeviceProperties.containsKey(key)) {
                        String val = previousDeviceProperties.get(key);
                        if (val != null &&!val.equals(newDeviceInfo.getDeviceDetailsMap().get(key))) {
                            updatableProps.put(key, newDeviceInfo.getDeviceDetailsMap().get(key));
                        }
                    } else {
                        injectableProps.put(key, newDeviceInfo.getDeviceDetailsMap().get(key));
                    }
                }
                deviceDetailsDAO.updateDeviceProperties(updatableProps, device.getId(),
                        device.getEnrolmentInfo().getId());
                deviceDetailsDAO.addDeviceProperties(injectableProps, device.getId(),
                        device.getEnrolmentInfo().getId());
            }
            deviceDAO.updateDevice(device, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            DeviceManagementDAOFactory.commitTransaction();

            //TODO :: This has to be fixed by adding the enrollment ID.
            if (DeviceManagerUtil.isPublishDeviceInfoResponseEnabled()) {
                Object[] metaData = {device.getDeviceIdentifier(), device.getType()};
                Object[] payload = new Object[]{
                        Calendar.getInstance().getTimeInMillis(),
                        deviceInfo.getDeviceDetailsMap().get("IMEI"),
                        deviceInfo.getDeviceDetailsMap().get("IMSI"),
                        deviceInfo.getDeviceModel(),
                        deviceInfo.getVendor(),
                        deviceInfo.getOsVersion(),
                        deviceInfo.getOsBuildDate(),
                        deviceInfo.getBatteryLevel(),
                        deviceInfo.getInternalTotalMemory(),
                        deviceInfo.getInternalAvailableMemory(),
                        deviceInfo.getExternalTotalMemory(),
                        deviceInfo.getExternalAvailableMemory(),
                        deviceInfo.getOperator(),
                        deviceInfo.getConnectionType(),
                        deviceInfo.getMobileSignalStrength(),
                        deviceInfo.getSsid(),
                        deviceInfo.getCpuUsage(),
                        deviceInfo.getTotalRAMMemory(),
                        deviceInfo.getAvailableRAMMemory(),
                        deviceInfo.isPluggedIn()
                };
                DeviceManagerUtil.getEventPublisherService().publishEvent(
                        DEVICE_INFO_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                );
            }
        } catch (TransactionManagementException e) {
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device information.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device information.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Error occurred while retrieving the device information.", e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while updating the last update timestamp of the " +
                    "device", e);
        } catch (DataPublisherConfigurationException e) {
            throw new DeviceDetailsMgtException("Error occurred while publishing the device location information.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    public int publishEvents(String deviceId, String deviceType, String payload, String eventType)
            throws DeviceDetailsMgtException {

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);

        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceIdentifier, false);
            DeviceDetailsWrapper deviceDetailsWrapper = new DeviceDetailsWrapper();
            deviceDetailsWrapper.setEvents(payload);
            return publishEvents(device, deviceDetailsWrapper, eventType);
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Event publishing error. Could not get device " + deviceId;
            log.error(msg, e);
            throw new DeviceDetailsMgtException(msg, e);
        }
    }

    /**
     * Send device details from core to reporting backend
     * @param device Device that is sending event
     * @param deviceDetailsWrapper Payload to send(example, deviceinfo, applist, raw events)
     */
    private int publishEvents(Device device, DeviceDetailsWrapper deviceDetailsWrapper, String
            eventType)  {
        String reportingHost = HttpReportingUtil.getReportingHost();
        if (!StringUtils.isBlank(reportingHost)
                && HttpReportingUtil.isPublishingEnabledForTenant()) {
            try {
                deviceDetailsWrapper.setDevice(device);
                deviceDetailsWrapper.setTenantId(DeviceManagerUtil.getTenantId());
                GroupManagementProviderService groupManagementService = DeviceManagementDataHolder
                        .getInstance().getGroupManagementProviderService();

                List<DeviceGroup> groups = groupManagementService.getGroups(device, false);
                if (groups != null && groups.size() > 0) {
                    deviceDetailsWrapper.setGroups(groups);
                }

                String[] rolesOfUser = DeviceManagerUtil.getRolesOfUser(CarbonContext
                        .getThreadLocalCarbonContext()
                        .getUsername());
                if (rolesOfUser != null && rolesOfUser.length > 0) {
                    deviceDetailsWrapper.setRole(rolesOfUser);
                }

                String eventUrl = reportingHost + DeviceManagementConstants.Report
                        .REPORTING_CONTEXT + DeviceManagementConstants.URL_SEPERATOR + eventType;
                return HttpReportingUtil.invokeApi(deviceDetailsWrapper.getJSONString(), eventUrl);
            } catch (EventPublishingException e) {
                log.error("Error occurred while sending events", e);
            } catch (GroupManagementException e) {
                log.error("Error occurred while getting group list", e);
            } catch (UserStoreException e) {
                log.error("Error occurred while getting role list", e);
            }
        } else {
            if(log.isTraceEnabled()) {
                log.trace("Event publishing is not enabled for tenant "
                        + DeviceManagerUtil.getTenantId());
            }
        }
        return 0;
    }

    @Override
    public DeviceInfo getDeviceInfo(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device = getDevice(deviceId);
        if (device == null) {
            return null;
        }
        return getDeviceInfo(device);
    }

    @Override
    public DeviceInfo getDeviceInfo(Device device) throws DeviceDetailsMgtException {
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceInfo deviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId(),
                    device.getEnrolmentInfo().getId());
            if (deviceInfo == null) {
                deviceInfo = new DeviceInfo();
            }
            deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(device.getId(),
                    device.getEnrolmentInfo().getId()));
            DeviceLocation location = deviceDetailsDAO.getDeviceLocation(device.getId(),
                    device.getEnrolmentInfo().getId());
            if (location != null) {
                //There are some cases where the device-info is not updated properly. Hence returning a null value.
                deviceInfo.setLocation(location);
            }
            return deviceInfo;
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device " +
                    device.getDeviceIdentifier() + "'s info from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device details.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceInfo> getDevicesInfo(List<DeviceIdentifier> deviceIdentifiers) throws DeviceDetailsMgtException {
        List<DeviceInfo> deviceInfos = new ArrayList<>();

        Map<String, DeviceIdentifier> identifierMap = new HashMap<>();
        for (DeviceIdentifier identifier : deviceIdentifiers) {
            identifierMap.put(identifier.getId(), identifier);
        }
        try {
            List<Device> deviceIds = new ArrayList<>();
            List<Device> devices = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    getAllDevices(false);
            for (Device device : devices) {
                if (identifierMap.containsKey(device.getDeviceIdentifier()) &&
                        device.getType().equals(identifierMap.get(device.getDeviceIdentifier()).getType())) {
                    deviceIds.add(device);
                }
            }
            DeviceManagementDAOFactory.openConnection();
            DeviceInfo deviceInfo;
            for (Device device : deviceIds) {
                deviceInfo = deviceDetailsDAO.getDeviceInformation(device.getId(),
                        device.getEnrolmentInfo().getId());
                if (deviceInfo == null) {
                    deviceInfo = new DeviceInfo();
                }
                deviceInfo.setDeviceDetailsMap(deviceDetailsDAO.getDeviceProperties(device.getId(),
                        device.getEnrolmentInfo().getId()));
                DeviceLocation location = deviceDetailsDAO.getDeviceLocation(device.getId(),
                        device.getEnrolmentInfo().getId());
                if (location != null) {
                    //There are some cases where the device-info is not updated properly. Hence returning a null value.
                    deviceInfo.setLocation(location);
                }
                deviceInfos.add(deviceInfo);
            }
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving devices from database.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the devices.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving devices details.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceInfos;
    }

    @Override
    @Deprecated
    public void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {
        try {
            Device device = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getDevice(deviceLocation.getDeviceIdentifier(), false);
            addDeviceLocation(device, deviceLocation);
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Error occurred while updating the last updated timestamp of " +
                    "the device", e);
        }
    }

    @Override
    public void addDeviceLocation(Device device, DeviceLocation deviceLocation) throws DeviceDetailsMgtException {
        try {
            deviceLocation.setDeviceId(device.getId());
            DeviceManagementDAOFactory.beginTransaction();
            DeviceLocation previousLocation = deviceDetailsDAO.getDeviceLocation(device.getId(),
                    device.getEnrolmentInfo().getId());
            if (previousLocation == null) {
                deviceDetailsDAO.addDeviceLocation(deviceLocation, device.getEnrolmentInfo().getId());
            } else {
                deviceDetailsDAO.updateDeviceLocation(deviceLocation, device.getEnrolmentInfo().getId());
            }
            deviceDetailsDAO.addDeviceLocationInfo(device, deviceLocation,
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (DeviceManagerUtil.isPublishLocationResponseEnabled()) {
                Object[] metaData = {device.getDeviceIdentifier(), device.getEnrolmentInfo().getOwner(), device.getType()};
                Object[] payload = new Object[]{
                        deviceLocation.getUpdatedTime().getTime(),
                        deviceLocation.getLatitude(),
                        deviceLocation.getLongitude(),
                        deviceLocation.getAltitude(),
                        deviceLocation.getSpeed(),
                        deviceLocation.getBearing(),
                        deviceLocation.getDistance()
                };
                DeviceManagerUtil.getEventPublisherService().publishEvent(
                        LOCATION_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                );
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            throw new DeviceDetailsMgtException("Transactional error occurred while adding the device location " +
                    "information.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while adding the device location information.", e);
        } catch (DeviceManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while getting the device information.", e);
        } catch (DataPublisherConfigurationException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceDetailsMgtException("Error occurred while publishing the device location information.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceLocation getDeviceLocation(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device = getDevice(deviceId);
        if (device == null) {
            return null;
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDetailsDAO.getDeviceLocation(device.getId(), device.getEnrolmentInfo().getId());
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device location.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private Device getDevice(DeviceIdentifier deviceId) throws DeviceDetailsMgtException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                            "' and type '" + deviceId.getType() + "'. Therefore returning null");
                }
                return null;
            }
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the device.", e);
        }
        return device;
    }

    @Override
    public List<DeviceLocation> getDeviceLocations(
            List<DeviceIdentifier> deviceIdentifiers) throws DeviceDetailsMgtException {

        try {
            List<Device> devices = DeviceManagementDataHolder.getInstance().
                    getDeviceManagementProvider().getAllDevices(deviceIdentifiers.get(0).getType(), false);
            List<DeviceLocation> deviceLocations = new ArrayList<>();
            DeviceManagementDAOFactory.openConnection();
            DeviceLocation deviceLocation;
            for (Device device : devices) {
                deviceLocation = deviceDetailsDAO.getDeviceLocation(device.getId(),
                        device.getEnrolmentInfo().getId());
                if (deviceLocation != null) {
                    deviceLocations.add(deviceLocation);
                }
            }
            return deviceLocations;
        } catch (DeviceManagementException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving the devices.", e);
        } catch (SQLException e) {
            throw new DeviceDetailsMgtException("SQL error occurred while retrieving device from database.", e);
        } catch (DeviceDetailsMgtDAOException e) {
            throw new DeviceDetailsMgtException("Exception occurred while retrieving device locations.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private DeviceInfo processDeviceInfo(DeviceInfo previousDeviceInfo, DeviceInfo newDeviceInfo) {
        if (newDeviceInfo.getDeviceModel().isEmpty()) {
            newDeviceInfo.setDeviceModel(previousDeviceInfo.getDeviceModel());
        }
        if (newDeviceInfo.getVendor().isEmpty()) {
            newDeviceInfo.setVendor(previousDeviceInfo.getVendor());
        }
        if (newDeviceInfo.getOsBuildDate().isEmpty()) {
            newDeviceInfo.setOsBuildDate(previousDeviceInfo.getOsBuildDate());
        }
        if (newDeviceInfo.getOsVersion().isEmpty()) {
            newDeviceInfo.setOsVersion(previousDeviceInfo.getOsVersion());
        }
        if (newDeviceInfo.getBatteryLevel() == -1D) {
            newDeviceInfo.setBatteryLevel(previousDeviceInfo.getBatteryLevel());
        }
        if (newDeviceInfo.getInternalTotalMemory() == -1D) {
            newDeviceInfo.setInternalTotalMemory(previousDeviceInfo.getInternalTotalMemory());
        }
        if (newDeviceInfo.getInternalAvailableMemory() == -1D) {
            newDeviceInfo.setInternalAvailableMemory(previousDeviceInfo.getInternalAvailableMemory());
        }
        if (newDeviceInfo.getExternalTotalMemory() == -1D) {
            newDeviceInfo.setExternalTotalMemory(previousDeviceInfo.getExternalTotalMemory());
        }
        if (newDeviceInfo.getExternalAvailableMemory() == -1D) {
            newDeviceInfo.setExternalAvailableMemory(previousDeviceInfo.getExternalAvailableMemory());
        }
        if (newDeviceInfo.getOperator().isEmpty()) {
            newDeviceInfo.setOperator(previousDeviceInfo.getOperator());
        }
        if (newDeviceInfo.getConnectionType().isEmpty()) {
            newDeviceInfo.setConnectionType(previousDeviceInfo.getConnectionType());
        }
        if (newDeviceInfo.getMobileSignalStrength() == 0.0) {
            newDeviceInfo.setMobileSignalStrength(previousDeviceInfo.getMobileSignalStrength());
        }
        if (newDeviceInfo.getSsid().isEmpty()) {
            newDeviceInfo.setSsid(previousDeviceInfo.getSsid());
        }
        if (newDeviceInfo.getCpuUsage() == 0.0) {
            newDeviceInfo.setCpuUsage(previousDeviceInfo.getCpuUsage());
        }
        if (newDeviceInfo.getTotalRAMMemory() == -1D) {
            newDeviceInfo.setTotalRAMMemory(previousDeviceInfo.getTotalRAMMemory());
        }
        if (newDeviceInfo.getAvailableRAMMemory() == -1D) {
            newDeviceInfo.setAvailableRAMMemory(previousDeviceInfo.getAvailableRAMMemory());
        }
        Map<String, String> newDeviceDetailsMap = newDeviceInfo.getDeviceDetailsMap();
        Map<String, String> previousDeviceDetailsMap = previousDeviceInfo.getDeviceDetailsMap();
        for (String eachKey : previousDeviceDetailsMap.keySet()) {
            if (!newDeviceDetailsMap.containsKey(eachKey)) {
                newDeviceDetailsMap.put(eachKey, previousDeviceDetailsMap.get(eachKey));
            }
        }
        return newDeviceInfo;
    }


    /**
     * Generate and add a value depending on the device's OS version included in device info
     *
     * @param device        device data
     * @param newDeviceInfo device info data
     */
    private void addOSVersionValue(Device device, DeviceInfo newDeviceInfo) {
        String deviceTypeName = device.getType();
        if (StringUtils.isBlank(device.getType())) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to generate a OS version value for device type: " +
                          deviceTypeName + ". Device type cannot be null or empty");
            }
        } else {
            if (!deviceTypeName.equals("android") && !deviceTypeName.equals("ios")) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to generate a OS version value for device type: " +
                              deviceTypeName + ". OS version value is only generatable for " +
                              "android and ios");
                }
            } else {
                String osVersion = newDeviceInfo.getOsVersion();
                String osValue = String.valueOf(DeviceManagerUtil.generateOSVersionValue(osVersion));
                if (StringUtils.isBlank(osValue)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to generate a OS version value for OS version: " +
                                  osVersion + " for device type: " + deviceTypeName);
                    }
                } else {
                    newDeviceInfo.getDeviceDetailsMap().put(Constants.OS_VALUE, osValue);
                }
            }
        }
    }

}

