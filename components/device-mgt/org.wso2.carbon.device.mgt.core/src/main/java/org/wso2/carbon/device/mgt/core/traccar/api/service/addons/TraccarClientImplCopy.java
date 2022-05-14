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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.exceptions.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.traccar.api.service.TraccarClient;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarUser;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarGateway;
import org.wso2.carbon.device.mgt.core.traccar.common.util.TraccarUtil;
import org.wso2.carbon.device.mgt.core.traccar.core.config.TraccarConfigurationManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TraccarClientImplCopy /*implements TraccarClient*/ {
    private static final Log log = LogFactory.getLog(TraccarClientImplCopy.class);
    private static final int THREAD_POOL_SIZE = 50;
    private final OkHttpClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    final TraccarGateway traccarGateway = getTraccarGateway();
    final String endpoint = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.ENDPOINT).getValue();
    final String authorization = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION).getValue();
    final String authorizationKey = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION_KEY).getValue();
    final String defaultPort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.DEFAULT_PORT).getValue();
    final String locationUpdatePort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.LOCATION_UPDATE_PORT).getValue();

    final String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    private final TrackerDAO trackerDAO;

    public TraccarClientImplCopy() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50,30,TimeUnit.SECONDS))
                .build();
        this.trackerDAO = TrackerManagementDAOFactory.getTrackerDAO();
    }

    private class TrackerExecutor implements Runnable {
        final int deviceId;
        final int groupId;
        final int tenantId;
        final JSONObject payload;
        final String context;
        final String publisherUrl;
        private final String method;
        private final String type;

        private TrackerExecutor(int id, int tenantId, String publisherUrl, String context, JSONObject payload,
                                String method, String type) {
            this.deviceId = id;
            this.groupId = id;
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

            if(Objects.equals(method, TraccarHandlerConstants.Methods.POST)){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.post(requestBody);
            }else if(Objects.equals(method, TraccarHandlerConstants.Methods.PUT)){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.put(requestBody);
            }else if(Objects.equals(method, TraccarHandlerConstants.Methods.DELETE)){
                builder = builder.delete();
            }

            request = builder.url(publisherUrl + context).addHeader(authorization, authorizationKey).build();
            String msg;
            try {
                response = client.newCall(request).execute();
                if(Objects.equals(method, TraccarHandlerConstants.Methods.POST)){
                    String result = response.body().string();
                    log.info(result);
                    if(Objects.equals(type, TraccarHandlerConstants.Types.PERMISSION)){
                        if(result.equals("")){
                            msg ="Successfully the device is assigned to the user";
                        }else{
                            msg = "Error occurred while fetching users .";
                        }
                        log.info(msg);
                    }else if(result.charAt(0)=='{'){
                        JSONObject obj = new JSONObject(result);
                        if (obj.has("id")){
                            int traccarId = obj.getInt("id");
                            try {
                                TrackerManagementDAOFactory.beginTransaction();
                                if(Objects.equals(type, TraccarHandlerConstants.Types.DEVICE)){
                                    trackerDAO.addTrackerDevice(traccarId, deviceId, tenantId);
                                    TrackerDeviceInfo res = trackerDAO.getTrackerDevice(deviceId, tenantId);
                                    if(res.getStatus()==0){
                                        trackerDAO.updateTrackerDeviceIdANDStatus(res.getTraccarDeviceId(), deviceId, tenantId, 1);

                                        TraccarUser traccarUser = new TraccarUser();
                                        traccarUser.setName(username);
                                        traccarUser.setLogin(username);
                                        traccarUser.setEmail(username);
                                        traccarUser.setPassword(generateRandomString(10));
                                        traccarUser.setToken(generateRandomString(32));
                                        traccarUser.setDeviceLimit(-1);

                                        log.info("=============="+new Gson().toJson(traccarUser)+"==============");
                                        //device is available
                                        //device is not available
                                        //user is available
                                        //user is not available
                                        fetchAllUsers(TraccarHandlerConstants.Types.USER_CREATE_WITH_INSERT_DEVICE, traccarUser, traccarId);
                                    }
                                }else if(Objects.equals(type, TraccarHandlerConstants.Types.GROUP)){
                                    trackerDAO.addTrackerGroup(traccarId, groupId, tenantId);
                                    TrackerGroupInfo res = trackerDAO.getTrackerGroup(groupId, tenantId);
                                    if(res.getStatus()==0){
                                        trackerDAO.updateTrackerGroupIdANDStatus(res.getTraccarGroupId(), groupId, tenantId, 1);
                                    }
                                }else if(Objects.equals(type, TraccarHandlerConstants.Types.USER_CREATE)){
                                    log.info("=============User inserted=============");
                                }else if(Objects.equals(type, TraccarHandlerConstants.Types.USER_CREATE_WITH_INSERT_DEVICE)){
                                    int userId = traccarId;
                                    log.info("=============User inserted and setting to create session=============");
                                    setPermission(userId, deviceId);
                                }
                                TrackerManagementDAOFactory.commitTransaction();
                            } catch (JSONException e) {
                                TrackerManagementDAOFactory.rollbackTransaction();
                                msg = "Error occurred on JSON object .";
                                log.error(msg, e);
                            } catch (TransactionManagementException e) {
                                TrackerManagementDAOFactory.rollbackTransaction();
                                msg = "Error occurred establishing the DB connection .";
                                log.error(msg, e);
                            } catch (TrackerManagementDAOException e) {
                                TrackerManagementDAOFactory.rollbackTransaction();
                                msg = null;
                                switch (type) {
                                    case TraccarHandlerConstants.Types.DEVICE:
                                        msg = "Already device with deviceId " + deviceId + " exists";
                                        break;
                                    case TraccarHandlerConstants.Types.GROUP:
                                        msg = "Already the group with groupId - " + groupId + " exists!";
                                        break;
                                    case TraccarHandlerConstants.Types.USER:
                                        msg = "Error occurred while fetching users.";
                                        break;
                                    case TraccarHandlerConstants.Types.PERMISSION:
                                        msg = "Error occurred while assigning the device to the user." + traccarId + deviceId;
                                        break;
                                }
                                log.error(msg, e);
                            } finally {
                                TrackerManagementDAOFactory.closeConnection();
                            }
                        }
                        response.close();
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully the request is proceed and communicated with Traccar");
                    }
                }else if(Objects.equals(method, TraccarHandlerConstants.Methods.GET)){
                    if(!Objects.equals(type, TraccarHandlerConstants.Types.DEVICE)){
                        response = client.newCall(request).execute();
                        String result = response.body().string();

                        JSONArray fetchAllUsers = new JSONArray(result);
                        int userAvailability = 0;
                        int userId = 0;
                        for(int i=0; i<fetchAllUsers.length();i++){
                            if(fetchAllUsers.getJSONObject(i).getString("login").equals(username)){

                                //TODO :: when user is available then assgin the device to the user

                                userAvailability=1;
                                log.info(fetchAllUsers.getJSONObject(i));
                                log.info(new Gson().toJson(fetchAllUsers.getJSONObject(i)));
                                log.info("Token: "+fetchAllUsers.getJSONObject(i).getString("token"));
                                userId = fetchAllUsers.getJSONObject(i).getInt("id");
                                break;
                            }
                        }

                        if(Objects.equals(type, TraccarHandlerConstants.Types.USER_CREATE_WITH_INSERT_DEVICE)){
                            if(userAvailability==0){
                                log.info("============");
                                log.info("Creating User");
                                TraccarUser traccarUser = (TraccarUser) payload.get("data");
                                log.info(traccarUser);
                                log.info("============");
                                createUser(traccarUser, type, deviceId);
                            }else{
                                if(userId!=0){
                                    log.info("=============");
                                    log.info("User inserted and setting to create session");
                                    log.info("=============");
                                    setPermission(userId, deviceId);
                                }else{
                                    log.info("UserId is null");
                                }
                            }
                        }else if(Objects.equals(type, TraccarHandlerConstants.Types.USER_CREATE)){
                        /*if(userAvailability==1){
                            log.info("Update User");
                            log.info(payload);
                            log.info(new Gson().toJson(payload));
                            updateUser(payload);
                            log.info("Update User");
                        }*/
                        }
                    }

                }

            } catch (IOException | TraccarConfigurationException e) {
                log.error("Couldnt connect to traccar.", e);
            }
        }
    }

    /**
     * Add Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        try {
            TrackerManagementDAOFactory.openConnection();
            TrackerDeviceInfo res = trackerDAO.getTrackerDevice(deviceInfo.getId(), tenantId);
            if(res!=null){
                String msg = "The device already exist";
                log.error(msg);
                throw new TrackerAlreadyExistException(msg);
            }
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        JSONObject payload = TraccarUtil.TraccarDevicePayload(deviceInfo);
        String context = defaultPort+"/api/devices";
        Runnable trackerExecutor = new TrackerExecutor(deviceInfo.getId(), tenantId, endpoint, context, payload,
                TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.DEVICE);
        executor.execute(trackerExecutor);
    }

    /**
     * update Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerDeviceInfo res = null;
        String msg;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerDevice(deviceInfo.getId(), tenantId);
        } catch (TrackerManagementDAOException e) {
            msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if ((res==null) || (res.getTraccarDeviceId()==0)){
            try {
                String lastUpdatedTime = String.valueOf((new Date().getTime()));
                deviceInfo.setLastUpdate(lastUpdatedTime);
                addDevice(deviceInfo, tenantId);
            } catch (TraccarConfigurationException e) {
                msg = "Error occurred while mapping with groupId";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                msg = "The group already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else if (res!=null && (res.getTraccarDeviceId()!=0 && res.getStatus()==0)){
            //update the traccarGroupId and status
            try {
                TrackerManagementDAOFactory.beginTransaction();
                trackerDAO.updateTrackerDeviceIdANDStatus(res.getTraccarDeviceId(), deviceInfo.getId(), tenantId, 1);
                TrackerManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException e) {
                msg = "Error occurred establishing the DB connection .";
                log.error(msg, e);
            } catch (TrackerManagementDAOException e) {
                msg="Could not add the traccar group";
                log.error(msg, e);
            } finally{
                TrackerManagementDAOFactory.closeConnection();
            }
        }else{
            JSONObject payload = TraccarUtil.TraccarDevicePayload(deviceInfo);
            String context = defaultPort+"/api/devices";
            Runnable trackerExecutor = new TrackerExecutor(deviceInfo.getId(), tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
        }
    }

    /*private JSONObject TraccarDevicePayload(TraccarDevice deviceInfo){
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
    }*/

    /**
     * Add Device GPS Location operation.
     * @param deviceInfo  with DeviceIdentifier, Timestamp, Lat, Lon, Bearing, Speed, ignition
     */
    public void updateLocation(TraccarDevice device, TraccarPosition deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerDeviceInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerDevice(device.getId(), tenantId);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            String msg="Could add new device location";
            log.error(msg, e);
        } finally{
            TrackerManagementDAOFactory.closeConnection();
        }

        if (res == null){
            try {
                addDevice(device, tenantId);
            } catch (TraccarConfigurationException e) {
                String msg = "Error occurred add the new device";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                String msg = "The device already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else{
            String context = locationUpdatePort+"/?id="+deviceInfo.getDeviceIdentifier()+"&timestamp="+deviceInfo.getTimestamp()+
                    "&lat="+deviceInfo.getLat()+"&lon="+deviceInfo.getLon()+"&bearing="+deviceInfo.getBearing()+
                    "&speed="+deviceInfo.getSpeed()+"&ignition=true";
            Runnable trackerExecutor = new TrackerExecutor(0, 0, endpoint, context, null,
                    TraccarHandlerConstants.Methods.GET, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
            log.info("Device GPS location added on traccar");
        }

    }

    /**
     * Dis-enroll a Device operation.
     * @param deviceId  identified via deviceIdentifier
     * @throws TraccarConfigurationException Failed while dis-enroll a Traccar Device operation
     */
    public void disEndrollDevice(int deviceId, int tenantId) throws TraccarConfigurationException {
        TrackerDeviceInfo  res = null;
        JSONObject obj = null;
        try {
            TrackerManagementDAOFactory.beginTransaction();
            res = trackerDAO.getTrackerDevice(deviceId, tenantId);
            if(res!=null){
                obj = new JSONObject(res);
                if(obj!=null){
                    trackerDAO.removeTrackerDevice(deviceId, tenantId);
                    TrackerManagementDAOFactory.commitTransaction();
                }
            }
        } catch (TransactionManagementException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred establishing the DB connection";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if(obj != null){
            String context = defaultPort+"/api/devices/"+obj.getInt("traccarDeviceId");
            Runnable trackerExecutor = new TrackerExecutor(obj.getInt("traccarDeviceId"), tenantId, endpoint, context, null,
                    TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerGroupInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
            if (res!=null){
                String msg = "The group already exit";
                log.error(msg);
                throw new TrackerAlreadyExistException(msg);
            }
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }


        log.info("response.body().string()");
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://localhost/?token=b2zNFM9CvXAaHVxaQcLw22GgCXnaluy9")
                    .method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            log.info(response.body().string());

            /*Desktop desktop = java.awt.Desktop.getDesktop();
            URI oURL = new URI("http://localhost:8085");
            desktop.browse(oURL);*/
        } catch (IOException e) {
            log.info("IOException e" +e);
        } catch (Exception e) {
            log.info("Exception e" +e );
        }
        log.info("response.body().string()");


        if (res==null){
            JSONObject payload = new JSONObject();
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups";
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    /**
     * update Traccar Group operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerGroupInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            String msg="Could not find traccar group details";
            log.error(msg, e);
        } finally{
            TrackerManagementDAOFactory.closeConnection();
        }

        if ((res==null) || (res.getTraccarGroupId()==0)){
            //add a new traccar group
            try {
                addGroup(groupInfo, groupId, tenantId);
            } catch (TraccarConfigurationException e) {
                String msg = "Error occurred while mapping with groupId";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                String msg = "The group already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else if (res!=null && (res.getTraccarGroupId()!=0 && res.getStatus()==0)){
            //update the traccargroupId and status
            try {
                TrackerManagementDAOFactory.beginTransaction();
                trackerDAO.updateTrackerGroupIdANDStatus(res.getTraccarGroupId(), groupId, tenantId, 1);
                TrackerManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException e) {
                String msg = "Error occurred establishing the DB connection .";
                log.error(msg, e);
            } catch (TrackerManagementDAOException e) {
                String msg="Could not add the traccar group";
                log.error(msg, e);
            } finally{
                TrackerManagementDAOFactory.closeConnection();
            }
        }else{
            JSONObject obj = new JSONObject(res);
            JSONObject payload = new JSONObject();
            payload.put("id", obj.getInt("traccarGroupId"));
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups/"+obj.getInt("traccarGroupId");
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupId
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void deleteGroup(int groupId, int tenantId) throws TraccarConfigurationException {
        TrackerGroupInfo res = null;
        JSONObject obj = null;
        try {
            TrackerManagementDAOFactory.beginTransaction();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
            if(res!=null){
                obj = new JSONObject(res);
                if(obj!=null){
                    trackerDAO.removeTrackerGroup(obj.getInt("id"));
                    TrackerManagementDAOFactory.commitTransaction();
                }
            }
        } catch (TransactionManagementException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred establishing the DB connection";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while mapping with groupId";
            log.error(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if(obj!=null){
            String context = defaultPort+"/api/groups/"+obj.getInt("traccarGroupId");
            Runnable trackerExecutor = new TrackerExecutor(obj.getInt("traccarGroupId"), tenantId, endpoint, context,
                    null, TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    public void fetchAllUsers(String type, TraccarUser traccarUser, int deviceId) throws TraccarConfigurationException {
        String context = defaultPort+"/api/users/";

        JSONObject payload = new JSONObject();
        payload.put("data", traccarUser);

        Runnable trackerExecutor = new TrackerExecutor(deviceId, 0, endpoint, context,
                payload, TraccarHandlerConstants.Methods.GET, type);
        executor.execute(trackerExecutor);
    }

    /*private JSONObject TraccarUserPayload(TraccarUser traccarUser){
        JSONObject payload = new JSONObject();
        payload.put("id", traccarUser.getId());
        payload.put("name", traccarUser.getName());
        payload.put("login", traccarUser.getLogin());
        payload.put("email", traccarUser.getEmail());
        payload.put("password", traccarUser.getPassword());
        payload.put("token", traccarUser.getToken());
        payload.put("administrator", traccarUser.getAdministrator());
        payload.put("deviceLimit", traccarUser.getDeviceLimit());
        payload.put("userLimit", traccarUser.getUserLimit());
        payload.put("disabled", traccarUser.getDisabled());
        payload.put("deviceReadonly", traccarUser.getDeviceReadonly());
        payload.put("readonly", traccarUser.getReadonly());
        payload.put("expirationTime", traccarUser.getExpirationTime());

        return payload;
    }*/
    public void createUser(TraccarUser traccarUser, String type, int deviceId) throws TraccarConfigurationException {
        JSONObject payload = TraccarUtil.TraccarUserPayload(traccarUser);
        
        String context = defaultPort+"/api/users";
        Runnable trackerExecutor = new TrackerExecutor(deviceId, 0, endpoint, context, payload,
                TraccarHandlerConstants.Methods.POST, type);
        executor.execute(trackerExecutor);
    }

    public void updateUser(JSONObject traccarUser) throws TraccarConfigurationException {
        /*JSONObject payload = traccarUser;
        String context = defaultPort+"/api/users";
        Runnable trackerExecutor = new TrackerExecutor(0, 0, endpoint, context, payload,
                TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.USER);
        executor.execute(trackerExecutor);*/
    }

    public void setPermission(int userId, int deviceId) throws TraccarConfigurationException {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("deviceId", deviceId);

        String context = defaultPort+"/api/permissions";
        Runnable trackerExecutor = new TrackerExecutor(deviceId, 0, endpoint, context, payload,
                TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.PERMISSION);
        executor.execute(trackerExecutor);
    }

    public String fetchAllUsers() {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(endpoint+defaultPort+"/api/users")
                .method("GET", null)
                .addHeader(authorization, authorizationKey)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return e.toString();
        }
    }

    public String fetchUserInfo(String userName) throws TraccarConfigurationException {
        String allUsers = fetchAllUsers(); //get all users
        JSONArray fetchAllUsers = new JSONArray(allUsers); //loop users
        for(int i=0; i<fetchAllUsers.length();i++){

            //if login is null then check the name or if login is not null then check the login
            if(
                    (!fetchAllUsers.getJSONObject(i).isNull("login") &&
                            fetchAllUsers.getJSONObject(i).getString("login").equals(userName)) ||
                    (fetchAllUsers.getJSONObject(i).isNull("login") &&
                            fetchAllUsers.getJSONObject(i).getString("name").equals(userName))
            ){
                return fetchAllUsers.getJSONObject(i).toString();
            }
        }

        return TraccarHandlerConstants.Types.USER_NOT_FOUND;
    }

    public String createUser(TraccarUser traccarUser) throws  TraccarConfigurationException {

        JSONObject payload = TraccarUtil.TraccarUserPayload(traccarUser);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(payload.toString(), mediaType);
        Request request = new Request.Builder()
                .url(endpoint+defaultPort+"/api/users")
                .method("POST", body)
                .addHeader(authorization, authorizationKey)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return e.toString();
        }
    }

    public String updateUser(TraccarUser traccarUser, int userId) throws  TraccarConfigurationException {

        JSONObject payload = TraccarUtil.TraccarUserPayload(traccarUser);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(payload.toString(), mediaType);
        Request request = new Request.Builder()
                .url(endpoint+defaultPort+"/api/users/"+userId)
                .method("PUT", body)
                .addHeader("Content-Type", "application/json")
                .addHeader(authorization, authorizationKey)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return e.toString();
        }
    }

    public String generateRandomString(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(
                TraccarHandlerConstants.TraccarConfig.GATEWAY_NAME);
    }
}
