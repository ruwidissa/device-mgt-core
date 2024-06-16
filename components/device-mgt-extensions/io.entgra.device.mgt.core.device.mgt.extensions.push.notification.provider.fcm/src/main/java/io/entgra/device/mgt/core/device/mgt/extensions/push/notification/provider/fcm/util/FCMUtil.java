package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util;

import com.google.auth.oauth2.GoogleCredentials;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.ContextMetadata;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.FCMNotificationStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class FCMUtil {

    private static final Log log = LogFactory.getLog(FCMUtil.class);
    private static volatile FCMUtil instance;
    private static GoogleCredentials defaultApplication;
    private static final String FCM_SERVICE_ACCOUNT_PATH = CarbonUtils.getCarbonHome() + File.separator +
            "repository" + File.separator + "resources" + File.separator + "service-account.json";
    private static final String[] FCM_SCOPES = { "https://www.googleapis.com/auth/firebase.messaging" };
    private Properties contextMetadataProperties;

    private FCMUtil() {
        initContextConfigs();
        initDefaultOAuthApplication();
    }

    private void initDefaultOAuthApplication() {
        if (defaultApplication == null) {
            Path serviceAccountPath = Paths.get(FCM_SERVICE_ACCOUNT_PATH);
            try {
                defaultApplication = GoogleCredentials.
                        fromStream(Files.newInputStream(serviceAccountPath)).
                        createScoped(FCM_SCOPES);
            } catch (IOException e) {
                log.error("Fail to initialize default OAuth application for FCM communication");
                throw new IllegalStateException(e);
            }
        }
    }

    private void initContextConfigs() {
        PushNotificationConfiguration pushNotificationConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getPushNotificationConfiguration();
        List<ContextMetadata> contextMetadata = pushNotificationConfiguration.getContextMetadata();
        Properties properties = new Properties();
        if (contextMetadata != null) {
            for (ContextMetadata metadata : contextMetadata) {
                properties.setProperty(metadata.getKey(), metadata.getValue());
            }
        }
        contextMetadataProperties = properties;
    }

    public static FCMUtil getInstance() {
        if (instance == null) {
            synchronized (FCMUtil.class) {
                if (instance == null) {
                    instance = new FCMUtil();
                }
            }
        }
        return instance;
    }

    public GoogleCredentials getDefaultApplication() {
        return defaultApplication;
    }

    public Properties getContextMetadataProperties() {
        return contextMetadataProperties;
    }
}
