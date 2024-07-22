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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.impl;

import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscription;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionFilterCriteria;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionMetadata;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionResponse;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionStatistics;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionStatisticDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.SubscriptionManagementHelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    private static final Log log = LogFactory.getLog(UserBasedSubscriptionManagementHelperServiceImpl.class);

    private UserBasedSubscriptionManagementHelperServiceImpl() {
    }

    public static UserBasedSubscriptionManagementHelperServiceImpl getInstance() {
        return UserBasedSubscriptionManagementHelperServiceImpl.UserBasedSubscriptionManagementHelperServiceImplHolder.INSTANCE;
    }

    @Override
    public SubscriptionResponse getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        List<DeviceSubscriptionDTO> deviceSubscriptionDTOS;
        int deviceCount = 0;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            ConnectionManagerUtil.openDBConnection();
            List<Integer> deviceIdsOwnByUser = getDeviceIdsOwnByUser(subscriptionInfo.getIdentifier());

            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationDTO == null) {
                String msg = "Application not found for the release UUID: " + subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            String deviceSubscriptionStatus = SubscriptionManagementHelperUtil.getDeviceSubscriptionStatus(subscriptionInfo);
            DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria = subscriptionInfo.getDeviceSubscriptionFilterCriteria();
            DeviceManagementProviderService deviceManagementProviderService = HelperUtil.getDeviceManagementProviderService();
            List<String> dbSubscriptionStatus = SubscriptionManagementHelperUtil.getDBSubscriptionStatus(subscriptionInfo.getDeviceSubscriptionStatus());

            if (Objects.equals(SubscriptionMetadata.DeviceSubscriptionStatus.NEW, deviceSubscriptionStatus)) {
                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIdsOwnByUser, null,
                        null, deviceSubscriptionFilterCriteria.getTriggeredBy(), -1, -1);

                List<Integer> deviceIdsOfSubscription = deviceSubscriptionDTOS.stream().
                        map(DeviceSubscriptionDTO::getDeviceId).collect(Collectors.toList());

                for (Integer deviceId : deviceIdsOfSubscription) {
                    deviceIdsOwnByUser.remove(deviceId);
                }
                List<Integer> paginatedNewDeviceIds = deviceManagementProviderService.getDevicesInGivenIdList(deviceIdsOwnByUser,
                        new PaginationRequest(offset, limit));
                deviceSubscriptionDTOS = paginatedNewDeviceIds.stream().map(DeviceSubscriptionDTO::new).collect(Collectors.toList());

                deviceCount = deviceIdsOwnByUser.size();
            } else {
                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIdsOwnByUser, dbSubscriptionStatus,
                        null, deviceSubscriptionFilterCriteria.getTriggeredBy(), -1, -1);

                deviceCount = SubscriptionManagementHelperUtil.getTotalDeviceSubscriptionCount(deviceSubscriptionDTOS,
                        subscriptionInfo.getDeviceSubscriptionFilterCriteria(), applicationDTO.getDeviceTypeId());
            }
            List<DeviceSubscription> deviceSubscriptions = SubscriptionManagementHelperUtil.getDeviceSubscriptionData(deviceSubscriptionDTOS,
                    subscriptionInfo.getDeviceSubscriptionFilterCriteria(), isUnsubscribe, applicationDTO.getDeviceTypeId(), limit, offset);
            return new SubscriptionResponse(subscriptionInfo.getApplicationUUID(), deviceCount, deviceSubscriptions);
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while getting device details";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public SubscriptionResponse getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }
            List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.
                    getUserSubscriptionsByAppReleaseID(applicationReleaseDTO.getId(), isUnsubscribe, tenantId, offset, limit);
            int subscriptionCount = isUnsubscribe ? subscriptionDAO.getUserUnsubscriptionCount(applicationReleaseDTO.getId(), tenantId) :
                    subscriptionDAO.getUserSubscriptionCount(applicationReleaseDTO.getId(), tenantId);
            return new SubscriptionResponse(subscriptionInfo.getApplicationUUID(), subscriptionCount, subscriptionEntities);
        } catch (DBConnectionException | ApplicationManagementDAOException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public SubscriptionStatistics getSubscriptionStatistics(SubscriptionInfo subscriptionInfo) throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            List<Integer> deviceIdsOwnByUser = getDeviceIdsOwnByUser(subscriptionInfo.getIdentifier());
            SubscriptionStatisticDTO subscriptionStatisticDTO = subscriptionDAO.
                    getSubscriptionStatistic(deviceIdsOwnByUser, null, isUnsubscribe, tenantId);
            int allDeviceCount = deviceIdsOwnByUser.size();
            return SubscriptionManagementHelperUtil.getSubscriptionStatistics(subscriptionStatisticDTO, allDeviceCount);
        } catch (DeviceManagementException | ApplicationManagementDAOException e) {
            String msg = "Error encountered while getting subscription statistics for user: " + subscriptionInfo.getIdentifier();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getDeviceIdsOwnByUser(String username) throws DeviceManagementException {
        List<Device> deviceListOwnByUser = new ArrayList<>();
        PaginationRequest paginationRequest = new PaginationRequest(-1, -1);
        paginationRequest.setOwner(username);
        paginationRequest.setStatusList(Arrays.asList("ACTIVE", "INACTIVE", "UNREACHABLE"));
        PaginationResult ownDeviceIds = HelperUtil.getDeviceManagementProviderService().
                getAllDevicesIdList(paginationRequest);
        if (ownDeviceIds.getData() != null) {
            deviceListOwnByUser.addAll((List<Device>) ownDeviceIds.getData());
        }
        return deviceListOwnByUser.stream().map(Device::getId).collect(Collectors.toList());
    }

    private static class UserBasedSubscriptionManagementHelperServiceImplHolder {
        private static final UserBasedSubscriptionManagementHelperServiceImpl INSTANCE
                = new UserBasedSubscriptionManagementHelperServiceImpl();
    }
}
