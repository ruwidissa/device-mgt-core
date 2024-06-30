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
package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao;

import io.entgra.device.mgt.core.device.mgt.common.ActivityPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationResponse;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.DeviceActivity;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dto.OperationDTO;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.OperationResponseMeta;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMapping;

import java.util.List;
import java.util.Map;

public interface OperationDAO {

    int addOperation(Operation operation) throws OperationManagementDAOException;

    Operation getOperation(int operationId) throws OperationManagementDAOException;

    Operation getOperationByDeviceAndId(int enrolmentId, int operationId) throws OperationManagementDAOException;

    List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, Operation.Status status)
            throws OperationManagementDAOException;

    List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, PaginationRequest request, Operation.Status status)
            throws OperationManagementDAOException;

    List<? extends Operation> getOperationsForDevice(int enrolmentId) throws OperationManagementDAOException;

    int getOperationCountForDevice(int enrolmentId, PaginationRequest request) throws OperationManagementDAOException;

    int getOperationCountForDeviceWithDeviceIdentifier(DeviceIdentifier deviceId, PaginationRequest request) throws OperationManagementDAOException;

    List<? extends Operation> getOperationsForDevice(int enrolmentId, PaginationRequest request) throws OperationManagementDAOException;

    List<? extends Operation> getOperationsForDeviceByDeviceIdentifier(DeviceIdentifier deviceId, PaginationRequest request) throws OperationManagementDAOException;

    Operation getNextOperation(int enrolmentId, Operation.Status status) throws OperationManagementDAOException;

    boolean updateOperationStatus(int enrolmentId, int operationId,Operation.Status status)
            throws OperationManagementDAOException;

    void updateEnrollmentOperationsStatus(int enrolmentId, String operationCode, Operation.Status existingStatus,
                                          Operation.Status newStatus) throws OperationManagementDAOException;

    Map<Integer, Integer> getExistingNotExecutedOperationIDs(Integer[] enrolmentIds, String operationCode)
            throws OperationManagementDAOException;

    OperationResponseMeta addOperationResponse(int enrolmentId, io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation operation, String deviceId)
            throws OperationManagementDAOException;

    Activity getActivity(int operationId) throws OperationManagementDAOException;

    List<Activity> getActivityList(List<Integer> operationIds) throws OperationManagementDAOException;

    void addOperationResponseLarge(OperationResponseMeta responseMeta,
                                   io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation operation,
                                   String deviceId) throws OperationManagementDAOException;

    Map<String, Map<String, List<OperationResponse>>> getLargeOperationResponsesInBulk(List<Integer> operationIds) throws OperationManagementDAOException;

    void populateLargeOperationResponses(List<Activity> activities, List<Integer> largeResponseIDs) throws OperationManagementDAOException;

    Activity getActivityByDevice(int operationId, int deviceId) throws OperationManagementDAOException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementDAOException;

    List<Activity> getFilteredActivities(String operationCode, int limit, int offset) throws OperationManagementDAOException;

    int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementDAOException;

    List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset) throws OperationManagementDAOException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementDAOException;

    int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementDAOException;

    /**
     * This method provides operation mappings for given status
     *
     * @param opStatus               Operation status
     * @param pushNotificationStatus Push notification Status
     * @param limit                  Limit for no devices
     * @return Tenant based operation mappings list
     * @throws OperationManagementDAOException
     */
    Map<Integer, List<OperationMapping>> getOperationMappingsByStatus(Operation.Status opStatus, Operation.PushNotificationStatus pushNotificationStatus,
                                                                      int limit) throws OperationManagementDAOException;

    Map<Integer, List<OperationMapping>> getAllocatedOperationMappingsByStatus(Operation.Status opStatus,
            Operation.PushNotificationStatus pushNotificationStatus, int limit, int activeServerCount, int serverIndex)
            throws OperationManagementDAOException;

    List<Activity> getActivities(List<String> deviceTypes, String operationCode, long updatedSince, String operationStatus)
            throws OperationManagementDAOException;

    List<Activity> getActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException;

    int getActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException;

    List<DeviceActivity> getDeviceActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException;

    int getDeviceActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException;

    /**
     * This method is used to get the details of device subscriptions related to a UUID.
     *
     * @param operationId the operationId of the operation to be retrieved.
     * @param tenantId id of the current tenant.
     * @return {@link OperationDTO} which contains the details of device operations.
     * @throws OperationManagementDAOException if connection establishment or SQL execution fails.
     */
    OperationDTO getOperationDetailsById(int operationId, int tenantId)
            throws OperationManagementDAOException;
}
