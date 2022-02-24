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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarClient;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDeviceInfo;
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

    public String updateLocation(TraccarDeviceInfo deviceInfo) throws IOException{
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

    public String addDevice(TraccarDeviceInfo deviceInfo) throws IOException{

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

    // /TODO FIX THIS WITH GET REQUEST
    public String deleteDevice(TraccarDeviceInfo deviceInfo){

        //Retrieve the traccar Gateway by passing the Gateway name
        TraccarGateway traccarGateway = getTraccarGateway();

        //Retrieve the properties in the Traccar Gateway by passing the property name
        String endpoint = traccarGateway.getPropertyByName(MAIN_ENDPOINT).getValue();
        String authorization = traccarGateway.getPropertyByName(AUTHORIZATION).getValue();
        String authorizationKey = traccarGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

        /*
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
          .url("endpoint+"/devices/"+deviceInfo)
          .method("DELETE", body)
          .addHeader(authorization, authorizationKey)
          .build();
        Response response = client.newCall(request).execute();
        */

        return "";
    }
    
    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(TraccarHandlerConstants.GATEWAY_NAME);
    }
}
