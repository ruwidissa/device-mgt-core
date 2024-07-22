/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt;

import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscription;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionFilterCriteria;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionData;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionMetadata;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionStatistics;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionStatisticDTO;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionManagementHelperUtil {

    /**
     * Retrieves device subscription data based on the provided filters.
     *
     * @param deviceSubscriptionDTOS List of DeviceSubscriptionDTO objects.
     * @param deviceSubscriptionFilterCriteria Filter criteria for device subscription.
     * @param isUnsubscribed Boolean indicating whether to filter unsubscribed devices.
     * @param deviceTypeId Device type ID.
     * @param limit Limit for pagination.
     * @param offset Offset for pagination.
     * @return List of DeviceSubscription objects.
     * @throws DeviceManagementException If an error occurs during device management.
     */
    public static List<DeviceSubscription> getDeviceSubscriptionData(List<DeviceSubscriptionDTO> deviceSubscriptionDTOS,
                                                                     DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria,
                                                                     boolean isUnsubscribed, int deviceTypeId, int limit, int offset)
            throws DeviceManagementException {
        List<Integer> deviceIds = deviceSubscriptionDTOS.stream().map(DeviceSubscriptionDTO::getDeviceId).collect(Collectors.toList());
        PaginationRequest paginationRequest = new PaginationRequest(offset, limit);
        paginationRequest.setDeviceName(deviceSubscriptionFilterCriteria.getName());
        paginationRequest.setDeviceStatus(deviceSubscriptionFilterCriteria.getDeviceStatus());
        paginationRequest.setOwner(deviceSubscriptionFilterCriteria.getOwner());
        paginationRequest.setDeviceTypeId(deviceTypeId);
        List<Device> devices = HelperUtil.getDeviceManagementProviderService().getDevicesByDeviceIds(paginationRequest, deviceIds);
        return populateDeviceData(deviceSubscriptionDTOS, devices, isUnsubscribed);
    }

    /**
     * Retrieves the total count of device subscriptions based on the provided filters.
     *
     * @param deviceSubscriptionDTOS List of DeviceSubscriptionDTO objects.
     * @param deviceSubscriptionFilterCriteria Filter criteria for device subscription.
     * @param deviceTypeId Device type ID.
     * @return int Total count of device subscriptions.
     * @throws DeviceManagementException If an error occurs during device management.
     */
    public static int getTotalDeviceSubscriptionCount(List<DeviceSubscriptionDTO> deviceSubscriptionDTOS,
                                                      DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria, int deviceTypeId)
            throws DeviceManagementException {
        List<Integer> deviceIds = deviceSubscriptionDTOS.stream().map(DeviceSubscriptionDTO::getDeviceId).collect(Collectors.toList());
        PaginationRequest paginationRequest = new PaginationRequest(-1, -1);
        paginationRequest.setDeviceName(deviceSubscriptionFilterCriteria.getName());
        paginationRequest.setDeviceStatus(deviceSubscriptionFilterCriteria.getDeviceStatus());
        paginationRequest.setOwner(deviceSubscriptionFilterCriteria.getOwner());
        paginationRequest.setDeviceTypeId(deviceTypeId);
        return HelperUtil.getDeviceManagementProviderService().getDeviceCountByDeviceIds(paginationRequest, deviceIds);
    }

    /**
     * Populates device subscription data based on the provided devices and subscription DTOs.
     *
     * @param deviceSubscriptionDTOS List of DeviceSubscriptionDTO objects.
     * @param devices List of Device objects.
     * @param isUnsubscribed Boolean indicating whether to filter unsubscribed devices.
     * @return List of DeviceSubscription objects.
     */
    private static List<DeviceSubscription> populateDeviceData(List<DeviceSubscriptionDTO> deviceSubscriptionDTOS,
                                                               List<Device> devices, boolean isUnsubscribed) {
        List<DeviceSubscription> deviceSubscriptions = new ArrayList<>();
        for (Device device : devices) {
            int idx = deviceSubscriptionDTOS.indexOf(new DeviceSubscriptionDTO(device.getId()));
            if (idx >= 0) {
                DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptionDTOS.get(idx);
                DeviceSubscription deviceSubscription = new DeviceSubscription();
                deviceSubscription.setDeviceId(device.getId());
                deviceSubscription.setDeviceIdentifier(device.getDeviceIdentifier());
                deviceSubscription.setDeviceOwner(device.getEnrolmentInfo().getOwner());
                deviceSubscription.setDeviceType(device.getType());
                deviceSubscription.setDeviceName(device.getName());
                deviceSubscription.setDeviceStatus(device.getEnrolmentInfo().getStatus().name());
                deviceSubscription.setOwnershipType(device.getEnrolmentInfo().getOwnership().name());
                deviceSubscription.setDateOfLastUpdate(new Timestamp(device.getEnrolmentInfo().getDateOfLastUpdate()));
                SubscriptionData subscriptionData = getSubscriptionData(isUnsubscribed, deviceSubscriptionDTO);
                deviceSubscription.setSubscriptionData(subscriptionData);
                deviceSubscriptions.add(deviceSubscription);
            }
        }
        return deviceSubscriptions;
    }

    /**
     * Creates a SubscriptionData object based on the provided subscription DTO.
     *
     * @param isUnsubscribed Boolean indicating whether to filter unsubscribed devices.
     * @param deviceSubscriptionDTO DeviceSubscriptionDTO object.
     * @return SubscriptionData object.
     */
    private static SubscriptionData getSubscriptionData(boolean isUnsubscribed, DeviceSubscriptionDTO deviceSubscriptionDTO) {
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTriggeredBy(isUnsubscribed ? deviceSubscriptionDTO.getUnsubscribedBy() :
                deviceSubscriptionDTO.getSubscribedBy());
        subscriptionData.setTriggeredAt(deviceSubscriptionDTO.getSubscribedTimestamp());
        subscriptionData.setDeviceSubscriptionStatus(deviceSubscriptionDTO.getStatus());
        subscriptionData.setSubscriptionType(deviceSubscriptionDTO.getActionTriggeredFrom());
        subscriptionData.setSubscriptionId(deviceSubscriptionDTO.getId());
        return subscriptionData;
    }

    /**
     * Retrieves the device subscription status based on the provided subscription info.
     *
     * @param subscriptionInfo SubscriptionInfo object.
     * @return Device subscription status.
     */
    public static String getDeviceSubscriptionStatus(SubscriptionInfo subscriptionInfo) {
        return getDeviceSubscriptionStatus(subscriptionInfo.getDeviceSubscriptionFilterCriteria().
                getFilteringDeviceSubscriptionStatus(), subscriptionInfo.getDeviceSubscriptionStatus());
    }

    /**
     * Retrieves the device subscription status based on the provided filter and status.
     *
     * @param deviceSubscriptionStatusFilter Filtered device subscription status.
     * @param deviceSubscriptionStatus Device subscription status.
     * @return Device subscription status.
     */
    public static String getDeviceSubscriptionStatus(String deviceSubscriptionStatusFilter, String deviceSubscriptionStatus) {
        return (deviceSubscriptionStatusFilter != null && !deviceSubscriptionStatusFilter.isEmpty()) ?
                deviceSubscriptionStatusFilter : deviceSubscriptionStatus;
    }

    /**
     * Retrieves subscription statistics based on the provided subscription statistics DTO and device count.
     *
     * @param subscriptionStatisticDTO SubscriptionStatisticDTO object.
     * @param allDeviceCount Total count of all devices.
     * @return SubscriptionStatistics object.
     */
    public static SubscriptionStatistics getSubscriptionStatistics(SubscriptionStatisticDTO subscriptionStatisticDTO, int allDeviceCount) {
        SubscriptionStatistics subscriptionStatistics = new SubscriptionStatistics();
        subscriptionStatistics.setCompletedPercentage(
                getPercentage(subscriptionStatisticDTO.getCompletedDeviceCount(), allDeviceCount));
        subscriptionStatistics.setPendingPercentage(
                getPercentage(subscriptionStatisticDTO.getPendingDevicesCount(), allDeviceCount));
        subscriptionStatistics.setFailedPercentage(
                getPercentage(subscriptionStatisticDTO.getFailedDevicesCount(), allDeviceCount));
        subscriptionStatistics.setNewDevicesPercentage(getPercentage((allDeviceCount -
                subscriptionStatisticDTO.getCompletedDeviceCount() -
                subscriptionStatisticDTO.getPendingDevicesCount() -
                subscriptionStatisticDTO.getFailedDevicesCount()), allDeviceCount));
        return subscriptionStatistics;
    }

    /**
     * Calculates the percentages.
     *
     * @param numerator Numerator value.
     * @param denominator Denominator value.
     * @return Calculated percentage.
     */
    public static float getPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0.0f;
        }
        return ((float) numerator / (float) denominator) * 100;
    }

    /**
     * Retrieves database subscription statuses based on the provided device subscription status.
     *
     * @param deviceSubscriptionStatus Device subscription status.
     * @return List of database subscription statuses.
     */
    public static List<String> getDBSubscriptionStatus(String deviceSubscriptionStatus) {
        return SubscriptionMetadata.deviceSubscriptionStatusToDBSubscriptionStatusMap.get(deviceSubscriptionStatus);
    }
}
