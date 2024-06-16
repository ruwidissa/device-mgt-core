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
package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.ContextMetadata;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util.FCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class FCMNotificationStrategy implements NotificationStrategy {

    private static final Log log = LogFactory.getLog(FCMNotificationStrategy.class);
    private static final String NOTIFIER_TYPE_FCM = "FCM";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String FCM_API_KEY = "fcmAPIKey";
    private static final int TIME_TO_LIVE = 2419199; // 1 second less than 28 days
    private static final int HTTP_STATUS_CODE_OK = 200;
    private final PushNotificationConfig config;
    private static final String FCM_ENDPOINT_KEY = "FCM_SERVER_ENDPOINT";

    public FCMNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        try {
            if (NOTIFIER_TYPE_FCM.equals(config.getType())) {
                Device device = FCMDataHolder.getInstance().getDeviceManagementProviderService()
                        .getDeviceWithTypeProperties(ctx.getDeviceId());
                if(device.getProperties() != null && getFCMToken(device.getProperties()) != null) {
                    FCMUtil.getInstance().getDefaultApplication().refresh();
                    sendWakeUpCall(FCMUtil.getInstance().getDefaultApplication().getAccessToken().getTokenValue(),
                            getFCMToken(device.getProperties()));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Not using FCM notifier as notifier type is set to " + config.getType() +
                            " in Platform Configurations.");
                }
            }
        } catch (DeviceManagementException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while retrieving device information", e);
        } catch (IOException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while sending push notification", e);
        }
    }



    private void sendWakeUpCall(String accessToken, String registrationId) throws IOException,
            PushNotificationExecutionFailedException {
        OutputStream os = null;
        HttpURLConnection conn = null;

        String fcmServerEndpoint = FCMUtil.getInstance().getContextMetadataProperties().getProperty(FCM_ENDPOINT_KEY);
        if(fcmServerEndpoint == null) {
            String msg = "Encountered configuration issue. " + FCM_ENDPOINT_KEY + " is not defined";
            log.error(msg);
            throw new PushNotificationExecutionFailedException(msg);
        }

        try {
            byte[] bytes = getFCMRequest(registrationId).getBytes();
            URL url = new URL(fcmServerEndpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            os.write(bytes);

            int status = conn.getResponseCode();
            if (status != 200) {
                log.error("Response Status: " + status + ", Response Message: " + conn.getResponseMessage());
            }
        } finally {
            if (os != null) {
                os.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String getFCMRequest(String registrationId) {
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("token", registrationId);

        JsonObject fcmRequest = new JsonObject();
        fcmRequest.add("message", messageObject);

        return fcmRequest.toString();
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {

    }

    private static String getFCMToken(List<Device.Property> properties) {
        String fcmToken = null;
        for (Device.Property property : properties) {
            if (FCM_TOKEN.equals(property.getName())) {
                fcmToken = property.getValue();
                break;
            }
        }
        return fcmToken;
    }

    @Override
    public PushNotificationConfig getConfig() {
        return config;
    }
}
