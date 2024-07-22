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
package io.entgra.device.mgt.core.application.mgt.core.dao;

import io.entgra.device.mgt.core.application.mgt.common.ExecutionStatus;
import io.entgra.device.mgt.core.application.mgt.common.dto.GroupSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionStatisticDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionsDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceOperationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;

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

    int getDeviceIdForSubId(int subId, int tenantId) throws ApplicationManagementDAOException;

    List<Integer> getOperationIdsForSubId(int subId, int tenantId) throws ApplicationManagementDAOException;

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


    /**
     * Retrieves app details by operation id.
     *
     * @param operationId ID of the operation which app details needs to be retrieved
     * @param tenantId ID of tenant
     * @return {@link Activity}
     * @throws ApplicationManagementDAOException if error occurred while retrieving the app details
     */
    Activity getOperationAppDetails(int operationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete Operation mapping details of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteOperationMappingByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete device subscriptions of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteDeviceSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete group subscriptions of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteGroupSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete role subscriptions of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteRoleSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete user subscriptions of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteUserSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete scheduled subscription details of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteScheduledSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of group subscriptions related to a appReleaseId.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link GroupSubscriptionDTO} which contains the details of group subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment fails.
     */
    List<SubscriptionEntity> getGroupsSubscriptionDetailsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset, int limit)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of user subscriptions related to a appReleaseId.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link SubscriptionsDTO} which contains the details of subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<SubscriptionEntity> getUserSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                 int offset, int limit) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of role subscriptions related to a appReleaseId.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link SubscriptionsDTO} which contains the details of subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<SubscriptionEntity>  getRoleSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset, int limit)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of device subscriptions related to a appReleaseId.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link DeviceSubscriptionDTO} which contains the details of device subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<DeviceSubscriptionDTO> getDeviceSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset, int limit)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of device subscriptions related to a UUID.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param deviceId the deviceId of the device that need to get operation details.
     * @param tenantId id of the current tenant.
     * @return {@link DeviceOperationDTO} which contains the details of device subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<DeviceOperationDTO> getSubscriptionOperationsByAppReleaseIDAndDeviceID(int appReleaseId, int deviceId, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of device subscriptions related to a UUID.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param actionStatus Status of the action
     * @param actionType type of the action
     * @param actionTriggeredBy subscribed by
     * @param deviceIds deviceIds deviceIds to retrieve data.
     * @return {@link DeviceOperationDTO} which contains the details of device subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<DeviceSubscriptionDTO> getSubscriptionDetailsByDeviceIds(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                  List<Integer> deviceIds, List<String> actionStatus, String actionType,
                                                                  String actionTriggeredBy, int limit, int offset) throws ApplicationManagementDAOException;
    int getDeviceSubscriptionCount(int appReleaseId, boolean unsubscribe, int tenantId,
                                   List<Integer> deviceIds, List<String> actionStatus, String actionType,
                                   String actionTriggeredBy) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the details of device subscriptions related to a UUID.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param unsubscribe the Status of the subscription.
     * @param tenantId id of the current tenant.
     * @param actionStatus Status of the action
     * @param actionType type of the action
     * @param actionTriggeredBy subscribed by
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link DeviceOperationDTO} which contains the details of device subscriptions.
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    List<DeviceSubscriptionDTO> getAllSubscriptionsDetails(int appReleaseId, boolean unsubscribe, int tenantId, List<String> actionStatus, String actionType,
                                                           String actionTriggeredBy, int offset, int limit) throws ApplicationManagementDAOException;

    int getAllSubscriptionsCount(int appReleaseId, boolean unsubscribe, int tenantId,
                              List<String> actionStatus, String actionType, String actionTriggeredBy)
            throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of all subscription types related to a UUID.
     *
     * @param appReleaseId the appReleaseId of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getAllSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of all unsubscription types related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getAllUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of device subscriptions related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getDeviceSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of device unsubscription related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getDeviceUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of group subscriptions related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getGroupSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of group unsubscription related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getGroupUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of role subscriptions related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getRoleSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of role unsubscription related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getRoleUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of user subscriptions related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getUserSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * This method is used to get the counts of user unsubscription related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int getUserUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException;

    SubscriptionStatisticDTO getSubscriptionStatistic(List<Integer> deviceIds, String subscriptionType, boolean isUnsubscribed,
                                                      int tenantId) throws ApplicationManagementDAOException;
    /**
     * This method is used to get the counts of devices related to a UUID.
     *
     * @param appReleaseId the UUID of the application release.
     * @param tenantId id of the current tenant.
     * @param actionStatus categorized status.
     * @param actionTriggeredFrom type of the action.
     * @return {@link int} which contains the count of the subscription type
     * @throws ApplicationManagementDAOException if connection establishment or SQL execution fails.
     */
    int countSubscriptionsByStatus(int appReleaseId, int tenantId, String actionStatus, String actionTriggeredFrom) throws ApplicationManagementDAOException;
}
