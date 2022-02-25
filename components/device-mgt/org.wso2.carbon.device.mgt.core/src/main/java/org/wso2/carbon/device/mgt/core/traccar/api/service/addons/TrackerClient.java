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

import com.google.gson.Gson;
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
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarGateway;
import org.wso2.carbon.device.mgt.core.traccar.core.config.TraccarConfigurationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.ENDPOINT;
import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.AUTHORIZATION;
import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.AUTHORIZATION_KEY;
import static org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants.MAIN_ENDPOINT;

public class TrackerClient implements TraccarClient {
    private static final Log log = LogFactory.getLog(TrackerClient.class);

    public Request getDeviceByDeviceIdentifier(String deviceId) {
        //device identifier matches with traccar uniqueId
        //Retrieve the traccar Gateway by passing the Gateway name
        TraccarGateway traccarGateway = getTraccarGateway();

        //Retrieve the properties in the Traccar Gateway by passing the property name
        String endpoint = traccarGateway.getPropertyByName(MAIN_ENDPOINT).getValue();
        String authorization = traccarGateway.getPropertyByName(AUTHORIZATION).getValue();
        String authorizationKey = traccarGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(endpoint+"/devices?uniqueId="+deviceId)
                .method("GET", null)
                .addHeader(authorization, authorizationKey)
                .build();
        return request;
    }

    public String updateLocation(TraccarPosition deviceInfo) throws IOException {
        //Retrieve the traccar Gateway by passing the Gateway name
        TraccarGateway traccarGateway = getTraccarGateway();

        //Retrieve the properties in the Traccar Gateway by passing the property name
        String endpoint = traccarGateway.getPropertyByName(ENDPOINT).getValue();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(endpoint+"id="+deviceInfo.getDeviceIdentifier()+
                        "&timestamp="+deviceInfo.getTimestamp()+
                        "&lat="+deviceInfo.getLat()+"&lon="+deviceInfo.getLon()+
                        "&bearing="+deviceInfo.getBearing()+"&speed="+deviceInfo.getSpeed()+"&ignition=true")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
        log.info(String.valueOf(response));
        return String.valueOf(response);
    }

    public String addDevice(TraccarDevice deviceInfo) throws IOException{
        //Retrieve the traccar Gateway by passing the Gateway name
        TraccarGateway traccarGateway = getTraccarGateway();

        //Retrieve the properties in the Traccar Gateway by passing the property name
        String endpoint = traccarGateway.getPropertyByName(MAIN_ENDPOINT).getValue();
        String authorization = traccarGateway.getPropertyByName(AUTHORIZATION).getValue();
        String authorizationKey = traccarGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject data = new JSONObject();
        data.put("name", deviceInfo.getDeviceName());
        data.put("uniqueId", deviceInfo.getUniqueId());
        data.put("status", deviceInfo.getStatus());
        data.put("disabled", deviceInfo.getDisabled());
        data.put("lastUpdate", deviceInfo.getLastUpdate());
        data.put("positionId", deviceInfo.getPositionId());
        data.put("groupId", deviceInfo.getGroupId());
        data.put("phone", deviceInfo.getPhone());
        data.put("model", deviceInfo.getModel());
        data.put("contact", deviceInfo.getContact());
        data.put("category", deviceInfo.getCategory());
        List<String> geofenceIds = new ArrayList<>();
        data.put("geofenceIds", geofenceIds);
        data.put("attributes", new JSONObject());

        RequestBody body = RequestBody.create(mediaType, data.toString());
        Request request = new Request.Builder()
                .url(endpoint+"/devices")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader(authorization, authorizationKey)
                .build();
        Response response = client.newCall(request).execute();

        return String.valueOf(response);
    }

    public String disDevice(TraccarDevice deviceInfo) throws IOException {

        //Retrieve the traccar Gateway by passing the Gateway name
        TraccarGateway traccarGateway = getTraccarGateway();

        //Retrieve the properties in the Traccar Gateway by passing the property name
        String endpoint = traccarGateway.getPropertyByName(MAIN_ENDPOINT).getValue();
        String authorization = traccarGateway.getPropertyByName(AUTHORIZATION).getValue();
        String authorizationKey = traccarGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

        OkHttpClient client = new OkHttpClient();
        Request deviceDetails = getDeviceByDeviceIdentifier(deviceInfo.getDeviceIdentifier());
        Response response = client.newCall(deviceDetails).execute();

        String result = response.body().string();
        String jsonData ="{"+ "\"geodata\": "+ result+ "}";

        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONArray geodata = obj.getJSONArray("geodata");
            JSONObject jsonResponse = geodata.getJSONObject(0);

            OkHttpClient client1 = new OkHttpClient();
            Request request1 = new Request.Builder()
                    .url(endpoint+"/devices/"+jsonResponse.getInt("id")).delete()
                    .addHeader(authorization, authorizationKey).build();
            Response response1 = client1.newCall(request1).execute();
            log.info(String.valueOf(response1));
            return String.valueOf(response1);
        } catch (JSONException e) {
            log.info("Delete Error "+e);
            return String.valueOf(e);
        }
    }
    
    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(
                TraccarHandlerConstants.GATEWAY_NAME);
    }
}
