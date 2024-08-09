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

import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util.FCMUtil;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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


    /**
     * Send FCM message to the FCM server to initiate the push notification
     * @param accessToken Access token to authenticate with the FCM server
     * @param registrationId Registration ID of the device
     * @throws IOException If an error occurs while sending the request
     * @throws PushNotificationExecutionFailedException If an error occurs while sending the push notification
     */
    private void sendWakeUpCall(String accessToken, String registrationId) throws IOException,
            PushNotificationExecutionFailedException {
        String fcmServerEndpoint = FCMUtil.getInstance().getContextMetadataProperties()
                .getProperty(FCM_ENDPOINT_KEY);
        if(fcmServerEndpoint == null) {
            String msg = "Encountered configuration issue. " + FCM_ENDPOINT_KEY + " is not defined";
            log.error(msg);
            throw new PushNotificationExecutionFailedException(msg);
        }

        RequestBody fcmRequest = getFCMRequest(registrationId);
        Request request = new Request.Builder()
                .url(fcmServerEndpoint)
                .post(fcmRequest)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try (Response response = FCMUtil.getInstance().getHttpClient().newCall(request).execute()) {
            if (log.isDebugEnabled()) {
                log.debug("FCM message sent to the FCM server. Response code: " + response.code()
                        + " Response message : " + response.message());
            }
            if(!response.isSuccessful()) {
                String msg = "Response Status: " + response.code() + ", Response Message: " + response.message();
                log.error(msg);
                throw new IOException(msg);
            }
        }
    }

    /**
     * Get the FCM request as a JSON string
     * @param registrationId Registration ID of the device
     * @return FCM request as a JSON string
     */
    private static RequestBody getFCMRequest(String registrationId) {
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("token", registrationId);

        JsonObject fcmRequest = new JsonObject();
        fcmRequest.add("message", messageObject);

        return RequestBody.create(fcmRequest.toString(), okhttp3.MediaType.parse("application/json"));
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
