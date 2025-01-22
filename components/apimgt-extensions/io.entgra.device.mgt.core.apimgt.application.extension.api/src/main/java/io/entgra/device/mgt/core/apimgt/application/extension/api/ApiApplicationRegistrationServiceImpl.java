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

package io.entgra.device.mgt.core.apimgt.application.extension.api;

import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.core.apimgt.application.extension.api.util.APIUtil;
import io.entgra.device.mgt.core.apimgt.application.extension.api.util.RegistrationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;


public class ApiApplicationRegistrationServiceImpl implements ApiApplicationRegistrationService {
    private static final Log log = LogFactory.getLog(ApiApplicationRegistrationServiceImpl.class);

    @Path("register/tenants")
    @POST
    public Response register(@QueryParam("tenantDomain") String tenantDomain,
                             @QueryParam("applicationName") String applicationName) {
        String authenticatedTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(authenticatedTenantDomain)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() == -1) {
                String msg = "Invalid tenant domain : " + tenantDomain;
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(msg).build();
            }

            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);

            ApiApplicationProfile apiApplicationProfile = new ApiApplicationProfile();
            apiApplicationProfile.setApplicationName(applicationName);
            apiApplicationProfile.setTags(APIUtil.getDefaultTags());
            apiApplicationProfile.setGrantTypes("");


            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            ApiApplicationKey apiApplicationKey =
                    apiManagementProviderService.registerApiApplication(apiApplicationProfile);
            return Response.status(Response.Status.CREATED).entity(apiApplicationKey.toString()).build();
        } catch (APIManagerException e) {
            String msg = "Error occurred while registering an application '" + applicationName + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UserStoreException e) {
            String msg = "Failed to retrieve the tenant" + tenantDomain + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Failed to retrieve the device service";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Application profile contains invalid attributes";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnexpectedResponseException e) {
            String msg = "Unexpected error encountered while registering api application " + applicationName;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Path("register")
    @POST
    public Response register(RegistrationProfile registrationProfile) {
        try {
            if ((registrationProfile.getTags() != null && registrationProfile.getTags().length != 0)) {
                if (!new HashSet<>(APIUtil.getAllowedApisTags()).containsAll(Arrays.asList(registrationProfile.getTags()))) {
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity("APIs(Tags) are not allowed to this" +
                            " user."
                    ).build();
                }
            }

            if (!StringUtils.isBlank(registrationProfile.getTokenType())) {
                try {
                    Enum.valueOf(ApiApplicationProfile.TOKEN_TYPE.class, registrationProfile.getTokenType());
                } catch (IllegalArgumentException e) {
                    String msg =
                            "Can not find a token type associated with provided token type " + registrationProfile.getTokenType();
                    log.error(msg, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
            } else {
                registrationProfile.setTokenType(ApiApplicationProfile.TOKEN_TYPE.JWT.toString());
            }

            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();

            ApiApplicationProfile apiApplicationProfile = new ApiApplicationProfile();
            apiApplicationProfile.setApplicationName(registrationProfile.getApplicationName());
            apiApplicationProfile.setTags(registrationProfile.getTags());
            apiApplicationProfile.setCallbackUrl(registrationProfile.getCallbackUrl());
            apiApplicationProfile.setGrantTypes(String.join(" ", registrationProfile.getSupportedGrantTypes()));
            apiApplicationProfile.setTokenType(Enum.valueOf(ApiApplicationProfile.TOKEN_TYPE.class,
                    registrationProfile.getTokenType()));
            ApiApplicationKey apiApplicationKey =
                    apiManagementProviderService.registerApiApplication(apiApplicationProfile);
            return Response.status(Response.Status.CREATED).entity(apiApplicationKey).build();
        } catch (DeviceManagementException e) {
            String msg =
                    "Error encountered while retrieving allowed api tags for registering api application " + registrationProfile.getApplicationName();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (BadRequestException e) {
            String msg =
                    "Received bad request for registering api application " + registrationProfile.getApplicationName();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnexpectedResponseException e) {
            String msg =
                    "Received unexpected response when registering the api application " + registrationProfile.getApplicationName();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (APIManagerException e) {
            String msg =
                    "Error occurred while registering an application " + registrationProfile.getApplicationName() +
                            " with apis '"
                            + StringUtils.join(registrationProfile.getTags(), ",") + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
