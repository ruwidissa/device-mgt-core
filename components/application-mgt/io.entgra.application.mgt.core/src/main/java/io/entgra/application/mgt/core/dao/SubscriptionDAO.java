/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.core.dao;

import io.entgra.application.mgt.common.ExecutionStatus;
import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;
import java.util.Map;

/**
 * This interface provides the list of operations that are supported with subscription database.
 *
 */
public interface SubscriptionDAO {

    void addDeviceSubscription(String subscribedBy, List<Integer> deviceIds, String subscribedFrom,
            String installStatus, int releaseId, int tenantId ) throws ApplicationManagementDAOException;

    void updateDeviceSubscription(String updateBy, List<Integer> deviceIds, String action, String actionTriggeredFrom,
            String installStatus, int releaseId, int tenantId) throws ApplicationManagementDAOException;

    void addOperationMapping (int operationId, List<Integer> deviceSubscriptionId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between user and the application which the application is subscribed on. This mapping will be
     * added when an app subscription triggered to the user.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param users list of user names of the users whose devices are subscribed to the application
     * @param releaseId id of the {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException If unable to add a mapping between user and application
     */
    void addUserSubscriptions(int tenantId, String subscribedBy, List<String> users, int releaseId, String action)
            throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between role and the application which the application is subscribed on. This mapping will be
     * added when an app subscription triggered to the role.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param roles list of role names of the roles whose devices are subscribed to the application
     * @param releaseId id of the {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException If unable to add a mapping between role and application
     */
    void addRoleSubscriptions(int tenantId, String subscribedBy, List<String> roles, int releaseId, String action)
            throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between group and the application which the application is subscribed on. This mapping will be
     * added when an app subscription triggered to the user.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param groups list of group names of the groups whose devices are subscribed to the application
     * @param releaseId id of the {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException If unable to add a mapping between group and application
     */
    void addGroupSubscriptions(int tenantId, String subscribedBy, List<String> groups, int releaseId, String action)
            throws ApplicationManagementDAOException;

    List<DeviceSubscriptionDTO> getDeviceSubscriptions(int appReleaseId, int tenantId, String actionStatus, String action) throws
            ApplicationManagementDAOException;

    Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> deviceIds, int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException;

    List<String> getAppSubscribedUserNames(List<String> users, int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException;

    List<String> getAppSubscribedRoleNames(List<String> roles, int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException;

    List<String> getAppSubscribedGroupNames(List<String> groups, int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException;

    void updateSubscriptions(int tenantId, String updateBy, List<String> paramList,
            int releaseId, String subType, String action) throws ApplicationManagementDAOException;

    List<Integer> getDeviceSubIds(List<Integer> deviceIds, int applicationReleaseId, int tenantId)
            throws ApplicationManagementDAOException;

    List<Integer> getDeviceSubIdsForOperation(int operationId, int deviceID, int tenantId)
            throws ApplicationManagementDAOException;

    boolean updateDeviceSubStatus(int deviceId, List<Integer> deviceSubIds, String status, int tenantcId)
            throws ApplicationManagementDAOException;

    /**
     * Creates a scheduled subscription entry in the data store.
     *
     * @param subscriptionDTO {@link ScheduledSubscriptionDTO} which contains the details of the subscription
     * @throws ApplicationManagementDAOException if error occurred while creating an entry in the data store.
     */
    boolean createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO) throws ApplicationManagementDAOException;

    /**
     * Updates the existing entry of a scheduled subscription.
     *
     * @param id          id of the existing subscription
     * @param scheduledAt scheduled time
     * @param scheduledBy username of the user who scheduled the subscription
     * @throws ApplicationManagementDAOException if error occurred while updating the entry
     */
    boolean updateScheduledSubscription(int id, long scheduledAt, String scheduledBy)
            throws ApplicationManagementDAOException;

    /**
     * Marks A list of given scheduled subscription as deleted.
     *
     * @param subscriptionIdList list of ids of the subscriptions to delete
     * @throws ApplicationManagementDAOException if error occurred while deleting the subscription
     */
    boolean deleteScheduledSubscription(List<Integer> subscriptionIdList) throws ApplicationManagementDAOException;

    /**
     * Update the status of an existing subscription.
     *
     * @param id     id of the existing subscription
     * @param status changed status {@see {@link ExecutionStatus}}
     * @throws ApplicationManagementDAOException if error occurs while changing the status of the subscription
     */
    boolean updateScheduledSubscriptionStatus(int id, ExecutionStatus status) throws ApplicationManagementDAOException;

    /**
     * Retrieve a list of scheduled subscriptions of a given state
     *
     * @param status  status of the subscriptions
     * @param deleted is the subscription marked as deleted
     * @return list of {@link ScheduledSubscriptionDTO}
     * @throws ApplicationManagementDAOException if error occurred while retrieving the subscriptions
     */
    List<ScheduledSubscriptionDTO> getScheduledSubscriptionByStatus(ExecutionStatus status, boolean deleted)
            throws ApplicationManagementDAOException;

    /**
     * Gets the UUID of an application if the app is subscribed in entgra store
     *
     * @param id          id of the device
     * @param packageName package name of the application
     * @throws SubscriptionManagementException if error
     *                                         occurred while cleaning up subscriptions.
     */
    String getUUID(int id, String packageName) throws ApplicationManagementDAOException;

    /**
     * Retrieves a list of subscriptions that are not executed on the scheduled time.
     *
     * @return list of {@link ScheduledSubscriptionDTO}
     * @throws ApplicationManagementDAOException if error occurred while retrieving the subscriptions.
     */
    List<ScheduledSubscriptionDTO> getNonExecutedSubscriptions() throws ApplicationManagementDAOException;

    /**
     * Retrieves a subscription by taskName which is in the <code>ExecutionStatus.PENDING</code> state.
     *
     * @param taskName name of the task to retrieve.
     * @return {@link ScheduledSubscriptionDTO}
     * @throws ApplicationManagementDAOException if error occurred while retrieving the subscription
     */
    ScheduledSubscriptionDTO getPendingScheduledSubscriptionByTaskName(String taskName) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of users
     *
     * @param tenantId id of the current tenant
     * @param offsetValue offset value for get paginated result
     * @param limitValue limit value for get paginated result
     * @param appReleaseId id of the application release.
     * @return subscribedUsers - list of app subscribed users.
     * @throws {@link ApplicationManagementDAOException} if connections establishment fails.
     */
    List<String> getAppSubscribedUsers(int offsetValue, int limitValue, int appReleaseId,
                                       int tenantId)
            throws ApplicationManagementDAOException;

    int getSubscribedUserCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of roles
     *
     * @param tenantId id of the current tenant
     * @param offsetValue offset value for get paginated request.
     * @param limitValue limit value for get paginated request.
     * @param appReleaseId id of the application release.
     * @return subscribedRoles - list of app subscribed roles.
     * @throws {@link ApplicationManagementDAOException} if connections establishment fails.
     */
    List<String> getAppSubscribedRoles(int offsetValue, int limitValue, int appReleaseId,
                                       int tenantId)
            throws ApplicationManagementDAOException;

    int getSubscribedRoleCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of subscribed groups
     *
     * @param tenantId id of the current tenant
     * @param offsetValue offset value for get paginated request.
     * @param limitValue limit value for get paginated request.
     * @param appReleaseId id of the application release.
     * @return subscribedGroups - list of app subscribed groups.
     * @throws {@link ApplicationManagementDAOException} if connections establishment fails.
     */
    List<String> getAppSubscribedGroups(int offsetValue, int limitValue, int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException;

    int getSubscribedGroupCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of subscribed groups
     *
     * @param tenantId id of the current tenant
     * @param appReleaseId id of the application release..
     * @param subtype application subscribed type.
     * @return subscribedDevices - list of app subscribed devices under the subtype.
     * @throws {@link ApplicationManagementDAOException} if connections establishment fails.
     */
    List<Integer> getAppSubscribedDevicesForGroups(int appReleaseId, String subtype, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the currently installed version for given app release id
     * @param appId id of the application
     * @param deviceIdList id list of devices
     * @return Map with device id as a key and currently installed version as value
     * @throws {@link ApplicationManagementDAOException} if connections establishment fails.
     */
    Map<Integer,String> getCurrentInstalledAppVersion(int appId, List<Integer> deviceIdList, String installedVersion) throws ApplicationManagementDAOException;
}
