/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.api.service.addons;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarClient;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarGateway;
import org.wso2.carbon.device.mgt.core.traccar.core.config.TraccarConfigurationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.ENDPOINT;
import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.AUTHORIZATION;
import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.AUTHORIZATION_KEY;

public class TrackerClient implements TraccarClient {
    private static final Log log = LogFactory.getLog(TrackerClient.class);
    private static final int THREAD_POOL_SIZE = 50;
    private final OkHttpClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    final TraccarGateway traccarGateway = getTraccarGateway();
    final String endpoint = traccarGateway.getPropertyByName(ENDPOINT).getValue();
    final String authorization = traccarGateway.getPropertyByName(AUTHORIZATION).getValue();
    final String authorizationKey = traccarGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

    public TrackerClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50,30,TimeUnit.SECONDS))
                .build();
    }

    private class TrackerExecutor implements Runnable {
        final JSONObject payload;
        final String context;
        final String publisherUrl;
        private final String method;

        private TrackerExecutor(String publisherUrl, String context, JSONObject payload, String method) {
            this.payload = payload;
            this.context = context;
            this.publisherUrl = publisherUrl;
            this.method = method;
        }

        public void run() {
            RequestBody requestBody;
            Request.Builder builder = new Request.Builder();
            Request request;

            if(method=="post"){
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
                builder = builder.post(requestBody);
            }else if(method=="delete"){
                builder = builder.delete();
            }

            request = builder.url(publisherUrl + context)
                    .addHeader(authorization, authorizationKey)
                    .build();

            try {
                client.newCall(request).execute();
                if (log.isDebugEnabled()) {
                    log.debug("Successfully the request is proceed and communicated with Traccar");
                }
            } catch (IOException e) {
                log.error("Error occurred", e);

            }
        }
    }

    /**
     * Add Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException {
        try{
             JSONObject payload = new JSONObject();
            payload.put("name", deviceInfo.getDeviceName());
            payload.put("uniqueId", deviceInfo.getUniqueId());
            payload.put("status", deviceInfo.getStatus());
            payload.put("disabled", deviceInfo.getDisabled());
            payload.put("lastUpdate", deviceInfo.getLastUpdate());
            payload.put("positionId", deviceInfo.getPositionId());
            payload.put("groupId", deviceInfo.getGroupId());
            payload.put("phone", deviceInfo.getPhone());
            payload.put("model", deviceInfo.getModel());
            payload.put("contact", deviceInfo.getContact());
            payload.put("category", deviceInfo.getCategory());
            List<String> geoFenceIds = new ArrayList<>();
            payload.put("geofenceIds", geoFenceIds);
            payload.put("attributes", new JSONObject());
            String context = "8082/api/devices";
            Runnable trackerExecutor = new TrackerExecutor(endpoint, context, payload, "post");
            executor.execute(trackerExecutor);
            log.info("Device successfully enorolled on traccar");
         }catch (Exception e){
             String msg="Could not enroll traccar device";
             log.error(msg, e);
             throw new TraccarConfigurationException(msg, e);
         }
    }

    /**
     * Add Device GPS Location operation.
     * @param deviceInfo  with DeviceIdentifier, Timestamp, Lat, Lon, Bearing, Speed, ignition
     */
    public void updateLocation(TraccarPosition deviceInfo) throws TraccarConfigurationException {
         try{
             String context = "5055/?id="+deviceInfo.getDeviceIdentifier()+"&timestamp="+deviceInfo.getTimestamp()+
                     "&lat="+deviceInfo.getLat()+"&lon="+deviceInfo.getLon()+"&bearing="+deviceInfo.getBearing()+
                     "&speed="+deviceInfo.getSpeed()+"&ignition=true";
             Runnable trackerExecutor = new TrackerExecutor(endpoint, context, null, "get");
             executor.execute(trackerExecutor);
             log.info("Device GPS location added on traccar");
         }catch (Exception e){
             String msg="Could not add GPS location";
             log.error(msg, e);
             throw new TraccarConfigurationException(msg, e);
         }
    }

    /**
     * Add Device GPS Location operation.
     * @param deviceId
     * @return device info
     * @throws TraccarConfigurationException Failed while add Traccar Device location operation
     */
    @Override
    public String getDeviceByDeviceIdentifier(String deviceId) throws TraccarConfigurationException {
        try {
            String context = "8082/api/devices?uniqueId="+ deviceId;
            Runnable trackerExecutor = new TrackerExecutor(endpoint, context, null, "get");
            executor.execute(trackerExecutor);
            Request request = new Request.Builder()
                    .url(endpoint+context)
                    .addHeader(authorization, authorizationKey)
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            log.info("Device info found");
            return result;
        } catch (IOException e) {
            String msg="Could not find device information";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        }
    }

    /**
     * Dis-enroll a Device operation.
     * @param deviceInfo  identified via deviceIdentifier
     * @throws TraccarConfigurationException Failed while dis-enroll a Traccar Device operation
     */
    public void disDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException {
        try{
            String result = getDeviceByDeviceIdentifier(deviceInfo.getDeviceIdentifier());
            String jsonData ="{"+ "\"geodata\": "+ result+ "}";

            log.info("======================");
            log.info("result");
            log.info(result);
            log.info(deviceInfo.getDeviceIdentifier());
            log.info("===========================");
            JSONObject obj = new JSONObject(jsonData);
            JSONArray geodata = obj.getJSONArray("geodata");
            JSONObject jsonResponse = geodata.getJSONObject(0);

            String context = "8082/api/devices/"+jsonResponse.getInt("id");
            Runnable trackerExecutor = new TrackerExecutor(endpoint, context, null, "delete");
            executor.execute(trackerExecutor);
            log.info("Device successfully dis-enrolled");
        }catch (JSONException e){
            String msg = "Could not find the device infomation to dis-enroll the device";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg);
        }catch (TraccarConfigurationException ex){
            String msg = "Could not find the device infomation to dis-enroll the device";
            log.error(msg, ex);
            throw new TraccarConfigurationException(msg, ex);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addGroup(TraccarGroups groupInfo) throws TraccarConfigurationException {
        try{
            JSONObject payload = new JSONObject();
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = "8082/api/groups";
            Runnable trackerExecutor = new TrackerExecutor(endpoint, context, payload, "post");
            executor.execute(trackerExecutor);
            log.info("Group successfully on traccar");
        }catch (Exception e){
            String msg="Could not add a traccar group";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        }
    }

    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(
                TraccarHandlerConstants.GATEWAY_NAME);
    }
}
