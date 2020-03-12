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
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.common.InitialOperationConfig;
import org.wso2.carbon.device.mgt.common.DeviceTransferRequest;
import org.wso2.carbon.device.mgt.common.DevicePropertyNotification;
import org.wso2.carbon.device.mgt.common.DeviceEnrollmentInfoNotification;
import org.wso2.carbon.device.mgt.common.DeviceNotification;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.UnauthorizedDeviceAccessException;
import org.wso2.carbon.device.mgt.common.exceptions.UserNotFoundException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.configuration.mgt.AmbiguousConfigurationException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DevicePropertyInfo;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistory;
import org.wso2.carbon.device.mgt.common.enrollment.notification.EnrollmentNotificationConfiguration;
import org.wso2.carbon.device.mgt.common.enrollment.notification.EnrollmentNotifier;
import org.wso2.carbon.device.mgt.common.enrollment.notification.EnrollmentNotifierException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationSubscriber;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypePlatformDetails;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypePlatformVersion;
import org.wso2.carbon.device.mgt.common.ui.policy.mgt.PolicyConfigurationManager;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.cache.DeviceCacheKey;
import org.wso2.carbon.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeServiceIdentifier;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeVersion;
import org.wso2.carbon.device.mgt.core.geo.GeoCluster;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.PluginInitializationListener;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.email.sender.core.ContentProviderInfo;
import org.wso2.carbon.email.sender.core.EmailContext;
import org.wso2.carbon.email.sender.core.EmailSendingFailedException;
import org.wso2.carbon.email.sender.core.EmailTransportNotConfiguredException;
import org.wso2.carbon.email.sender.core.TypedValue;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class DeviceManagementProviderServiceImpl implements DeviceManagementProviderService,
        PluginInitializationListener {

    private static Log log = LogFactory.getLog(DeviceManagementProviderServiceImpl.class);
    private static final String OPERATION_RESPONSE_EVENT_STREAM_DEFINITION = "org.wso2.iot.OperationResponseStream";
    private DeviceDAO deviceDAO;
    private DeviceDetailsDAO deviceInfoDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private EnrollmentDAO enrollmentDAO;
    private ApplicationDAO applicationDAO;
    private DeviceManagementPluginRepository pluginRepository;
    private DeviceInformationManager deviceInformationManager;

    public DeviceManagementProviderServiceImpl() {
        this.pluginRepository = new DeviceManagementPluginRepository();
        this.deviceInformationManager = new DeviceInformationManagerImpl();
        initDataAccessObjects();
        /* Registering a listener to retrieve events when some device management service plugin is installed after
         * the component is done getting initialized */
        DeviceManagementServiceComponent.registerPluginInitializationListener(this);
    }

    private void initDataAccessObjects() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceInfoDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration configuration) throws DeviceManagementException {
        DeviceManager dms =
                pluginRepository.getDeviceManagementService(configuration.getType(),
                        this.getTenantId()).getDeviceManager();
        return dms.saveConfiguration(configuration);
    }

    @Override
    public PlatformConfiguration getConfiguration(String deviceType) throws DeviceManagementException {
        DeviceManager dms =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId()).getDeviceManager();
        if (dms == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device type '" + deviceType + "' does not have an associated device management " +
                        "plugin registered within the framework. Therefore, not attempting getConfiguration method");
            }
            return null;
        }
        return dms.getConfiguration();
    }

    @Override
    public FeatureManager getFeatureManager(String deviceType) throws DeviceTypeNotFoundException {
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'getFeatureManager'");
            }
            throw new DeviceTypeNotFoundException("Device type '" + deviceType + "' not found.");
        }
        return deviceManager.getFeatureManager();
    }

    @Override
    public PolicyConfigurationManager getPolicyUIConfigurationManager(String deviceType) throws DeviceTypeNotFoundException {
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        if (deviceManager == null) {
            String msg = "Device type '" + deviceType + "' not found.";
            log.error(msg);
            throw new DeviceTypeNotFoundException(msg);
        }
        return deviceManager.getPolicyUIConfigurationManager();
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        if (device == null) {
            String msg = "Received empty device for device enrollment";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Enrolling the device " + device.getId() + "of type '" + device.getType() + "'");
        }
        boolean status = false;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'enrollDevice'");
            }
            return false;
        }
        deviceManager.enrollDevice(device);
        if (deviceManager.isClaimable(deviceIdentifier)) {
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.INACTIVE);
        } else {
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.ACTIVE);
        }
        int tenantId = this.getTenantId();

        Device existingDevice = this.getDevice(deviceIdentifier, false);

        if (existingDevice != null) {
            EnrolmentInfo existingEnrolmentInfo = existingDevice.getEnrolmentInfo();
            EnrolmentInfo newEnrolmentInfo = device.getEnrolmentInfo();
            if (existingEnrolmentInfo != null && newEnrolmentInfo != null) {
                //Get all the enrollments of current user for the same device
                List<EnrolmentInfo> enrolmentInfos = this.getEnrollmentsOfUser(existingDevice.getId(),
                        newEnrolmentInfo.getOwner());
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    //If the enrollments are same then we'll update the existing enrollment.
                    if (enrolmentInfo.equals(newEnrolmentInfo)) {
                        device.setId(existingDevice.getId());
                        device.getEnrolmentInfo().setDateOfEnrolment(enrolmentInfo.getDateOfEnrolment());
                        device.getEnrolmentInfo().setDateOfLastUpdate(enrolmentInfo.getDateOfLastUpdate());
                        device.getEnrolmentInfo().setId(enrolmentInfo.getId());
                        this.modifyEnrollment(device);
                        status = true;
                        break;
                    }
                }
                if (!status) {
                    int updateStatus = 0;
                    EnrolmentInfo enrollment;
                    try {
                        //Remove the existing enrollment
                        DeviceManagementDAOFactory.beginTransaction();
                        if (!EnrolmentInfo.Status.REMOVED.equals(existingEnrolmentInfo.getStatus())) {
                            existingEnrolmentInfo.setStatus(EnrolmentInfo.Status.REMOVED);
                            updateStatus = enrollmentDAO.updateEnrollment(existingEnrolmentInfo);
                        }
                        if ((updateStatus > 0) || EnrolmentInfo.Status.REMOVED.
                                equals(existingEnrolmentInfo.getStatus())) {
                            enrollment = enrollmentDAO.
                                    addEnrollment(existingDevice.getId(), newEnrolmentInfo, tenantId);
                            if (enrollment == null ){
                                DeviceManagementDAOFactory.rollbackTransaction();
                                throw new DeviceManagementException(
                                        "Enrollment data persistence is failed in a re-enrollment. Device id : "
                                                + existingDevice.getId() + " Device Identifier: " + device
                                                .getDeviceIdentifier());
                            }
                            device.setEnrolmentInfo(enrollment);
                            DeviceManagementDAOFactory.commitTransaction();
                            this.removeDeviceFromCache(deviceIdentifier);
                            if (log.isDebugEnabled()) {
                                log.debug("An enrolment is successfully added with the id '" + enrollment.getId() +
                                        "' associated with " + "the device identified by key '" +
                                        device.getDeviceIdentifier() + "', which belongs to " + "platform '" +
                                        device.getType() + " upon the user '" + device.getEnrolmentInfo().getOwner() +
                                        "'");
                            }
                            status = true;
                        } else {
                            log.warn("Unable to update device enrollment for device : " + device.getDeviceIdentifier() +
                                    " belonging to user : " + device.getEnrolmentInfo().getOwner());
                        }
                    } catch (DeviceManagementDAOException e) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        String msg = "Error occurred while adding enrolment related metadata for device: " + device.getId();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    } catch (Exception e) {
                        String msg = "Error occurred while enrolling device: " + device.getId();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    } finally {
                        DeviceManagementDAOFactory.closeConnection();
                    }
                }
            }
        } else {
            EnrolmentInfo enrollment;
            try {
                DeviceManagementDAOFactory.beginTransaction();
                DeviceType type = deviceTypeDAO.getDeviceType(device.getType(), tenantId);
                if (type != null) {
                    int deviceId = deviceDAO.addDevice(type.getId(), device, tenantId);
                    enrollment = enrollmentDAO.addEnrollment(deviceId, device.getEnrolmentInfo(), tenantId);
                    if (enrollment == null ){
                        DeviceManagementDAOFactory.rollbackTransaction();
                        throw new DeviceManagementException(
                                "Enrollment data persistence is failed in a new enrollment. Device id: " + deviceId
                                        + " Device Identifier: " + device.getDeviceIdentifier());
                    }
                    device.setEnrolmentInfo(enrollment);
                    DeviceManagementDAOFactory.commitTransaction();
                } else {
                    DeviceManagementDAOFactory.rollbackTransaction();
                    throw new DeviceManagementException("No device type registered with name - " + device.getType()
                            + " and hence unable to find succeed the enrollment of device - "
                            + device.getDeviceIdentifier());
                }
            } catch (DeviceManagementDAOException e) {
                DeviceManagementDAOFactory.rollbackTransaction();
                String msg = "Error occurred while adding metadata of '" + device.getType() +
                        "' device carrying the identifier '" + device.getDeviceIdentifier() + "'";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (TransactionManagementException e) {
                String msg = "Error occurred while initiating transaction";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred while enrolling device: " + device.getId();
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }

            if (log.isDebugEnabled()) {
                log.debug("An enrolment is successfully created with the id '" + enrollment.getId() + "' associated with " +
                        "the device identified by key '" + device.getDeviceIdentifier() + "', which belongs to " +
                        "platform '" + device.getType() + " upon the user '" +
                        device.getEnrolmentInfo().getOwner() + "'");
            }
            status = true;
        }
        if (status) {
            addDeviceToGroups(deviceIdentifier, device.getEnrolmentInfo().getOwnership());
            addInitialOperations(deviceIdentifier, device.getType());
            sendNotification(device);
        }
        extractDeviceLocationToUpdate(device);
        try {
            if (device.getDeviceInfo() != null) {
                deviceInformationManager.addDeviceInfo(device, device.getDeviceInfo());
            }
        } catch (DeviceDetailsMgtException e) {
            //This is not logging as error, neither throwing an exception as this is not an exception in main
            // business logic.
            String msg = "Error occurred while adding device info";
            log.warn(msg, e);
        }
        return status;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        if (device == null) {
            String msg = "Required values are not set to modify device enrollment";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Modifying enrollment for device: " + device.getId() + " of type '" + device.getType() + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'modifyEnrolment'");
            }
            return false;
        }
        boolean status = deviceManager.modifyEnrollment(device);
        try {
            int tenantId = this.getTenantId();
            Device currentDevice = this.getDevice(deviceIdentifier, false);
            DeviceManagementDAOFactory.beginTransaction();
            device.setId(currentDevice.getId());
            if (device.getEnrolmentInfo().getId() == 0) {
                device.getEnrolmentInfo().setId(currentDevice.getEnrolmentInfo().getId());
            }
            if (device.getName() == null) {
                device.setName(currentDevice.getName());
            }
            deviceDAO.updateDevice(device, tenantId);
            enrollmentDAO.updateEnrollment(device.getEnrolmentInfo());
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceIdentifier);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while modifying the device '" + device.getId() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to modify device: " + device.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while modifying device: " + device.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        extractDeviceLocationToUpdate(device);
        return status;
    }

    private List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user)
            throws DeviceManagementException {
        if (user == null || user.isEmpty()) {
            String msg = "Required values are not set to getEnrollmentsOfUser";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get enrollments for user '" + user + "' device: " + deviceId);
        }
        List<EnrolmentInfo> enrolmentInfos;
        try {
            DeviceManagementDAOFactory.openConnection();
            enrolmentInfos = enrollmentDAO.getEnrollmentsOfUser(deviceId, user, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the enrollment information device for id '" + deviceId
                    + "' and user : " + user;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getEnrollmentsOfUser user '" + user + "' device: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return enrolmentInfos;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Required values are not set to dis-enroll device";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Dis-enrolling device: " + deviceId.getId() + " of type '" + deviceId.getType() + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'dis-enrollDevice'");
            }
            return false;
        }

        int tenantId = this.getTenantId();

        Device device = this.getDevice(deviceId, false);
        if (device == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device not found for id '" + deviceId.getId() + "'");
            }
            return false;
        }

        if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.REMOVED)) {
            if (log.isDebugEnabled()) {
                log.debug("Device has already dis-enrolled : " + deviceId.getId() + "'");
            }
            return true;
        }

        try {
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
            DeviceManagementDAOFactory.beginTransaction();
            enrollmentDAO.updateEnrollment(device.getId(), device.getEnrolmentInfo(), tenantId);
            deviceDAO.updateDevice(device, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceId);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while dis-enrolling '" + deviceId.getType() +
                    "' device with the identifier '" + deviceId.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while dis-enrolling device: " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceManager.disenrollDevice(deviceId);
    }

    @Override
    public boolean deleteDevices(List<String> deviceIdentifiers) throws DeviceManagementException,
                                                                        InvalidDeviceException {
        if (deviceIdentifiers == null || deviceIdentifiers.isEmpty()) {
            String msg = "Required values of device identifiers are not set to permanently delete device/s.";
            log.error(msg);
            throw new InvalidDeviceException(msg);
        }
        HashSet<Integer> deviceIds = new HashSet<>();
        List<Integer> enrollmentIds = new ArrayList<>();
        List<String> validDeviceIdentifiers = new ArrayList<>();
        Map<String, List<String>> deviceIdentifierMap = new HashMap<>();
        Map<String, DeviceManager> deviceManagerMap = new HashMap<>();
        List<DeviceCacheKey> deviceCacheKeyList = new ArrayList<>();
        List<Device> existingDevices;
        int tenantId = this.getTenantId();

        try {
            DeviceManagementDAOFactory.openConnection();
            existingDevices = deviceDAO.getDevicesByIdentifiers(deviceIdentifiers, tenantId);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while permanently deleting '" + deviceIdentifiers +
                         "' devices";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        try {
            DeviceCacheKey deviceCacheKey;
            for (Device device : existingDevices) {
                if (!EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus())) {
                    String msg = "Device " + device.getDeviceIdentifier() + " of type " + device.getType()
                                 + " is not dis-enrolled to permanently delete the device";
                    log.error(msg);
                    throw new InvalidDeviceException(msg);
                }
                deviceCacheKey = new DeviceCacheKey();
                deviceCacheKey.setDeviceId(device.getDeviceIdentifier());
                deviceCacheKey.setDeviceType(device.getType());
                deviceCacheKey.setTenantId(tenantId);
                deviceCacheKeyList.add(deviceCacheKey);
                deviceIds.add(device.getId());
                validDeviceIdentifiers.add(device.getDeviceIdentifier());
                enrollmentIds.add(device.getEnrolmentInfo().getId());
                if (deviceIdentifierMap.containsKey(device.getType())) {
                    deviceIdentifierMap.get(device.getType()).add(device.getDeviceIdentifier());
                } else {
                    deviceIdentifierMap.put(device.getType(),
                            new ArrayList<>(Arrays.asList(device.getDeviceIdentifier())));
                    DeviceManager deviceManager = this.getDeviceManager(device.getType());
                    if (deviceManager == null) {
                        log.error("Device Manager associated with the device type '" +device.getType() +
                                  "' is null. Therefore, not attempting method 'deleteDevice'");
                        return false;
                    }
                    deviceManagerMap.put(device.getType(), deviceManager);
                }
            }
            if (deviceIds.isEmpty()) {
                String msg = "No device IDs found for the device identifiers '" + deviceIdentifiers + "'";
                log.error(msg);
                throw new InvalidDeviceException(msg);
            }
            if (log.isDebugEnabled()) {
                log.debug("Permanently deleting the details of devices : " + validDeviceIdentifiers);
            }
            DeviceManagementDAOFactory.beginTransaction();
            //deleting device from the core
            deviceDAO.deleteDevices(validDeviceIdentifiers, new ArrayList<>(deviceIds), enrollmentIds);
            for (Map.Entry<String, DeviceManager> entry : deviceManagerMap.entrySet()) {
                try {
                    // deleting device from the plugin level
                    entry.getValue().deleteDevices(deviceIdentifierMap.get(entry.getKey()));
                } catch (DeviceManagementException e) {
                    String msg = "Error occurred while permanently deleting '" + entry.getKey() +
                            "' devices with the identifiers: '" + deviceIdentifierMap.get(entry.getKey())
                            + "' in plugin.";
                    log.error(msg, e);
                    // a DeviceManagementException is thrown when the device deletion fails from the plugin level.
                    // Here, that exception is caught and a DeviceManagementDAOException is thrown
                    throw new DeviceManagementDAOException(msg, e);
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDevicesFromCache(deviceCacheKeyList);
            if (log.isDebugEnabled()) {
                log.debug("Successfully permanently deleted the details of devices : " + validDeviceIdentifiers);
            }
            return true;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while permanently deleting '" + deviceIdentifiers +
                    "' devices";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device = this.getDevice(deviceId, false);
        return device != null;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'isActive'");
            }
            return false;
        }
        return deviceManager.isActive(deviceId);
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'setActive'");
            }
            return false;
        }
        return deviceManager.setActive(deviceId, status);
    }

    @Override
    public List<Device> getAllDevices(String deviceType) throws DeviceManagementException {
        return this.getAllDevices(deviceType, true);
    }

    @Override
    public List<Device> getAllDevices(String deviceType, boolean requireDeviceInfo) throws DeviceManagementException {
        if (deviceType == null) {
            String msg = "Device type is empty for method getAllDevices";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices of type '" + deviceType + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(deviceType, this.getTenantId());
            if (allDevices == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceType + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving all devices of type '" +
                    deviceType + "' that are being managed within the scope of current tenant";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while getting all devices of device type '" + deviceType + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return this.getAllDevices(true);
    }

    @Override
    public List<Device> getAllDevices(boolean requireDeviceInfo) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices with requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list pertaining to the current tenant";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in get all devices";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (!allDevices.isEmpty() && requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public List<Device> getDevices(Date since) throws DeviceManagementException {
        return this.getDevices(since, true);
    }

    @Override
    public List<Device> getDevices(Date since, boolean requireDeviceInfo) throws DeviceManagementException {
        if (since == null) {
            String msg = "Given date is empty for method getDevices";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices since date '" + since.toString() + "' and required device info: "
                    + requireDeviceInfo);
        }
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(since.getTime(), this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list pertaining to the current tenant";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred get devices since '" + since.toString() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByType(request, true);
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete pagination request for getDevicesByType";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices with pagination " + request.toString() + " and required deviceinfo: "
                    + requireDeviceInfo);
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices;
        int count;
        int tenantId = this.getTenantId();
        String deviceType = request.getDeviceType();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(request, tenantId);
            count = deviceDAO.getDeviceCountByType(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list pertaining to the current tenant of type "
                    + deviceType;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceByType";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            paginationResult.setData(this.getAllDeviceInfo(allDevices));
        } else {
            paginationResult.setData(allDevices);
        }

        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException {
        return this.getAllDevices(request, true);
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete pagination request for method getAllDevices";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices with pagination " + request.toString() + " and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> devicesForRoles;
        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices;
        int count = 0;
        int tenantId = this.getTenantId();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        if (!StringUtils.isEmpty(request.getOwnerRole())) {
            devicesForRoles = this.getAllDevicesOfRole(request.getOwnerRole(), false);
            if (devicesForRoles != null) {
                count = devicesForRoles.size();
                if (requireDeviceInfo) {
                    paginationResult.setData(getAllDeviceInfo(devicesForRoles));
                }
            }
        } else {
            try {
                DeviceManagementDAOFactory.openConnection();
                if(request.getGroupId()!=0){
                    allDevices = deviceDAO.searchDevicesInGroup(request, tenantId);
                    count = deviceDAO.getCountOfDevicesInGroup(request, tenantId);
                } else{
                    allDevices = deviceDAO.getDevices(request, tenantId);
                    count = deviceDAO.getDeviceCount(request, tenantId);
                }
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while retrieving device list pertaining to the current tenant";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred in getAllDevices";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
            if (requireDeviceInfo && !allDevices.isEmpty()) {
                paginationResult.setData(getAllDeviceInfo(allDevices));
            } else {
                paginationResult.setData(allDevices);
            }
        }
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, boolean requireDeviceInfo) throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received null device identifier for method getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device by device id :" + deviceId.getId() + " of type '" + deviceId.getType()
                    + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        int tenantId = this.getTenantId();
        Device device = this.getDeviceFromCache(deviceId);
        if (device == null) {
            try {
                DeviceManagementDAOFactory.openConnection();
                device = deviceDAO.getDevice(deviceId, tenantId);
                if (device == null) {
                    String msg = "No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    return null;
                }
                this.addDeviceToCache(deviceId, device);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while obtaining the device for '" + deviceId.getId() + "'";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg);
                throw new DeviceManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred in getDevice: " + deviceId.getId();
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(String deviceId, boolean requireDeviceInfo) throws DeviceManagementException {
        if (deviceId == null) {
            String message = "Received null device identifier for method getDevice";
            log.error(message);
            throw new DeviceManagementException(message);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device by device id :" + deviceId + " '" +
                    "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        int tenantId = this.getTenantId();
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, tenantId);
            if (device == null) {
                String message = "No device is found upon the id '" +
                        deviceId + "'";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                return null;
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, device.getType());
            this.addDeviceToCache(deviceIdentifier, device);
        } catch (DeviceManagementDAOException e) {
            String message = "Error occurred while obtaining the device for '" + deviceId + "'";
            log.error(message, e);
            throw new DeviceManagementException(message, e);
        } catch (SQLException e) {
            String message = "Error occurred while opening a connection to the data source";
            log.error(message);
            throw new DeviceManagementException(message, e);
        } catch (Exception e) {
            String message = "Error occurred in getDevice: " + deviceId;
            log.error(message, e);
            throw new DeviceManagementException(message, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, String owner, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received null device identifier for method getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device by device id :" + deviceId.getId() + " of type '" + deviceId.getType() +
                    " and owner '" + owner + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        int tenantId = this.getTenantId();
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, owner, tenantId);
            if (device == null) {
                String msg = "No device is found upon the type '" + deviceId.getType() + "' and id '" +
                        deviceId.getId() + "' and owner '" + owner + "'";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device for '" + deviceId.getId() + "' and owner '"
                    + owner + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevice: " + deviceId.getId() + " with owner: " + owner;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public void sendEnrolmentInvitation(String templateName, EmailMetaInfo metaInfo) throws DeviceManagementException,
            ConfigurationManagementException {
        if (metaInfo == null) {
            String msg = "Received incomplete data to method sendEnrolmentInvitation";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Send enrollment invitation, templateName '" + templateName + "'");
        }
        Map<String, TypedValue<Class<?>, Object>> params = new HashMap<>();
        Properties props = metaInfo.getProperties();
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            params.put(key, new TypedValue<>(String.class, props.getProperty(key)));
        }
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
        try {
            EmailContext ctx =
                    new EmailContext.EmailContextBuilder(new ContentProviderInfo(templateName, params),
                            metaInfo.getRecipients()).build();
            DeviceManagementDataHolder.getInstance().getEmailSenderService().sendEmail(ctx);
        } catch (EmailSendingFailedException ex) {
            String msg = "Error occurred while sending enrollment invitation";
            log.error(msg, ex);
            throw new DeviceManagementException(msg, ex);
        } catch (EmailTransportNotConfiguredException ex) {
            String msg = "Mail Server is not configured.";
            throw new ConfigurationManagementException(msg, ex);
        } catch (Exception ex) {
            String msg = "Error occurred in setEnrollmentInvitation";
            log.error(msg, ex);
            throw new DeviceManagementException(msg, ex);
        }
    }

    @Override
    public void sendRegistrationEmail(EmailMetaInfo metaInfo) throws DeviceManagementException,
            ConfigurationManagementException {
        if (metaInfo == null) {
            String msg = "Received incomplete request for sendRegistrationEmail";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Send registration email");
        }
        EmailSenderService emailSenderService = DeviceManagementDataHolder.getInstance().getEmailSenderService();
        if (emailSenderService != null) {
            Map<String, TypedValue<Class<?>, Object>> params = new HashMap<>();
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.FIRST_NAME,
                    new TypedValue<>(String.class, metaInfo.getProperty("first-name")));
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.USERNAME,
                    new TypedValue<>(String.class, metaInfo.getProperty("username")));
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.PASSWORD,
                    new TypedValue<>(String.class, metaInfo.getProperty("password")));
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.DOMAIN,
                    new TypedValue<>(String.class, metaInfo.getProperty("domain")));
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                    new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
            params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                    new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
            try {
                EmailContext ctx =
                        new EmailContext.EmailContextBuilder(
                                new ContentProviderInfo(
                                        DeviceManagementConstants.EmailAttributes.USER_REGISTRATION_TEMPLATE,
                                        params),
                                metaInfo.getRecipients()).build();
                emailSenderService.sendEmail(ctx);
            } catch (EmailSendingFailedException e) {
                String msg = "Error occurred while sending user registration notification." + e.getMessage();
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (EmailTransportNotConfiguredException e) {
                String msg = "Error occurred while sending user registration email." + e.getMessage();
                throw new ConfigurationManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred while sending Registration Email.";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            }
        }
    }

    @Override
    public HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received null deviceIdentifier for getTenantedDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get tenanted device with id: " + deviceIdentifier.getId() + " of type '" +
                    deviceIdentifier.getType() + "'");
        }
        HashMap<Integer, Device> deviceHashMap;
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceHashMap = deviceDAO.getDevice(deviceIdentifier);
            if (deviceHashMap == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceIdentifier.getType() + "' and id '" +
                            deviceIdentifier.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device for id '" + deviceIdentifier.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getTenantedDevice device: " + deviceIdentifier.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceHashMap;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return this.getDevice(deviceId, true);
    }

    @Override
    public Device getDeviceWithTypeProperties(DeviceIdentifier deviceId) throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received null deviceIdentifier for getDeviceWithTypeProperties";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get tenanted device with type properties, deviceId: " + deviceId.getId());
        }
        Device device = this.getDevice(deviceId, false);

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return device;
        }
        Device dmsDevice =
                deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        if (dmsDevice != null) {
            device.setFeatures(dmsDevice.getFeatures());
            device.setProperties(dmsDevice.getProperties());
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, Date since) throws DeviceManagementException {
        return this.getDevice(deviceId, since, true);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException {
        if (deviceId == null || since == null) {
            String msg = "Received incomplete data for getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device since '" + since.toString() + "' with identifier: " + deviceId.getId()
                    + " and type '" + deviceId.getType() + "'");
        }
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, since, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device for id '" + deviceId.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevice for device: " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceData deviceData, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (deviceData.getDeviceIdentifier() == null) {
            String msg = "Received null device identifier for method getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device by device identifier :" + deviceData.getDeviceIdentifier().getId() + " of type '"
                    + deviceData.getDeviceIdentifier().getType() + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        Device device = null;
        int tenantId = this.getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceData, tenantId);
            if (device == null) {
                String msg =
                        "No device is found upon the type '" + deviceData.getDeviceIdentifier().getType() + "' and id '"
                                + deviceData.getDeviceIdentifier().getId() + "'";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg =
                    "Error occurred while obtaining the device for '" + deviceData.getDeviceIdentifier().getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevice: " + deviceData.getDeviceIdentifier().getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(device);
        }
        return device;
    }


    @Override
    public List<Device> getDevicesBasedOnProperties(Map deviceProps) throws DeviceManagementException {
        if (deviceProps == null || deviceProps.isEmpty()) {
            String msg = "Devices retrieval criteria cannot be null or empty.";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Attempting to get devices based on criteria : " + deviceProps);
        }
        List<Device> devices;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            devices = deviceDAO.getDeviceBasedOnDeviceProperties(deviceProps, tenantId);
            if (devices == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found against criteria : " + deviceProps + " and tenantId "+ tenantId);
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining devices based on criteria : " + deviceProps;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while obtaining devices based on criteria : " + deviceProps;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    @Override
    public Device getDevice(String deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException {
        if (deviceId == null || since == null) {
            String message = "Received incomplete data for getDevice";
            log.error(message);
            throw new DeviceManagementException(message);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device since '" + since.toString() + "' with identifier: " + deviceId + "");
        }
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, since, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the id '" + deviceId + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String message = "Error occurred while obtaining the device for id '" + deviceId + "'";
            log.error(message, e);
            throw new DeviceManagementException(message, e);
        } catch (SQLException e) {
            String message = "Error occurred while opening a connection to the data source";
            log.error(message, e);
            throw new DeviceManagementException(message, e);
        } catch (Exception e) {
            String message = "Error occurred in getDevice for device: " + deviceId;
            log.error(message, e);
            throw new DeviceManagementException(message, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, String owner, Date since, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (deviceId == null || since == null) {
            String msg = "Received incomplete data for getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device since '" + since.toString() + "' with identifier: " + deviceId.getId()
                    + " and type '" + deviceId.getType() + "' and owner '" + owner + "'");
        }
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, owner, since, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "' and owner name '" + owner + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device for id '" + deviceId.getId() + "' and owner '" +
                    owner + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevice for device: " + deviceId.getId() + " and owner: " + owner;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException {
        return this.getDevice(deviceId, status, true);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received null deviceIdentifier for getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device with identifier: " + deviceId.getId() + " and type '" + deviceId.getType() + "'");
        }
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, status, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device for id '" + deviceId.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevice for device: " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public List<String> getAvailableDeviceTypes() throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get available device types");
        }
        List<DeviceType> deviceTypesProvidedByTenant;
        List<String> publicSharedDeviceTypesInDB;
        List<String> deviceTypesResponse = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = this.getTenantId();
            deviceTypesProvidedByTenant = deviceTypeDAO.getDeviceTypesByProvider(tenantId);
            publicSharedDeviceTypesInDB = deviceTypeDAO.getSharedDeviceTypes();
            Map<DeviceTypeServiceIdentifier, DeviceManagementService> registeredTypes =
                    pluginRepository.getAllDeviceManagementServices(tenantId);
            // Get the device from the public space, however if there is another device with same name then give
            // priority to that
            if (deviceTypesProvidedByTenant != null) {
                for (DeviceType deviceType : deviceTypesProvidedByTenant) {
                    deviceTypesResponse.add(deviceType.getName());
                }
            }
            if (publicSharedDeviceTypesInDB != null) {
                for (String deviceType: publicSharedDeviceTypesInDB) {
                    if (!deviceTypesResponse.contains(deviceType)) {
                        deviceTypesResponse.add(deviceType);
                    }
                }
            }
            if (registeredTypes != null) {
                for (DeviceTypeServiceIdentifier deviceType: registeredTypes.keySet()) {
                    if (!deviceTypesResponse.contains(deviceType.getDeviceType())) {
                        deviceTypesResponse.add(deviceType.getDeviceType());
                    }
                }
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types.";
            log.info(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.info(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getAvailableDeviceTypes";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceTypesResponse;
    }

    @Override
    public List<String> getPolicyMonitoringEnableDeviceTypes() throws DeviceManagementException {

        List<String> deviceTypes = this.getAvailableDeviceTypes();
        List<String> deviceTypesToMonitor = new ArrayList<>();
        int tenantId = this.getTenantId();
        Map<DeviceTypeServiceIdentifier, DeviceManagementService> registeredTypes =
                pluginRepository.getAllDeviceManagementServices(tenantId);

        List<DeviceManagementService> services = new ArrayList<>(registeredTypes.values());
        for (DeviceManagementService deviceType : services) {
            if (deviceType != null && deviceType.getGeneralConfig() != null &&
                    deviceType.getGeneralConfig().isPolicyMonitoringEnabled()) {
                for (String type : deviceTypes) {
                    if (type.equalsIgnoreCase(deviceType.getType())) {
                        deviceTypesToMonitor.add(type);
                    }
                }
            }
        }
        return deviceTypesToMonitor;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceId, Device device) throws DeviceManagementException {
        if (deviceId == null || device == null) {
            String msg = "Received incomplete data for updateDeviceInfo";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Update device info of device: " + deviceId.getId());
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'updateDeviceInfo'");
            }
            return false;
        }
        return deviceManager.updateDeviceInfo(deviceId, device);
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received incomplete data for setOwnership";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Set ownership of device: " + deviceId.getId() + " ownership type '" + ownershipType + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'setOwnership'");
            }
            return false;
        }
        return deviceManager.setOwnership(deviceId, ownershipType);
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'isClaimable'");
            }
            return false;
        }
        return deviceManager.isClaimable(deviceId);
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        if (deviceId == null) {
            String msg = "Received null deviceIdentifier for setStatus";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Set status of device: " + deviceId.getId());
        }
        try {
            boolean success = false;
            int tenantId = this.getTenantId();
            Device device = this.getDevice(deviceId, false);
            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
            DeviceManagementDAOFactory.beginTransaction();
            if (enrolmentInfo != null) {
                success = enrollmentDAO.setStatus(enrolmentInfo.getId(), status, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceId);
            return success;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while setting enrollment status";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in setStatus for device :" + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean setStatus(String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Update enrollment with status");
        }
        try {
            boolean success;
            int tenantId = this.getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            success = enrollmentDAO.setStatus(currentOwner, status, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            return success;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while setting enrollment status";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in setStatus";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    @Deprecated
    public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> deviceIds)
            throws DeviceManagementException {

//        for (DeviceIdentifier deviceId : deviceIds) {
//            DeviceManagementService dms =
//                    pluginRepository.getDeviceManagementService(deviceId.getType(), this.getTenantId());
//            //TODO FIX THIS WITH PUSH NOTIFICATIONS
//            //dms.notifyOperationToDevices(operation, deviceIds);
//        }

    }

    @Override
    public License getLicense(String deviceType, String languageCode) throws DeviceManagementException {
        if (deviceType == null || languageCode == null) {
            String msg = "Received incomplete data for getLicence";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get the licence for device type '" + deviceType + "' languageCode '" + languageCode + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        License license;
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'getLicense'");
            }
            return null;
        }
        try {
            license = deviceManager.getLicense(languageCode);
            if (license == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find a license for '" + deviceType + "' device type");
                }
            }
            return license;
        } catch (LicenseManagementException e) {
            String msg = "Error occurred while retrieving license configured for " +
                    "device type '" + deviceType + "' and language code '" + languageCode + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getLicence for device type '" + deviceType + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public void addLicense(String deviceType, License license) throws DeviceManagementException {
        if (deviceType == null || license == null) {
            String msg = "Received incomplete data for addLicence";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Add the licence for device type '" + deviceType + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return;
        }
        try {
            deviceManager.addLicense(license);
        } catch (LicenseManagementException e) {
            String msg = "Error occurred while adding license for device type '" + deviceType + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in addLicence for device type '" + deviceType + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public Activity addOperation(String type, Operation operation,
                                 List<DeviceIdentifier> devices) throws OperationManagementException, InvalidDeviceException {
        return pluginRepository.getOperationManager(type, this.getTenantId()).addOperation(operation, devices);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId()).getOperations(deviceId);
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperations(deviceId, DeviceManagerUtil.validateOperationListPageSize(request));
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId)
            throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getPendingOperations(deviceId);
    }

    @Override
    public List<? extends Operation> getPendingOperations(Device device) throws OperationManagementException {
        return pluginRepository.getOperationManager(device.getType(), this.getTenantId())
                .getPendingOperations(device);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        // // setting notNowOperationFrequency to -1 to avoid picking notnow operations
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getNextPendingOperation(deviceId, -1);
    }

    public Operation getNextPendingOperation(DeviceIdentifier deviceId, long notNowOperationFrequency)
            throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getNextPendingOperation(deviceId, notNowOperationFrequency);
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException {
        pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .updateOperation(deviceId, operation);
        try {
            if (DeviceManagerUtil.isPublishOperationResponseEnabled()) {
                List<String> permittedOperations = DeviceManagerUtil.getEnabledOperationsForResponsePublish();
                if (permittedOperations.contains(operation.getCode())
                        || permittedOperations.contains("*")) {
                    Object[] metaData = {deviceId.getId(), deviceId.getType()};
                    Object[] payload = new Object[]{
                            Calendar.getInstance().getTimeInMillis(),
                            operation.getId(),
                            operation.getCode(),
                            operation.getType() != null ? operation.getType().toString() : null,
                            operation.getStatus() != null ? operation.getStatus().toString() : null,
                            operation.getOperationResponse()
                    };
                    DeviceManagerUtil.getEventPublisherService().publishEvent(
                            OPERATION_RESPONSE_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                    );
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while reading configs.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        } catch (DataPublisherConfigurationException e) {
            String msg = "Error occurred while publishing event.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        }
    }

    @Override
    public void updateOperation(Device device, Operation operation) throws OperationManagementException {
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        if (enrolmentInfo == null) {
            throw new OperationManagementException(
                    "Device not found for device id:" + device.getDeviceIdentifier() + " " + "type:" +
                            device.getType());
        }
        pluginRepository.getOperationManager(device.getType(), this.getTenantId())
                .updateOperation(enrolmentInfo.getId(), operation);
        try {
            if (DeviceManagerUtil.isPublishOperationResponseEnabled()) {
                List<String> permittedOperations = DeviceManagerUtil.getEnabledOperationsForResponsePublish();
                if (permittedOperations.contains(operation.getCode())
                        || permittedOperations.contains("*")) {
                    Object[] metaData = {device.getDeviceIdentifier(), device.getType()};
                    Object[] payload = new Object[]{
                            Calendar.getInstance().getTimeInMillis(),
                            operation.getId(),
                            operation.getCode(),
                            operation.getType() != null ? operation.getType().toString() : null,
                            operation.getStatus() != null ? operation.getStatus().toString() : null,
                            operation.getOperationResponse()
                    };
                    DeviceManagerUtil.getEventPublisherService().publishEvent(
                            OPERATION_RESPONSE_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
                    );
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while reading configs.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        } catch (DataPublisherConfigurationException e) {
            String msg = "Error occurred while publishing event.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        }
    }

    @Override
    public boolean updateProperties(DeviceIdentifier deviceId, List<Device.Property> properties)
            throws DeviceManagementException {
        if (deviceId == null || properties == null) {
            String msg = "Received incomplete data for updateDeviceInfo";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Update device info of device: " + deviceId.getId());
        }
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'updateProperties'");
            }
            return false;
        }
        return deviceManager.updateDeviceProperties(deviceId, properties);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId,
                                                        int operationId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperationByDeviceAndOperationId(deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperationsByDeviceAndStatus(deviceId, status);
    }

    @Override
    public Operation getOperation(String type, int operationId) throws OperationManagementException {
        return pluginRepository.getOperationManager(type, this.getTenantId()).getOperation(operationId);
    }

    @Override
    public Activity getOperationByActivityId(String activity) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityId(activity);
    }

    @Override
    public List<Activity> getOperationByActivityIds(List<String> idList) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityIds(idList);
    }

    public Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityIdAndDevice(activity, deviceId);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException {
        limit = DeviceManagerUtil.validateActivityListPageSize(limit);
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivitiesUpdatedAfter(timestamp, limit, offset);
    }

    @Override
    public List<Activity> getFilteredActivities(String operationCode, int limit, int offset) throws OperationManagementException {
        limit = DeviceManagerUtil.validateActivityListPageSize(limit);
        return DeviceManagementDataHolder.getInstance().getOperationManager().getFilteredActivities(operationCode, limit, offset);
    }

    @Override
    public int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getTotalCountOfFilteredActivities(operationCode);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset) throws OperationManagementException {
        limit = DeviceManagerUtil.validateActivityListPageSize(limit);
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivitiesUpdatedAfterByUser(timestamp, user, limit, offset);
    }

    @Override
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivityCountUpdatedAfter(timestamp);
    }

    @Override
    public int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivityCountUpdatedAfterByUser(timestamp, user);
    }

    @Override
    public List<MonitoringOperation> getMonitoringOperationList(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);

        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.getMonitoringOperation();
    }

    @Override
    public  List<String> getStartupOperations(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);

        StartupOperationConfig startupOperationConfig = dms.getStartupOperationConfig();
        if (startupOperationConfig != null) {
            return startupOperationConfig.getStartupOperations();
        }
        return null;
    }

    @Override
    public int getDeviceMonitoringFrequency(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.getFrequency();
    }

    @Override
    public OperationMonitoringTaskConfig getDeviceMonitoringConfig(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        return dms.getOperationMonitoringConfig();
    }

    @Override
    public StartupOperationConfig getStartupOperationConfig(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        return dms.getStartupOperationConfig();
    }

    @Override
    public boolean isDeviceMonitoringEnabled(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.isEnabled();
    }

    @Override
    public PolicyMonitoringManager getPolicyMonitoringManager(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        return dms.getPolicyMonitoringManager();
    }

    @Override
    public List<Device> getDevicesOfUser(String username) throws DeviceManagementException {
        return this.getDevicesOfUser(username, true);
    }

    @Override
    public List<Device> getDevicesOfUser(String username, boolean requireDeviceInfo) throws DeviceManagementException {
        if (username == null) {
            String msg = "Username null in getDevicesOfUser";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices of user with username '" + username + "' and requiredDeviceInfo " + requireDeviceInfo);
        }
        List<Device> userDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(username, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesOfUser for username '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(userDevices);
        }
        return userDevices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, String deviceType) throws DeviceManagementException {
        return this.getDevicesOfUser(username, deviceType, true);
    }

    @Override
    public List<Device> getDevicesOfUser(String username, String deviceType, boolean requireDeviceInfo) throws
            DeviceManagementException {
        if (username == null || deviceType == null) {
            String msg = "Received incomplete data for getDevicesOfUser";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get '" + deviceType + "' devices of user with username '" + username + "' requiredDeviceInfo: "
                    + requireDeviceInfo);
        }
        List<Device> userDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(username, deviceType, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesOfUser for '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(userDevices);
        }
        return userDevices;
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesOfUser(request, true);
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete pagination request for getDevicesOfUser";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get paginated results of devices of user " + request.toString() + " and requiredDeviceInfo: "
                    + requireDeviceInfo);
        }
        PaginationResult result = new PaginationResult();
        int deviceCount;
        int tenantId = this.getTenantId();
        String username = request.getOwner();
        List<Device> userDevices;
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(request, tenantId);
            deviceCount = deviceDAO.getDeviceCountByUser(username, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the list of devices that belong to the user '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesOfUser";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(userDevices));
        } else {
            result.setData(userDevices);
        }

        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest request)
            throws DeviceManagementException {
        return this.getDevicesByOwnership(request, true);
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getDevicesByOwnership";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices by ownership " + request.toString());
        }
        PaginationResult result = new PaginationResult();
        List<Device> allDevices;
        int deviceCount;
        int tenantId = this.getTenantId();
        String ownerShip = request.getOwnership();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByOwnership(request, tenantId);
            deviceCount = deviceDAO.getDeviceCountByOwnership(ownerShip, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the list of devices that matches to ownership : '" + ownerShip + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesByOwnership";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }

        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role) throws DeviceManagementException {
        return this.getAllDevicesOfRole(role, true);
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role, boolean requireDeviceInfo) throws DeviceManagementException {
        if (role == null || role.isEmpty()) {
            String msg = "Received empty role for the method getAllDevicesOfRole";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices of role '" + role + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> devices = new ArrayList<>();
        String[] users;
        int tenantId = this.getTenantId();
        try {
            users = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().getUserListOfRole(role);
        } catch (UserStoreException e) {
            String msg = "Error occurred while obtaining the users, who are assigned with the role '" + role + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getAllDevicesOfRole for role '" + role + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }

        List<Device> userDevices;
        for (String user : users) {
            try {
                DeviceManagementDAOFactory.openConnection();
                userDevices = deviceDAO.getDevicesOfUser(user, tenantId);
            } catch (DeviceManagementDAOException | SQLException e) {
                String msg = "Error occurred while obtaining the devices of user '" + user + "'";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred getAllDevicesOfRole for role '" + role + "'";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
            if (requireDeviceInfo) {
                this.getAllDeviceInfo(userDevices);
            }
            devices.addAll(userDevices);
        }
        return devices;
    }

    @Override
    public int getDeviceCount(String username) throws DeviceManagementException {
        if (username == null) {
            String msg = "Received empty username for getDeviceCount";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting device count of the user '" + username + "'");
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCount(username, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the device count of user '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount for username '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCount() throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get devices count");
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCount(this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the device count";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCount(String deviceType, EnrolmentInfo.Status status) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get devices count for type '" + deviceType + "' and status: " + status.toString());
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCountByStatus(deviceType, status.toString(), this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the device count for type '" + deviceType +
                    "' and status: " + status.toString();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount for type '" + deviceType + "' and status: " + status.toString();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCount(EnrolmentInfo.Status status) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get devices count status: " + status.toString());
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCountByStatus(status.toString(), this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the device count";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount status: " + status.toString();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Device> getDevicesByNameAndType(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getDevicesByNameAndType";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices by name " + request.toString() + " and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> allDevices;
        int limit = DeviceManagerUtil.validateDeviceListPageSize(request.getRowCount());
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByNameAndType(request.getDeviceName(), request.getDeviceType(),
                    this.getTenantId(), request.getStartIndex(), limit);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the list of devices that matches to '"
                    + request.getDeviceName() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesByNameAndType";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByName(request, true);
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest request, boolean requireDeviceInfo) throws
            DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getDevicesByName";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices by name " + request.toString() + " requiredDeviceInfo: " + requireDeviceInfo);
        }
        PaginationResult result = new PaginationResult();
        int tenantId = this.getTenantId();
        List<Device> allDevices;
        String deviceName = request.getDeviceName();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByName(request, tenantId);
            int deviceCount = deviceDAO.getDeviceCountByName(deviceName, tenantId);
            result.setRecordsTotal(deviceCount);
            result.setRecordsFiltered(deviceCount);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the list of devices that matches to '" + deviceName + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesByName";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }
        return result;
    }

    @Override
    public void registerDeviceManagementService(DeviceManagementService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Registering device management service");
        }
        try {
            pluginRepository.addDeviceManagementProvider(deviceManagementService);
            initializeDeviceTypeVersions(deviceManagementService);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while registering device management plugin '" +
                    deviceManagementService.getType() + "'";
            log.error(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in registerDeviceManagementService";
            log.error(msg, e);
        }
    }

    @Override
    public void unregisterDeviceManagementService(DeviceManagementService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Unregister a device management service");
        }
        try {
            pluginRepository.removeDeviceManagementProvider(deviceManagementService);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while un-registering device management plugin '" +
                    deviceManagementService.getType() + "'", e);
        } catch (Exception e) {
            String msg = "Error occurred in unregisterDeviceManagementService";
            log.error(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException {
        return this.getDevicesByStatus(status, true);
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status, boolean requireDeviceInfo) throws
            DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("get devices by status and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByStatus(status, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException(
                    "Error occurred while fetching the list of devices that matches to status: '" + status + "'", e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesByStatus";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByStatus(request, true);
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getDevicesByStatus";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices by status " + request.toString() + " and requiredDeviceInfo: "
                    + requireDeviceInfo);
        }
        List<String> statusList = request.getStatusList();
        if (statusList == null || statusList.isEmpty()) {
            String msg = "Invalid enrollment status type received. Status can't be null or empty" +
                         "Valid status types are ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE " +
                         "| SUSPENDED | DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (statusList.size() > 1) {
            String msg = "Invalid enrollment status received. Devices can only be filtered by one " +
                         "type of status, more than one are not allowed";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        String status = statusList.get(0);
        if (StringUtils.isBlank(status)){
            String msg = "Invalid enrollment status type received. Status can't be null or empty" +
                         "Valid status types are ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE " +
                         "| SUSPENDED | DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        PaginationResult result = new PaginationResult();
        List<Device> allDevices;
        int tenantId = this.getTenantId();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByStatus(request, tenantId);
            int deviceCount = deviceDAO.getDeviceCountByStatus(status, tenantId);
            result.setRecordsTotal(deviceCount);
            result.setRecordsFiltered(deviceCount);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the list of devices that matches to status: " +
                         status;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesByStatus";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }
        return result;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId, String user) throws DeviceManagementException {
        Device device = this.getDevice(deviceId, false);
        return device != null && device.getEnrolmentInfo() != null && device.getEnrolmentInfo().getOwner().equals(user);
    }

    @Override
    public NotificationStrategy getNotificationStrategyByDeviceType(String deviceType) throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        OperationManager operationManager = pluginRepository.getOperationManager(deviceType, tenantId);
        if (operationManager != null) {
            return operationManager.getNotificationStrategy();
        } else {
            throw new DeviceManagementException("Cannot find operation manager for given device type :" + deviceType);
        }
    }

    /**
     * Change device status.
     *
     * @param deviceIdentifier {@link DeviceIdentifier} object
     * @param newStatus        New status of the device
     * @return Whether status is changed or not
     * @throws DeviceManagementException on errors while trying to change device status
     */
    @Override
    public boolean changeDeviceStatus(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status newStatus)
            throws DeviceManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received incomplete data for getDevicesByStatus";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Change device status of device: " + deviceIdentifier.getId() + " of type '"
                    + deviceIdentifier.getType() + "'");
        }
        boolean isDeviceUpdated;
        Device device = getDevice(deviceIdentifier, false);
        int deviceId = device.getId();
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        enrolmentInfo.setStatus(newStatus);
        int tenantId = this.getTenantId();
        switch (newStatus) {
            case ACTIVE:
                isDeviceUpdated = updateEnrollment(deviceId, enrolmentInfo, tenantId);
                break;
            case INACTIVE:
                isDeviceUpdated = updateEnrollment(deviceId, enrolmentInfo, tenantId);
                break;
            case REMOVED:
                isDeviceUpdated = disenrollDevice(deviceIdentifier);
                break;
            default:
                throw new DeviceManagementException("Invalid status retrieved. Status : " + newStatus);
        }
        return isDeviceUpdated;
    }

    @Override
    public List<Integer> getDeviceEnrolledTenants() throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("get device enrolled tenants");
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceEnrolledTenants();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the tenants which have device enrolled.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceEnrolledTenants";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private boolean updateEnrollment(int deviceId, EnrolmentInfo enrolmentInfo, int tenantId)
            throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Update enrollment of device: " + deviceId);
        }
        boolean isUpdatedEnrollment = false;
        boolean isAutoCommit = true;
        try {
            DeviceManagementDAOFactory.openConnection();
            isAutoCommit = DeviceManagementDAOFactory.getConnection().getAutoCommit();
            DeviceManagementDAOFactory.getConnection().setAutoCommit(true);
            int updatedRows = enrollmentDAO.updateEnrollment(deviceId, enrolmentInfo, tenantId);
            if (updatedRows > 0) {
                isUpdatedEnrollment = true;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while updating the enrollment information device for" +
                    "id '" + deviceId + "' .";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in updateEnrollment for deviceId: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            try {
                DeviceManagementDAOFactory.getConnection().setAutoCommit(isAutoCommit);
            } catch (SQLException e) {
                log.error("Exception occurred while setting auto commit.");
            }
            DeviceManagementDAOFactory.closeConnection();
        }
        return isUpdatedEnrollment;
    }


    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private DeviceManager getDeviceManager(String deviceType) {
        DeviceManagementService deviceManagementService =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId());
        if (deviceManagementService == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device type '" + deviceType + "' does not have an associated device management " +
                        "plugin registered within the framework. Therefore, returning null");
            }
            return null;
        }
        return deviceManagementService.getDeviceManager();
    }

    /**
     * Adds the enrolled devices to the default groups based on ownership
     *
     * @param deviceIdentifier of the device.
     * @param ownership        of the device.
     * @throws DeviceManagementException If error occurred in adding the device to the group.
     */
    private void addDeviceToGroups(DeviceIdentifier deviceIdentifier, EnrolmentInfo.OwnerShip ownership)
            throws DeviceManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received incomplete data for addDeviceToGroup";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Add device:" + deviceIdentifier.getId() + " to default group");
        }
        GroupManagementProviderService groupManagementProviderService = new GroupManagementProviderServiceImpl();
        try {
            DeviceGroup defaultGroup = createDefaultGroup(groupManagementProviderService, ownership.toString());
            if (defaultGroup != null) {
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                deviceIdentifiers.add(deviceIdentifier);
                groupManagementProviderService.addDevices(defaultGroup.getGroupId(), deviceIdentifiers);
            }
        } catch (DeviceNotFoundException e) {
            String msg = "Unable to find the device with the id: '" + deviceIdentifier.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "An error occurred when adding the device to the group.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    private void addInitialOperations(DeviceIdentifier deviceIdentifier, String deviceType) throws DeviceManagementException {
        if (deviceIdentifier == null || deviceType == null) {
            String msg = "Received incomplete data for getDevicesByStatus";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Add initial operations to the device:" + deviceIdentifier.getId() + " of type '"
                    + deviceType + "'");
        }
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        DeviceManagementService deviceManagementService =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId());
        InitialOperationConfig init = deviceManagementService.getInitialOperationConfig();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        deviceIdentifiers.add(deviceIdentifier);
        if (init != null) {
            List<String> initialOperations = init.getOperations();
            if (initialOperations != null) {
                for (String str : initialOperations) {
                    CommandOperation operation = new CommandOperation();
                    operation.setEnabled(true);
                    operation.setType(Operation.Type.COMMAND);
                    operation.setCode(str);
                    try {
                        deviceManagementProviderService.addOperation(deviceType, operation, deviceIdentifiers);
                    } catch (OperationManagementException e) {
                        String msg = "Unable to add the operation for the device with the id: '" + deviceIdentifier.getId();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    } catch (InvalidDeviceException e) {
                        String msg = "Unable to find the device with the id: '" + deviceIdentifier.getId();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    } catch (Exception e) {
                        String msg = "Error occurred";
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    }
                }
            }
        }
    }

    /**
     * Checks for the default group existence and create group based on device ownership
     *
     * @param service   {@link GroupManagementProviderService} instance.
     * @param groupName of the group to create.
     * @return Group with details.
     * @throws GroupManagementException Group Management Exception
     */
    private DeviceGroup createDefaultGroup(GroupManagementProviderService service, String groupName)
            throws GroupManagementException {
        if (service == null || groupName == null) {
            String msg = "Received incomplete data for createDefaultGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Create default group with name '" + groupName + "'");
        }
        DeviceGroup defaultGroup = service.getGroup(groupName, false);
        if (defaultGroup == null) {
            defaultGroup = new DeviceGroup(groupName);
            // Setting system level user (wso2.system.user) as the owner
            defaultGroup.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            defaultGroup.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
            defaultGroup.setDescription("Default system group for devices with " + groupName + " ownership.");
            try {
                service.createGroup(defaultGroup, DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE,
                        DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS);
            } catch (GroupAlreadyExistException e) {
                String msg = "Default group: " + defaultGroup.getName() + " already exists. Skipping group creation.";
                log.error(msg);
                throw new GroupManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred";
                log.error(msg, e);
                throw new GroupManagementException(msg, e);
            }
            return service.getGroup(groupName, false);
        } else {
            return defaultGroup;
        }
    }

    @Override
    public void registerDeviceType(DeviceManagementService deviceManagementService) throws DeviceManagementException {
        if (deviceManagementService != null) {
            pluginRepository.addDeviceManagementProvider(deviceManagementService);
        }
    }

    @Override
    public DeviceType getDeviceType(String deviceType) throws DeviceManagementException {
        if (deviceType != null) {
            if (log.isDebugEnabled()) {
                log.debug("Get device type '" + deviceType + "'");
            }
        } else {
            String msg = "Received null deviceType for getDeviceType";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeDAO.getDeviceType(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device type " + deviceType;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceType> getDeviceTypes() throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get device types");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceTypeDAO.getDeviceTypes(tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types for tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceTypes";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceType> getDeviceTypes(PaginationRequest paginationRequest)
            throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceTypeDAO.getDeviceTypes(tenantId, paginationRequest);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while obtaining the device types for tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceLocationHistory> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from, long to)
            throws DeviceManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Get device location information");
        }

        List<DeviceLocationHistory> deviceLocationHistory;
        String errMessage;

        try {
            DeviceManagementDAOFactory.openConnection();
            deviceLocationHistory = deviceDAO.getDeviceLocationInfo(deviceIdentifier, from, to);

        } catch (DeviceManagementDAOException e) {
            errMessage = "Error occurred in getDeviceLocationInfo";
            log.error(errMessage, e);
            throw new DeviceManagementException(errMessage, e);
        } catch (SQLException e) {
            errMessage = "Error occurred while opening a connection to the data source";
            log.error(errMessage, e);
            throw new DeviceManagementException(errMessage, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        return deviceLocationHistory;
    }

    @Override
    public void notifyPullNotificationSubscriber(DeviceIdentifier deviceIdentifier, Operation operation)
            throws PullNotificationExecutionFailedException {
        if (log.isDebugEnabled()) {
            log.debug("Notify pull notification subscriber");
        }
        DeviceManagementService dms =
                pluginRepository.getDeviceManagementService(deviceIdentifier.getType(), this.getTenantId());
        if (dms == null) {
            String message = "Device type '" + deviceIdentifier.getType() + "' does not have an associated " +
                    "device management plugin registered within the framework";
            log.error(message);
            throw new PullNotificationExecutionFailedException(message);
        }
        PullNotificationSubscriber pullNotificationSubscriber = dms.getPullNotificationSubscriber();
        if (pullNotificationSubscriber == null) {
            String message = "Pull Notification Subscriber is not configured " +
                    "for device type" + deviceIdentifier.getType();
            log.error(message);
            throw new PullNotificationExecutionFailedException(message);
        }
        pullNotificationSubscriber.execute(deviceIdentifier, operation);
    }

    /**
     * Returns all the device-info including location of the given device.
     */
    private DeviceInfo getDeviceInfo(Device device) throws DeviceManagementException {
        if (device == null) {
            String msg = "Received incomplete data for getDeviceInfo";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device info of device: " + device.getId() + " of type '" + device.getType() + "'");
        }
        DeviceInfo info = null;
        try {
            DeviceManagementDAOFactory.openConnection();
            info = deviceInfoDAO.getDeviceInformation(device.getId(), device.getEnrolmentInfo().getId());
            DeviceLocation location = deviceInfoDAO.getDeviceLocation(device.getId(),
                    device.getEnrolmentInfo().getId());
            if (location != null) {
                //There are some cases where the device-info is not updated properly. Hence returning a null value.
                if (info != null) {
                    info.setLocation(location);
                } else {
                    info = new DeviceInfo();
                    info.setLocation(location);
                }
            }
        } catch (DeviceDetailsMgtDAOException e) {
            String msg = "Error occurred while retrieving advance info of '" + device.getType() +
                    "' that carries the id '" + device.getDeviceIdentifier() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceInfo for device: " + device.getId();
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return info;
    }

    /**
     * Returns all the installed apps of the given device.
     */
    private List<Application> getInstalledApplications(Device device) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get installed applications of device: " + device.getId() + " of type '" + device.getType() + "'");
        }
        List<Application> applications;
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            applications = applicationDAO.getInstalledApplications(device.getId(),
                    device.getEnrolmentInfo().getId(), tenantId);
            device.setApplications(applications);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the application list of '" + device.getType() + "', " +
                    "which carries the id '" + device.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getInstalledApplications";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return applications;
    }

    /**
     * Returns all the available information (device-info, location, applications and plugin-db data)
     * of the given device list.
     */
    private List<Device> getAllDeviceInfo(List<Device> allDevices) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get all device info of devices, num of devices: " + allDevices.size());
        }
        List<Device> devices = new ArrayList<>();
        for (Device device : allDevices) {
            device.setDeviceInfo(this.getDeviceInfo(device));
            device.setApplications(this.getInstalledApplications(device));
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    /**
     * Returns all the available information (device-info, location, applications and plugin-db data)
     * of a given device.
     */
    private Device getAllDeviceInfo(Device device) throws DeviceManagementException {
        if (device == null) {
            String msg = "Received empty device for getAllDeviceInfo";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get all device info of device: " + device.getId() + " of type '" + device.getType() + "'");
        }
        device.setDeviceInfo(this.getDeviceInfo(device));
        device.setApplications(this.getInstalledApplications(device));

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return device;
        }
        Device dmsDevice =
                deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        if (dmsDevice != null) {
            device.setFeatures(dmsDevice.getFeatures());
            device.setProperties(dmsDevice.getProperties());
        }
        return device;
    }

    private Device getDeviceFromCache(DeviceIdentifier deviceIdentifier) {
        return DeviceCacheManagerImpl.getInstance().getDeviceFromCache(deviceIdentifier, this.getTenantId());
    }

    private void addDeviceToCache(DeviceIdentifier deviceIdentifier, Device device) {
        DeviceCacheManagerImpl.getInstance().addDeviceToCache(deviceIdentifier, device, this.getTenantId());
    }

    private void removeDeviceFromCache(DeviceIdentifier deviceIdentifier) {
        DeviceCacheManagerImpl.getInstance().removeDeviceFromCache(deviceIdentifier, this.getTenantId());
    }

    /***
     * This method removes a given list of devices from the cache
     * @param deviceList list of DeviceCacheKey objects
     */
    private void removeDevicesFromCache(List<DeviceCacheKey> deviceList) {
        DeviceCacheManagerImpl.getInstance().removeDevicesFromCache(deviceList);
    }

    @Override
    public List<GeoCluster> findGeoClusters(String deviceType, GeoCoordinate southWest, GeoCoordinate northEast,
                                            int geohashLength) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            if (deviceType == null || deviceType.isEmpty()) {
                log.debug("get information about geo clusters.");
            } else {
                log.debug("get information about geo clusters for device type: " + deviceType);
            }
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.findGeoClusters(deviceType, southWest, northEast, geohashLength, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the geo clusters.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in findGeoClusters";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCountOfTypeByStatus(String deviceType, String deviceStatus) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCount(deviceType, deviceStatus, getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in while retrieving device count by status for deviceType :" +deviceType + " status : " + deviceStatus;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<String> getDeviceIdentifiersByStatus(String deviceType, String deviceStatus) throws DeviceManagementException {
        List<String> deviceIds;
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceIds = deviceDAO.getDeviceIdentifiers(deviceType, deviceStatus, getTenantId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in while retrieving devices by status for deviceType :" +deviceType + " status : " + deviceStatus;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceIds;
    }

    @Override
    public boolean bulkUpdateDeviceStatus(String deviceType, List<String> deviceList, String status)
            throws DeviceManagementException {
        boolean success;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            success = deviceDAO.setEnrolmentStatusInBulk(deviceType, status, getTenantId(), deviceList);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred in while updating status of devices :" + deviceType + " status : " + status;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return success;
    }

    public boolean updateEnrollment(String owner, List<String> deviceIdentifiers)
            throws DeviceManagementException, UserNotFoundException, InvalidDeviceException {

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<Device> existingDevices;
            owner = validateOwner(owner, tenantId);
            try {
                DeviceManagementDAOFactory.beginTransaction();
                existingDevices = deviceDAO.getDevicesByIdentifiers(deviceIdentifiers, tenantId);
                if (existingDevices.size() != deviceIdentifiers.size()) {
                    for (Device device : existingDevices) {
                        deviceIdentifiers.remove(device.getDeviceIdentifier());
                    }
                    String msg =
                            "Couldn't find device ids for requested all device identifiers. Therefore payload should "
                                    + "contains device identifiers which are not in the system. Invalid device "
                                    + "identifiers are " + deviceIdentifiers.toString();
                    log.error(msg);
                    throw new InvalidDeviceException(msg);
                }
                if (enrollmentDAO.updateOwnerOfEnrollment(existingDevices, owner, tenantId)) {
                    DeviceManagementDAOFactory.commitTransaction();
                    return true;
                }
                DeviceManagementDAOFactory.rollbackTransaction();
                return false;
            } catch (TransactionManagementException e) {
                String msg = "Error occurred while initiating the transaction.";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred either verifying existence of device ids or updating owner of the device.";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
    }

    private String validateOwner(String owner, int tenantId) throws UserNotFoundException, DeviceManagementException {
        try {
            if (StringUtils.isEmpty(owner)) {
                owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            } else {
                boolean isUserExisit = DeviceManagementDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId).getUserStoreManager().isExistingUser(owner);
                if (!isUserExisit) {
                    String msg = "Owner does not exist in the user storage. Owner: " + owner;
                    log.error(msg);
                    throw new UserNotFoundException(msg);
                }
            }
            return owner;
        } catch (UserStoreException e) {
            String msg = "Error occurred when checking whether owner is exist or not. Owner: " + owner;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    /**
     * Extracting device location properties
     * @param device Device object
     */
    private void extractDeviceLocationToUpdate(Device device) {
        List<Device.Property> properties = device.getProperties();
        if (properties != null) {
            String latitude = null;
            String longitude = null;
            String altitude = null;
            String speed = null;
            String bearing = null;
            String distance = null;
            for (Device.Property p : properties) {
                if (p.getName().equalsIgnoreCase("latitude")) {
                    latitude = p.getValue();
                }
                if (p.getName().equalsIgnoreCase("longitude")) {
                    longitude = p.getValue();
                }
                if (p.getName().equalsIgnoreCase("altitude")) {
                    altitude = p.getValue();
                }
                if (p.getName().equalsIgnoreCase("speed")) {
                    speed = p.getValue();
                }
                if (p.getName().equalsIgnoreCase("bearing")) {
                    bearing = p.getValue();
                }
                if (p.getName().equalsIgnoreCase("distance")) {
                    distance = p.getValue();
                }
            }
            if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude) &&
                    StringUtils.isNotBlank(altitude) && StringUtils.isNotBlank(speed) &&
                        StringUtils.isNotBlank(bearing) && StringUtils.isNotBlank(distance)) {
                DeviceLocation deviceLocation = new DeviceLocation();
                deviceLocation.setDeviceId(device.getId());
                deviceLocation.setDeviceIdentifier(new DeviceIdentifier(device.getDeviceIdentifier(),
                        device.getType()));
                try {
                    deviceLocation.setAltitude(Double.parseDouble(altitude));
                    deviceLocation.setLatitude(Double.parseDouble(latitude));
                    deviceLocation.setLongitude(Double.parseDouble(longitude));
                    deviceLocation.setDistance(Double.parseDouble(distance));
                    deviceLocation.setSpeed(Float.parseFloat(speed));
                    deviceLocation.setBearing(Float.parseFloat(bearing));
                    deviceInformationManager.addDeviceLocation(deviceLocation);
                } catch (Exception e) {
                    //We are not failing the execution since this is not critical for the functionality. But logging as
                    // a warning for reference.
                    log.warn("Error occurred while trying to add '" + device.getType() + "' device '" +
                            device.getDeviceIdentifier() + "' (id:'" + device.getId() + "') location (lat:" + latitude +
                            ", lon:" + longitude + ", altitude: " + altitude +
                            ", speed: " + speed + ", bearing:" + bearing + ", distance: " + distance + ") due to:" + e.getMessage());
                }
            }
        }
    }

    /***
     *
     * <p>
     * If the device enrollment is succeeded and the enrollment notification sending is enabled, this method executes.
     * If it is configured to send enrollment notification by using the extension, initiate the instance from
     * configured instance class and execute the notify method to send enrollment notification.
     * </p>
     *
     *<p>
     * In default, if it is enabled the enrollment notification sending and disabled the notifying through extension,
     * it uses pre-defined API to send enrollment notification. In that case, invoke the
     * /api/device-mgt/enrollment-notification API with the constructed payload.
     *</p>
     * @param device {@link Device} object
     */
    private void sendNotification(Device device) {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        EnrollmentNotificationConfiguration enrollmentNotificationConfiguration = config
                .getEnrollmentNotificationConfiguration();
        try {
            if (enrollmentNotificationConfiguration != null && enrollmentNotificationConfiguration.isEnabled()) {
                if (enrollmentNotificationConfiguration.getNotifyThroughExtension()) {
                    Class<?> clz = Class.forName(enrollmentNotificationConfiguration.getExtensionClass());
                    EnrollmentNotifier enrollmentNotifier = (EnrollmentNotifier) clz.newInstance();
                    enrollmentNotifier.notify(device);
                } else {
                    String internalServerAddr = enrollmentNotificationConfiguration.getNotyfyingInternalHost();
                    if (internalServerAddr == null) {
                        internalServerAddr = "https://localhost:8243";
                    }
                    invokeApi(device, internalServerAddr);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Either Enrollment Notification Configuration is disabled or not defined in the cdm-config.xml");
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Extension class cannot be located", e);
        } catch (IllegalAccessException e) {
            log.error("Can't access  the class or its nullary constructor is not accessible.", e);
        } catch (InstantiationException e) {
            log.error("Extension class instantiation is failed", e);
        } catch (EnrollmentNotifierException e) {
            log.error("Error occured while sending enrollment notification." + e);
        }
    }

    private void invokeApi(Device device, String internalServerAddr) throws EnrollmentNotifierException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost apiEndpoint = new HttpPost(
                    internalServerAddr + DeviceManagementConstants.ENROLLMENT_NOTIFICATION_API_ENDPOINT);
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_XML.toString());
            apiEndpoint.setEntity(constructEnrollmentNotificationPayload(device));
            HttpResponse response = client.execute(apiEndpoint);
            if (response != null) {
                log.info("Enrollment Notification is sent through a configured API. Response code: " + response
                        .getStatusLine().getStatusCode());
            } else {
                log.error("Response is 'NUll' for the Enrollment notification sending API call.");
            }
        } catch (IOException e) {
            throw new EnrollmentNotifierException("Error occured when invoking API. API endpoint: " + internalServerAddr
                    + DeviceManagementConstants.ENROLLMENT_NOTIFICATION_API_ENDPOINT, e);
        }
    }

    /***
     *
     * Convert device object into XML string and construct {@link StringEntity} object and returns.
     * <p>
     * First create {@link JAXBContext} and thereafter create {@link Marshaller} by usig created {@link JAXBContext}.
     * Then enable formatting and get the converted xml string output of {@link Device}.
     * </p>
     *
     * @param device {@link Device} object
     * @return {@link StringEntity}
     * @throws EnrollmentNotifierException, if error occured while converting {@link Device} object into XML sting
     */
    private static StringEntity constructEnrollmentNotificationPayload(Device device)
            throws EnrollmentNotifierException {
        try {
            DevicePropertyNotification devicePropertyNotification = new DevicePropertyNotification();
            for (Device.Property property : device.getProperties()) {
                if ("SERIAL".equals(property.getName())) {
                    devicePropertyNotification.setSerial(property.getValue());
                }
                if ("IMEI".equals((property.getName()))) {
                    devicePropertyNotification.setImei(property.getValue());
                }
            }
            DeviceEnrollmentInfoNotification deviceEnrollmentInfoNotification = new DeviceEnrollmentInfoNotification();
            deviceEnrollmentInfoNotification.setOwner(device.getEnrolmentInfo().getOwner());
            deviceEnrollmentInfoNotification.setDateOfEnrolment(device.getEnrolmentInfo().getDateOfEnrolment());
            deviceEnrollmentInfoNotification.setDateOfLastUpdate(device.getEnrolmentInfo().getDateOfLastUpdate());
            deviceEnrollmentInfoNotification.setOwnership(device.getEnrolmentInfo().getOwnership().toString());
            deviceEnrollmentInfoNotification.setStatus(device.getEnrolmentInfo().getStatus().toString());

            DeviceNotification deviceNotification = new DeviceNotification(device.getDeviceIdentifier(), device.getName(),
                    device.getType(), device.getDescription(), devicePropertyNotification,
                    deviceEnrollmentInfoNotification);

            JAXBContext jaxbContext = JAXBContext.newInstance(DeviceNotification.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(deviceNotification, sw);
            String payload = sw.toString();
            return new StringEntity(payload, ContentType.APPLICATION_XML);
        } catch (JAXBException e) {
            throw new EnrollmentNotifierException(
                    "Error occured when converting Device object into xml string. Hence enrollment notification payload "
                            + "constructing is failed", e);
        }
    }

    private void initializeDeviceTypeVersions(DeviceManagementService deviceManagementService) {
        DeviceTypePlatformDetails deviceTypePlatformDetails = deviceManagementService.getDeviceTypePlatformDetails();
        String deviceType = deviceManagementService.getType();
        try {
            if (deviceTypePlatformDetails != null && deviceTypePlatformDetails.getDeviceTypePlatformVersion() != null
                    && deviceTypePlatformDetails.getDeviceTypePlatformVersion().size() > 0) {

                List<DeviceTypePlatformVersion> fromXML = deviceTypePlatformDetails.getDeviceTypePlatformVersion();
                List<DeviceTypePlatformVersion> newPlatformsToBeAdded = new ArrayList<>();
                List<DeviceTypeVersion> existingPlatformVersions = getDeviceTypeVersions(deviceType);

                for (DeviceTypePlatformVersion versionFromXml : fromXML) {
                    boolean match = false;
                    if (existingPlatformVersions != null && existingPlatformVersions.size() > 0) {
                        for (DeviceTypeVersion existingVersion : existingPlatformVersions) {
                            if (existingVersion.getVersionName().equals(versionFromXml.getVersionsName())) {
                                match = true;
                            }
                        }
                    }

                    if (!match) {
                        newPlatformsToBeAdded.add(versionFromXml);
                    }
                }

                DeviceTypeVersion deviceTypeVersion;
                for (DeviceTypePlatformVersion version : newPlatformsToBeAdded) {
                    deviceTypeVersion = new DeviceTypeVersion();
                    deviceTypeVersion.setDeviceTypeId(getDeviceType(deviceType).getId());
                    deviceTypeVersion.setDeviceTypeName(deviceManagementService.getType());
                    deviceTypeVersion.setVersionName(version.getVersionsName());
                    addDeviceTypeVersion(deviceTypeVersion);
                }
            }
        } catch (DeviceManagementException e) {
            log.error("Error while adding versions for device type: " + deviceManagementService.getType(), e);
        }
    }

    @Override
    public boolean addDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion) throws DeviceManagementException {
        boolean success;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            success = deviceTypeDAO.addDeviceTypeVersion(deviceTypeVersion);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding versions to device type: " + deviceTypeVersion.getDeviceTypeName();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return success;
    }

    @Override
    public List<DeviceTypeVersion> getDeviceTypeVersions(String typeName) throws
            DeviceManagementException {
        List<DeviceTypeVersion> versions = null;
        DeviceType deviceType = getDeviceType(typeName);
        if (deviceType != null) {
            try {
                DeviceManagementDAOFactory.openConnection();
                versions = deviceTypeDAO.getDeviceTypeVersions(deviceType.getId(), typeName);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while getting versions of device type: " + typeName;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        return versions;
    }

    @Override
    public boolean updateDeviceTypeVersion(DeviceTypeVersion deviceTypeVersion) throws DeviceManagementException {
        boolean success;
        try {
            DeviceType deviceType = getDeviceType(deviceTypeVersion.getDeviceTypeName());
            DeviceManagementDAOFactory.beginTransaction();
            deviceTypeVersion.setDeviceTypeId(deviceType.getId());
            success = deviceTypeDAO.updateDeviceTypeVersion(deviceTypeVersion);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating versions to device type: "
                    + deviceTypeVersion.getDeviceTypeName();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return success;
    }

    @Override
    public boolean isDeviceTypeVersionChangeAuthorized(String deviceTypeName, String version) throws
            DeviceManagementException {
        boolean success = false;
        try {
            // Get the device type details of the deviceTypeName provided in current tenant.
            DeviceType deviceType = getDeviceType(deviceTypeName);
            DeviceManagementDAOFactory.openConnection();
            if (deviceType != null) {
                success = deviceTypeDAO.isDeviceTypeVersionModifiable(deviceType.getId(), version, this.getTenantId());
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting authorization details of device type : " + deviceTypeName;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting db connection to authorize details of device type : " +
                    deviceTypeName;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return success;
    }

    public DeviceTypeVersion getDeviceTypeVersion(String deviceTypeName, String version) throws
            DeviceManagementException {
        DeviceTypeVersion versions = null;
        DeviceType deviceType = getDeviceType(deviceTypeName);
        if (deviceType != null) {
            try {
                DeviceManagementDAOFactory.openConnection();
                versions = deviceTypeDAO.getDeviceTypeVersion(deviceType.getId(), version);
            } catch (DeviceManagementDAOException e) {
                String msg = "Error occurred while getting versions of device type: " + deviceTypeName + " ,version: "
                        + version;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        return versions;
    }

    @Override
    public boolean deleteDeviceType(String deviceTypeName, DeviceType deviceType)
            throws DeviceManagementException {
        List<String> deviceIdentifiers;

        if (deviceType == null || StringUtils.isBlank(deviceTypeName)) {
            String msg = "Error, device type cannot be null or empty or a blank space";
            log.error(msg);
            return false;
        }
        List<Device> devices = getAllDevices(deviceTypeName, false);
        if (devices == null || devices.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No devices found for the device type: " + deviceTypeName);
            }
        } else {
            // dis-enroll devices
            disEnrollDevices(devices);
            // delete devices
            deviceIdentifiers = devices.stream()
                    .map(Device::getDeviceIdentifier).collect(Collectors.toList());
            try {
                if(!deleteDevices(deviceIdentifiers)){
                    log.error("Failed to delete devices of device type: " + deviceTypeName);
                    return false;
                }
            } catch (InvalidDeviceException e) {
                String msg = "Error occurred while deleting devices of type: " + deviceTypeName;
                log.error(msg);
                throw new DeviceManagementException(msg, e);
            }
        }

        // remove device type versions
        if (!deleteDeviceTypeVersions(deviceType)) {
            log.error("Failed to delete device type vesions for device type: " + deviceTypeName);
            return false;
        }

        try {
            // delete device type
            DeviceManagementDAOFactory.beginTransaction();
            deviceTypeDAO.deleteDeviceType(getTenantId(), deviceType.getId());
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting device type of: " + deviceTypeName;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return true;
    }

    @Override
    public void disEnrollDevices(List<Device> devices)
            throws DeviceManagementException {
        int tenantId = getTenantId();

        try {
            DeviceManagementDAOFactory.beginTransaction();
            for (Device device : devices) {
                if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.REMOVED)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Device: " + device.getName() + " has already dis-enrolled");
                    }
                } else {
                    device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
                    device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
                    // different try blocks are used to isolate transactions
                    try {
                        enrollmentDAO.updateEnrollment(device.getId(), device.getEnrolmentInfo(),
                                                       tenantId);
                    } catch (DeviceManagementDAOException e) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        String msg = "Error occurred while dis-enrolling device: " +
                                     device.getName();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    }
                    try {
                        deviceDAO.updateDevice(device, tenantId);
                    } catch (DeviceManagementDAOException e) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        String msg = "Error occurred while updating device: " +
                                     device.getName();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    }
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deleteDeviceTypeVersions(DeviceType deviceType)
            throws DeviceManagementException {
        boolean result;
        String deviceTypeName = deviceType.getName();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<DeviceTypeVersion> deviceTypeVersions = deviceTypeDAO
                    .getDeviceTypeVersions(deviceType.getId(), deviceTypeName);
            if (deviceTypeVersions == null || deviceTypeVersions.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Device of type: " + deviceTypeName + "doesn't have any type " +
                              "versions");
                }
            } else {
                for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
                    result = deviceTypeDAO.isDeviceTypeVersionModifiable(deviceType.getId()
                            , deviceTypeVersion.getVersionName(), getTenantId());
                    if (!result) {
                        String msg = "Device type of: " + deviceTypeName + "is unauthorized to " +
                                     "modify version";
                        log.error(msg);
                        return false;
                    }
                    deviceTypeVersion.setVersionStatus("REMOVED");
                    result = deviceTypeDAO.updateDeviceTypeVersion(deviceTypeVersion);
                    if (!result) {
                        String msg = "Could not delete the version of device type: " + deviceTypeName;
                        log.error(msg);
                        return false;
                    }
                }
                DeviceManagementDAOFactory.commitTransaction();
            }
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting device type of: " + deviceTypeName;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return true;
    }


    @Override
    public DeviceConfiguration getDeviceConfiguration(Map<String, String> deviceProps)
            throws DeviceManagementException, DeviceNotFoundException, UnauthorizedDeviceAccessException,
                   AmbiguousConfigurationException {

        if (log.isDebugEnabled()) {
            log.debug("Attempting to get device configurations based on properties.");
        }

        DevicePropertyInfo deviceProperties;
        List<DevicePropertyInfo> devicePropertyList;
        try {
            DeviceManagementDAOFactory.openConnection();
            devicePropertyList = deviceDAO.getDeviceBasedOnDeviceProperties(deviceProps);
            if (devicePropertyList == null || devicePropertyList.isEmpty()) {
                String msg = "Cannot find device for specified properties";
                log.info(msg);
                throw new DeviceNotFoundException(msg);
            }
            //In this service, there should be only one device for the specified property values
            //If multiple values retrieved, It'll be marked as ambiguous.
            if (devicePropertyList.size() > 1) {
                String msg = "Device property list contains more than one element";
                log.error(msg);
                throw new AmbiguousConfigurationException(msg);
            }
            //Get the only existing value of the list
            deviceProperties = devicePropertyList.get(0);

        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Devices configuration retrieval criteria cannot be null or empty.";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        }  finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantId(Integer.parseInt(deviceProperties.getTenantId()), true);
            Device device = this.getDevice(new DeviceIdentifier(deviceProperties.getDeviceIdentifier(),
                                                                deviceProperties.getDeviceTypeName()), false);
            String owner = device.getEnrolmentInfo().getOwner();
            PlatformConfiguration configuration = this.getConfiguration(device.getType());
            List<ConfigurationEntry> configurationEntries = new ArrayList<>();
            if (configuration != null) {
                configurationEntries = configuration.getConfiguration();
            }
            return wrapConfigurations(device, ctx.getTenantDomain(), configurationEntries, owner);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public List<String> transferDeviceToTenant(DeviceTransferRequest deviceTransferRequest)
            throws DeviceManagementException, DeviceNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to transfer devices to '" +
                    deviceTransferRequest.getDestinationTenant() + "'");
        }
        List<String> enrolledDevices = new ArrayList<>();
        DeviceIdentifier deviceIdentifier;
        for (String deviceId : deviceTransferRequest.getDeviceIds()) {
            deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceTransferRequest.getDeviceType());
            if (isEnrolled(deviceIdentifier)) {
                enrolledDevices.add(deviceId);
            } else {
                log.warn("Device '" + deviceId + "' is not enrolled with super tenant. Hence excluding from transferring");
            }
        }

        if (enrolledDevices.isEmpty()) {
            throw new DeviceNotFoundException("No any enrolled device found to transfer");
        }

        int destinationTenantId;
        String owner;
        try {
            TenantMgtAdminService tenantMgtAdminService = new TenantMgtAdminService();
            TenantInfoBean tenantInfoBean = tenantMgtAdminService.getTenant(deviceTransferRequest.getDestinationTenant());
            destinationTenantId = tenantInfoBean.getTenantId();
            owner = tenantInfoBean.getAdmin();
        } catch (Exception e) {
            String msg = "Error getting destination tenant id and admin from domain'" +
                    deviceTransferRequest.getDestinationTenant() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            List<String> movedDevices = new ArrayList<>();
            for (String deviceId : enrolledDevices) {
                if (deviceDAO.transferDevice(deviceTransferRequest.getDeviceType(), deviceId, owner, destinationTenantId)){
                    movedDevices.add(deviceId);
                } else {
                    log.warn("Device '" + deviceId + "' not transferred to tenant " + destinationTenantId);
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
            return movedDevices;
        } catch (SQLException | DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error in transferring devices to tenant '" + deviceTransferRequest.getDestinationTenant() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        }  finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getAppSubscribedDevices(int offsetValue, int limitValue, List<Integer> devicesIds,
            String status) throws DeviceManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices details for device ids: " + devicesIds);
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> subscribedDeviceDetails;
        try {
            DeviceManagementDAOFactory.openConnection();
            subscribedDeviceDetails = deviceDAO
                    .getSubscribedDevices(offsetValue, limitValue, devicesIds, tenantId, status);
            if (subscribedDeviceDetails.isEmpty()){
                paginationResult.setData(new ArrayList<>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            int count = deviceDAO.getSubscribedDeviceCount(devicesIds, tenantId, status);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list for device ids " + devicesIds;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        paginationResult.setData(getAllDeviceInfo(subscribedDeviceDetails));
        return paginationResult;
    }

    @Override
    public PaginationResult getApplications(PaginationRequest request)
            throws ApplicationManagementException, DeviceTypeNotFoundException {
        PaginationResult paginationResult = new PaginationResult();
        try {
            int tenantId = DeviceManagementDAOUtil.getTenantId();
            request = DeviceManagerUtil.validateDeviceListPageSize(request);

            String deviceType = request.getDeviceType();
            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(
                    deviceType, tenantId);
            if (deviceTypeObj == null) {
                String msg = "Error, device of type (application platform): " + deviceType + " does not exist";
                log.error(msg);
                throw new DeviceTypeNotFoundException(msg);
            }

            try {
                DeviceManagementDAOFactory.openConnection();
                List<Application> applicationList = applicationDAO.getApplications(
                        request,
                        tenantId
                );
                paginationResult.setData(applicationList);
                paginationResult.setRecordsTotal(applicationList.size());
                return paginationResult;
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection " +
                        "to the data source";
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            }  finally {
                DeviceManagementDAOFactory.closeConnection();
            }

        } catch (DeviceManagementException e) {
            String msg = "Error occurred while validating device list page size";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public List<String> getAppVersions(String packageName)
            throws ApplicationManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            List<String> versions = applicationDAO.getAppVersions(
                    DeviceManagementDAOUtil.getTenantId(),
                    packageName
            );
            return versions;
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection " +
                    "to the data source";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving Tenant ID";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }  finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Wrap the device configuration data into DeviceConfiguration bean
     * @param device Device queried using the properties
     * @param tenantDomain tenant domain
     * @param configurationEntries platformConfiguration list
     * @param deviceOwner name of the device owner
     * @return Wrapped {@link DeviceConfiguration} object with data
     */
    private DeviceConfiguration wrapConfigurations(Device device,
                                                   String tenantDomain,
                                                   List<ConfigurationEntry> configurationEntries,
                                                   String deviceOwner) {
        DeviceConfiguration deviceConfiguration = new DeviceConfiguration();
        deviceConfiguration.setDeviceId(device.getDeviceIdentifier());
        deviceConfiguration.setDeviceType(device.getType());
        deviceConfiguration.setTenantDomain(tenantDomain);
        deviceConfiguration.setConfigurationEntries(configurationEntries);
        deviceConfiguration.setDeviceOwner(deviceOwner);
        return deviceConfiguration;
    }

    public int getFunctioningDevicesInSystem() throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get functioning devices count");
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getFunctioningDevicesInSystem();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the device count";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
