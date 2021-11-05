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
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.util;

import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistory;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshotWrapper;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.UnAuthorizedException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.report.mgt.ReportManagementService;
import org.wso2.carbon.device.mgt.common.spi.DeviceTypeGeneratorService;
import org.wso2.carbon.device.mgt.common.spi.OTPManagementService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeVersion;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.device.mgt.core.privacy.PrivacyComplianceProvider;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceTypeVersionWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationStatusBean;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventAttributeList;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.identity.user.store.count.AbstractCountRetrieverFactory;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.jdbc.JDBCCountRetrieverFactory;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalCountRetrieverFactory;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * MDMAPIUtils class provides utility function used by CDM REST-API classes.
 */
public class DeviceMgtAPIUtils {

    private final static String CDM_ADMIN_PERMISSION = "/device-mgt/devices/any-device/permitted-actions-under-owning-device";
    private static final String NOTIFIER_FREQUENCY = "notifierFrequency";
    private static final String STREAM_DEFINITION_PREFIX = "iot.per.device.stream.";
    private static final String DEFAULT_HTTP_PROTOCOL = "https";
    private static final String EVENT_RECIEVER_CONTEXT = "EventReceiverAdminService/";
    private static final String EVENT_PUBLISHER_CONTEXT = "EventPublisherAdminService/";
    private static final String EVENT_STREAM_CONTEXT = "EventStreamAdminService/";
    private static final String EVENT_PERSISTENCE_CONTEXT = "EventStreamPersistenceAdminService/";
    private static final String EVENT_PROCESSOR_CONTEXT = "EventProcessorAdminService";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer";
    public static final String DAS_PORT = "${iot.analytics.https.port}";
    public static final String DAS_HOST_NAME = "${iot.analytics.host}";
    private static final String KEY_STORE_TYPE = "JKS";
    private static final String TRUST_STORE_TYPE = "JKS";
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type
    private static final String SSLV3 = "SSLv3";
    private static final String EVENT_CACHE_MANAGER_NAME = "mqttAuthorizationCacheManager";
    private static final String EVENT_CACHE_NAME = "mqttAuthorizationCache";
    public static final String DAS_ADMIN_SERVICE_EP = "https://" + DAS_HOST_NAME + ":" + DAS_PORT + "/services/";
    private static SSLContext sslContext;

    private static final Log log = LogFactory.getLog(DeviceMgtAPIUtils.class);
    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static char[] keyStorePassword;

//    private static IntegrationClientService integrationClientService;
    private static MetadataManagementService metadataManagementService;
    private static OTPManagementService otpManagementService;

    private static volatile SubscriptionManager subscriptionManager;
    private static volatile ApplicationManager applicationManager;

    static {
        String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
        String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Password");
        String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
        String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Location");

        //Call to load the keystore.
        try {
            loadKeyStore(keyStoreLocation, keyStorePassword);
            //Call to load the TrustStore.
            loadTrustStore(trustStoreLocation, trustStorePassword);
            //Create the SSL context with the loaded TrustStore/keystore.
            initSSLConnection();
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException
                | UnrecoverableKeyException | KeyManagementException e) {
            log.error("publishing dynamic event receiver is failed due to  " + e.getMessage(), e);
        }
    }

