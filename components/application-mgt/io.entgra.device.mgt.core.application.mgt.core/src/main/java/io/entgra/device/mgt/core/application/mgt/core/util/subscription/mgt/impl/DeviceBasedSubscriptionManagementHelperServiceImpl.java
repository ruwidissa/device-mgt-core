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
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionMetadata;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionResponse;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionStatistics;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.SubscriptionManagementHelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeviceBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    private static final Log log = LogFactory.getLog(DeviceBasedSubscriptionManagementHelperServiceImpl.class);

    private DeviceBasedSubscriptionManagementHelperServiceImpl() {
    }

    public static DeviceBasedSubscriptionManagementHelperServiceImpl getInstance() {
        return DeviceBasedSubscriptionManagementHelperServiceImpl.DeviceBasedSubscriptionManagementHelperServiceImplHolder.INSTANCE;
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
                deviceSubscriptionDTOS = subscriptionDAO.getAllSubscriptionsDetails(applicationReleaseDTO.
                                getId(), isUnsubscribe, tenantId, null, null,
                        deviceSubscriptionFilterCriteria.getTriggeredBy(), -1, -1);

                List<Integer> deviceIdsOfSubscription = deviceSubscriptionDTOS.stream().
                        map(DeviceSubscriptionDTO::getDeviceId).collect(Collectors.toList());

                List<Integer> newDeviceIds = deviceManagementProviderService.getDevicesNotInGivenIdList(deviceIdsOfSubscription,
                        new PaginationRequest(offset, limit));

                deviceSubscriptionDTOS = newDeviceIds.stream().map(DeviceSubscriptionDTO::new).collect(Collectors.toList());

                deviceCount = deviceManagementProviderService.getDeviceCountNotInGivenIdList(deviceIdsOfSubscription);
            } else {
                deviceSubscriptionDTOS = subscriptionDAO.getAllSubscriptionsDetails(applicationReleaseDTO.
                        getId(), isUnsubscribe, tenantId, dbSubscriptionStatus, null,
                        deviceSubscriptionFilterCriteria.getTriggeredBy(), -1, -1);

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
        return new SubscriptionResponse(subscriptionInfo.getApplicationUUID(), Collections.emptyList());
    }

    @Override
    public SubscriptionStatistics getSubscriptionStatistics(SubscriptionInfo subscriptionInfo)
            throws ApplicationManagementException {
        return null;
    }

    private static class DeviceBasedSubscriptionManagementHelperServiceImplHolder {
        private static final DeviceBasedSubscriptionManagementHelperServiceImpl INSTANCE
                = new DeviceBasedSubscriptionManagementHelperServiceImpl();
    }

}
