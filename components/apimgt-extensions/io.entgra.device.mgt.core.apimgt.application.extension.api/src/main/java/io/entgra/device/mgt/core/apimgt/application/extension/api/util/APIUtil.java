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

package io.entgra.device.mgt.core.apimgt.application.extension.api.util;

import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.StringTokenizer;

/**
 * This class provides utility functions used by REST-API.
 */
public class APIUtil {

    private static Log log = LogFactory.getLog(APIUtil.class);
    private static final String DEFAULT_CDMF_API_TAG = "device_management";
    private static final String DEFAULT_AGENT_API_TAG = "device_agent";
    private static final String DEFAULT_CERT_API_TAG = "scep_management";
    private static final String DEFAULT_APP_MGT_TAG = "application_management";
    private static final String DEFAULT_APP_MGT_REVIEW_MGT_TAG = "review_management";
    private static final String DEFAULT_APP_MGT_SUB_MGT_TAG = "subscription_management";
    private static final String DEFAULT_ANALYTICS_ARTIFACT_TAG = "analytics_artifacts_management";
    private static final String DEFAULT_TRANSPORT_MGT_TAG = "transport_management";
    private static final String DEFAULT_ENTERPRISE_TAG= "androidforwork";
    private static final String DEFAULT_ANALYTICS_MGT_TAG= "analytics_management";

    private static final String DEFAULT_DEVICE_ORGANIZATION_MGT_TAG= "device_organization_management";

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static String getTenantDomainOftheUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return threadLocalCarbonContext.getTenantDomain();
    }

    public static APIManagementProviderService getAPIManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        APIManagementProviderService apiManagementProviderService =
                (APIManagementProviderService) ctx.getOSGiService(APIManagementProviderService.class, null);
        if (apiManagementProviderService == null) {
            String msg = "API management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return apiManagementProviderService;
    }

    public static RealmService getRealmService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService =
                (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    public static DeviceManagementProviderService getDeviceManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static String[] getDefaultTags() throws DeviceManagementException {
        String[] allowedApisTags = new String[1];
        allowedApisTags[0] = DEFAULT_CDMF_API_TAG;
        return allowedApisTags;
    }

    public static List<String> getAllowedApisTags() throws DeviceManagementException {
        List<String> allowedApisTags = getDeviceManagementProviderService().getAvailableDeviceTypes();
        allowedApisTags.add(DEFAULT_CDMF_API_TAG);
        allowedApisTags.add(DEFAULT_CERT_API_TAG);
        allowedApisTags.add(DEFAULT_AGENT_API_TAG);
        allowedApisTags.add(DEFAULT_APP_MGT_TAG);
        allowedApisTags.add(DEFAULT_APP_MGT_REVIEW_MGT_TAG);
        allowedApisTags.add(DEFAULT_APP_MGT_SUB_MGT_TAG);
        allowedApisTags.add(DEFAULT_ANALYTICS_ARTIFACT_TAG);
        allowedApisTags.add(DEFAULT_TRANSPORT_MGT_TAG);
        allowedApisTags.add(DEFAULT_ANALYTICS_MGT_TAG);
        allowedApisTags.add(DEFAULT_DEVICE_ORGANIZATION_MGT_TAG);
        // In an environment only super tenant should be capable of calling this API tag
        if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() ==
                MultitenantConstants.SUPER_TENANT_ID) {
            allowedApisTags.add(DEFAULT_ENTERPRISE_TAG);
        }
        return allowedApisTags;
    }
}
