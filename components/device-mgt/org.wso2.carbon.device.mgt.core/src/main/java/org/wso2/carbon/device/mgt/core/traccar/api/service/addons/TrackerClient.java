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
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
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

public class TrackerClient implements TraccarClient {
    private static final Log log = LogFactory.getLog(TrackerClient.class);
    private static final int THREAD_POOL_SIZE = 50;
    private final OkHttpClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    final TraccarGateway traccarGateway = getTraccarGateway();
    final String endpoint = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.ENDPOINT).getValue();
    final String authorization = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION).getValue();
    final String authorizationKey = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION_KEY).getValue();
    final String defaultPort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.DEFAULT_PORT).getValue();
    final String locationUpdatePort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.LOCATION_UPDATE_PORT).getValue();

    private final TrackerDAO trackerGroupDAO;
    private final TrackerDAO trackerDeviceDAO;

    public TrackerClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50,30,TimeUnit.SECONDS))
                .build();
        this.trackerDeviceDAO = GroupManagementDAOFactory.getTrackerDAO();
        this.trackerGroupDAO = DeviceManagementDAOFactory.getTrackerDAO();
    }

    private class TrackerExecutor implements Runnable {
        final int id;
        final int tenantId;
        final JSONObject payload;
        final String context;
        final String publisherUrl;
        private final String method;
        private final String type;

        private TrackerExecutor(int id, int tenantId, String publisherUrl, String context, JSONObject payload,
                                String method, String type) {
            this.id = id;
            this.tenantId = tenantId;
            this.payload = payload;
            this.context = context;
            this.publisherUrl = publisherUrl;
            this.method = method;
            this.type = type;
        }

        public void run() {
            RequestBody requestBody;
            Request.Builder builder = new Request.Builder();
            Request request;
            Response response;

            if(method==TraccarHandlerConstants.Methods.POST){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.post(requestBody);
            }if(method==TraccarHandlerConstants.Methods.PUT){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.put(requestBody);
            }else if(method==TraccarHandlerConstants.Methods.DELETE){
                builder = builder.delete();
            }

            request = builder.url(publisherUrl + context).addHeader(authorization, authorizationKey).build();

            try {
                response = client.newCall(request).execute();

                if(method==TraccarHandlerConstants.Methods.POST){
                    String result = response.body().string();
                    JSONObject obj = new JSONObject(result);
                    int traccarId = obj.getInt("id");

                    if(type==TraccarHandlerConstants.Types.DEVICE){
                        try {
                            DeviceManagementDAOFactory.beginTransaction();
                            trackerDeviceDAO.addTraccarDevice(traccarId, id, tenantId);
                            DeviceManagementDAOFactory.commitTransaction();
                        } catch (TransactionManagementException e) {
                            DeviceManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred establishing the DB connection .";
                            log.error(msg, e);
                        } catch (TrackerManagementDAOException e) {
                            DeviceManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred while mapping traccarDeviceId with deviceId .";
                            log.error(msg, e);
                        } finally {
                            DeviceManagementDAOFactory.closeConnection();
                        }
                    }else if(type==TraccarHandlerConstants.Types.GROUP){
                        try {
                            GroupManagementDAOFactory.beginTransaction();
                            trackerGroupDAO.addTraccarGroup(traccarId, id, tenantId);
                            GroupManagementDAOFactory.commitTransaction();
                        } catch (TransactionManagementException e) {
                            GroupManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred establishing the DB connection .";
                            log.error(msg, e);
                        } catch (TrackerManagementDAOException e) {
                            GroupManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred while mapping traccarGroupId with groupId .";
                            log.error(msg, e);
                        } finally {
                            GroupManagementDAOFactory.closeConnection();
                        }
                    }
                }
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
    public void addDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException {
        try{
            JSONObject payload = payload(deviceInfo);
            String context = defaultPort+"/api/devices";
      Runnable trackerExecutor =
          new TrackerExecutor( deviceInfo.getId(),tenantId, endpoint, context, payload,
              TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
        }catch (Exception e){
             String msg="Could not enroll traccar device";
             log.error(msg, e);
             throw new TraccarConfigurationException(msg, e);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException {
        try{
            JSONObject payload = payload(deviceInfo);
            String context = defaultPort+"/api/devices";
            Runnable trackerExecutor = new TrackerExecutor(deviceInfo.getId(), tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
        }catch (Exception e){
            String msg="Could not enroll traccar device";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        }
    }

    private JSONObject payload(TraccarDevice deviceInfo){
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
        return payload;
    }

    /**
     * Add Device GPS Location operation.
     * @param deviceInfo  with DeviceIdentifier, Timestamp, Lat, Lon, Bearing, Speed, ignition
     */
    public void updateLocation(TraccarPosition deviceInfo) throws TraccarConfigurationException {
         try{
             String context = locationUpdatePort+"/?id="+deviceInfo.getDeviceIdentifier()+"&timestamp="+deviceInfo.getTimestamp()+
                     "&lat="+deviceInfo.getLat()+"&lon="+deviceInfo.getLon()+"&bearing="+deviceInfo.getBearing()+
                     "&speed="+deviceInfo.getSpeed()+"&ignition=true";
             Runnable trackerExecutor = new TrackerExecutor(0, 0, endpoint, context, null,
                     TraccarHandlerConstants.Methods.GET, TraccarHandlerConstants.Types.DEVICE);
             executor.execute(trackerExecutor);
             log.info("Device GPS location added on traccar");
         }catch (Exception e){
             String msg="Could not add GPS location";
             log.error(msg, e);
             throw new TraccarConfigurationException(msg, e);
         }
    }

    /**
     * Dis-enroll a Device operation.
     * @param traccarDeviceId  identified via deviceIdentifier
     * @throws TraccarConfigurationException Failed while dis-enroll a Traccar Device operation
     */
    public void disEndrollDevice(int traccarDeviceId, int tenantId) throws TraccarConfigurationException {
        try{
            String context = defaultPort+"/api/devices/"+traccarDeviceId;
            Runnable trackerExecutor = new TrackerExecutor(traccarDeviceId, tenantId, endpoint, context, null,
                    TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
            log.info("Device successfully dis-enrolled");
        }catch (JSONException e){
            String msg = "Could not find the device information to dis-enroll the device";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws TraccarConfigurationException {
        try{
            JSONObject payload = new JSONObject();
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups";
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
            log.info("Group successfully added on traccar");
        }catch (Exception e){
            String msg="Could not add a traccar group";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateGroup(TraccarGroups groupInfo, int traccarGroupId, int groupId, int tenantId) throws TraccarConfigurationException {
        try{
            JSONObject payload = new JSONObject();
            payload.put("id", traccarGroupId);
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups/"+traccarGroupId;
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
            log.info("Group successfully updated on traccar");
        }catch (Exception e){
            String msg="Could not update the traccar group";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param traccarGroupId
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void deleteGroup(int traccarGroupId, int tenantId) throws TraccarConfigurationException {
        try{
            String context = defaultPort+"/api/groups/"+traccarGroupId;
            Runnable trackerExecutor = new TrackerExecutor(traccarGroupId, tenantId, endpoint, context,
                    null, TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }catch (JSONException e){
            String msg = "Could not find the device information to dis-enroll the device";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg);
        }
    }

    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(
                TraccarHandlerConstants.GATEWAY_NAME);
    }
}
