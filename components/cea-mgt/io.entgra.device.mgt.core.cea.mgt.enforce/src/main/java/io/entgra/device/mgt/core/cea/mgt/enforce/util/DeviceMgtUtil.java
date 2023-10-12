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

package io.entgra.device.mgt.core.cea.mgt.enforce.util;

import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncDevice;
import io.entgra.device.mgt.core.cea.mgt.enforce.internal.EnforcementServiceComponentDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeviceMgtUtil {
    private static final Log log = LogFactory.getLog(DeviceMgtUtil.class);

    public static List<ActiveSyncDevice> getEnrolledActiveSyncDevicesSince(Date since)
            throws DeviceManagementException, UserStoreException {
        DeviceManagementProviderService deviceManagementProviderService = getDeviceManagementProviderService();
        if (deviceManagementProviderService == null) {
            String msg = "Device management provider service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        List<Device> devices = deviceManagementProviderService.getEnrolledDevicesSince(since);
        if (devices == null) {
            return new ArrayList<>();
        }
        return DeviceMgtUtil.constructActiveSyncDeviceList(devices);

    }

    public static List<ActiveSyncDevice> getEnrolledActiveSyncDevicesPriorTo(Date priorTo)
            throws DeviceManagementException, UserStoreException {
        DeviceManagementProviderService deviceManagementProviderService = getDeviceManagementProviderService();
        if (deviceManagementProviderService == null) {
            String msg = "Device management provider service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        List<Device> devices = deviceManagementProviderService.getEnrolledDevicesPriorTo(priorTo);
        if (devices == null) {
            return new ArrayList<>();
        }
        return DeviceMgtUtil.constructActiveSyncDeviceList(devices);
    }

    private static DeviceManagementProviderService getDeviceManagementProviderService() {
        return EnforcementServiceComponentDataHolder.getInstance().getDeviceManagementProviderService();
    }

    private static UserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = EnforcementServiceComponentDataHolder.getInstance().getRealmService();
        if (realmService == null) {
            String msg = "Realm service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
    }

    private static String getIdentity(String owner, UserStoreManager userStoreManager)
            throws UserStoreException {
        return userStoreManager.getUserClaimValue(owner, Constants.EMAIL_CLAIM_URI, null);
    }

    private static List<ActiveSyncDevice> constructActiveSyncDeviceList(List<Device> devices)
            throws UserStoreException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<ActiveSyncDevice> activeSyncDevices = new ArrayList<>();
        UserStoreManager userStoreManager = getUserStoreManager(tenantId);
        if (userStoreManager == null) {
            String msg = "Retrieved null for user store manager";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        // filter out the android devices since android devices are resolved from the active sync server
        devices = devices.stream().filter(device -> !Objects.equals(device.getType(), Constants.DEVICE_TYPE_ANDROID)).
                collect(Collectors.toList());

        for (Device device : devices) {
            activeSyncDevices.add(mapToActiveSyncDevice(device, userStoreManager));
        }

        return activeSyncDevices;
    }

    public static ActiveSyncDevice mapToActiveSyncDevice(Device device, UserStoreManager userStoreManager)
            throws UserStoreException {
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        ActiveSyncDevice activeSyncDevice = new ActiveSyncDevice();
        activeSyncDevice.setUserPrincipalName(DeviceMgtUtil.getIdentity(enrolmentInfo.getOwner(), userStoreManager));
        if (!Objects.equals(device.getType(), Constants.DEVICE_TYPE_ANDROID)) {
            for (Device.Property property : device.getProperties()) {
                if (property != null && Objects.equals(property.getName(), Constants.DEVICE_PROPERTY_EAS_ID)) {
                    activeSyncDevice.setDeviceId(property.getValue());
                }
            }
        }
        return activeSyncDevice;
    }
}
