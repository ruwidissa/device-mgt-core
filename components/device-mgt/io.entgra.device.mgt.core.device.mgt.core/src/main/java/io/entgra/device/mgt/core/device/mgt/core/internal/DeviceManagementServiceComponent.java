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
package io.entgra.device.mgt.core.device.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.common.authorization.GroupAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import io.entgra.device.mgt.core.device.mgt.common.event.config.EventConfigurationProviderService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoLocationProviderService;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.WhiteLabelManagementService;
import io.entgra.device.mgt.core.device.mgt.common.notification.mgt.NotificationManagementService;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManager;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagerService;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.ReportManagementService;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceTypeGeneratorService;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.ApplicationManagerProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.config.AppManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.config.AppManagementConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.authorization.GroupAccessAuthorizationServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.EventManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.TrackerManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceInformationManager;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.event.config.EventConfigurationProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.geo.service.GeoLocationProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.DeviceStatusManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.MetadataManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.WhiteLabelManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.notification.mgt.NotificationManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.dao.OTPManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.service.OTPManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.privacy.PrivacyComplianceProvider;
import io.entgra.device.mgt.core.device.mgt.core.privacy.impl.PrivacyComplianceProviderImpl;
import io.entgra.device.mgt.core.device.mgt.core.push.notification.mgt.PushNotificationProviderRepository;
import io.entgra.device.mgt.core.device.mgt.core.push.notification.mgt.task.PushNotificationSchedulerTask;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.ReportManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.SearchManagerService;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.impl.SearchManagerServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceTypeEventManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceTypeEventManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.TagManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.TagManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.task.DeviceTaskManagerService;
import io.entgra.device.mgt.core.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import io.entgra.device.mgt.core.device.mgt.core.traccar.api.service.impl.DeviceAPIClientServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagementSchemaInitializer;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import io.entgra.device.mgt.core.transport.mgt.email.sender.core.service.EmailSenderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent",
        immediate = true)
public class DeviceManagementServiceComponent {

    private static final Object LOCK = new Object();
    private static final Log log = LogFactory.getLog(DeviceManagementServiceComponent.class);
    private static final List<PluginInitializationListener> listeners = new ArrayList<>();
    private static final List<DeviceManagementService> deviceManagers = new ArrayList<>();
    private static final List<DeviceManagerStartupListener> startupListeners = new ArrayList<>();

    public static void registerPluginInitializationListener(PluginInitializationListener listener) {
        synchronized (LOCK) {
            listeners.add(listener);
            for (DeviceManagementService deviceManagementService : deviceManagers) {
                listener.registerDeviceManagementService(deviceManagementService);
            }
        }
    }

    public static void registerStartupListener(DeviceManagerStartupListener startupListener) {
        startupListeners.add(startupListener);
    }

