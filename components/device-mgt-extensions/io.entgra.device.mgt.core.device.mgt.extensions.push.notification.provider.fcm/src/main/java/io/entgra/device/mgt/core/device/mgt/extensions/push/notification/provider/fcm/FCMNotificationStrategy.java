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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hazelcast.aws.utility.Environment;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraDeviceConnectivityLoggerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class FCMNotificationStrategy implements NotificationStrategy {

    private static final Log log = LogFactory.getLog(FCMNotificationStrategy.class);

    private static final String NOTIFIER_TYPE_FCM = "FCM";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send";
    private static final String FCM_API_KEY = "fcmAPIKey";
    private static final int TIME_TO_LIVE = 2419199; // 1 second less that 28 days
    private static final int HTTP_STATUS_CODE_OK = 200;
    private final PushNotificationConfig config;

    public FCMNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        String token = getFcmOauthToken();
        try {
            if (NOTIFIER_TYPE_FCM.equals(config.getType())) {
                Device device = FCMDataHolder.getInstance().getDeviceManagementProviderService()
                        .getDeviceWithTypeProperties(ctx.getDeviceId());
                if(device.getProperties() != null && getFCMToken(device.getProperties()) != null) {
                    this.sendWakeUpCall(ctx.getOperation().getCode(), device, token);
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

    private String getFcmOauthToken() {
        GoogleCredentials googleCredentials = null;
        try {
            googleCredentials = GoogleCredentials
                    .fromStream(new FileInputStream("/etc/service-account.json"))
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refresh();
            if (null != googleCredentials) {
                writeLog("========= Google Credentials created " + googleCredentials.getAccessToken());
            } else {
                writeLog("========= Google Credentials is null");
            }
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            log.error("Error occurred while getting the FCM OAuth token.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {

    }

    private void sendWakeUpCall(String message, Device device, String token) throws IOException,
                                                                      PushNotificationExecutionFailedException {
        if (device.getProperties() != null) {
            writeLog("===== Calling senWakeupCall " + device);
            OutputStream os = null;
            byte[] bytes = getFCMRequest(message, getFCMToken(device.getProperties())).getBytes();

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(FCM_ENDPOINT).openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                os.write(bytes);
            } finally {
                if (os != null) {
                    os.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            int status = conn.getResponseCode();
            if (log.isDebugEnabled()) {
                log.debug("Result code: " + status + ", Message: " + conn.getResponseMessage());
            }
            if (status != HTTP_STATUS_CODE_OK) {
                throw new PushNotificationExecutionFailedException("Push notification sending failed with the HTTP " +
                        "error code '" + status + "'");
            }
        }
    }

    private static String getFCMRequest(String message, String registrationId) {
        JsonObject fcmRequest = new JsonObject();
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("token", registrationId);
        JsonObject notification = new JsonObject();
        notification.addProperty("title", "FCM Message");
        notification.addProperty("body", message);
        messageObject.add("notification", notification);
        fcmRequest.add("message", messageObject);


        /*fcmRequest.addProperty("delay_while_idle", false);
        fcmRequest.addProperty("time_to_live", TIME_TO_LIVE);
        fcmRequest.addProperty("priority", "high");

        //Add message to FCM request
        JsonObject data = new JsonObject();
        if (message != null && !message.isEmpty()) {
            data.addProperty("data", message);
            fcmRequest.add("data", data);
        }

        //Set device reg-id
        JsonArray regIds = new JsonArray();
        regIds.add(new JsonPrimitive(registrationId));

        fcmRequest.add("registration_ids", regIds);*/

        writeLog("========= FCM Request " + fcmRequest);
        return fcmRequest.toString();
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

    private static void writeLog(String message) {
        try (FileWriter fw = new FileWriter("/opt/entgra/migration/entgra-uem-ultimate-6.0.3.0/log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PushNotificationConfig getConfig() {
        return config;
    }


}
