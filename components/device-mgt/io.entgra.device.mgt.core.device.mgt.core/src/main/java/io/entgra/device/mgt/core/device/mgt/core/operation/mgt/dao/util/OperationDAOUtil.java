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
package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.DeviceActivity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityHolder;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityMapper;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityStatus;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationResponse;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.ConfigOperation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.PolicyOperation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.ProfileOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class OperationDAOUtil {
    private static final Log log = LogFactory.getLog(OperationDAOUtil.class);

    public static Operation convertOperation(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation operation) {

        Operation dtoOperation = null;

        if (operation.getType().equals(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Type.COMMAND)) {
            dtoOperation = new CommandOperation();
        } else if (operation.getType().equals(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Type.PROFILE)) {
            dtoOperation = new ProfileOperation();
        } else if (operation.getType().equals(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Type.POLICY)) {
            dtoOperation = new PolicyOperation();
        } else if (operation.getType().equals(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Type.CONFIG)) {
            dtoOperation = new ConfigOperation();
        } else {
            dtoOperation = new Operation();
        }

        dtoOperation.setEnabled(operation.isEnabled());
        dtoOperation.setCode(operation.getCode());

        if (operation.getType() != null) {
            dtoOperation.setType(Operation.Type.valueOf(operation.getType().toString()));
        } else {
            dtoOperation.setType(null);
        }

        dtoOperation.setCreatedTimeStamp(operation.getCreatedTimeStamp());

        if (operation.getStatus() != null) {
            dtoOperation.setStatus(Operation.Status.valueOf(operation.getStatus().toString()));
        } else {
            dtoOperation.setStatus(null);
        }

        dtoOperation.setId(operation.getId());
        dtoOperation.setPayLoad(operation.getPayLoad());
        dtoOperation.setReceivedTimeStamp(operation.getReceivedTimeStamp());
        dtoOperation.setProperties(operation.getProperties());

        if (operation.getControl() != null) {
            dtoOperation.setControl(Operation.Control.valueOf(operation.getControl().toString()));
        }

        if (operation.getInitiatedBy() != null) {
            dtoOperation.setInitiatedBy(operation.getInitiatedBy());
        }

        return dtoOperation;
    }

    public static io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation convertOperation(Operation dtoOperation) {

        io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation operation = null;

        if (dtoOperation.getType().equals(Operation.Type.COMMAND)) {
            operation = new io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation();
        } else if (dtoOperation.getType().equals(Operation.Type.PROFILE)) {
            operation = new io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation();
        } else {
            operation = new io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation();
        }
        operation.setEnabled(dtoOperation.isEnabled());
        operation.setCode(dtoOperation.getCode());

        if (dtoOperation.getType() != null) {
            operation.setType(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Type.valueOf(dtoOperation
                    .getType().toString()));
        }

        operation.setCreatedTimeStamp(dtoOperation.getCreatedTimeStamp());

        if (dtoOperation.getStatus() != null) {
            operation.setStatus(io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation.Status.valueOf(dtoOperation
                    .getStatus().toString()));
        }

        operation.setId(dtoOperation.getId());
        operation.setPayLoad(dtoOperation.getPayLoad());
        operation.setReceivedTimeStamp(dtoOperation.getReceivedTimeStamp());
        operation.setEnabled(dtoOperation.isEnabled());
        operation.setProperties(dtoOperation.getProperties());
        operation.setActivityId(dtoOperation.getActivityId());
        operation.setInitiatedBy(dtoOperation.getInitiatedBy());


        return operation;
    }

    public static OperationResponse getLargeOperationResponse(ResultSet rs) throws
                                                                       ClassNotFoundException, IOException, SQLException {
        OperationResponse response = new OperationResponse();
        if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
            Timestamp receivedTimestamp = rs.getTimestamp("RECEIVED_TIMESTAMP");
            response.setReceivedTimeStamp(new Date(receivedTimestamp.getTime()).toString());
        }
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        byte[] contentBytes;
        try {
            if (rs.getBytes("OPERATION_RESPONSE") != null) {
                contentBytes = (byte[]) rs.getBytes("OPERATION_RESPONSE");
                bais = new ByteArrayInputStream(contentBytes);
                ois = new ObjectInputStream(bais);
                response.setResponse(ois.readObject().toString());
            }
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ByteArrayOutputStream", e);
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ObjectOutputStream", e);
                }
            }
        }
        return response;
    }


    public static OperationResponse getOperationResponse(ResultSet rs) throws SQLException {
        OperationResponse response = new OperationResponse();
        if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
            Timestamp receivedTimestamp = rs.getTimestamp("RECEIVED_TIMESTAMP");
            response.setReceivedTimeStamp(new Date(receivedTimestamp.getTime()).toString());
        }
        if (rs.getString("OPERATION_RESPONSE") != null) {
            response.setResponse(rs.getString("OPERATION_RESPONSE"));
        }
        return response;
    }

    public static Operation.Type getType(String type) {
        return Operation.Type.valueOf(type);
    }

    public static void setActivityId(Operation operation, int operationId) {
        operation.setActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY + operationId);
    }

    public static String getActivityId(int operationId) {
        return DeviceManagementConstants.OperationAttributes.ACTIVITY + operationId;
    }

    public static ActivityHolder getActivityHolder(ResultSet rs) throws OperationManagementDAOException {
        Map<Integer, ActivityMapper> activityInfo = new TreeMap<>();
        List<Integer> largeResponseIDs = new ArrayList<>();
        List<Activity> activities = new ArrayList<>();
        try {
            while (rs.next()) {
                ActivityMapper activityMapper = activityInfo.get(rs.getInt("OPERATION_ID"));
                if (activityMapper != null) {
                    ActivityStatus activityStatus = activityMapper.getActivityStatusMap()
                            .get(rs.getInt("ENROLMENT_ID"));
                    if (activityStatus == null) {

                        activityStatus = new ActivityStatus();
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                        deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));

                        activityStatus.setDeviceIdentifier(deviceIdentifier);
                        activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                        if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                            activityStatus.setUpdatedTimestamp(
                                    new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                        }
                        if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                            if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                                largeResponseIDs.add(rs.getInt("OP_RES_ID"));
                            } else {
                                List<OperationResponse> operationResponses = new ArrayList<>();
                                operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                                activityStatus.setResponses(operationResponses);
                            }
                        }
                        activityMapper.getActivityStatusMap().put(rs.getInt("ENROLMENT_ID"), activityStatus);
                    } else {
                        if (rs.getInt("OP_RES_ID") != 0 && rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                            if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                                largeResponseIDs.add(rs.getInt("OP_RES_ID"));
                            } else {
                                if (activityStatus.getResponses() == null) {
                                    List<OperationResponse> operationResponses = new ArrayList<>();
                                    activityStatus.setResponses(operationResponses);
                                }
                                activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                            }
                        }
                    }
                } else {
                    Activity activity = new Activity();
                    List<ActivityStatus> statusList = new ArrayList<>();
                    ActivityStatus activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setOperationId(rs.getInt("OPERATION_ID"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(rs.getInt("OP_RES_ID"));
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(getActivityId(rs.getInt("OPERATION_ID")));

                    ActivityMapper newActivityMapper = new ActivityMapper();
                    Map<Integer, ActivityStatus> activityStatusMap = new TreeMap<>();
                    activityStatusMap.put(rs.getInt("ENROLMENT_ID"), activityStatus);

                    newActivityMapper.setActivity(activity);
                    newActivityMapper.setActivityStatusMap(activityStatusMap);
                    activityInfo.put(rs.getInt("OPERATION_ID"), newActivityMapper);
                }
            }

            activityInfo.values().forEach(holder -> {
                List<ActivityStatus> activityStatuses = new ArrayList<>(holder.getActivityStatusMap().values());
                holder.getActivity().setActivityStatus(activityStatuses);
                activities.add(holder.getActivity());
            });

            ActivityHolder activityHolder = new ActivityHolder();
            activityHolder.setActivityList(activities);
            activityHolder.setLargeResponseIDs(largeResponseIDs);
            return activityHolder;
        } catch (SQLException e) {
            String msg = "Error occurred while getting activity data from the result set.";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
    }

    public static DeviceActivity populateActivity(ResultSet rs) throws SQLException {
        DeviceActivity deviceActivity = new DeviceActivity();
        List<OperationResponse> operationResponses = new ArrayList<>();

        deviceActivity.setType(DeviceActivity.Type.valueOf(rs.getString("TYPE")));
        deviceActivity.setCreatedTimeStamp(new Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
        deviceActivity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));
        deviceActivity.setCode(rs.getString("OPERATION_CODE"));
        deviceActivity.setOperationId(rs.getInt("OPERATION_ID"));
        deviceActivity.setInitiatedBy(rs.getString("INITIATED_BY"));

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
        deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
        deviceActivity.setDeviceIdentifier(deviceIdentifier);
        deviceActivity.setStatus(DeviceActivity.Status.valueOf(rs.getString("STATUS")));
        if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
            deviceActivity.setResponses(operationResponses);
        }
        deviceActivity.setUpdatedTimestamp(new Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
        return deviceActivity;
    }

    public static Operation getOperation(ResultSet rs) throws OperationManagementDAOException, SQLException {
        Operation operation = new Operation();
        operation.setId(rs.getInt("ID"));
        operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
        operation.setCreatedTimeStamp(new Timestamp(rs.getLong("CREATED_TIMESTAMP") * 1000L).toString());
        if (rs.getLong("RECEIVED_TIMESTAMP") == 0) {
            operation.setReceivedTimeStamp("");
        } else {
            operation.setReceivedTimeStamp(
                    new Timestamp((rs.getLong("RECEIVED_TIMESTAMP") * 1000)).toString());
        }
        operation.setCode(rs.getString("OPERATION_CODE"));
        operation.setInitiatedBy(rs.getString("INITIATED_BY"));
        byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
        if (!rs.wasNull()) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(operationDetails);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                operation.setPayLoad(ois.readObject());
            } catch (IOException e) {
                String msg = "IO Error occurred while retrieving operation details";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            } catch (ClassNotFoundException e) {
                String msg = "Class not found error occurred while retrieving operation details";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred while retrieving operation details";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            }
        }
        byte[] operationProperties = rs.getBytes("OPERATION_PROPERTIES");
        if (!rs.wasNull()) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(operationProperties);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                operation.setProperties((Properties) ois.readObject());
            } catch (IOException e) {
                String msg = "IO Error occurred while retrieving operation properties";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            } catch (ClassNotFoundException e) {
                String msg = "Class not found error occurred while retrieving operation properties";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred while retrieving operation properties";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            }
        }
        OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
        return operation;
    }

    /**
     * @param blob
     * @return
     * @throws SQLException
     */
    public static JSONObject convertBlobToJsonObject(Blob blob) throws SQLException {
        String jsonString;
        try (InputStream inputStream = blob.getBinaryStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] blobBytes = outputStream.toByteArray();

            // Check if the blob data is a serialized Java object
            if (blobBytes.length > 2 && (blobBytes[0] & 0xFF) == 0xAC && (blobBytes[1] & 0xFF) == 0xED) {
                // Deserialize the object
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blobBytes))) {
                    Object obj = ois.readObject();
                    if (obj instanceof String) {
                        jsonString = (String) obj;
                    } else {
                        jsonString = new JSONObject(obj).toString();
                    }
                } catch (ClassNotFoundException e) {
                    String msg = "Failed to deserialize object from BLOB";
                    log.error(msg, e);
                    throw new SQLException(msg, e);
                }
            } else {
                // If not serialized, treat it as plain JSON string
                jsonString = new String(blobBytes, "UTF-8");
            }
        } catch (IOException e) {
            String msg = "Failed to convert BLOB to JSON string";
            log.error(msg, e);
            throw new SQLException(msg, e);
        }
        // Convert JSON string to JSONObject
        if (jsonString == null || jsonString.isEmpty()) {
            String msg = "Converted JSON string is null or empty";
            log.error(msg);
            throw new SQLException(msg);
        }
        return new JSONObject(jsonString);
    }

}
