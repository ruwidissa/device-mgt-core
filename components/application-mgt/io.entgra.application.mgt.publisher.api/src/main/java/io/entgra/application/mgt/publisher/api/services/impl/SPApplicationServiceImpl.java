/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.publisher.api.services.impl;

import io.entgra.application.mgt.common.IdentityServerResponse;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.dto.IdentityServiceProviderDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppWrapper;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.publisher.api.services.SPApplicationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

    @Path("/identity-servers/identity-service-providers")
    @GET
    @Override
    public Response getIdentityServiceProviders() {
        SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
        try {
            List<IdentityServiceProviderDTO> identityServiceProviders = spAppManager.getIdentityServiceProviders();
            return Response.status(Response.Status.OK).entity(identityServiceProviders).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting identity service providers";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Path("/identity-servers")
    @GET
    @Override
    public Response getIdentityServers() {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            List<IdentityServerResponse> identityServers = spAppManager.getIdentityServers();
            return Response.status(Response.Status.OK).entity(identityServers).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/identity-servers/{id}")
    @DELETE
    @Override
    public Response deleteIdentityServer(@PathParam("id") int id) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            spAppManager.deleteIdentityServer(id);
            return Response.status(Response.Status.OK).entity("Successfully deleted identity server").build();
        } catch (NotFoundException e) {
            String msg = "Identity server with the id " + id + " does not exist.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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
            IdentityServerResponse identityServer = spAppManager.getIdentityServerResponse(id);
            return Response.status(Response.Status.OK).entity(identityServer).build();
        } catch (NotFoundException e) {
            String msg = "Identity server with the id " + id + " does not exist.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/identity-servers/{id}")
    @PUT
    @Override
    public Response updateIdentityServer(IdentityServerDTO identityServerDTO, @PathParam("id") int id) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            IdentityServerResponse identityServerResponse = spAppManager.updateIdentityServer(identityServerDTO, id);
            return Response.status(Response.Status.OK).entity(identityServerResponse).build();
        } catch (NotFoundException e) {
            String msg = "Identity server with the id " + id + " does not exist.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (BadRequestException e) {
            String errMsg = "Identity server request payload is invalid";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }


    @Path("/identity-servers")
    @POST
    @Override
    public Response createIdentityServer(IdentityServerDTO identityServerDTO) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            IdentityServerResponse identityServer = spAppManager.createIdentityServer(identityServerDTO);
            return Response.status(Response.Status.CREATED).entity(identityServer).build();
        } catch (BadRequestException e) {
            String errMsg = "Identity server request payload is invalid";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @GET
    @Path("/identity-servers/identity-server-name")
    @Override
    public Response isIdentityServerNameExists(
            @QueryParam("identityServerName") String identityServerName) {
        try {
            if (identityServerName == null) {
                String msg = "Invalid identity server name, identityServerName query param cannot be empty/null.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            if (spAppManager.isIdentityServerNameExist(identityServerName)) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.OK).build();
        } catch (BadRequestException e) {
            String errMsg = "Identity server request payload is invalid";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @GET
    @Path("/identity-servers/identity-server-url")
    @Override
    public Response isIdentityServerUrlExists(
            @QueryParam("identityServerUrl") String identityServerUrl) {
        try {
            if (identityServerUrl == null) {
                String msg = "Invalid identity server url, identityServerName query param cannot be empty/null.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            if (spAppManager.isIdentityServerUrlExist(identityServerUrl)) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.OK).build();
        } catch (BadRequestException e) {
            String errMsg = "Identity server request payload is invalid";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/{identity-server-id}/service-providers")
    @GET
    @Override
    public Response getServiceProviders(@DefaultValue("30") @QueryParam("limit") Integer limit,@DefaultValue("0") @QueryParam("offset") Integer offset,
                                        @PathParam("identity-server-id") int identityServerId) {
        try {
            SPApplicationManager spAppManager = APIUtil.getSPApplicationManager();
            SPApplicationListResponse applications = spAppManager.retrieveSPApplicationFromIdentityServer(identityServerId, limit, offset);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (NotFoundException e) {
            String errMsg = "No Identity server exist with the id: " + identityServerId;
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to merge identity server apps with existing apps";
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/attach")
    @POST
    @Override
    public Response attachApps(@PathParam("identity-server-id") int identityServerId,
                               @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds) {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        try {
            spApplicationManager.validateAttachAppsRequest(identityServerId, serviceProviderId, appIds);
            spApplicationManager.attachSPApplications(identityServerId, serviceProviderId, appIds);
        } catch (NotFoundException e) {
            String msg = "No identity server exist with the id " + identityServerId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Invalid appIds provided";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while attaching apps to service provider with the id" + serviceProviderId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/detach")
    @POST
    @Override
    public Response detachApps(@PathParam("identity-server-id") int identityServerId,
                               @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds) {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        try {
            spApplicationManager.validateDetachAppsRequest(identityServerId, serviceProviderId, appIds);
            spApplicationManager.detachSPApplications(identityServerId, serviceProviderId, appIds);
        } catch (NotFoundException e) {
            String msg = "No identity server exist with the id " + identityServerId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Invalid appIds provided";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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
                                  @PathParam("service-provider-id") String serviceProviderId, ApplicationWrapper app,
                                    @QueryParam("isPublished") boolean isPublished) {
        return createSPApplication(identityServerId, serviceProviderId, app, isPublished);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/public-app")
    @POST
    @Override
    public Response createPubApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, PublicAppWrapper app,
                                 @QueryParam("isPublished") boolean isPublished) {
        return createSPApplication(identityServerId, serviceProviderId, app, isPublished);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/web-app")
    @POST
    @Override
    public Response createWebApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, WebAppWrapper app,
                                 @QueryParam("isPublished") boolean isPublished) {
        return createSPApplication(identityServerId, serviceProviderId, app, isPublished);
    }

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/custom-app")
    @POST
    @Override
    public Response createCustomApp(@PathParam("identity-server-id") int identityServerId,
                                 @PathParam("service-provider-id") String serviceProviderId, CustomAppWrapper app,
                                    @QueryParam("isPublished") boolean isPublished) {
        return createSPApplication(identityServerId, serviceProviderId, app, isPublished);
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
    private <T> Response createSPApplication(int identityServerId, String spUID, T appWrapper, boolean isPublished) {
        try {
            SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
            Application createdApp = spApplicationManager.createSPApplication(appWrapper, identityServerId, spUID, isPublished);
            return Response.status(Response.Status.CREATED).entity(createdApp).build();
        } catch (NotFoundException e) {
            String msg = "No identity server exist with the id " + identityServerId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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

}