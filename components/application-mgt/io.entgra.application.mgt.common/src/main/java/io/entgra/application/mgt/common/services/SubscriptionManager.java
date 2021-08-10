/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.ApplicationInstallResponse;
import io.entgra.application.mgt.common.ExecutionStatus;
import io.entgra.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.SubscriptionManagementException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.app.mgt.App;

import java.util.List;
import java.util.Properties;

/**
 * This interface manages all the operations related with ApplicationDTO Subscription.
 */
public interface SubscriptionManager {
    /**
     * Performs bulk subscription operation for a given application and a subscriber list.
     *  @param applicationUUID UUID of the application to subscribe/unsubscribe
     * @param params          list of subscribers. This list can be of either
     *                        {@link DeviceIdentifier} if {@param subType} is equal
     *                        to DEVICE or
     *                        {@link String} if {@param subType} is USER, ROLE or GROUP
     * @param subType         subscription type. E.g. <code>DEVICE, USER, ROLE, GROUP</code> {@see {
 * @param action          subscription action. E.g. <code>INSTALL/UNINSTALL</code> {@see {
 * @param <T>             generic type of the method.
 * @return {@link ApplicationInstallResponse}
 * @throws ApplicationManagementException if error occurs when subscribing to the given application
 * @link io.entgra.application.mgt.common.SubscriptionType}}
 * @link io.entgra.application.mgt.common.SubAction}}
     * @param properties
     */
    <T> ApplicationInstallResponse performBulkAppOperation(String applicationUUID, List<T> params, String subType,
                                                           String action, Properties properties) throws ApplicationManagementException;

    /**
     * Create an entry related to the scheduled task in the database.
     *
     * @param subscriptionDTO {@link ScheduledSubscriptionDTO} with details of the subscription
     * @throws SubscriptionManagementException if unable to create/update entry for the scheduled task
     */
    void createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO) throws SubscriptionManagementException;

    /**
     * Mark already executed, misfired and failed tasks as deleted.
     *
     * @return deleted list of subscriptions
     * @throws SubscriptionManagementException if error occurred while cleaning up subscriptions.
     */
    List<ScheduledSubscriptionDTO> cleanScheduledSubscriptions() throws SubscriptionManagementException;

    /**
     * Check app is subscribed in entgra store or not
     *
     * @param id          id of the device
     * @param packageName package name of the application
     * @throws SubscriptionManagementException if error occurred while cleaning up subscriptions.
     */
    String checkAppSubscription(int id, String packageName) throws SubscriptionManagementException;

    /**
     * Retrieves the subscription entry which is pending by task name. At a given time, there should be only a single
     * entry in the status {@code PENDING} and not marked as deleted.
     *
     * @param taskName name of the task to retrieve
     * @return {@link ScheduledSubscriptionDTO}
     * @throws SubscriptionManagementException if error occurred while retrieving the subscription details
     */
    ScheduledSubscriptionDTO getPendingScheduledSubscription(String taskName) throws SubscriptionManagementException;

    /**
     * Updates the status of a subscription.
     *
     * @param id     id of the subscription
     * @param status new status of the subscription. {@see {@link ExecutionStatus}}
     * @throws SubscriptionManagementException if error occurred while updating the status of the subscription
     */
    void updateScheduledSubscriptionStatus(int id, ExecutionStatus status) throws SubscriptionManagementException;

    /**
     * Perform google enterprise app install
     * @param applicationUUID UUID of the application to subscribe/unsubscribe
     * @param params          list of subscribers. This list can be of either
     *                        {@link org.wso2.carbon.device.mgt.common.DeviceIdentifier} if {@param subType} is equal
     *                        to DEVICE or {@link String} if {@param subType} is USER, ROLE or GROUP
     * @param subType         subscription type. E.g. <code>DEVICE, USER, ROLE, GROUP</code> {@see {
     * @param action          subscription action. E.g. <code>INSTALL/UNINSTALL</code> {@see {
     * @param <T>             generic type of the method.
     * @param requiresUpdatingExternal  should an external server be updated. Such as Google EMM APIs
     * @return {@link ApplicationInstallResponse}
     * @throws ApplicationManagementException ApplicationManagementException if error occurs when subscribing to the
     * given application
     * @link io.entgra.application.mgt.common.SubscriptionType}}
     */
    <T> void performEntAppSubscription(String applicationUUID, List<T> params, String subType, String action,
                                       boolean requiresUpdatingExternal) throws ApplicationManagementException;

    /**
     * Install given application releases for given device. If application is already installed that application skips.
     * This is used in enterprise app installing policy.
     *
     * @param deviceIdentifier Device identifiers
     * @param releaseUUID UUIs of applicatios
     * @throws ApplicationManagementException if error occurred while installing given applications into the given
     * device
     */
    void installAppsForDevice(DeviceIdentifier deviceIdentifier, List<App> apps)
            throws ApplicationManagementException;

    /***
     * This method used to get the app id ,device ids and pass them to DM service method.
     *
     * @param appUUID UUID of the application release.
     * @param request paginated request object.
     * @return deviceDetails - device details for given application release.
     * @throws {@link ApplicationManagementException} Exception of the application management
     */
    PaginationResult getAppInstalledDevices(PaginationRequest request, String appUUID) throws ApplicationManagementException;

    /***
     * This method used to get category details.
     *
     * @param appUUID UUID of the application release.
     * @param subType subscription type of the application.
     * @param offsetValue offset value for get paginated request.
     * @param limitValue limit value for get paginated request.
     * @return {@link PaginationResult} pagination result of the category details.
     * @throws {@link ApplicationManagementException} Exception of the application management
     */
    PaginationResult getAppInstalledSubscribers(int offsetValue, int limitValue, String appUUID,
                                               String subType) throws ApplicationManagementException;

    /**
     * This method is responsible to provide application subscription data for given application release UUID.
     *
     * @param request paginated request object.
     * @param actionStatus status of the operation.
     * @param action action related to the device.
     * @param appUUID application release UUID
     * @return {@link PaginationResult}
     * @throws ApplicationManagementException if offset or limit contains incorrect values, if it couldn't find an
     * application release for given UUID, if an error occurred while getting device details of subscribed device ids,
     * if an error occurred while getting subscription details of given application release UUID.
     */
    PaginationResult getAppSubscriptionDetails(PaginationRequest request, String appUUID, String actionStatus, String action)
            throws ApplicationManagementException;

    /***
     * This method is responsible to provide application subscription devices data for given application release UUID.
     * @param request  PaginationRequest object holding the data for pagination
     * @param appUUID UUID of the application release.
     * @param subType subscription type of the application(eg: GROUP, USER, ...)
     * @param subTypeName subscription type name of the application (Name of the group, Name of the user, ...).
     * @return {@link PaginationResult} pagination result of the category details.
     * @throws {@link ApplicationManagementException} Exception of the application management
     */
    PaginationResult getAppInstalledSubscribeDevices(PaginationRequest request, String appUUID, String subType,
                                                     String subTypeName) throws ApplicationManagementException;
}