    public static void notifyStartupListeners() {
        for (DeviceManagerStartupListener startupListener : startupListeners) {
            startupListener.notifyObserver();
        }
    }

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device management core bundle");
            }
            /* Initializing Device Management Configuration */
            DeviceConfigurationManager.getInstance().initConfig();
            UIConfigurationManager.getInstance().initConfig();
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();

            DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();

            DeviceManagementDAOFactory.init(dsConfig);
            GroupManagementDAOFactory.init(dsConfig);
            TrackerManagementDAOFactory.init(dsConfig);
            NotificationManagementDAOFactory.init(dsConfig);
            OperationManagementDAOFactory.init(dsConfig);
            MetadataManagementDAOFactory.init(dsConfig);
            EventManagementDAOFactory.init(dsConfig);
            OTPManagementDAOFactory.init(dsConfig.getJndiLookupDefinition().getJndiName());
            /*Initialize the device cache*/
            DeviceManagerUtil.initializeDeviceCache();

            /* Initialize Operation Manager */
            this.initOperationsManager();

            PushNotificationProviderRepository pushNotificationRepo = new PushNotificationProviderRepository();
            List<String> pushNotificationProviders = config.getPushNotificationConfiguration()
                    .getPushNotificationProviders();
            if (pushNotificationProviders != null) {
                for (String pushNoteProvider : pushNotificationProviders) {
                    pushNotificationRepo.addProvider(pushNoteProvider);
                }
            }
            DeviceManagementDataHolder.getInstance().setPushNotificationProviderRepository(pushNotificationRepo);

            /* If -Dsetup option enabled then create device management database schema */
            String setupOption =
                    System.getProperty(DeviceManagementConstants.Common.SETUP_PROPERTY);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Device management repository schema initialization is about to " +
                            "begin");
                }
                this.setupDeviceManagementSchema(dsConfig);
            }

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(componentContext);

            /* This is a workaround to initialize all Device Management Service Providers after the initialization
             * of Device Management Service component in order to avoid bundle start up order related complications */
            notifyStartupListeners();
            if (log.isDebugEnabled()) {
                log.debug("Push notification batch enabled : " + config.getPushNotificationConfiguration()
                        .isSchedulerTaskEnabled());
            }
            // Start Push Notification Scheduler Task
            if (config.getPushNotificationConfiguration().isSchedulerTaskEnabled()) {
                if (config.getPushNotificationConfiguration().getSchedulerBatchSize() <= 0) {
                    log.error("Push notification batch size cannot be 0 or less than 0. Setting default batch size " +
                            "to:" + DeviceManagementConstants.PushNotifications.DEFAULT_BATCH_SIZE);
                    config.getPushNotificationConfiguration().setSchedulerBatchSize(DeviceManagementConstants
                            .PushNotifications.DEFAULT_BATCH_SIZE);
                }
                if (config.getPushNotificationConfiguration().getSchedulerBatchDelayMills() <= 0) {
                    log.error("Push notification batch delay cannot be 0 or less than 0. Setting default batch delay " +
                            "milliseconds to" + DeviceManagementConstants.PushNotifications.DEFAULT_BATCH_DELAY_MILLS);
                    config.getPushNotificationConfiguration().setSchedulerBatchDelayMills(DeviceManagementConstants
                            .PushNotifications.DEFAULT_BATCH_DELAY_MILLS);
                }
                if (config.getPushNotificationConfiguration().getSchedulerTaskInitialDelay() < 0) {
                    log.error("Push notification initial delay cannot be less than 0. Setting default initial " +
                            "delay milliseconds to" + DeviceManagementConstants.PushNotifications
                            .DEFAULT_SCHEDULER_TASK_INITIAL_DELAY);
                    config.getPushNotificationConfiguration().setSchedulerTaskInitialDelay(DeviceManagementConstants
                            .PushNotifications.DEFAULT_SCHEDULER_TASK_INITIAL_DELAY);
                }
                ScheduledExecutorService pushNotificationExecutor = Executors.newSingleThreadScheduledExecutor();
                pushNotificationExecutor.scheduleWithFixedDelay(new PushNotificationSchedulerTask(), config
                        .getPushNotificationConfiguration().getSchedulerTaskInitialDelay(), config
                        .getPushNotificationConfiguration().getSchedulerBatchDelayMills(), TimeUnit.MILLISECONDS);
            }

            PrivacyComplianceProvider privacyComplianceProvider = new PrivacyComplianceProviderImpl();
            DeviceManagementDataHolder.getInstance().setPrivacyComplianceProvider(privacyComplianceProvider);
            componentContext.getBundleContext().registerService(PrivacyComplianceProvider.class.getName(),
                    privacyComplianceProvider, null);

            if (log.isDebugEnabled()) {
                log.debug("Device management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void initOperationsManager() throws OperationManagementException {
        OperationManager operationManager = new OperationManagerImpl();
        DeviceManagementDataHolder.getInstance().setOperationManager(operationManager);
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        /* Registering Tenants Observer */
        BundleContext bundleContext = componentContext.getBundleContext();
        TenantCreateObserver listener = new TenantCreateObserver();
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null);

        /* Registering Device Management Startup Handler */
        DeviceManagementStartupHandler deviceManagementStartupHandler = new DeviceManagementStartupHandler();
        DeviceManagementDataHolder.getInstance().setDeviceManagementStartupHandler(deviceManagementStartupHandler);
        bundleContext.registerService(ServerStartupObserver.class.getName(), deviceManagementStartupHandler, null);

        /* Registering Device Management Service */
        DeviceManagementProviderService deviceManagementProvider = new DeviceManagementProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProvider);
        bundleContext.registerService(DeviceManagementProviderService.class.getName(), deviceManagementProvider, null);

        /* Registering Device API Client Service */
        DeviceAPIClientService deviceAPIClientService = new DeviceAPIClientServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceAPIClientService(deviceAPIClientService);
        bundleContext.registerService(DeviceAPIClientService.class.getName(), deviceAPIClientService, null);

        /* Registering Group Management Service */
        GroupManagementProviderService groupManagementProvider = new GroupManagementProviderServiceImpl();
        String defaultGroups =
                DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getDefaultGroupsConfiguration();
        List<String> groups = this.parseDefaultGroups(defaultGroups);
        for (String group : groups) {
            try {
                groupManagementProvider.createDefaultGroup(group);
            } catch (GroupManagementException e) {
                // Error is ignored, because error could be group already exist exception. Therefore it does not require
                // to print the error.
                if (log.isDebugEnabled()) {
                    log.error("Error occurred while adding the group");
                }
            }
        }

        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupManagementProvider);
        bundleContext.registerService(GroupManagementProviderService.class.getName(), groupManagementProvider, null);

        /* Registering Tenant Configuration Management Service */
        PlatformConfigurationManagementService
                tenantConfiguration = new PlatformConfigurationManagementServiceImpl();
        bundleContext.registerService(PlatformConfigurationManagementService.class.getName(), tenantConfiguration, null);

        /* Registering Notification Service */
        NotificationManagementService notificationManagementService
                = new NotificationManagementServiceImpl();
        bundleContext.registerService(NotificationManagementService.class.getName(), notificationManagementService, null);

        /* Registering Report Service */
        ReportManagementService reportManagementService = new ReportManagementServiceImpl();
        bundleContext.registerService(ReportManagementService.class.getName(), reportManagementService, null);

        /* Registering Tag Management Service */
        TagManagementProviderService tagManagementProviderService = new TagManagementProviderServiceImpl();
        bundleContext.registerService(TagManagementProviderService.class.getName(), tagManagementProviderService, null);

        /* Registering Event Management Service */
        DeviceTypeEventManagementProviderService deviceTypeEventManagementProviderService = new DeviceTypeEventManagementProviderServiceImpl();
        bundleContext.registerService(DeviceTypeEventManagementProviderService.class.getName(), deviceTypeEventManagementProviderService, null);

        /* Registering DeviceAccessAuthorization Service */
        DeviceAccessAuthorizationService deviceAccessAuthorizationService = new DeviceAccessAuthorizationServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(deviceAccessAuthorizationService);
        bundleContext.registerService(DeviceAccessAuthorizationService.class.getName(),
                deviceAccessAuthorizationService, null);

        /* Registering GroupAccessAuthorization Service */
        GroupAccessAuthorizationService groupAccessAuthorizationService = new GroupAccessAuthorizationServiceImpl();
        DeviceManagementDataHolder.getInstance().setGroupAccessAuthorizationService(groupAccessAuthorizationService);
        bundleContext.registerService(GroupAccessAuthorizationService.class.getName(),
                groupAccessAuthorizationService, null);

        /* Registering Geo Service */
        GeoLocationProviderService geoService = new GeoLocationProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setGeoLocationProviderService(geoService);
        bundleContext.registerService(GeoLocationProviderService.class.getName(), geoService, null);

        /* Registering Metadata Service */
        MetadataManagementService metadataManagementService = new MetadataManagementServiceImpl();
        DeviceManagementDataHolder.getInstance().setMetadataManagementService(metadataManagementService);
        bundleContext.registerService(MetadataManagementService.class.getName(), metadataManagementService, null);

        /* Registering Whitelabel Service */
        try {
            WhiteLabelManagementService whiteLabelManagementService = new WhiteLabelManagementServiceImpl();
            DeviceManagementDataHolder.getInstance().setWhiteLabelManagementService(whiteLabelManagementService);
            whiteLabelManagementService.addDefaultWhiteLabelThemeIfNotExist(tenantId);
            bundleContext.registerService(WhiteLabelManagementService.class.getName(), whiteLabelManagementService, null);
        } catch (MetadataManagementException e) {
            log.error("Error occurred while initializing the white label management service", e);
        }

        /* Registering DeviceState Filter Service */
        DeviceStatusManagementService deviceStatusManagementService = new DeviceStatusManagementServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceStatusManagementService(deviceStatusManagementService);
        try {
            deviceStatusManagementService.addDefaultDeviceStatusFilterIfNotExist(tenantId);
        } catch (Throwable e) {
            log.error("Error occurred while adding default tenant device status", e);

        }
        bundleContext.registerService(DeviceStatusManagementService.class.getName(), deviceStatusManagementService, null);

        /* Registering Event Configuration Service */
        EventConfigurationProviderService eventConfigurationService = new EventConfigurationProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setEventConfigurationProviderService(eventConfigurationService);
        bundleContext.registerService(EventConfigurationProviderService.class.getName(), eventConfigurationService, null);

        OTPManagementService otpManagementService = new OTPManagementServiceImpl();
        bundleContext.registerService(OTPManagementService.class.getName(), otpManagementService, null);

        /* Registering App Management service */
        try {
            AppManagementConfigurationManager.getInstance().initConfig();
            AppManagementConfig appConfig =
                    AppManagementConfigurationManager.getInstance().getAppManagementConfig();
            bundleContext.registerService(ApplicationManagementProviderService.class.getName(),
                    new ApplicationManagerProviderServiceImpl(appConfig), null);
        } catch (ApplicationManagementException e) {
            log.error("Application management service not registered.", e);
        }

        /* Registering PermissionManager Service */
        PermissionManagerService permissionManagerService = PermissionManagerServiceImpl.getInstance();
        bundleContext.registerService(PermissionManagerService.class.getName(), permissionManagerService, null);

        DeviceInformationManager deviceInformationManager = new DeviceInformationManagerImpl();
        bundleContext.registerService(DeviceInformationManager.class, deviceInformationManager, null);
        DeviceManagementDataHolder.getInstance().setDeviceInformationManager(deviceInformationManager);

        bundleContext.registerService(SearchManagerService.class, new SearchManagerServiceImpl(), null);

        ExecutorService executorService = Executors.newFixedThreadPool(50);
        DeviceManagementDataHolder.getInstance().setEventConfigExecutors(executorService);
    }

    private void setupDeviceManagementSchema(DataSourceConfig config) throws DeviceManagementException {
        DeviceManagementSchemaInitializer initializer = new DeviceManagementSchemaInitializer(config);
        String checkSql = "select * from DM_DEVICE_TYPE";
        try {
            if (!initializer.isDatabaseStructureCreated(checkSql)) {
                log.info("Initializing device management repository database schema");
                initializer.createRegistryDatabase();
            } else {
                log.info("Device management database already exists. Not creating a new database.");
            }
        } catch (Exception e) {
            throw new DeviceManagementException(
                    "Error occurred while initializing Device Management database schema", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Device management metadata repository schema has been successfully initialized");
        }
    }

    private List<String> parseDefaultGroups(String defaultGroups) {
        List<String> defaultGroupsList = new ArrayList<>();
        if (defaultGroups != null && !defaultGroups.isEmpty()) {
            String gps[] = defaultGroups.split(",");
            if (gps.length != 0) {
                for (String group : gps) {
                    defaultGroupsList.add(group.trim());
                }
            }
        }
        return defaultGroupsList;
    }

    /**
     * Sets Device Manager service.
     *
     * @param deviceManagementService An instance of DeviceManagementService
     */
    @Reference(
            name = "device.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService")
    protected void setDeviceManagementService(DeviceManagementService deviceManagementService) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting Device Management Service Provider: '" +
                        deviceManagementService.getType() + "'");
            }
            synchronized (LOCK) {
                deviceManagers.add(deviceManagementService);
                for (PluginInitializationListener listener : listeners) {
                    listener.registerDeviceManagementService(deviceManagementService);
                }
            }
            log.info("Device Type deployed successfully : " + deviceManagementService.getType() + " for tenant "
                    + deviceManagementService.getProvisioningConfig().getProviderTenantDomain());
        } catch (Throwable e) {
            log.error("Failed to register device management service for device type" + deviceManagementService.getType() +
                    " for tenant " + deviceManagementService.getProvisioningConfig().getProviderTenantDomain(), e);
        }
    }

    /**
     * Unsets Device Management service.
     *
     * @param deviceManagementService An Instance of DeviceManagementService
     */
    protected void unsetDeviceManagementService(DeviceManagementService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Device Management Service Provider : '" +
                    deviceManagementService.getType() + "'");
        }
        for (PluginInitializationListener listener : listeners) {
            listener.unregisterDeviceManagementService(deviceManagementService);
        }
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        DeviceManagementDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Registry Service");
        }
        DeviceManagementDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Unsets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Registry Service");
        }
        DeviceManagementDataHolder.getInstance().setRegistryService(null);
    }

    /**
     * Sets HeartBeatManagementService Service.
     *
     * @param heartBeatService An instance of HeartBeatManagementService
     */
    @Reference(
            name = "heart.beat.service",
            service = io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHeartBeatService")
    protected void setHeartBeatService(HeartBeatManagementService heartBeatService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Heart Beat Service");
        }
        DeviceManagementDataHolder.getInstance().setHeartBeatService(heartBeatService);
    }

    /**
     * Unsets Registry Service.
     */
    protected void unsetHeartBeatService(HeartBeatManagementService heartBeatService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Heart Beat Service");
        }
        DeviceManagementDataHolder.getInstance().setHeartBeatService(null);
    }

    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }
        DeviceManagementDataHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        DeviceManagementDataHolder.getInstance().setConfigurationContextService(null);
    }

    @Reference(
            name = "email.sender.service",
            service = io.entgra.device.mgt.core.transport.mgt.email.sender.core.service.EmailSenderService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEmailSenderService")
    protected void setEmailSenderService(EmailSenderService emailSenderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Email Sender Service");
        }
        DeviceManagementDataHolder.getInstance().setEmailSenderService(emailSenderService);
    }

    protected void unsetEmailSenderService(EmailSenderService emailSenderService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting Email Sender Service");
        }
        DeviceManagementDataHolder.getInstance().setEmailSenderService(null);
    }


    @Reference(
            name = "device.task.service",
            service = io.entgra.device.mgt.core.device.mgt.core.task.DeviceTaskManagerService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceTaskManagerService")
    protected void setDeviceTaskManagerService(DeviceTaskManagerService deviceTaskManagerService) {
        if (log.isDebugEnabled()) {
        }
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(deviceTaskManagerService);
    }

    protected void unsetDeviceTaskManagerService(DeviceTaskManagerService deviceTaskManagerService) {
        if (log.isDebugEnabled()) {
        }
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
    }

    /**
     * sets DeviceTypeGeneratorService.
     *
     * @param deviceTypeGeneratorService An Instance of DeviceTypeGeneratorService
     */
    @Reference(
            name = "device.type.generator.service",
            service = io.entgra.device.mgt.core.device.mgt.common.spi.DeviceTypeGeneratorService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceTypeGeneratorService")
    protected void setDeviceTypeGeneratorService(DeviceTypeGeneratorService deviceTypeGeneratorService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Device DeviceTypeGeneratorService");
        }
        DeviceManagementDataHolder.getInstance().setDeviceTypeGeneratorService(deviceTypeGeneratorService);
    }

    /**
     * sets DeviceTypeGeneratorService.
     *
     * @param deviceTypeGeneratorService An Instance of DeviceTypeGeneratorService
     */
    protected void unsetDeviceTypeGeneratorService(DeviceTypeGeneratorService deviceTypeGeneratorService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Device DeviceTypeGeneratorService");
        }
        DeviceManagementDataHolder.getInstance().setDeviceTypeGeneratorService(null);
    }
}


