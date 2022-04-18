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

package io.entgra.application.mgt.publisher.api.services;

import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;

import javax.ws.rs.DELETE;
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

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Service Provider Application Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SPApplicationService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/identity-server-applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "App publisher related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "view a service provider applications",
                        description = "Get service provider application details",
                        key = "perm:app:publisher:service-provider:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/service-provider/application/view"}
                ),
                @Scope(
                        name = "Create new identity server",
                        description = "Connect to new identity server",
                        key = "perm:app:publisher:service-provider:connect",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/service-provider/application/connect"}
                ),
                @Scope(
                        name = "Create a service provider application",
                        description = "Create an application and attach (map) to service provider",
                        key = "perm:app:publisher:service-provider:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/service-provider/application/create"}
                ),
                @Scope(
                        name = "Attach a service provider application",
                        description = "Map an application to service provider",
                        key = "perm:app:publisher:service-provider:attach",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/service-provider/application/attach"}
                ),
                @Scope(
                        name = "Detach a service provider application",
                        description = "Remove an application from service provider",
                        key = "perm:app:publisher:service-provider:detach",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/publisher/service-provider/application/detach"}
                )
        }
)
@Path("/identity-server-applications")
@Api(value = "SPApplication Management")
@Produces(MediaType.APPLICATION_JSON)
public interface SPApplicationService {

    String SCOPE = "scope";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/identity-servers/identity-service-providers")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get available identity service providers",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response getIdentityServiceProviders();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/identity-servers")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all identity servers",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response getIdentityServers();

    @Path("/identity-servers/{id}")
    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "get identity server by id",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:connect")
                    })
            }
    )
    Response deleteIdentityServer(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/identity-servers/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get identity server by id",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response getIdentityServer(@PathParam("id") int id);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/identity-servers")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "create new identity server",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:connect")
                    })
            }
    )
    Response createIdentityServer(IdentityServerDTO identityServerDTO);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/identity-servers/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "edit existing identity server",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:connect")
                    })
            }
    )
    Response updateIdentityServer(IdentityServerDTO identityServerDTO, @PathParam("id") int id);

    @GET
    @Path("/identity-servers/identity-server-name")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Check if identity server name is already exists",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response isIdentityServerNameExists(
            @QueryParam("identityServerName") String identityServerName);

    @GET
    @Path("/identity-servers/identity-server-url")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Check if identity server url is already exists",
            tags = "Identity Server Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response isIdentityServerUrlExists(
            @QueryParam("identityServerUrl") String identityServerUrl);

    /**
     * This method is used to register an APIM application for tenant domain.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{identity-server-id}/service-providers")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get service providers by identity server id",
            notes = "This will get service providers with the existing applications",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:view")
                    })
            }
    )
    Response getServiceProviders(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset,
                                 @PathParam("identity-server-id") int identityServerId);

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/attach")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "map application to service provider",
            notes = "This will map a given list application ids with the given service provider id",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:attach")
                    })
            }
    )
    Response attachApps(@PathParam("identity-server-id") int identityServerId,
                        @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds);

    /**
     * This method is used to register an APIM application for tenant domain.
     */
    @Path("/{identity-server-id}/service-provider/{service-provider-id}/detach")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Remove mapping with service provider of the given application ids",
            notes = "This will remove applications from service provider",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:detach")
                    })
            }
    )
    Response detachApps(@PathParam("identity-server-id") int identityServerId,
                        @PathParam("service-provider-id") String serviceProviderId, List<Integer> appIds);

    /**
     * This method is used to register an APIM application for tenant domain.
     */
    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/ent-app")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create enterprise application and attach to service provider",
            notes = "This will get create an enterprise application and map with service provider",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:create")
                    })
            }
    )
    Response createEntApp(@PathParam("identity-server-id") int identityServerId,
                          @PathParam("service-provider-id") String serviceProviderId, ApplicationWrapper app,
                          @ApiParam(
                                  name = "isPublished",
                                  value = "Published state of the application"
                          )
                          @QueryParam("isPublished") boolean isPublished);

    /**
     * This method is used to register an APIM application for tenant domain.
     */
    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/public-app")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create public application and attach to service provider",
            notes = "This will get create a public application and map with service provider",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:create")
                    })
            }
    )
    Response createPubApp(@PathParam("identity-server-id") int identityServerId,
                          @PathParam("service-provider-id") String serviceProviderId, PublicAppWrapper app,
                          @ApiParam(
                                  name = "isPublished",
                                  value = "Published state of the application"
                          )
                          @QueryParam("isPublished") boolean isPublished);

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/web-app")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create web application and attach to service provider",
            notes = "This will get create a web application and map with service provider",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:create")
                    })
            }
    )
    Response createWebApp(@PathParam("identity-server-id") int identityServerId,
                          @PathParam("service-provider-id") String serviceProviderId, WebAppWrapper app,
                          @ApiParam(
                                  name = "isPublished",
                                  value = "Published state of the application"
                          )
                          @QueryParam("isPublished") boolean isPublished);

    @Path("/{identity-server-id}/service-provider/{service-provider-id}/create/custom-app")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create custom application and attach to service provider",
            notes = "This will get create an custom application and map with service provider",
            tags = "Service Provider Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:service-provider:create")
                    })
            }
    )
    Response createCustomApp(@PathParam("identity-server-id") int identityServerId,
                          @PathParam("service-provider-id") String serviceProviderId, CustomAppWrapper app,
                             @ApiParam(
                                     name = "isPublished",
                                     value = "Published state of the application"
                             )
                             @QueryParam("isPublished") boolean isPublished);
}
