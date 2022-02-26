/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.entgra.application.mgt.publisher.api.services.impl;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.IdentityServerList;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppWrapper;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.publisher.api.services.SPApplicationService;
import io.entgra.application.mgt.publisher.api.services.util.SPAppRequestHandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("/identity-server-applications")
public class SPApplicationServiceImpl implements SPApplicationService {
    private static final Log log = LogFactory.getLog(SPApplicationServiceImpl.class);

    @Path("/identity-servers")
    @GET
    @Override
    public Response getIdentityServers() {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            IdentityServerList identityServers = spAppManager.getIdentityServers();
            return Response.status(Response.Status.OK).entity(identityServers).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/identity-servers/{id}")
    @GET
    @Override
    public Response getIdentityServer(@PathParam("id") int id) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            IdentityServer identityServer = spAppManager.getIdentityServer(id);
            return Response.status(Response.Status.OK).entity(identityServer).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/{identity-server-id}/service-providers")
    @GET
    @Override
    public Response getServiceProviders(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset,
                                        @PathParam("identity-server-id") int identityServerId) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            SPApplicationListResponse applications = SPAppRequestHandlerUtil.
                    retrieveSPApplications(identityServerId, limit, offset);
            spAppManager.addExistingApps(identityServerId, applications.getApplications());
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/applications")
    @POST
    @Override
    public Response attachApps(@PathParam("identity-server-id") int identityServerId,
                               @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds) {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        try {
            validateServiceProviderUID(identityServerId, serviceProviderId);
            spApplicationManager.validateAttachAppsRequest(identityServerId, appIds);
            spApplicationManager.attachSPApplications(identityServerId, serviceProviderId, appIds);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while attaching apps to service provider with the id" + serviceProviderId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/delete/applications")
    @POST
    @Override
    public Response detachApps(@PathParam("identity-server-id") int identityServerId,
                               @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds) {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        try {
            validateServiceProviderUID(identityServerId, serviceProviderId);
            spApplicationManager.validateDetachAppsRequest(identityServerId, serviceProviderId, appIds);
            spApplicationManager.detachSPApplications(identityServerId, serviceProviderId, appIds);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while attaching apps to service provider with the id" + serviceProviderId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/ent-app")
    @POST
    @Override
    public Response createEntApp(@PathParam("identity-server-id") int identityServerId,
                                  @PathParam("service-provider-id") String serviceProviderId, ApplicationWrapper app) {
        return createSPApplication(identityServerId, serviceProviderId, app);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/public-app")
    @POST
    @Override
    public Response createPubApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, PublicAppWrapper app) {
        return createSPApplication(identityServerId, serviceProviderId, app);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/web-app")
    @POST
    @Override
    public Response createWebApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, WebAppWrapper app) {
        return createSPApplication(identityServerId, serviceProviderId, app);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/custom-app")
    @POST
    @Override
    public Response createCustomApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, CustomAppWrapper app) {
        return createSPApplication(identityServerId, serviceProviderId, app);
    }

    /**
     * Validates and creates service provider application
     *
     * @param identityServerId id of the identity server
     * @param spUID uid of the service provider
     * @param appWrapper application wrapper
     * @param <T> application wrapper class
     * @return Response
     */
    private <T> Response createSPApplication(int identityServerId, String spUID, T appWrapper) {
        try {
            validateServiceProviderUID(identityServerId, spUID);
            SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
            Application createdApp = spApplicationManager.createSPApplication(appWrapper, identityServerId, spUID);
            return Response.status(Response.Status.CREATED).entity(createdApp).build();
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with create service provider app request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Found invalid release payload with create service provider app request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating service provider app";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Responsible for validating service provider in requests
     *
     * @param identityServerId identity server id of the service provider
     * @param spUID uid of the service provider
     * @throws ApplicationManagementException
     */
    private void validateServiceProviderUID(int identityServerId, String spUID) throws
            ApplicationManagementException {
        try {
            boolean isSPAppExists = SPAppRequestHandlerUtil.
                    isSPApplicationExist(identityServerId, spUID);
            if (!isSPAppExists) {
                String errMsg = "Service provider with the uid " + spUID + " does not exist.";
                log.error(errMsg);
                throw new BadRequestException(errMsg);
            }
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to validate service provider uid";
            log.error(errMsg, e);
            throw new ApplicationManagementException(errMsg, e);
        }
    }

}