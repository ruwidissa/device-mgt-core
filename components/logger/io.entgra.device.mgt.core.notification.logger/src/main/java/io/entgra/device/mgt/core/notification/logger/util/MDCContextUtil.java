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
package io.entgra.device.mgt.core.notification.logger.util;

import io.entgra.device.mgt.core.notification.logger.*;
import org.apache.log4j.MDC;

public final class MDCContextUtil {

    public static void populateDeviceMDCContext(final DeviceLogContext mdcContext) {
        if (mdcContext.getDeviceName() != null) {
            MDC.put("DeviceName", mdcContext.getDeviceName());
        }
        if (mdcContext.getDeviceType() != null) {
            MDC.put("DeviceType", mdcContext.getDeviceType());
        }
        if (mdcContext.getOperationCode() != null) {
            MDC.put("OperationCode", mdcContext.getOperationCode());
        }
        if (mdcContext.getTenantID() != null) {
            MDC.put("TenantId", mdcContext.getTenantID());
        }
    }

    public static void populateUserMDCContext(final UserLogContext mdcContext) {
        if (mdcContext.getUserName() != null) {
            MDC.put("UserName", mdcContext.getUserName());
        }
        if (mdcContext.getUserEmail() != null) {
            MDC.put("UserEmail", mdcContext.getUserEmail());
        }
        if (mdcContext.getMetaInfo() != null) {
            MDC.put("MetaInfo", mdcContext.getMetaInfo());
        }
        if (mdcContext.getTenantID() != null) {
            MDC.put("TenantId", mdcContext.getTenantID());
        }
        if (mdcContext.isUserRegistered()) {
            MDC.put("IsUserRegistered", "Registered");
        }
        if (mdcContext.isDeviceRegisterged()) {
            MDC.put("IsDeviceRegistered", mdcContext.isDeviceRegisterged());
        }
        if (mdcContext.getTenantDomain() != null) {
            MDC.put("TenantDomain", mdcContext.getTenantDomain());
        }

    }

    public static void populatePolicyMDCContext(final PolicyLogContext mdcContext) {
        if (mdcContext.getPolicyName() != null) {
            MDC.put("PolicyName", mdcContext.getPolicyName());
        }
        if (mdcContext.getPayload() != null) {
            MDC.put("Payload", mdcContext.getPayload());
        }
        if (mdcContext.getActionTag() != null) {
            MDC.put("ActionTag", mdcContext.getActionTag());
        }
        if (mdcContext.getUserName() != null) {
            MDC.put("UserName", mdcContext.getUserName());
        }
        if (mdcContext.getTenantDomain() != null) {
            MDC.put("TenantDomain", mdcContext.getTenantDomain());
        }
        if (mdcContext.getTenantID() != null) {
            MDC.put("TenantId", mdcContext.getTenantID());
        }
    }

    public static void populateAppInstallMDCContext(final AppInstallLogContext mdcContext) {
        if (mdcContext.getAppId() != null) {
            MDC.put("AppId", mdcContext.getAppId());
        }
        if (mdcContext.getAppName() != null) {
            MDC.put("AppName", mdcContext.getAppName());
        }
        if (mdcContext.getAppType() != null) {
            MDC.put("AppType", mdcContext.getAppType());
        }
        if (mdcContext.getSubType() != null) {
            MDC.put("SubType", mdcContext.getSubType());
        }
        if (mdcContext.getDevice() != null) {
            MDC.put("Device", mdcContext.getDevice());
        }
        if (mdcContext.getTenantDomain() != null) {
            MDC.put("TenantDomain", mdcContext.getTenantDomain());
        }
        if (mdcContext.getTenantId() != null) {
            MDC.put("TenantId", mdcContext.getTenantId());
        }
        if (mdcContext.getUserName() != null) {
            MDC.put("UserName", mdcContext.getUserName());
        }
        if (mdcContext.getAction() != null) {
            MDC.put("Action", mdcContext.getAction());
        }
    }

    public static void populateDeviceConnectivityMDCContext(final DeviceConnectivityLogContext mdcContext) {
        if (mdcContext.getDeviceId() != null) {
            MDC.put("DeviceId", mdcContext.getDeviceId());
        }
        if (mdcContext.getDeviceType() != null) {
            MDC.put("DeviceType", mdcContext.getDeviceType());
        }
        if (mdcContext.getOperationCode() != null) {
            MDC.put("OperationCode", mdcContext.getOperationCode());
        }
        if (mdcContext.getTenantDomain() != null) {
            MDC.put("TenantDomain", mdcContext.getTenantDomain());
        }
        if (mdcContext.getTenantId() != null) {
            MDC.put("TenantId", mdcContext.getTenantId());
        }
        if (mdcContext.getUserName() != null) {
            MDC.put("UserName", mdcContext.getUserName());
        }
        if (mdcContext.getActionTag() != null) {
            MDC.put("ActionTag", mdcContext.getActionTag());
        }
    }

    public static void populateDeviceEnrolmentMDCContext(final DeviceEnrolmentLogContext mdcContext) {
        if (mdcContext.getDeviceId() != null) {
            MDC.put("DeviceId", mdcContext.getDeviceId());
        }
        if (mdcContext.getDeviceType() != null) {
            MDC.put("DeviceType", mdcContext.getDeviceType());
        }
        if (mdcContext.getOwner() != null) {
            MDC.put("Owner", mdcContext.getOwner());
        }
        if (mdcContext.getOwnership() != null) {
            MDC.put("Ownership", mdcContext.getOwnership());
        }
        if (mdcContext.getTenantID() != null) {
            MDC.put("TenantId", mdcContext.getTenantID());
        }
        if (mdcContext.getTenantDomain() != null) {
            MDC.put("TenantDomain", mdcContext.getTenantDomain());
        }
        if (mdcContext.getUserName() != null) {
            MDC.put("UserName", mdcContext.getUserName());
        }
    }
}


