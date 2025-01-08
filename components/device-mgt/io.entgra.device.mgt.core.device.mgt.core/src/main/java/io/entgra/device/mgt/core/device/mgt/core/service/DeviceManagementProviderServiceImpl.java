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

package io.entgra.device.mgt.core.device.mgt.core.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.ConflictException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.EnrollmentDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceStatusDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.TenantDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.TagDAO;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceDetailsDTO;
import io.entgra.device.mgt.core.device.mgt.core.dto.OwnerWithDeviceDTO;
import io.entgra.device.mgt.core.device.mgt.core.dto.OperationDTO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.DeviceEnrolmentLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraDeviceEnrolmentLoggerImpl;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.StringUtils;
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
import io.entgra.device.mgt.core.device.mgt.common.ActivityPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceEnrollmentInfoNotification;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManager;
import io.entgra.device.mgt.core.device.mgt.common.DeviceNotification;
import io.entgra.device.mgt.core.device.mgt.common.DevicePropertyNotification;
import io.entgra.device.mgt.core.device.mgt.common.DeviceTransferRequest;
import io.entgra.device.mgt.core.device.mgt.common.DynamicTaskContext;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.FeatureManager;
import io.entgra.device.mgt.core.device.mgt.common.InitialOperationConfig;
import io.entgra.device.mgt.core.device.mgt.common.MonitoringOperation;
import io.entgra.device.mgt.core.device.mgt.common.OperationMonitoringTaskConfig;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.StartupOperationConfig;
import io.entgra.device.mgt.core.device.mgt.common.BillingResponse;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.AmbiguousConfigurationException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationEntry;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.CorrectiveActionConfig;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.DeviceConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.DevicePropertyInfo;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.EnrollmentConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.cost.mgt.Cost;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceData;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocation;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import io.entgra.device.mgt.core.device.mgt.common.enrollment.notification.EnrollmentNotificationConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.enrollment.notification.EnrollmentNotifier;
import io.entgra.device.mgt.core.device.mgt.common.enrollment.notification.EnrollmentNotifierException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidDeviceException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UnauthorizedDeviceAccessException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UserNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoQuery;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupConstants;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupAlreadyExistException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitationDetails;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.License;
import io.entgra.device.mgt.core.device.mgt.common.license.mgt.LicenseManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.DeviceActivity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManager;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import io.entgra.device.mgt.core.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.common.pull.notification.PullNotificationSubscriber;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceStatus;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypePlatformDetails;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypePlatformVersion;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementPluginRepository;
import io.entgra.device.mgt.core.device.mgt.core.cache.DeviceCacheKey;
import io.entgra.device.mgt.core.device.mgt.core.cache.impl.BillingCacheManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.common.Constants;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceInformationManager;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceTypeServiceIdentifier;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceTypeVersion;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoCluster;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent;
import io.entgra.device.mgt.core.device.mgt.core.internal.PluginInitializationListener;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.device.mgt.core.util.HttpReportingUtil;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.ContentProviderInfo;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.EmailContext;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.EmailSendingFailedException;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.EmailTransportNotConfiguredException;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.TypedValue;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class DeviceManagementProviderServiceImpl implements DeviceManagementProviderService,
        PluginInitializationListener {

    DeviceEnrolmentLogContext.Builder deviceEnrolmentLogContextBuilder = new DeviceEnrolmentLogContext.Builder();

    private static final EntgraLogger log = new EntgraDeviceEnrolmentLoggerImpl(DeviceManagementProviderServiceImpl.class);

    private static final String OPERATION_RESPONSE_EVENT_STREAM_DEFINITION = "org.wso2.iot.OperationResponseStream";
    private final DeviceManagementPluginRepository pluginRepository;
    private final DeviceDAO deviceDAO;
    private final DeviceTypeDAO deviceTypeDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final OperationDAO operationDAO;
    private final ApplicationDAO applicationDAO;
    private MetadataDAO metadataDAO;
    private final DeviceStatusDAO deviceStatusDAO;
    private final TenantDAO tenantDao;
    private final TagDAO tagDAO;
    int count = 0;

    public DeviceManagementProviderServiceImpl() {
        this.pluginRepository = new DeviceManagementPluginRepository();
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
        this.operationDAO = OperationManagementDAOFactory.getOperationDAO();
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
        this.deviceStatusDAO = DeviceManagementDAOFactory.getDeviceStatusDAO();
        this.tenantDao = DeviceManagementDAOFactory.getTenantDAO();
        this.tagDAO = DeviceManagementDAOFactory.getTagDAO();

        /* Registering a listener to retrieve events when some device management service plugin is installed after
         * the component is done getting initialized */
        DeviceManagementServiceComponent.registerPluginInitializationListener(this);
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
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        if (device == null) {
            String msg = "Received empty device for device enrollment";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (device.getEnrolmentInfo() == null) {
            String msg = "Received device without valid enrollment info for device enrollment";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (device.getEnrolmentInfo().getStatus() == null) {
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.ACTIVE);
        }
        if (log.isDebugEnabled()) {
            log.debug("Enrolling the device " + device.getId() + "of type '" + device.getType() + "'");
        }

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'enrollDevice'");
            }
            return false;
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        EnrollmentConfiguration enrollmentConfiguration = DeviceManagerUtil.getEnrollmentConfigurationEntry();
        String deviceSerialNumber = null;
        if (enrollmentConfiguration != null) {
            deviceSerialNumber = DeviceManagerUtil.getPropertyString(device.getProperties(),
                    DeviceManagementConstants.Common.SERIAL);
            if (!DeviceManagerUtil.isDeviceEnrollable(enrollmentConfiguration, deviceSerialNumber)) {
                String msg = "Serial number based enrollment has been enabled and device having the serial number '"
                        + deviceSerialNumber + "' is not configured to be enrolled.";
                log.error(msg);
                throw new DeviceManagementException(msg);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Enrolling the device " + device.getId() + "of type '" + device.getType() + "'");
        }
        boolean status = false;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());

        int tenantId = this.getTenantId();
        Device existingDevice = this.getDevice(deviceIdentifier, false);

        if (existingDevice != null) {
            deviceManager.modifyEnrollment(device);
            EnrolmentInfo existingEnrolmentInfo = existingDevice.getEnrolmentInfo();
            EnrolmentInfo newEnrolmentInfo = device.getEnrolmentInfo();
            if (existingEnrolmentInfo != null && newEnrolmentInfo != null) {
                if (existingDevice.getEnrolmentInfo().isTransferred()
                        && existingDevice.getEnrolmentInfo().getStatus() != EnrolmentInfo.Status.REMOVED) {
                    newEnrolmentInfo = existingEnrolmentInfo;
                    status = true;
                } else {
                    //Get all the enrollments of current user for the same device
                    List<EnrolmentInfo> enrolmentInfos = this.getEnrollmentsOfUser(existingDevice.getId(),
                            newEnrolmentInfo.getOwner());
                    for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                        //If the enrollments are same (owner & ownership) then we'll update the existing enrollment.
                        if (enrolmentInfo.equals(newEnrolmentInfo)) {
                            newEnrolmentInfo.setDateOfEnrolment(enrolmentInfo.getDateOfEnrolment());
                            newEnrolmentInfo.setId(enrolmentInfo.getId());
                            //We are explicitly setting device status only if matching device enrollment is in
                            // removed status.
                            if (enrolmentInfo.getStatus() == EnrolmentInfo.Status.REMOVED &&
                                    newEnrolmentInfo.getStatus() == null) {
                                newEnrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
                            } else if (newEnrolmentInfo.getStatus() == null) {
                                newEnrolmentInfo.setStatus(enrolmentInfo.getStatus());
                            }
                            status = true;
                            break;
                        }
                    }
                }
                if (status) {
                    device.setId(existingDevice.getId());
                    device.setEnrolmentInfo(newEnrolmentInfo);
                    this.modifyEnrollment(device);
                } else {
                    int updateStatus = 0;
                    EnrolmentInfo enrollment;
                    try {
                        //Remove the existing enrollment
                        DeviceManagementDAOFactory.beginTransaction();
                        if (!EnrolmentInfo.Status.REMOVED.equals(existingEnrolmentInfo.getStatus())) {
                            existingEnrolmentInfo.setStatus(EnrolmentInfo.Status.REMOVED);
                            updateStatus = enrollmentDAO.updateEnrollment(existingEnrolmentInfo, tenantId);
                        }
                        if ((updateStatus > 0) || EnrolmentInfo.Status.REMOVED.
                                equals(existingEnrolmentInfo.getStatus())) {
                            enrollment = enrollmentDAO
                                    .addEnrollment(existingDevice.getId(), deviceIdentifier,
                                            newEnrolmentInfo, tenantId);
                            if (enrollment == null) {
                                DeviceManagementDAOFactory.rollbackTransaction();
                                throw new DeviceManagementException(
                                        "Enrollment data persistence is failed in a re-enrollment. Existing device: "
                                                + existingDevice.toString() + ", New Device: " + device.toString());
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
                            log.info("Device enrolled successfully", deviceEnrolmentLogContextBuilder
                                    .setDeviceId(String.valueOf(existingDevice.getId()))
                                    .setDeviceType(String.valueOf(existingDevice.getType()))
                                    .setOwner(newEnrolmentInfo.getOwner())
                                    .setOwnership(String.valueOf(newEnrolmentInfo.getOwnership()))
                                    .setTenantID(String.valueOf(tenantId))
                                    .setTenantDomain(tenantDomain)
                                    .setUserName(userName)
                                    .build());
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
            deviceManager.enrollDevice(device);
            EnrolmentInfo enrollment;
            try {
                DeviceManagementDAOFactory.beginTransaction();
                DeviceType type = deviceTypeDAO.getDeviceType(device.getType(), tenantId);
                if (type != null) {
                    int deviceId = deviceDAO.addDevice(type.getId(), device, tenantId);
                    device.setId(deviceId);
                    enrollment = enrollmentDAO.addEnrollment(deviceId, deviceIdentifier, device.getEnrolmentInfo(), tenantId);
                    if (enrollment == null) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        throw new DeviceManagementException(
                                "Enrollment data persistence is failed in a new enrollment. Device: " + device.toString());
                    }
                    device.setEnrolmentInfo(enrollment);
                    DeviceManagementDAOFactory.commitTransaction();
                    log.info("Device enrolled successfully", deviceEnrolmentLogContextBuilder.setDeviceId(String.valueOf(device.getId())).setDeviceType(String.valueOf(device.getType())).setOwner(enrollment.getOwner()).setOwnership(String.valueOf(enrollment.getOwnership())).setTenantID(String.valueOf(tenantId)).setTenantDomain(tenantDomain).setUserName(userName).build());
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
                String msg = "Error occurred while enrolling device: " + device.toString();
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

        //enroll Traccar device
        if (HttpReportingUtil.isTrackerEnabled()) {
            DeviceManagementDataHolder.getInstance().getTraccarManagementService().addDevice(device);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Traccar is disabled");
            }
        }

        if (status) {
            addDeviceToGroups(deviceIdentifier, device.getEnrolmentInfo().getOwnership());
            if (enrollmentConfiguration != null) {
                DeviceManagerUtil.addDeviceToConfiguredGroup(enrollmentConfiguration, deviceSerialNumber,
                        deviceIdentifier);
            }
            addInitialOperations(deviceIdentifier, device.getType());
            sendNotification(device);
        }
        extractDeviceLocationToUpdate(device);
        try {
            if (device.getDeviceInfo() != null) {
                DeviceInformationManager deviceInformationManager = DeviceManagementDataHolder
                        .getInstance().getDeviceInformationManager();
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
    public boolean recordDeviceUpdate(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        int tenantId = this.getTenantId();
        boolean isUpdated;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            isUpdated = deviceDAO.recordDeviceUpdate(deviceIdentifier, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while setting updated " +
                    "timestamp of device: " + deviceIdentifier;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to set updated " +
                    "timestamp of device: " + deviceIdentifier;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return isUpdated;
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
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
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
            DeviceStatusManagementService deviceStatusManagementService = DeviceManagementDataHolder
                    .getInstance().getDeviceStatusManagementService();
            if (device.getEnrolmentInfo().getId() == 0) {
                device.getEnrolmentInfo().setId(currentDevice.getEnrolmentInfo().getId());
            }
            if (device.getEnrolmentInfo().getStatus() == null) {
                device.getEnrolmentInfo().setStatus(currentDevice.getEnrolmentInfo().getStatus());
            }
            if (device.getName() == null) {
                device.setName(currentDevice.getName());
            }

            int updatedRows = enrollmentDAO.updateEnrollment(device.getEnrolmentInfo(), tenantId);
            addDeviceStatus(deviceStatusManagementService, tenantId, updatedRows, device.getEnrolmentInfo(),
                    device.getType());

            DeviceManagementDAOFactory.commitTransaction();
            log.info("Device enrollment modified successfully",
                    deviceEnrolmentLogContextBuilder.setDeviceId(String.valueOf(currentDevice.getId()))
                            .setDeviceType(String.valueOf(currentDevice.getType()))
                            .setOwner(currentDevice.getEnrolmentInfo().getOwner())
                            .setOwnership(String.valueOf(currentDevice.getEnrolmentInfo().getOwnership()))
                            .setTenantID(String.valueOf(tenantId))
                            .setTenantDomain(tenantDomain)
                            .setUserName(userName).build());

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
        //enroll Traccar device
        if (HttpReportingUtil.isTrackerEnabled()) {
            DeviceManagementDataHolder.getInstance().getTraccarManagementService().updateDevice(device);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Traccar is disabled");
            }
        }

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
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
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
            DeviceStatusManagementService deviceStatusManagementService = DeviceManagementDataHolder
                    .getInstance().getDeviceStatusManagementService();
            int updatedRows = enrollmentDAO.updateEnrollment(device.getEnrolmentInfo(), tenantId);
            addDeviceStatus(deviceStatusManagementService, tenantId, updatedRows, device.getEnrolmentInfo(), device.getType());
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceId);

            //process to dis-enroll a device from traccar starts
            if (HttpReportingUtil.isTrackerEnabled()) {
                DeviceManagementDataHolder.getInstance().getTraccarManagementService()
                        .unLinkTraccarDevice(device.getEnrolmentInfo().getId());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Traccar is disabled");
                }
            }
            //process to dis-enroll a device from traccar ends
            log.info("Device disenrolled successfully",
                    deviceEnrolmentLogContextBuilder.setDeviceId(String.valueOf(device.getId()))
                            .setDeviceType(String.valueOf(device.getType()))
                            .setOwner(device.getEnrolmentInfo().getOwner())
                            .setOwnership(String.valueOf(device.getEnrolmentInfo().getOwnership()))
                            .setTenantID(String.valueOf(tenantId))
                            .setTenantDomain(tenantDomain)
                            .setUserName(userName).build());
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
        List<Device> validDevices = new ArrayList<>();
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
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

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
            validDevices.add(device);
            deviceIds.add(device.getId());
            validDeviceIdentifiers.add(device.getDeviceIdentifier());
            enrollmentIds.add(device.getEnrolmentInfo().getId());
            if (deviceIdentifierMap.containsKey(device.getType())) {
                deviceIdentifierMap.get(device.getType()).add(device.getDeviceIdentifier());
            } else {
                deviceIdentifierMap.put(device.getType(),
                        new ArrayList<>(Collections.singletonList(device.getDeviceIdentifier())));
                DeviceManager deviceManager = this.getDeviceManager(device.getType());
                if (deviceManager == null) {
                    log.error("Device Manager associated with the device type '" + device.getType()
                            + "' is null. Therefore, not attempting method 'deleteDevice'");
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

        try {
            DeviceManagementDAOFactory.beginTransaction();
            //deleting device from the core
            deviceDAO.deleteDevices(validDeviceIdentifiers, new ArrayList<>(deviceIds), enrollmentIds, validDevices);
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
            if (HttpReportingUtil.isTrackerEnabled()) {
                for (int enrollmentId : enrollmentIds) {
                    DeviceManagementDataHolder.getInstance().getTraccarManagementService().removeDevice(enrollmentId);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Traccar is disabled");
                }
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
            return this.populateAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public List<Device> getAllocatedDevices(String deviceType, int activeServerCount, int serverIndex) throws DeviceManagementException {
        if (deviceType == null) {
            String msg = "Device type is empty for method getAllDevices";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting allocated Devices for Server with index " + serverIndex + " and" +
                    " type '" + deviceType);
        }
        List<Device> allocatedDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allocatedDevices = deviceDAO.getAllocatedDevices(deviceType, this.getTenantId(), activeServerCount, serverIndex);
            if (allocatedDevices == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceType + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving all devices of type '" +
                    deviceType + "' that are being managed within the scope of current tenant";
            log.error(msg, e);
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
        return allocatedDevices;
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
            return this.populateAllDeviceInfo(allDevices);
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
            return this.populateAllDeviceInfo(allDevices);
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
        DeviceManagerUtil.validateDeviceListPageSize(request);
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
            paginationResult.setData(this.populateAllDeviceInfo(allDevices));
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

    /**
     * Calculate cost of a tenants Device List.
     * Once the full device list of a tenant is sent here devices are looped for cost calculation
     * Cost per tenant is retrieved from the Meta Table
     * When looping the devices the most recent device status of that device is retrieved from status table
     * If device is enrolled prior to start date now in removed state --> time is calculated from - startDate to last updated time
     * If device is enrolled prior to start date now in not removed --> time is calculated from - startDate to endDate
     * If device is enrolled after the start date now in removed state --> time is calculated from - dateOfEnrollment to last updated time
     * If device is enrolled after the start date now in not removed --> time is calculated from - dateOfEnrollment to endDate
     * Once time is calculated cost is set for each device
     *
     * @param tenantDomain Tenant domain cost id calculated for.
     * @param startDate    start date of usage period.
     * @param endDate      end date of usage period.
     * @param allDevices   device list of the tenant for the selected time-period.
     * @return Whether status is changed or not
     * @throws DeviceManagementException on errors while trying to calculate Cost
     */
    public BillingResponse calculateUsage(String tenantDomain, Timestamp startDate, Timestamp endDate, List<Device> allDevices) throws MetadataManagementDAOException, DeviceManagementException {

        BillingResponse billingResponse = new BillingResponse();
        List<Device> deviceStatusNotAvailable = new ArrayList<>();
        double totalCost = 0.0;

        try {
            MetadataManagementService meta = DeviceManagementDataHolder
                    .getInstance().getMetadataManagementService();
            Metadata metadata = meta.retrieveMetadata(DeviceManagementConstants.META_KEY);

            Gson g = new Gson();
            Collection<Cost> costData = null;
            int tenantIdContext = CarbonContext.getThreadLocalCarbonContext().getTenantId();

            Type collectionType = new TypeToken<Collection<Cost>>() {
            }.getType();
            if (tenantIdContext == MultitenantConstants.SUPER_TENANT_ID && metadata != null) {
                costData = g.fromJson(metadata.getMetaValue(), collectionType);
                for (Cost tenantCost : costData) {
                    if (tenantCost.getTenantDomain().equals(tenantDomain)) {
                        totalCost = generateCost(allDevices, startDate, endDate, tenantCost, deviceStatusNotAvailable, totalCost);
                    }
                }
            } else {
                totalCost = generateCost(allDevices, startDate, endDate, null, deviceStatusNotAvailable, totalCost);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred calculating cost of devices";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (MetadataManagementException e) {
            String msg = "Error when retrieving metadata of billing feature";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }

        if (!deviceStatusNotAvailable.isEmpty()) {
            allDevices.removeAll(deviceStatusNotAvailable);
        }

        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        calStart.setTimeInMillis(startDate.getTime());
        calEnd.setTimeInMillis(endDate.getTime());

        billingResponse.setDevice(allDevices);
        billingResponse.setYear(String.valueOf(calStart.get(Calendar.YEAR)));
        billingResponse.setStartDate(startDate.toString());
        billingResponse.setEndDate(endDate.toString());
        billingResponse.setBillPeriod(calStart.get(Calendar.YEAR) + " - " + calEnd.get(Calendar.YEAR));
        billingResponse.setTotalCostPerYear(Math.round(totalCost * 100.0) / 100.0);
        billingResponse.setDeviceCount(allDevices.size());

        return billingResponse;
    }

    public double generateCost(List<Device> allDevices, Timestamp startDate, Timestamp endDate,  Cost tenantCost, List<Device> deviceStatusNotAvailable, double totalCost) throws DeviceManagementException {
        List<DeviceStatus> deviceStatus;
        try {
            for (Device device : allDevices) {
                long dateDiff = 0;
                int tenantId = this.getTenantId();
                deviceStatus = deviceStatusDAO.getStatus(device.getId(), tenantId, null, endDate, true);
                if (device.getEnrolmentInfo().getDateOfEnrolment() < startDate.getTime()) {
                    if (!deviceStatus.isEmpty() && (String.valueOf(deviceStatus.get(0).getStatus()).equals("REMOVED")
                            || String.valueOf(deviceStatus.get(0).getStatus()).equals("DELETED"))) {
                        if (deviceStatus.get(0).getUpdateTime().getTime() >= startDate.getTime()) {
                            dateDiff = deviceStatus.get(0).getUpdateTime().getTime() - startDate.getTime();
                        }
                    } else if (!deviceStatus.isEmpty() && (!String.valueOf(deviceStatus.get(0).getStatus()).equals("REMOVED")
                            && !String.valueOf(deviceStatus.get(0).getStatus()).equals("DELETED"))) {
                        dateDiff = endDate.getTime() - startDate.getTime();
                    }
                } else {
                    if (!deviceStatus.isEmpty() && (String.valueOf(deviceStatus.get(0).getStatus()).equals("REMOVED")
                            || String.valueOf(deviceStatus.get(0).getStatus()).equals("DELETED"))) {
                        if (deviceStatus.get(0).getUpdateTime().getTime() >= device.getEnrolmentInfo().getDateOfEnrolment()) {
                            dateDiff = deviceStatus.get(0).getUpdateTime().getTime() - device.getEnrolmentInfo().getDateOfEnrolment();
                        }
                    } else if (!deviceStatus.isEmpty() && (!String.valueOf(deviceStatus.get(0).getStatus()).equals("REMOVED")
                            && !String.valueOf(deviceStatus.get(0).getStatus()).equals("DELETED"))) {
                        dateDiff = endDate.getTime() - device.getEnrolmentInfo().getDateOfEnrolment();
                    }
                }

                // Convert dateDiff to days as a decimal value
                double dateDiffInDays = (double) dateDiff / (24 * 60 * 60 * 1000);

                if (dateDiffInDays % 1 >= 0.9) {
                    dateDiffInDays = Math.ceil(dateDiffInDays);
                }

                long dateInDays = (long) dateDiffInDays;
                double cost = 0;
                if (tenantCost != null) {
                    cost = (tenantCost.getCost() / 365) * dateInDays;
                }
                totalCost += cost;
                device.setCost(Math.round(cost * 100.0) / 100.0);
                long totalDays = dateInDays + device.getDaysUsed();
                device.setDaysUsed((int) totalDays);
                if (deviceStatus.isEmpty()) {
                    deviceStatusNotAvailable.add(device);
                }
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in retrieving status history for a device in billing.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return totalCost;
    }

    @Override
    public PaginationResult createBillingFile(int tenantId, String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException {

        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices = new ArrayList<>();
        List<BillingResponse> billingResponseList = new ArrayList<>();
        double totalCost = 0.0;
        int deviceCount = 0;
        Timestamp initialStartDate = startDate;
        boolean remainingDaysConsidered = false;
        try {
            DeviceManagementDAOFactory.openConnection();

            // TODO Do the check of cache enabling here
            paginationResult = BillingCacheManagerImpl.getInstance().getBillingFromCache(tenantDomain, startDate, endDate);
            if (paginationResult == null) {
                paginationResult = new PaginationResult();
                long difference_In_Time = endDate.getTime() - startDate.getTime();

                long difference_In_Years = (difference_In_Time / (1000L * 60 * 60 * 24 * 365));

                long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

                if (difference_In_Time % (1000 * 60 * 60 * 24) >= 0.9 * (1000 * 60 * 60 * 24)) {
                    difference_In_Days++;
                }

                for (int i = 1; i <= difference_In_Years; i++) {
                    List<Device> allDevicesPerYear = new ArrayList<>();
                    LocalDateTime oneYearAfterStart = startDate.toLocalDateTime().plusYears(1).with(LocalTime.of(23, 59, 59));;
                    Timestamp newStartDate;
                    Timestamp newEndDate;

                    if (i == difference_In_Years) {
                        if (difference_In_Days == 0 || Timestamp.valueOf(oneYearAfterStart).getTime() >= endDate.getTime()) {
                            remainingDaysConsidered = true;
                            oneYearAfterStart = startDate.toLocalDateTime();
                            newEndDate = endDate;
                        } else {
                            oneYearAfterStart = startDate.toLocalDateTime().plusYears(1).with(LocalTime.of(23, 59, 59));;
                            newEndDate = Timestamp.valueOf(oneYearAfterStart);
                        }
                    } else {
                        oneYearAfterStart = startDate.toLocalDateTime().plusYears(1).with(LocalTime.of(23, 59, 59));;
                        newEndDate = Timestamp.valueOf(oneYearAfterStart);
                    }

                    newStartDate = startDate;

                    // The query returns devices which are enrolled in this year now in not removed state
                    allDevicesPerYear.addAll(deviceDAO.getNonRemovedYearlyDeviceList(tenantId, newStartDate, newEndDate));

                    // The query returns devices which are enrolled in this year now in removed state
                    allDevicesPerYear.addAll(deviceDAO.getRemovedYearlyDeviceList(tenantId, newStartDate, newEndDate));

                    // The query returns devices which are enrolled prior this year now in not removed state
                    allDevicesPerYear.addAll(deviceDAO.getNonRemovedPriorYearsDeviceList(tenantId, newStartDate, newEndDate));

                    // The query returns devices which are enrolled prior this year now in removed state
                    allDevicesPerYear.addAll(deviceDAO.getRemovedPriorYearsDeviceList(tenantId, newStartDate, newEndDate));

                    BillingResponse billingResponse = calculateUsage(tenantDomain, newStartDate, newEndDate, allDevicesPerYear);
                    billingResponseList.add(billingResponse);
                    allDevices.addAll(billingResponse.getDevice());
                    totalCost = totalCost + billingResponse.getTotalCostPerYear();
                    deviceCount = deviceCount + billingResponse.getDeviceCount();
                    LocalDateTime nextStartDate = oneYearAfterStart.plusDays(1).with(LocalTime.of(00, 00, 00));
                    startDate = Timestamp.valueOf(nextStartDate);
                }

                if (difference_In_Days != 0 && !remainingDaysConsidered) {
                    List<Device> allDevicesPerRemainingDays = new ArrayList<>();

                    // The query returns devices which are enrolled in this year now in not removed state
                    allDevicesPerRemainingDays.addAll(deviceDAO.getNonRemovedYearlyDeviceList(tenantId, startDate, endDate));

                    // The query returns devices which are enrolled in this year now in removed state
                    allDevicesPerRemainingDays.addAll(deviceDAO.getRemovedYearlyDeviceList(tenantId, startDate, endDate));

                    // The query returns devices which are enrolled prior this year now in not removed state
                    allDevicesPerRemainingDays.addAll(deviceDAO.getNonRemovedPriorYearsDeviceList(tenantId, startDate, endDate));

                    // The query returns devices which are enrolled prior this year now in removed state
                    allDevicesPerRemainingDays.addAll(deviceDAO.getRemovedPriorYearsDeviceList(tenantId, startDate, endDate));

                    BillingResponse billingResponse = calculateUsage(tenantDomain, startDate, endDate, allDevicesPerRemainingDays);
                    billingResponseList.add(billingResponse);
                    allDevices.addAll(billingResponse.getDevice());
                    totalCost = totalCost + billingResponse.getTotalCostPerYear();
                    deviceCount = deviceCount + billingResponse.getDeviceCount();
                }

                Calendar calStart = Calendar.getInstance();
                Calendar calEnd = Calendar.getInstance();
                calStart.setTimeInMillis(initialStartDate.getTime());
                calEnd.setTimeInMillis(endDate.getTime());

                BillingResponse billingResponse = new BillingResponse("all", Math.round(totalCost * 100.0) / 100.0, allDevices, calStart.get(Calendar.YEAR) + " - " + calEnd.get(Calendar.YEAR), initialStartDate.toString(), endDate.toString(), allDevices.size());
                billingResponseList.add(billingResponse);
                paginationResult.setData(billingResponseList);
                paginationResult.setTotalCost(Math.round(totalCost * 100.0) / 100.0);
                paginationResult.setTotalDeviceCount(deviceCount);
                BillingCacheManagerImpl.getInstance().addBillingToCache(paginationResult, tenantDomain, initialStartDate, endDate);
                return paginationResult;
            }

        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device bill list related to the current tenant";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error when retrieving metadata of billing feature";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getAllDevicesIds(PaginationRequest request) throws DeviceManagementException {
        return this.getAllDevicesIdList(request);
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
        DeviceManagerUtil.validateDeviceListPageSize(request);
        if (!StringUtils.isEmpty(request.getOwnerRole())) {
            devicesForRoles = this.getAllDevicesOfRole(request.getOwnerRole(), false);
            if (devicesForRoles != null) {
                count = devicesForRoles.size();
                if (requireDeviceInfo) {
                    paginationResult.setData(populateAllDeviceInfo(devicesForRoles));
                }
            }
        } else {
            try {
                DeviceManagementDAOFactory.openConnection();
                if (request.getGroupId() != 0) {
                    allDevices = deviceDAO.searchDevicesInGroup(request, tenantId);
                    count = deviceDAO.getCountOfDevicesInGroup(request, tenantId);
                } else {
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
                paginationResult.setData(populateAllDeviceInfo(allDevices));
            } else {
                paginationResult.setData(allDevices);
            }
        }
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public PaginationResult getAllDevicesIdList(PaginationRequest request) throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete pagination request for method getAllDevicesIdList";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices with pagination " + request.toString());
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices;
        int count = 0;
        int tenantId = this.getTenantId();
        DeviceManagerUtil.validateDeviceListPageSize(request);

        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesIds(request, tenantId);
            count = deviceDAO.getDeviceCount(request, tenantId);
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
        paginationResult.setData(allDevices);

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
            this.populateAllDeviceInfo(device);
        }
        this.addDeviceToCache(deviceId, device);
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
            this.populateAllDeviceInfo(device);
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
        if (owner == null) {
            String msg = "Received null device owner for method getDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device by device id :" + deviceId.getId() + " of type '" + deviceId.getType() +
                    " and owner '" + owner + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        Device device = this.getDeviceFromCache(deviceId);
        if (device == null || device.getEnrolmentInfo() == null
                || !owner.equals(device.getEnrolmentInfo().getOwner())) {
            int tenantId = this.getTenantId();
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
        }
        if (requireDeviceInfo) {
            this.populateAllDeviceInfo(device);
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
        params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
        params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
        params.put(DeviceManagementConstants.EmailAttributes.DOC_URL,
                new TypedValue<>(String.class, DeviceManagerUtil.getDocUrl()));
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
    public void sendEnrolmentGuide(String enrolmentGuide) throws DeviceManagementException {

        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        String recipientMail = config.getEnrollmentGuideConfiguration().getMail();
        Properties props = new Properties();
        props.setProperty("mail-subject", "[Enrollment Guide Triggered] (#" + ++count + ")");
        props.setProperty("enrollment-guide", enrolmentGuide);

        try {
            EmailMetaInfo metaInfo = new EmailMetaInfo(recipientMail, props);
            sendEnrolmentInvitation(DeviceManagementConstants.EmailAttributes.ENROLLMENT_GUIDE_TEMPLATE, metaInfo);
        } catch (ConfigurationManagementException e) {
            String msg = "Error occurred while sending the mail.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
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
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.FIRST_NAME,
                    new TypedValue<>(String.class, metaInfo.getProperty("first-name")));
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.USERNAME,
                    new TypedValue<>(String.class, metaInfo.getProperty("username")));
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.PASSWORD,
                    new TypedValue<>(String.class, metaInfo.getProperty("password")));
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.DOMAIN,
                    new TypedValue<>(String.class, metaInfo.getProperty("domain")));
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                    new TypedValue<>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
            params.put(io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
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
    public SingletonMap getTenantedDevice(DeviceIdentifier deviceIdentifier, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received null deviceIdentifier for getTenantedDevice";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get tenanted device with id: " + deviceIdentifier.getId() + " of type '" +
                    deviceIdentifier.getType() + "'");
        }

        SingletonMap deviceMap;
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceMap = deviceDAO.getDevice(deviceIdentifier);
            if (deviceMap == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to find device for type " + deviceIdentifier.getType() +
                            " and id " + deviceIdentifier.getId());
                }
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

        if (requireDeviceInfo && deviceMap != null) {
            populateAllDeviceInfo((Device) deviceMap.getValue());
        }
        return deviceMap;
    }

    @Override
    @Deprecated
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
            this.populateAllDeviceInfo(device);
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
        Device device;
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
            } else {
                Device deviceFromCache = getDeviceFromCache(deviceData.getDeviceIdentifier());
                if (deviceFromCache != null && device.getEnrolmentInfo() != null &&
                        deviceFromCache.getEnrolmentInfo().getStatus() != device.getEnrolmentInfo().getStatus()) {
                    this.addDeviceToCache(deviceData.getDeviceIdentifier(), device);
                }
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
            this.populateAllDeviceInfo(device);
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
                    log.debug("No device is found against criteria : " + deviceProps + " and tenantId " + tenantId);
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
            this.populateAllDeviceInfo(device);
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
            this.populateAllDeviceInfo(device);
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
            this.populateAllDeviceInfo(device);
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
                for (String deviceType : publicSharedDeviceTypesInDB) {
                    if (!deviceTypesResponse.contains(deviceType)) {
                        deviceTypesResponse.add(deviceType);
                    }
                }
            }
            if (registeredTypes != null) {
                for (DeviceTypeServiceIdentifier deviceType : registeredTypes.keySet()) {
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
    public boolean setStatus(Device device, EnrolmentInfo.Status status) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Set status of device: " + device.getDeviceIdentifier());
        }
        try {
            boolean success;
            int tenantId = this.getTenantId();
            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
            DeviceManagementDAOFactory.beginTransaction();
            if (enrolmentInfo == null) {
                enrolmentInfo = enrollmentDAO.getEnrollment(device.getId(), tenantId);
                if (enrolmentInfo == null) {
                    String msg = "Error occurred in getting enrollment for device :" + device.getDeviceIdentifier();
                    log.error(msg);
                    throw new DeviceManagementException(msg);
                }
            }
            success = enrollmentDAO.setStatus(enrolmentInfo.getId(), status, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            enrolmentInfo.setStatus(status);
            device.setEnrolmentInfo(enrolmentInfo);
            this.addDeviceToCache(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()), device);
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
            String msg = "Error occurred in setStatus for device :" + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceStatus> getDeviceStatusHistory(Device device, Date fromDate, Date toDate, boolean billingStatus) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("get status history of device: " + device.getDeviceIdentifier());
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = this.getTenantId();
            return deviceStatusDAO.getStatus(device.getId(), tenantId, fromDate, toDate, billingStatus);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred in retrieving status history for device :" + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.info(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceStatus> getDeviceCurrentEnrolmentStatusHistory(Device device, Date fromDate, Date toDate) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("get status history of device: " + device.getDeviceIdentifier());
        }
        try {
            int tenantId = this.getTenantId();
            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
            if (enrolmentInfo == null) {
                enrolmentInfo = enrollmentDAO.getEnrollment(device.getId(), tenantId);
                if (enrolmentInfo == null) {
                    String msg = "Error occurred in getting enrollment for device :" + device.getDeviceIdentifier();
                    log.error(msg);
                    throw new DeviceManagementException(msg);
                }
            }
            return deviceStatusDAO.getStatus(enrolmentInfo.getId(), fromDate, toDate);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving status history";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in retrieving status history for current enrolment of device : " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public List<DeviceStatus> getDeviceStatusHistory(Device device) throws DeviceManagementException {
        return getDeviceStatusHistory(device, null, null, false);
    }

    @Override
    public List<DeviceStatus> getDeviceCurrentEnrolmentStatusHistory(Device device) throws DeviceManagementException {
        return getDeviceCurrentEnrolmentStatusHistory(device, null, null);
    }

    @Override
    public boolean setStatus(String currentOwner, EnrolmentInfo.Status status) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Update enrollment with status");
        }
        try {
            boolean success;
            int tenantId = this.getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            success = enrollmentDAO.setStatusAllDevices(currentOwner, status, tenantId);
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
    public void addTaskOperation(String type, Operation operation, DynamicTaskContext taskContext) throws OperationManagementException {
        pluginRepository.getOperationManager(type, this.getTenantId()).addTaskOperation(type, operation, taskContext);
    }

    @Override
    public void addTaskOperation(String type, List<Device> devices, Operation operation)
            throws OperationManagementException {
        pluginRepository.getOperationManager(type, this.getTenantId()).addTaskOperation(devices, operation);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId()).getOperations(deviceId);
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        DeviceManagerUtil.validateOperationListPageSize(request);
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperations(deviceId, request);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId, Operation.Status status)
            throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperations(deviceId, status);
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
        // // setting notNowOperationFrequency to -1 to avoid picking not now operations
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

//                    DeviceManagerUtil.getEventPublisherService().publishEvent(
//                            OPERATION_RESPONSE_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
//                    );
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while reading configs.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);

        } //catch (DataPublisherConfigurationException e) {
//            String msg = "Error occurred while publishing event.";
//            log.error(msg, e);
//            throw new OperationManagementException(msg, e);
//        }
    }

    @Override
    public void updateOperation(Device device, Operation operation) throws OperationManagementException {
        try {
            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
            if (enrolmentInfo == null || device.getEnrolmentInfo().getId() <= 0) {
                pluginRepository.getOperationManager(device.getType(), this.getTenantId())
                        .updateOperation(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()),
                                operation);
            } else {
                pluginRepository.getOperationManager(device.getType(), this.getTenantId())
                        .updateOperation(device.getEnrolmentInfo().getId(), operation,
                                new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            }
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
//                    DeviceManagerUtil.getEventPublisherService().publishEvent(
//                            OPERATION_RESPONSE_EVENT_STREAM_DEFINITION, "1.0.0", metaData, new Object[0], payload
//                    );
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while reading configs.";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        } //catch (DataPublisherConfigurationException e) {
//            String msg = "Error occurred while publishing event.";
//            log.error(msg, e);
//            throw new OperationManagementException(msg, e);
//        }
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
    public List<Activity> getActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivities(activityPaginationRequest);
    }

    @Override
    public int getActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager()
                .getActivitiesCount(activityPaginationRequest);
    }

    @Override
    public List<DeviceActivity> getDeviceActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getDeviceActivities(activityPaginationRequest);
    }

    @Override
    public int getDeviceActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager()
                .getDeviceActivitiesCount(activityPaginationRequest);
    }

    @Override
    public List<MonitoringOperation> getMonitoringOperationList(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);

        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.getMonitoringOperation();
    }

    @Override
    public List<String> getStartupOperations(String deviceType) {
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
            return this.populateAllDeviceInfo(userDevices);
        }
        return userDevices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, List<String> deviceStatuses, boolean requireDeviceInfo)
            throws DeviceManagementException {
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
            userDevices = deviceDAO.getDevicesOfUser(username, this.getTenantId(), deviceStatuses);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to get devices of user: "
                    + username;
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
            return this.populateAllDeviceInfo(userDevices);
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
            return this.populateAllDeviceInfo(userDevices);
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
        DeviceManagerUtil.validateDeviceListPageSize(request);
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
            result.setData(this.populateAllDeviceInfo(userDevices));
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
        DeviceManagerUtil.validateDeviceListPageSize(request);
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
            result.setData(this.populateAllDeviceInfo(allDevices));
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
        String[] users = getUserListOfRole(role);
        int tenantId = this.getTenantId();

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
                this.populateAllDeviceInfo(userDevices);
            }
            devices.addAll(userDevices);
        }
        return devices;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role, List<String> deviceStatuses, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (role == null || role.isEmpty()) {
            String msg = "Received empty role for the method getAllDevicesOfRole";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices of role '" + role + "' and requiredDeviceInfo: " + requireDeviceInfo);
        }
        List<Device> devices = new ArrayList<>();
        String[] users = getUserListOfRole(role);

        for (String user : users) {
            devices.addAll(getDevicesOfUser(user, deviceStatuses, requireDeviceInfo));
        }
        return devices;
    }

    private String[] getUserListOfRole(String role) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get users of role '" + role);
        }
        int tenantId = this.getTenantId();
        try {
            return DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
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
            return this.populateAllDeviceInfo(allDevices);
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
        DeviceManagerUtil.validateDeviceListPageSize(request);
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
            result.setData(this.populateAllDeviceInfo(allDevices));
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
            return this.populateAllDeviceInfo(allDevices);
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
        if (StringUtils.isBlank(status)) {
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
            String msg = "Error occurred while fetching the list of devices that matches to status: '" + status + "'";
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
            result.setData(this.populateAllDeviceInfo(allDevices));
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
        this.removeDeviceFromCache(deviceIdentifier);
        Device device = getDevice(deviceIdentifier, false);
        int deviceId = device.getId();
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        if (enrolmentInfo.getStatus().equals(newStatus)) {
            return false; //New status is similar to current
        }
        int tenantId = this.getTenantId();
        if (EnrolmentInfo.Status.REMOVED == newStatus) {
            isDeviceUpdated = disenrollDevice(deviceIdentifier);
        } else {
            enrolmentInfo.setStatus(newStatus);
            isDeviceUpdated = updateEnrollment(deviceId, enrolmentInfo, tenantId, deviceIdentifier);
        }
        this.removeDeviceFromCache(deviceIdentifier);
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

    private boolean updateEnrollment(int deviceId, EnrolmentInfo enrolmentInfo, int tenantId, DeviceIdentifier deviceIdentifier)
            throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Update enrollment of device: " + deviceId);
        }
        boolean isUpdatedEnrollment = false;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int updatedRows = enrollmentDAO.updateEnrollment(enrolmentInfo, tenantId);
            String type = deviceIdentifier.getType();
            DeviceStatusManagementService deviceStatusManagementService = DeviceManagementDataHolder
                    .getInstance().getDeviceStatusManagementService();
            if (updatedRows > 0) {
                isUpdatedEnrollment = true;
            }
            addDeviceStatus(deviceStatusManagementService, tenantId, updatedRows, enrolmentInfo, type);
            DeviceManagementDAOFactory.commitTransaction();

        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating the enrollment information device for" +
                    "id '" + deviceId + "' .";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in updateEnrollment for deviceId: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return isUpdatedEnrollment;
    }

    /**
     * Save the status according to status check(allowed device status)
     * Before invoking this method the calling function should have started a transaction
     * @param deviceStatusManagementService instance of deviceStatusManagementService
     * @param tenantId ID of the tenant
     * @param updatedRows number of updated rows
     * @param enrolmentInfo enrollment info of the device
     * @param type type of the device
     */
    private void addDeviceStatus(DeviceStatusManagementService deviceStatusManagementService, int tenantId,
                                 int updatedRows,EnrolmentInfo enrolmentInfo,String type)
            throws MetadataManagementException, DeviceManagementDAOException {
        boolean isEnableDeviceStatusCheck = deviceStatusManagementService.getDeviceStatusCheck(tenantId);
        boolean isValidState = deviceStatusManagementService.isDeviceStatusValid(type, enrolmentInfo.getStatus().name(), tenantId);
        if (updatedRows == 1 && (!isEnableDeviceStatusCheck || isValidState)) {
            enrollmentDAO.addDeviceStatus(enrolmentInfo.getId(), enrolmentInfo.getStatus());
        }
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
        GroupManagementProviderService groupManagementProviderService = DeviceManagementDataHolder
                .getInstance().getGroupManagementProviderService();
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
        if (StringUtils.isBlank(deviceType)) {
            String msg = "Received either whitespace, empty (\"\") or null value as device type to get device type "
                    + "details.";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get device type '" + deviceType + "'");
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
    public List<DeviceLocationHistorySnapshot> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from,
                                                                     long to) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get device location information");
        }
        List<DeviceLocationHistorySnapshot> deviceLocationHistory;
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceLocationHistory = deviceDAO.getDeviceLocationInfo(deviceIdentifier, from, to);
        } catch (DeviceManagementDAOException e) {
            String errMessage = "Error occurred in getDeviceLocationInfo";
            log.error(errMessage, e);
            throw new DeviceManagementException(errMessage, e);
        } catch (SQLException e) {
            String errMessage = "Error occurred while opening a connection to the data source";
            log.error(errMessage, e);
            throw new DeviceManagementException(errMessage, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        return deviceLocationHistory;
    }

    @Override
    public void notifyPullNotificationSubscriber(Device device, Operation operation)
            throws PullNotificationExecutionFailedException {
        if (log.isDebugEnabled()) {
            log.debug("Notify pull notification subscriber");
        }
        DeviceManagementService dms =
                pluginRepository.getDeviceManagementService(device.getType(), this.getTenantId());
        if (dms == null) {
            String message = "Device type '" + device.getType() + "' does not have an associated " +
                    "device management plugin registered within the framework";
            log.error(message);
            throw new PullNotificationExecutionFailedException(message);
        }
        PullNotificationSubscriber pullNotificationSubscriber = dms.getPullNotificationSubscriber();
        if (pullNotificationSubscriber == null) {
            String message = "Pull Notification Subscriber is not configured " +
                    "for device type" + device.getType();
            log.error(message);
            throw new PullNotificationExecutionFailedException(message);
        }
        pullNotificationSubscriber.execute(device, operation);
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
        DeviceInfo info;
        try {
            DeviceInformationManager deviceInformationManager = DeviceManagementDataHolder
                    .getInstance().getDeviceInformationManager();
            info = deviceInformationManager.getDeviceInfo(device);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while retrieving advance info of '" + device.getType() +
                    "' that carries the id '" + device.getDeviceIdentifier() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
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
    private List<Device> populateAllDeviceInfo(List<Device> allDevices) throws DeviceManagementException {
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
    private void populateAllDeviceInfo(Device device) throws DeviceManagementException {
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
            return;
        }
        Device dmsDevice =
                deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        if (dmsDevice != null) {
            device.setFeatures(dmsDevice.getFeatures());
            device.setProperties(dmsDevice.getProperties());
        }
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

    private void updateDeviceInCache(DeviceIdentifier deviceIdentifier, Device device) {
        DeviceCacheManagerImpl.getInstance().updateDeviceInCache(deviceIdentifier, device, this.getTenantId());
    }

    /***
     * This method removes a given list of devices from the cache
     * @param deviceList list of DeviceCacheKey objects
     */
    private void removeDevicesFromCache(List<DeviceCacheKey> deviceList) {
        DeviceCacheManagerImpl.getInstance().removeDevicesFromCache(deviceList);
    }

    @Override
    public List<GeoCluster> findGeoClusters(GeoQuery geoQuery) throws DeviceManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get information about geo clusters for query: " + new Gson().toJson(geoQuery));
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.findGeoClusters(geoQuery, this.getTenantId());
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
            String msg = "Error occurred in while retrieving device count by status for deviceType :" + deviceType + " status : " + deviceStatus;
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
            String msg = "Error occurred in while retrieving devices by status for deviceType :" + deviceType + " status : " + deviceStatus;
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
            if (success) {
                for (String id : deviceList) {
                    this.removeDeviceFromCache(new DeviceIdentifier(id, deviceType));
                }
            }
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

    @Override
    public boolean updateEnrollment(String owner, boolean isTransfer, List<String> deviceIdentifiers)
            throws DeviceManagementException, UserNotFoundException, InvalidDeviceException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> existingDevices;
        List<Device> existingNonRemovedDevices = new ArrayList<>();
        owner = validateOwner(owner, tenantId);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            existingDevices = deviceDAO.getDevicesByIdentifiers(deviceIdentifiers, tenantId);
            if (log.isDebugEnabled()) {
                log.debug("Requested devices: " + deviceIdentifiers.toString());
            }
            for (Device device : existingDevices) {
                if (device.getEnrolmentInfo().getStatus() != EnrolmentInfo.Status.REMOVED) {
                    device.getEnrolmentInfo().setTransferred(isTransfer);
                    existingNonRemovedDevices.add(device);
                    deviceIdentifiers.remove(device.getDeviceIdentifier());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Valid devices: " + existingNonRemovedDevices.toString());
            }
            if (deviceIdentifiers.size() > 0) {
                String msg = "Couldn't find valid devices for invalid device identifiers: " +
                        deviceIdentifiers.toString();
                log.error(msg);
                throw new InvalidDeviceException(msg);
            }
            if (enrollmentDAO.updateOwnerOfEnrollment(existingNonRemovedDevices, owner, tenantId)) {
                DeviceManagementDAOFactory.commitTransaction();
                for (Device device : existingNonRemovedDevices) {
                    this.removeDeviceFromCache(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
                }
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
                boolean isUserExist = DeviceManagementDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId).getUserStoreManager().isExistingUser(owner);
                if (!isUserExist) {
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
     *
     * @param device Device object
     */
    private void extractDeviceLocationToUpdate(Device device) {
        DeviceInformationManager deviceInformationManager = DeviceManagementDataHolder
                .getInstance().getDeviceInformationManager();
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
                    deviceInformationManager.addDeviceLocation(device, deviceLocation);
                } catch (DeviceDetailsMgtException e) {
                    /***
                     * NOTE:
                     * We are not failing the execution since this is not critical for the functionality. But logging as
                     * a warning for reference.
                     * Exception was not thrown due to being conflicted with non-traccar features
                     */
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
                if (!deleteDevices(deviceIdentifiers)) {
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
                        String type = device.getType();
                        DeviceStatusManagementService deviceStatusManagementService = DeviceManagementDataHolder
                                .getInstance().getDeviceStatusManagementService();
                        int updatedRows = enrollmentDAO.updateEnrollment(device.getEnrolmentInfo(), tenantId);
                        addDeviceStatus(deviceStatusManagementService, tenantId, updatedRows, device.getEnrolmentInfo(),
                                type);
                    } catch (DeviceManagementDAOException e) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        String msg = "Error occurred while dis-enrolling device: " +
                                device.getName();
                        log.error(msg, e);
                        throw new DeviceManagementException(msg, e);
                    } catch (MetadataManagementException e) {
                        throw new RuntimeException(e);
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
        } finally {
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
                if (deviceDAO.transferDevice(deviceTransferRequest.getDeviceType(), deviceId, owner, destinationTenantId)) {
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
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getAppSubscribedDevices(PaginationRequest request, List<Integer> devicesIds) throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices details for device ids: " + devicesIds);
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> subscribedDeviceDetails;
        try {
            DeviceManagementDAOFactory.openConnection();
            subscribedDeviceDetails = deviceDAO.getSubscribedDevices(request, devicesIds, tenantId);
            if (subscribedDeviceDetails.isEmpty()) {
                paginationResult.setData(new ArrayList<>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            int count = deviceDAO.getSubscribedDeviceCount(devicesIds, tenantId, request.getStatusList());
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
        paginationResult.setData(populateAllDeviceInfo(subscribedDeviceDetails));
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
            } finally {
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
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Wrap the device configuration data into DeviceConfiguration bean
     *
     * @param device               Device queried using the properties
     * @param tenantDomain         tenant domain
     * @param configurationEntries platformConfiguration list
     * @param deviceOwner          name of the device owner
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

    @Override
    public boolean isOperationExist(DeviceIdentifier deviceId, int operationId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .isOperationExist(deviceId, operationId);
    }

    @Override
    public List<Device> getDeviceByIdList(List<String> deviceIdentifiers) throws DeviceManagementException {
        int tenantId = this.getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesByIdentifiers(deviceIdentifiers, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list.";
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
    public DeviceEnrollmentInvitationDetails getDeviceEnrollmentInvitationDetails(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        return dms.getDeviceEnrollmentInvitationDetails();
    }

    @Override
    public void triggerCorrectiveActions(String deviceIdentifier, String featureCode, List<String> actions,
                                         List<ConfigurationEntry> configList) throws DeviceManagementException, DeviceNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Triggering Corrective action. Device Identifier: " + deviceIdentifier);
        }

        if (StringUtils.isBlank(featureCode)) {
            String msg = "Found a Blan feature code: " + featureCode;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (configList == null || configList.isEmpty()) {
            String msg = "Platform config is not configured";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        Device device = getDevice(deviceIdentifier, false);
        if (device == null) {
            String msg = "Couldn't find and device for device identifier " + deviceIdentifier;
            log.error(msg);
            throw new DeviceNotFoundException(msg);
        }
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();

        for (String action : actions) {
            for (ConfigurationEntry config : configList) {
                if (featureCode.equals(config.getName())) {
                    CorrectiveActionConfig correctiveActionConfig = new Gson()
                            .fromJson((String) config.getValue(), CorrectiveActionConfig.class);
                    if (correctiveActionConfig.getActionTypes().contains(action)) {
                        if (DeviceManagementConstants.CorrectiveActions.E_MAIL.equals(action)) {
                            Properties props = new Properties();
                            props.setProperty("mail-subject", correctiveActionConfig.getMailSubject());
                            props.setProperty("feature-code", featureCode);
                            props.setProperty("device-id", deviceIdentifier);
                            props.setProperty("device-name", device.getName());
                            props.setProperty("device-owner", enrolmentInfo.getOwner());
                            props.setProperty("custom-mail-body", correctiveActionConfig.getMailBody());
                            try {
                                for (String mailAddress : correctiveActionConfig.getMailReceivers()) {
                                    EmailMetaInfo metaInfo = new EmailMetaInfo(mailAddress, props);
                                    sendEnrolmentInvitation(
                                            DeviceManagementConstants.EmailAttributes.POLICY_VIOLATE_TEMPLATE,
                                            metaInfo);
                                }
                            } catch (ConfigurationManagementException e) {
                                String msg = "Error occurred while sending the mail.";
                                log.error(msg);
                                throw new DeviceManagementException(msg, e);
                            }
                        }
                    } else {
                        log.warn("Corrective action: " + action + " is not configured in the platform configuration "
                                + "for policy " + featureCode);
                    }
                }
            }
        }
    }

    public List<Device> getDevicesByIdentifiersAndStatuses(List<String> deviceIdentifiers,
                                                           List<EnrolmentInfo.Status> statuses)
            throws DeviceManagementException {
        int tenantId = this.getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesByIdentifiersAndStatuses(deviceIdentifiers, statuses, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list.";
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
    public License getLicenseConfig(String deviceTypeName) throws DeviceManagementException {
        DeviceManagementService deviceManagementService =
                pluginRepository.getDeviceManagementService(deviceTypeName,
                        this.getTenantId());
        if (deviceManagementService == null) {
            String msg = "Device management service loading is failed for the device type: " + deviceTypeName;
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        return deviceManagementService.getLicenseConfig();
    }

    @Override
    public PaginationResult getDevicesDetails(PaginationRequest request, List<Integer> devicesIds,
                                              String groupName) throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Getting all devices details for device ids: " + devicesIds);
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> subscribedDeviceDetails;
        try {
            DeviceManagementDAOFactory.openConnection();
            subscribedDeviceDetails = deviceDAO.getGroupedDevicesDetails(request, devicesIds, groupName, tenantId);
            if (subscribedDeviceDetails.isEmpty()) {
                paginationResult.setData(new ArrayList<>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            int count = deviceDAO.getGroupedDevicesCount(request, devicesIds, groupName, tenantId);
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
        paginationResult.setData(populateAllDeviceInfo(subscribedDeviceDetails));
        return paginationResult;
    }

    @Override
    public Boolean sendDeviceNameChangedNotification(Device device) throws DeviceManagementException {

        try {
            ProfileOperation operation = new ProfileOperation();
            operation.setCode(Constants.SEND_USERNAME);
            operation.setType(Operation.Type.PROFILE);
            operation.setPayLoad(device.getName());

            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(device.getDeviceIdentifier());
            deviceIdentifier.setType(device.getType());

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(deviceIdentifier);
            Activity activity;
            activity = addOperation(device.getType(), operation, deviceIdentifiers);

            return activity != null;
        } catch (OperationManagementException e) {
            String msg = "Error occurred while sending operation";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (InvalidDeviceException e) {
            String msg = "Invalid Device exception occurred";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public void saveApplicationIcon(String iconPath, String packageName, String version) throws DeviceManagementException{
        int tenantId = 0;
        try{
            DeviceManagementDAOFactory.beginTransaction();
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if(applicationDAO.getApplicationPackageCount(packageName) == 0){
                applicationDAO.saveApplicationIcon(iconPath, packageName, version, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while saving app icon. Icon Path: " + iconPath +
                    " Package Name: " + packageName +
                    " Version: " + version +
                    " Tenant Id: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateApplicationIcon(String iconPath, String oldPackageName, String newPackageName, String version)
            throws DeviceManagementException{
        try {
            DeviceManagementDAOFactory.beginTransaction();
            applicationDAO.updateApplicationIcon(iconPath, oldPackageName, newPackageName, version);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating app icon info." +
                    " Package Name: " + oldPackageName;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteApplicationIcon(String packageName)
            throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            applicationDAO.deleteApplicationIcon(packageName);
            DeviceManagementDAOFactory.commitTransaction();
        }  catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting app icon info." +
                    " Package Name: " + packageName ;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private List<Application> getInstalledAppIconInfo(List<Application> applications) throws DeviceManagementException {
        String iconPath;
        try {
            DeviceManagementDAOFactory.openConnection();
            for (Application app : applications) {
                iconPath = applicationDAO.getIconPath(app.getApplicationIdentifier());
                app.setImageUrl(iconPath);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving installed app icon info";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return applications;
    }

    @Override
    public List<Application> getInstalledApplicationsOnDevice(Device device, int offset, int limit, int isSystemApp) throws DeviceManagementException {
        List<Application> applications;
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            applications = applicationDAO.getInstalledApplicationListOnDevice(device.getId(),
                    device.getEnrolmentInfo().getId(), offset, limit, tenantId, isSystemApp);
            if (applications == null) {
                String msg = "Couldn't found applications for device identifier '" + device.getId() + "'";
                log.error(msg);
                throw new DeviceManagementException(msg);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the application list of android device, " +
                    "which carries the id '" + device.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        List<Application> newApplicationList;
        newApplicationList = this.getInstalledAppIconInfo(applications);
        if (newApplicationList == null) {
            String msg = "Error occurred while getting app icon info for device identifier '" + device.getId() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        return newApplicationList;
    }

    public List<Application> getInstalledApplicationsOnDevice(Device device) throws DeviceManagementException {
        List<Application> applications;
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            applications = applicationDAO.getInstalledApplicationListOnDevice(device.getId(),
                    device.getEnrolmentInfo().getId(), tenantId);
            if (applications == null) {
                String msg = "Couldn't found applications for device identifier '" + device.getId() + "'";
                log.error(msg);
                throw new DeviceManagementException(msg);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the application list of android device, " +
                    "which carries the id '" + device.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        List<Application> newApplicationList;
        newApplicationList = this.getInstalledAppIconInfo(applications);
        if (newApplicationList == null) {
            String msg = "Error occurred while getting app icon info for device identifier '" + device.getId() + "'";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        return newApplicationList;
    }

    @Override
    public List<Device> getEnrolledDevicesSince(Date since) throws DeviceManagementException {
        List<Device> devices;
        try {
            DeviceManagementDAOFactory.openConnection();
            devices = deviceDAO.getDevicesEnrolledSince(since);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting devices enrolled device since " + since.getTime();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    @Override
    public List<Device> getEnrolledDevicesPriorTo(Date priorTo) throws DeviceManagementException {
        List<Device> devices;
        try {
            DeviceManagementDAOFactory.openConnection();
            devices = deviceDAO.getDevicesEnrolledPriorTo(priorTo);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting devices enrolled device prior to " + priorTo.getTime();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    @Override
    public void deleteDeviceDataByTenantId(int tenantId) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();

            tenantDao.deleteExternalPermissionMapping(tenantId);
            tenantDao.deleteExternalDeviceMappingByTenantId(tenantId);
            tenantDao.deleteExternalGroupMappingByTenantId(tenantId);
            // TODO: Check whether deleting DM_DEVICE_ORGANIZATION table data is necessary
//            tenantDao.deleteDeviceOrganizationByTenantId(tenantId);
            tenantDao.deleteDeviceHistoryLastSevenDaysByTenantId(tenantId);
            tenantDao.deleteDeviceDetailByTenantId(tenantId);
            tenantDao.deleteDeviceLocationByTenantId(tenantId);
            tenantDao.deleteDeviceInfoByTenantId(tenantId);
            tenantDao.deleteNotificationByTenantId(tenantId);
            tenantDao.deleteAppIconsByTenantId(tenantId);
            tenantDao.deleteTraccarUnsyncedDevicesByTenantId(tenantId);
            tenantDao.deleteDeviceEventGroupMappingByTenantId(tenantId);
            tenantDao.deleteGeofenceEventMappingByTenantId(tenantId);
            tenantDao.deleteDeviceEventByTenantId(tenantId);
            tenantDao.deleteGeofenceGroupMappingByTenantId(tenantId);
            tenantDao.deleteGeofenceByTenantId(tenantId);
            tenantDao.deleteDeviceGroupPolicyByTenantId(tenantId);
            tenantDao.deleteDynamicTaskPropertiesByTenantId(tenantId);
            tenantDao.deleteDynamicTaskByTenantId(tenantId);
            tenantDao.deleteMetadataByTenantId(tenantId);
            tenantDao.deleteOTPDataByTenantId(tenantId);
            tenantDao.deleteSubOperationTemplate(tenantId);
            tenantDao.deleteDeviceSubTypeByTenantId(tenantId);
            tenantDao.deleteCEAPoliciesByTenantId(tenantId);

            tenantDao.deleteApplicationByTenantId(tenantId);
            tenantDao.deletePolicyCriteriaPropertiesByTenantId(tenantId);
            tenantDao.deletePolicyCriteriaByTenantId(tenantId);
            tenantDao.deleteCriteriaByTenantId(tenantId);
            tenantDao.deletePolicyChangeManagementByTenantId(tenantId);
            tenantDao.deletePolicyComplianceFeaturesByTenantId(tenantId);
            tenantDao.deletePolicyComplianceStatusByTenantId(tenantId);
            tenantDao.deleteRolePolicyByTenantId(tenantId);
            tenantDao.deleteUserPolicyByTenantId(tenantId);
            tenantDao.deleteDeviceTypePolicyByTenantId(tenantId);
            tenantDao.deleteDevicePolicyAppliedByTenantId(tenantId);
            tenantDao.deleteDevicePolicyByTenantId(tenantId);
            tenantDao.deletePolicyCorrectiveActionByTenantId(tenantId);
            tenantDao.deletePolicyByTenantId(tenantId);
            tenantDao.deleteProfileFeaturesByTenantId(tenantId);
            tenantDao.deleteProfileByTenantId(tenantId);

            tenantDao.deleteDeviceOperationResponseLargeByTenantId(tenantId);
            tenantDao.deleteDeviceOperationResponseByTenantId(tenantId);
            tenantDao.deleteEnrolmentOpMappingByTenantId(tenantId);
            tenantDao.deleteDeviceStatusByTenantId(tenantId);
            tenantDao.deleteEnrolmentByTenantId(tenantId);
            tenantDao.deleteOperationByTenantId(tenantId);
            tenantDao.deleteDeviceGroupMapByTenantId(tenantId);
            tenantDao.deleteGroupPropertiesByTenantId(tenantId);
            tenantDao.deleteDevicePropertiesByTenantId(tenantId);
            tenantDao.deleteDeviceByTenantId(tenantId);
            tenantDao.deleteRoleGroupMapByTenantId(tenantId);
            tenantDao.deleteGroupByTenantId(tenantId);
            tenantDao.deleteDeviceCertificateByTenantId(tenantId);

            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error deleting data of tenant of ID: '" + tenantId + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error while initiating transaction when trying to delete tenant info of '" + tenantId + "'";
            log.error(msg);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public OwnerWithDeviceDTO getOwnersWithDeviceIds(String owner, int deviceTypeId, String deviceOwner, String deviceName, String deviceStatus)
            throws DeviceManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        OwnerWithDeviceDTO ownerWithDeviceDTO;

        List<String> allowingDeviceStatuses = Arrays.asList(EnrolmentInfo.Status.ACTIVE.toString(),
                EnrolmentInfo.Status.INACTIVE.toString(), EnrolmentInfo.Status.UNREACHABLE.toString());

        try {
            DeviceManagementDAOFactory.openConnection();
            ownerWithDeviceDTO = this.enrollmentDAO.getOwnersWithDevices(owner, allowingDeviceStatuses,
                    tenantId, deviceTypeId, deviceOwner, deviceName, deviceStatus);
        } catch (DeviceManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving device IDs for owner: " + owner;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return ownerWithDeviceDTO;
    }

//    @Override
//    public OwnerWithDeviceDTO getOwnersWithDeviceIds(String owner, int deviceTypeId, String deviceOwner, String deviceName, String deviceStatus)
//            throws DeviceManagementDAOException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
//        OwnerWithDeviceDTO ownerWithDeviceDTO;
//
//        List<String> allowingDeviceStatuses = new ArrayList<>();
//        allowingDeviceStatuses.add(EnrolmentInfo.Status.ACTIVE.toString());
//        allowingDeviceStatuses.add(EnrolmentInfo.Status.INACTIVE.toString());
//        allowingDeviceStatuses.add(EnrolmentInfo.Status.UNREACHABLE.toString());
//
//        try {
//            DeviceManagementDAOFactory.openConnection();
//            ownerWithDeviceDTO = this.enrollmentDAO.getOwnersWithDevices(owner, allowingDeviceStatuses, tenantId, deviceTypeId, deviceOwner, deviceName, deviceStatus);
//            if (ownerWithDeviceDTO == null) {
//                String msg = "No data found for owner: " + owner;
//                log.error(msg);
//                throw new DeviceManagementDAOException(msg);
//            }
//            List<Integer> deviceIds = ownerWithDeviceDTO.getDeviceIds();
//            if (deviceIds != null) {
//                ownerWithDeviceDTO.setDeviceCount(deviceIds.size());
//            } else {
//                ownerWithDeviceDTO.setDeviceCount(0);
//            }
//        } catch (DeviceManagementDAOException | SQLException e) {
//            String msg = "Error occurred while retrieving device IDs for owner: " + owner;
//            log.error(msg, e);
//            throw new DeviceManagementDAOException(msg, e);
//        } finally {
//            DeviceManagementDAOFactory.closeConnection();
//        }
//        return ownerWithDeviceDTO;
//    }


    @Override
    public OwnerWithDeviceDTO getOwnerWithDeviceByDeviceId(int deviceId, String deviceOwner, String deviceName, String deviceStatus)
            throws DeviceManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        OwnerWithDeviceDTO deviceOwnerWithStatus;

        try {
            DeviceManagementDAOFactory.openConnection();
            deviceOwnerWithStatus = enrollmentDAO.getOwnerWithDeviceByDeviceId(deviceId, tenantId, deviceOwner, deviceName, deviceStatus);
            if (deviceOwnerWithStatus == null) {
                throw new DeviceManagementDAOException("No data found for device ID: " + deviceId);
            }
            List<Integer> deviceIds = deviceOwnerWithStatus.getDeviceIds();
            if (deviceIds != null) {
                deviceOwnerWithStatus.setDeviceCount(deviceIds.size());
            } else {
                deviceOwnerWithStatus.setDeviceCount(0);
            }
        } catch (DeviceManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving owner and status for device ID: " + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceOwnerWithStatus;
    }

    @Override
    public List<DeviceDetailsDTO> getDevicesByTenantId(int tenantId, int deviceTypeId, String deviceOwner, String deviceStatus)
            throws DeviceManagementException {
        List<DeviceDetailsDTO> devices;
        List<String> allowingDeviceStatuses = new ArrayList<>();
        allowingDeviceStatuses.add(EnrolmentInfo.Status.ACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.INACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.UNREACHABLE.toString());
        try {
            DeviceManagementDAOFactory.openConnection();
            devices = enrollmentDAO.getDevicesByTenantId(tenantId, allowingDeviceStatuses, deviceTypeId, deviceOwner, deviceStatus);
        } catch (DeviceManagementDAOException e) {
            String msg = "Failed to retrieve devices for tenant ID: " + tenantId + ", deviceTypeId: " + deviceTypeId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred while accessing devices for tenant ID: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return devices;
    }


    @Override
    public OperationDTO getOperationDetailsById(int operationId) throws OperationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        OperationDTO operationDetails;
        try {
            OperationManagementDAOFactory.openConnection();
            operationDetails = this.operationDAO.getOperationDetailsById(operationId, tenantId);
            if (operationDetails == null) {
                String msg = "No operation details found for operation ID: " + operationId;
                log.error(msg);
                throw new OperationManagementException(msg);
            }
        } catch (SQLException | OperationManagementDAOException e) {
            String msg = "Error occurred while retrieving operation details for operation ID: " + operationId;
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operationDetails;
    }

    @Override
    public PaginationResult getDevicesNotInGroup(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        if (request == null) {
            String msg = "Received incomplete pagination request for method getDevicesNotInGroup";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get devices not in group with pagination " + request.toString() +
                    " and requiredDeviceInfo: " + requireDeviceInfo);
        }
        PaginationResult paginationResult = new PaginationResult();
        List<Device> devicesNotInGroup = null;
        int count = 0;
        int tenantId = this.getTenantId();
        DeviceManagerUtil.validateDeviceListPageSize(request);

        try {
            DeviceManagementDAOFactory.openConnection();
            if (request.getGroupId() != 0) {
                devicesNotInGroup = deviceDAO.searchDevicesNotInGroup(request, tenantId);
                count = deviceDAO.getCountOfDevicesNotInGroup(request, tenantId);
            } else {
                String msg = "Group ID is not provided for method getDevicesNotInGroup";
                log.error(msg);
                throw new DeviceManagementException(msg);
            }
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device list that are not in the specified group for the current tenant";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevicesNotInGroup";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo && devicesNotInGroup != null && !devicesNotInGroup.isEmpty()) {
            paginationResult.setData(populateAllDeviceInfo(devicesNotInGroup));
        } else {
            paginationResult.setData(devicesNotInGroup);
        }

        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public Device updateDeviceName(Device device, String deviceType, String deviceId)
            throws DeviceManagementException, DeviceNotFoundException, ConflictException {
        Device persistedDevice = this.getDevice(new DeviceIdentifier(deviceId, deviceType), true);
        if (persistedDevice == null) {
            String msg = "Device not found for the given deviceId and deviceType";
            log.error(msg);
            throw new DeviceNotFoundException(msg);
        }
        if (persistedDevice.getName().equals(device.getName())) {
            String msg = "Device names are the same.";
            log.info(msg);
            throw new ConflictException(msg);
        }
        persistedDevice.setName(device.getName());
        if (log.isDebugEnabled()) {
            log.debug("Rename Device name of: " + persistedDevice.getId() + " of type '" + persistedDevice.getType() + "'");
        }
        DeviceManager deviceManager = this.getDeviceManager(persistedDevice.getType());
        if (deviceManager == null) {
            String msg = "Device Manager associated with the device type '" + persistedDevice.getType() + "' is null. " +
                    "Therefore, not attempting method 'modifyEnrolment'";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(persistedDevice.getDeviceIdentifier(), persistedDevice.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = this.getTenantId();
            deviceDAO.updateDevice(persistedDevice, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            this.updateDeviceInCache(deviceIdentifier, persistedDevice);
            return persistedDevice;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while renaming the device '" + persistedDevice.getId() + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction to rename device: " + persistedDevice.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Integer> getDevicesNotInGivenIdList(List<Integer> deviceIds)
            throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesNotInGivenIdList(deviceIds, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting device ids";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Integer> getDevicesInGivenIdList(List<Integer> deviceIds)
            throws DeviceManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesInGivenIdList(deviceIds, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting device ids";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCountNotInGivenIdList(List<Integer> deviceIds)
            throws DeviceManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCountNotInGivenIdList(deviceIds, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting device ids";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Device> getDevicesByDeviceIds(PaginationRequest paginationRequest, List<Integer> deviceIds)
            throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (paginationRequest == null) {
            String msg = "Received null for pagination request";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }

        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDevicesByDeviceIds(paginationRequest, deviceIds, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting devices for device ids in " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCountByDeviceIds(PaginationRequest paginationRequest, List<Integer> deviceIds)
            throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (paginationRequest == null) {
            String msg = "Received null for pagination request";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }

        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCountByDeviceIds(paginationRequest, deviceIds, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error encountered while getting devices for device ids in " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Integer> getDeviceIdsByStatus(List<String> statuses) throws DeviceManagementException {
        if (statuses == null || statuses.isEmpty()) {
            String msg = "Received null or empty list for statuses";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }

        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceIdsByStatus(statuses);
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while getting device IDs for statuses: " + statuses;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error encountered while getting the database connection";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