    public static int getNotifierFrequency(PlatformConfiguration tenantConfiguration) {
        List<ConfigurationEntry> configEntryList = tenantConfiguration.getConfiguration();
        if (configEntryList != null && !configEntryList.isEmpty()) {
            for (ConfigurationEntry entry : configEntryList) {
                if (NOTIFIER_FREQUENCY.equals(entry.getName())) {
                    if (entry.getValue() == null) {
                        throw new InputValidationException(
                                new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                                        "Notifier frequency cannot be null. Please specify a valid non-negative " +
                                                "integer value to successfully set up notification frequency. " +
                                                "Should the service be stopped, use '0' as the notification " +
                                                "frequency.").build()
                        );
                    }
                    return (int) (Double.parseDouble(entry.getValue().toString()) + 0.5d);
                }
            }
        }
        return 0;
    }

    public static SubscriptionManager getSubscriptionManager() {
        if (subscriptionManager == null) {
            synchronized (DeviceMgtAPIUtils.class) {
                if (subscriptionManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    subscriptionManager =
                            (SubscriptionManager) ctx.getOSGiService(SubscriptionManager.class, null);
                    if (subscriptionManager == null) {
                        String msg = "Subscription Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return subscriptionManager;
    }

    public static ApplicationManager getApplicationManager() {
        if (applicationManager == null) {
            synchronized (DeviceMgtAPIUtils.class) {
                if (applicationManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationManager =
                            (ApplicationManager) ctx.getOSGiService(ApplicationManager.class, null);
                    if (applicationManager == null) {
                        String msg = "Application Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationManager;
    }

    public static void scheduleTaskService(int notifierFrequency) {
        TaskScheduleService taskScheduleService;
        try {
            taskScheduleService = getPolicyManagementService().getTaskScheduleService();
            if (taskScheduleService.isTaskScheduled()) {
                taskScheduleService.updateTask(notifierFrequency);
            } else {
                taskScheduleService.startTask(notifierFrequency);
            }
        } catch (PolicyMonitoringTaskException e) {
            log.error("Exception occurred while starting the Task service.", e);
        }
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "DeviceImpl Management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static DeviceTypeGeneratorService getDeviceTypeGeneratorService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceTypeGeneratorService deviceTypeGeneratorService =
                (DeviceTypeGeneratorService) ctx.getOSGiService(DeviceTypeGeneratorService.class, null);
        if (deviceTypeGeneratorService == null) {
            String msg = "DeviceTypeGeneratorService service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceTypeGeneratorService;
    }

    public static boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = getDeviceManagementService().getDevice(deviceIdentifier, false);
        if (device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null) {
            return false;
        } else return !EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus());
    }


    public static UserStoreCountRetriever getUserStoreCountRetrieverService()
            throws UserStoreCounterException, UserStoreException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        List<Object> countRetrieverFactories = ctx.getOSGiServices(AbstractCountRetrieverFactory.class, null);
        RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        RealmConfiguration realmConfiguration = realmService.getBootstrapRealmConfiguration();
        String userStoreType;
        if(DeviceMgtAPIUtils.getUserStoreManager() instanceof JDBCUserStoreManager) {
            userStoreType = JDBCCountRetrieverFactory.JDBC;
        } else {
            userStoreType = InternalCountRetrieverFactory.INTERNAL;
        }
        AbstractCountRetrieverFactory countRetrieverFactory = null;
        for (Object countRetrieverFactoryObj : countRetrieverFactories) {
            countRetrieverFactory = (AbstractCountRetrieverFactory) countRetrieverFactoryObj;
            if (userStoreType.equals(countRetrieverFactory.getCounterType())) {
                break;
            }
        }
        if (countRetrieverFactory == null) {
            return null;
        }
        return countRetrieverFactory.buildCountRetriever(realmConfiguration);
    }

    public static DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                (DeviceAccessAuthorizationService) ctx.getOSGiService(DeviceAccessAuthorizationService.class, null);
        if (deviceAccessAuthorizationService == null) {
            String msg = "DeviceAccessAuthorization service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceAccessAuthorizationService;
    }

    public static GroupManagementProviderService getGroupManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        GroupManagementProviderService groupManagementProviderService =
                (GroupManagementProviderService) ctx.getOSGiService(GroupManagementProviderService.class, null);
        if (groupManagementProviderService == null) {
            String msg = "GroupImpl Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return groupManagementProviderService;
    }

    public static UserStoreManager getUserStoreManager() throws UserStoreException {
        RealmService realmService;
        UserStoreManager userStoreManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        int tenantId = ctx.getTenantId();
        userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        return userStoreManager;
    }

    public static RealmService getRealmService() throws UserStoreException {
        RealmService realmService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    public static PrivacyComplianceProvider getPrivacyComplianceProvider() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PrivacyComplianceProvider privacyComplianceProvider =
                (PrivacyComplianceProvider) ctx.getOSGiService(PrivacyComplianceProvider.class, null);
        if (privacyComplianceProvider == null) {
            String msg = "PrivacyComplianceProvider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return privacyComplianceProvider;
    }

    /**
     * Initializing and accessing method for OTPManagementService.
     *
     * @return OTPManagementService instance
     * @throws IllegalStateException if OTPManagementService cannot be initialized
     */
    public static synchronized OTPManagementService getOTPManagementService() {
        if (otpManagementService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            otpManagementService = (OTPManagementService) ctx.getOSGiService(OTPManagementService.class, null);
            if (otpManagementService == null) {
                String msg = "OTP Management service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        return otpManagementService;
    }

    public static RegistryService getRegistryService() {
        RegistryService registryService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        registryService = (RegistryService) ctx.getOSGiService(RegistryService.class, null);
        if (registryService == null) {
            String msg = "registry service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return registryService;
    }

    public static JWTClientManagerService getJWTClientManagerService() {
        JWTClientManagerService jwtClientManagerService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        jwtClientManagerService = (JWTClientManagerService) ctx.getOSGiService(JWTClientManagerService.class, null);
        if (jwtClientManagerService == null) {
            String msg = "jwtClientManagerServicehas not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return jwtClientManagerService;
    }

    /**
     * Getting the current tenant's user realm
     */
    public static UserRealm getUserRealm() throws UserStoreException {
        RealmService realmService;
        UserRealm realm;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);

        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        realm = realmService.getTenantUserRealm(tenantId);
        return realm;
    }

    public static AuthorizationManager getAuthorizationManager() throws UserStoreException {
        RealmService realmService;
        AuthorizationManager authorizationManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized.");
        }
        int tenantId = ctx.getTenantId();
        authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();

        return authorizationManager;
    }

    public static ApplicationManagementProviderService getAppManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ApplicationManagementProviderService applicationManagementProviderService =
                (ApplicationManagementProviderService) ctx.getOSGiService(ApplicationManagementProviderService.class, null);
        if (applicationManagementProviderService == null) {
            throw new IllegalStateException("AuthenticationImpl management service has not initialized.");
        }
        return applicationManagementProviderService;
    }

    public static PolicyManagerService getPolicyManagementService() {
        PolicyManagerService policyManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        policyManagementService =
                (PolicyManagerService) ctx.getOSGiService(PolicyManagerService.class, null);
        if (policyManagementService == null) {
            String msg = "PolicyImpl Management service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return policyManagementService;
    }

    public static PlatformConfigurationManagementService getPlatformConfigurationManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PlatformConfigurationManagementService tenantConfigurationManagementService =
                (PlatformConfigurationManagementService) ctx.getOSGiService(
                        PlatformConfigurationManagementService.class, null);
        if (tenantConfigurationManagementService == null) {
            throw new IllegalStateException("Tenant configuration Management service not initialized.");
        }
        return tenantConfigurationManagementService;
    }

    public static NotificationManagementService getNotificationManagementService() {
        NotificationManagementService notificationManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        notificationManagementService = (NotificationManagementService) ctx.getOSGiService(
                NotificationManagementService.class, null);
        if (notificationManagementService == null) {
            throw new IllegalStateException("Notification Management service not initialized.");
        }
        return notificationManagementService;
    }

    /**
     * Initializing and accessing method for MetadataManagementService.
     *
     * @return MetadataManagementService instance
     * @throws IllegalStateException if metadataManagementService cannot be initialized
     */
    public static MetadataManagementService getMetadataManagementService() {
        if (metadataManagementService == null) {
            synchronized (DeviceMgtAPIUtils.class) {
                if (metadataManagementService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    metadataManagementService = (MetadataManagementService) ctx.getOSGiService(
                            MetadataManagementService.class, null);
                    if (metadataManagementService == null) {
                        throw new IllegalStateException("Metadata Management service not initialized.");
                    }
                }
            }
        }
        return metadataManagementService;
    }

    /**
     * Method for initializing ReportManagementService
     * @return ReportManagementServie Instance
     */
    public static ReportManagementService getReportManagementService() {
        ReportManagementService reportManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        reportManagementService = (ReportManagementService) ctx.getOSGiService(
                ReportManagementService.class, null);
        if (reportManagementService == null) {
            String msg = "Report Management service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return reportManagementService;
    }

    public static DeviceInformationManager getDeviceInformationManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager deviceInformationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);
        if (deviceInformationManager == null) {
            throw new IllegalStateException("DeviceImpl information Manager service has not initialized.");
        }
        return deviceInformationManager;
    }


    public static SearchManagerService getSearchManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        SearchManagerService searchManagerService =
                (SearchManagerService) ctx.getOSGiService(SearchManagerService.class, null);
        if (searchManagerService == null) {
            throw new IllegalStateException("DeviceImpl search manager service is not initialized.");
        }
        return searchManagerService;
    }

    public static GeoLocationProviderService getGeoService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        GeoLocationProviderService
                geoService = (GeoLocationProviderService) ctx.getOSGiService(GeoLocationProviderService.class, null);
        if (geoService == null) {
            throw new IllegalStateException("Geo Service has not been initialized.");
        }
        return geoService;
    }

    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        AnalyticsDataAPI analyticsDataAPI =
                (AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
        if (analyticsDataAPI == null) {
            String msg = "Analytics api service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return analyticsDataAPI;
    }

    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        RealmService realmService =
                (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service has not been initialized.");
        }
        try {
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new DeviceManagementException("Error occured while trying to " +
                    "obtain tenant id of currently logged in user");
        }
    }

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username != null && username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    // todo: amalka: commented
//    public static EventsPublisherService getEventPublisherService() {
//        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        EventsPublisherService eventsPublisherService =
//                (EventsPublisherService) ctx.getOSGiService(EventsPublisherService.class, null);
//        if (eventsPublisherService == null) {
//            String msg = "Event Publisher service has not initialized.";
//            log.error(msg);
//            throw new IllegalStateException(msg);
//        }
//        return eventsPublisherService;
//    }

    public static String getStreamDefinition(String deviceType, String tenantDomain) {
        return STREAM_DEFINITION_PREFIX + tenantDomain + "." + deviceType.replace(" ", ".");
    }

    public static EventStreamAdminServiceStub getEventStreamAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = new EventStreamAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP + EVENT_STREAM_CONTEXT));
        Options streamOptions = eventStreamAdminServiceStub._getServiceClient().getOptions();
        if (streamOptions == null) {
            streamOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"
        streamOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        streamOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                        , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                        , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        eventStreamAdminServiceStub._getServiceClient().setOptions(streamOptions);
        return eventStreamAdminServiceStub;
    }

    public static EventReceiverAdminServiceStub getEventReceiverAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventReceiverAdminServiceStub receiverAdminServiceStub = new EventReceiverAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP + EVENT_RECIEVER_CONTEXT));
        Options eventReciverOptions = receiverAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                        , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                        , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));

        receiverAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return receiverAdminServiceStub;
    }

    public static EventPublisherAdminServiceStub getEventPublisherAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = new EventPublisherAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP + EVENT_PUBLISHER_CONTEXT));
        Options eventReciverOptions = eventPublisherAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                        , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                        , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        eventPublisherAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return eventPublisherAdminServiceStub;
    }

    public static EventStreamPersistenceAdminServiceStub getEventStreamPersistenceAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventStreamPersistenceAdminServiceStub eventStreamPersistenceAdminServiceStub
                = new EventStreamPersistenceAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP + EVENT_PERSISTENCE_CONTEXT));
        Options eventReciverOptions = eventStreamPersistenceAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                        , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                        , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));

        eventStreamPersistenceAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return eventStreamPersistenceAdminServiceStub;
    }

    public static EventProcessorAdminServiceStub getEventProcessorAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP + EVENT_PROCESSOR_CONTEXT));
        Options eventProcessorOption = eventProcessorAdminServiceStub._getServiceClient().getOptions();
        if (eventProcessorOption == null) {
            eventProcessorOption = new Options();
        }
        // Get the tenant Domain
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                  .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        // Create the SSL context with the loaded TrustStore/keystore.
        JWTClient jwtClient = getJWTClientManagerService().getJWTClient();

        String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"

        eventProcessorOption.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventProcessorOption.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                        , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                        , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));

        eventProcessorAdminServiceStub._getServiceClient().setOptions(eventProcessorOption);
        return eventProcessorAdminServiceStub;
    }

    /**
     * This method is used to create the Cache that holds the event definition of the device type..
     *
     * @return Cachemanager
     */
    public static synchronized Cache<String, EventAttributeList> getDynamicEventCache() {
        return Caching.getCacheManagerFactory().getCacheManager(EVENT_CACHE_MANAGER_NAME).getCache(EVENT_CACHE_NAME);
    }

    /**
     * Loads the keystore.
     *
     * @param keyStorePath - the path of the keystore
     * @param ksPassword   - the keystore password
     */
    private static void loadKeyStore(String keyStorePath, String ksPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fis = null;
        try {
            keyStorePassword = ksPassword.toCharArray();
            keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            fis = new FileInputStream(keyStorePath);
            keyStore.load(fis, keyStorePassword);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Loads the trustore
     *
     * @param trustStorePath - the trustore path in the filesystem.
     * @param tsPassword     - the truststore password
     */
    private static void loadTrustStore(String trustStorePath, String tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        InputStream fis = null;
        try {
            trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
            fis = new FileInputStream(trustStorePath);
            trustStore.load(fis, tsPassword.toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Initializes the SSL Context
     */
    private static void initSSLConnection() throws NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        sslContext = SSLContext.getInstance(SSLV3);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
    }


    public static boolean isAdmin() throws UserStoreException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        UserRealm realmService = DeviceMgtAPIUtils.getRealmService().getTenantUserRealm(tenantId);
        String adminRoleName = realmService.getRealmConfiguration().getAdminRoleName();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String[] roles = realmService.getUserStoreManager().getRoleListOfUser(userName);
        for (String role: roles){
            if (role != null && role.equals(adminRoleName)){
                return true;
            }
        }
        return false;
    }

    public static boolean isAdminUser() throws UserStoreException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        UserRealm userRealm = DeviceMgtAPIUtils.getRealmService().getTenantUserRealm(tenantId);
        if (userRealm != null && userRealm.getAuthorizationManager() != null) {
            return userRealm.getAuthorizationManager()
                    .isUserAuthorized(removeTenantDomain(userName),
                            PermissionUtils.getAbsolutePermissionPath(CDM_ADMIN_PERMISSION),
                            CarbonConstants.UI_PERMISSION_ACTION);
        }
        return false;
    }

    private static String removeTenantDomain(String username) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static DeviceTypeVersion convertDeviceTypeVersionWrapper(String deviceTypeName, int deviceTypeId,
            DeviceTypeVersionWrapper deviceTypeVersion) {
        DeviceTypeVersion typeVersion = new DeviceTypeVersion();
        typeVersion.setDeviceTypeId(deviceTypeId);
        typeVersion.setDeviceTypeName(deviceTypeName);
        typeVersion.setVersionName(deviceTypeVersion.getVersionName());
        typeVersion.setVersionStatus(deviceTypeVersion.getVersionStatus());
        return typeVersion;
    }

    /**
     * Extract permissions from a UiPermissionNode using recursions
     * @param uiPermissionNode an UiPermissionNode Object to extract permissions
     * @param list provided list to add permissions
     */
    public static void iteratePermissions(UIPermissionNode uiPermissionNode, List<String> list) {
        // To prevent NullPointer exceptions
        if (uiPermissionNode == null) {
            return;
        }
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            if (permissionNode != null) {
                if(permissionNode.isSelected()){
                    list.add(permissionNode.getResourcePath());
                }
                if (permissionNode.getNodeList() != null
                        && permissionNode.getNodeList().length > 0) {
                    iteratePermissions(permissionNode, list);
                }
            }
        }
    }

    /**
     * This method validates the status of the operation
     *
     * @param operationStatusBean {@link OperationStatusBean} object
     * @return {@link Operation} Returns Operation object with status set.
     * @throws {@link BadRequestException} If invalid status received
     */
    public static Operation validateOperationStatusBean(OperationStatusBean operationStatusBean)
            throws BadRequestException {
        Operation operation = new Operation();
        if (operationStatusBean.getStatus() != null) {
            switch (operationStatusBean.getStatus().toLowerCase()) {
                case Constants.OperationStatus.COMPLETED:
                    operation.setStatus(Operation.Status.COMPLETED);
                    break;
                case Constants.OperationStatus.ERROR:
                    operation.setStatus(Operation.Status.ERROR);
                    break;
                case Constants.OperationStatus.IN_PROGRESS:
                    operation.setStatus(Operation.Status.IN_PROGRESS);
                    break;
                case Constants.OperationStatus.PENDING:
                    operation.setStatus(Operation.Status.PENDING);
                    break;
                case Constants.OperationStatus.NOTNOW:
                    operation.setStatus(Operation.Status.NOTNOW);
                    break;
                case Constants.OperationStatus.REPEATED:
                    operation.setStatus(Operation.Status.REPEATED);
                    break;
                default:
                    String msg = "Invalid operation status. Valid operations: " +
                            "[IN_PROGRESS, PENDING, COMPLETED, ERROR, REPEATED, NOTNOW]";
                    log.error(msg);
                    throw new BadRequestException(msg);
            }
        } else {
            String msg = "Payload does not contain status value";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        return operation;
    }

    /**
     * This method is used to set property name and value to ClaimPropertyDTO
     *
     * @param propertyName Name of the property
     * @param propertyValue Value of the property
     * @return {@link ClaimPropertyDTO}
     */
    public static ClaimPropertyDTO buildClaimPropertyDTO(String propertyName, String propertyValue) {
        ClaimPropertyDTO claimPropertyDTO = new ClaimPropertyDTO();
        claimPropertyDTO.setPropertyName(propertyName);
        claimPropertyDTO.setPropertyValue(propertyValue);
        return claimPropertyDTO;
    }

    /**
     * Getting Device History Snapshots for given Device Type and Identifier.
     *
     * @param deviceType Device type of the device
     * @param identifier Device identifier of the device
     * @param authorizedUser user who initiates the request
     * @param from time to start getting DeviceLocationHistorySnapshotWrapper in milliseconds
     * @param to time to end getting DeviceLocationHistorySnapshotWrapper in milliseconds
     * @param type output type should be for DeviceLocationHistorySnapshotWrapper
     * @param dms DeviceManagementService instance
     *
     * @return DeviceLocationHistorySnapshotWrapper instance
     * @throws DeviceManagementException if device information cannot be fetched
     * @throws DeviceAccessAuthorizationException  if device authorization get failed
     */
    public static DeviceLocationHistorySnapshotWrapper getDeviceHistorySnapshots(String deviceType,
                                                                                 String identifier,
                                                                                 String authorizedUser,
                                                                                 long from,
                                                                                 long to,
                                                                                 String type,
                                                                                 DeviceManagementProviderService dms)
            throws DeviceManagementException, DeviceAccessAuthorizationException {
            RequestValidationUtil.validateDeviceIdentifier(deviceType, identifier);
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(identifier, deviceType);

            if (!getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" +
                        identifier + "'";
                log.error(msg);
                throw new UnAuthorizedException(msg);
            }

            // Get the location history snapshots for the given period
            List<DeviceLocationHistorySnapshot> deviceLocationHistorySnapshots = dms.getDeviceLocationInfo(deviceIdentifier, from, to);

            OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getDeviceMonitoringConfig(deviceType);
            int taskFrequency = operationMonitoringTaskConfig.getFrequency();
            int operationRecurrentTimes = 0;

            List<MonitoringOperation> monitoringOperations = operationMonitoringTaskConfig.getMonitoringOperation();
            for (MonitoringOperation monitoringOperation : monitoringOperations) {
                if (monitoringOperation.getTaskName().equals("DEVICE_LOCATION")) {
                    operationRecurrentTimes = monitoringOperation.getRecurrentTimes();
                    break;
                }
            }

            // Device Location operation frequency in milliseconds. Adding 100000 ms as an error
            long operationFrequency = taskFrequency * operationRecurrentTimes + 100000;
            Queue<DeviceLocationHistorySnapshot> deviceLocationHistorySnapshotsQueue = new LinkedList<>(
                    deviceLocationHistorySnapshots);
            List<List<DeviceLocationHistorySnapshot>> locationHistorySnapshotList = new ArrayList<>();

            List<Object> pathsArray = new ArrayList<>();
            DeviceLocationHistorySnapshotWrapper snapshotWrapper = new DeviceLocationHistorySnapshotWrapper();
            while (!deviceLocationHistorySnapshotsQueue.isEmpty()) {
                List<DeviceLocationHistorySnapshot> snapshots = new ArrayList<>();
                // Make a copy of remaining snapshots
                List<DeviceLocationHistorySnapshot> cachedSnapshots = new ArrayList<>(
                        deviceLocationHistorySnapshotsQueue);

                List<Object> locationPoint = new ArrayList<>();
                for (int i = 0; i < cachedSnapshots.size(); i++) {
                    DeviceLocationHistorySnapshot currentSnapshot = deviceLocationHistorySnapshotsQueue.poll();
                    snapshots.add(currentSnapshot);
                    if (currentSnapshot != null) {
                        locationPoint.add(currentSnapshot.getLatitude());
                        locationPoint.add(currentSnapshot.getLongitude());
                        locationPoint.add(currentSnapshot.getUpdatedTime());
                        pathsArray.add(new ArrayList<>(locationPoint));
                        locationPoint.clear();
                    }
                    if (!deviceLocationHistorySnapshotsQueue.isEmpty()) {
                        DeviceLocationHistorySnapshot nextSnapshot = deviceLocationHistorySnapshotsQueue.peek();
                        locationPoint.add(nextSnapshot.getLatitude());
                        locationPoint.add(nextSnapshot.getLongitude());
                        locationPoint.add(nextSnapshot.getUpdatedTime());
                        pathsArray.add(new ArrayList<>(locationPoint));
                        locationPoint.clear();
                        if (nextSnapshot.getUpdatedTime().getTime() - currentSnapshot.getUpdatedTime().getTime()
                                > operationFrequency) {
                            break;
                        }
                    }
                }
                locationHistorySnapshotList.add(snapshots);
            }
            DeviceLocationHistory deviceLocationHistory = new DeviceLocationHistory();
            deviceLocationHistory.setLocationHistorySnapshots(locationHistorySnapshotList);
            if (type != null) {
                if (type.equals("path")) {
                    snapshotWrapper.setPathSnapshot(pathsArray);
                } else if (type.equals("full")) {
                    snapshotWrapper.setFullSnapshot(deviceLocationHistory);
                } else {
                    String msg = "Invalid type, use either 'path' or 'full'";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
            } else {
                snapshotWrapper.setFullSnapshot(deviceLocationHistory);
            }
            return snapshotWrapper;
    }

    /**
     * Check user who initiates the request has permission to list devices from given group Id.
     *
     * @param groupId Group ID of the group
     * @param authorizedUser user who initiates the request
     *
     * @return boolean instance
     * @throws UserStoreException if roles list of authorizedUser cannot be fetched
     * @throws DeviceAccessAuthorizationException if device authorization get failed.
     * @throws GroupManagementException if group or roles cannot be fetched using groupId
     */
    public static boolean checkPermission(int groupId, String authorizedUser) throws UserStoreException, DeviceAccessAuthorizationException, GroupManagementException  {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStoreManager userStoreManager = DeviceMgtAPIUtils.getRealmService()
                .getTenantUserRealm(tenantId).getUserStoreManager();
        String[] userRoles = userStoreManager.getRoleListOfUser(authorizedUser);
        boolean isPermitted = false;
        if (getDeviceAccessAuthorizationService().isDeviceAdminUser()) {
            isPermitted = true;
        } else {
            List<String> roles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(groupId);
            for (String userRole : userRoles) {
                if (roles.contains(userRole)) {
                    isPermitted = true;
                    break;
                }
            }
            if (!isPermitted) {
                DeviceGroup deviceGroup = DeviceMgtAPIUtils.getGroupManagementProviderService()
                        .getGroup(groupId, false);
                if (deviceGroup != null && authorizedUser.equals(deviceGroup.getOwner())) {
                    isPermitted = true;
                }
            }
        }
        return isPermitted;
    }

}
