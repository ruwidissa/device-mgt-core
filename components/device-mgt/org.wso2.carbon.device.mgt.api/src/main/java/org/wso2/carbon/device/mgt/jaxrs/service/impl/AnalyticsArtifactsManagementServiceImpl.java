/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Attribute;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterMappingConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.MappingProperty;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterProperty;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.MessageFormat;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.SiddhiExecutionPlan;
import org.wso2.carbon.device.mgt.jaxrs.exception.ArtifactAlreadyExistsException;
import org.wso2.carbon.device.mgt.jaxrs.exception.BadRequestException;
import org.wso2.carbon.device.mgt.jaxrs.exception.ErrorDTO;
import org.wso2.carbon.device.mgt.jaxrs.exception.InvalidExecutionPlanException;
import org.wso2.carbon.device.mgt.jaxrs.exception.NotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.AnalyticsArtifactsManagementService;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Adapter;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventStream;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventMappingPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationDto;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This class is used to create endpoints to serve the deployment of streams, publishers, receivers,
 * siddhi scripts to the Analytics server as Artifacts
 */
@Path("/analytics/artifacts")
public class AnalyticsArtifactsManagementServiceImpl
        implements AnalyticsArtifactsManagementService {
    private static final Log log = LogFactory.getLog(AnalyticsArtifactsManagementServiceImpl.class);

    @Override
    @POST
    @Path("/stream/{id}")
    public Response deployEventDefinitionAsString(@PathParam("id") String id,
                                                  @QueryParam("isEdited") boolean isEdited,
                                                  @Valid EventStream stream) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            String streamDefinition = stream.getDefinition();
            if (deployStream(id, streamDefinition, isEdited)) {
                return Response.ok().build();
            } else {
                String errMsg = "Failed to create the Stream artifact of id: " + id +
                                " for tenant domain: " + tenantDomain;
                return Response.serverError().entity(errMsg).build();
            }
        } catch (ArtifactAlreadyExistsException e) {
            String errMsg = "Failed to create Stream artifact for tenant domain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (NotFoundException e) {
            String errMsg = "Failed to edit Stream artifact for tenant domain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/stream")
    public Response deployEventDefinitionAsDto(@Valid EventStream stream) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            validateStreamProperties(stream);
            String name = stream.getName();
            String version = stream.getVersion();
            if (deployStream(stream)) {
                return Response.ok().build();
            } else {
                String errMsg = String.format("Failed to create the Stream artifact of id: %s:%s " +
                                              "for tenant domain: %s", name, version, tenantDomain);
                log.error(errMsg);
                return Response.serverError().entity(errMsg).build();
            }
        } catch (BadRequestException e) {
            String errMsg = "Failed to deploy stream due to invalid payload";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    @Override
    @DELETE
    @Path("/stream/{name}/{version}/delete")
    public Response deleteStream(@PathParam("name") String name,
                                 @PathParam("version") String version) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (undeployStream(name, version)) {
                return Response.ok().build();
            } else {
                String errMsg = String.format("Failed to undeploy the Stream artifact of id: %s:%s " +
                                              "for tenant domain: %s", name, version, tenantDomain);
                log.error(errMsg);
                return Response.serverError().entity(errMsg).build();
            }
        } catch (NotFoundException e) {
            String errMsg = String.format("Failed to undeploy Stream with id %s:%s for tenant %s"
                    , name, version, tenantDomain);
            log.error(errMsg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/receiver/{name}")
    public Response deployEventReceiverAsString(@PathParam("name") String name,
                                                @QueryParam("isEdited") boolean isEdited,
                                                @Valid Adapter receiver) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub;
        try {
            String receiverDefinition = receiver.getDefinition();
            eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();
            if (!isEdited) {
                eventReceiverAdminServiceStub.deployEventReceiverConfiguration(receiverDefinition);
            } else {
                eventReceiverAdminServiceStub.editActiveEventReceiverConfiguration(receiverDefinition, name);
            }
            return Response.ok().build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/receiver")
    public Response deployEventReceiverAsDto(@Valid Adapter receiver) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            AdapterConfiguration adapterConfiguration = receiver.getAdapterConfiguration();
            boolean customMapping = adapterConfiguration.isCustomMappingEnabled();
            validateAdapterProperties(adapterConfiguration.getAdapterProperties());
            if (customMapping) {
                validateAdapterMapping(adapterConfiguration.getAdapterMappingConfiguration());
            }
            deployReceiver(receiver, customMapping, adapterConfiguration);
            return Response.ok().build();
        } catch (BadRequestException e) {
            String errMsg = "Failed to deploy receiver due to invalid payload";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Override
    @DELETE
    @Path("/receiver/{name}/delete")
    public Response deleteReceiver(@PathParam("name") String name) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (undeployAdapter(name, "Receiver")) {
                return Response.ok().build();
            } else {
                String errMsg = String.format("Failed to undeploy the Receiver artifact of name: %s" +
                                              "for tenant domain: %s", name, tenantDomain);
                log.error(errMsg);
                return Response.serverError().entity(errMsg).build();
            }
        } catch (AxisFault e) {
            String errMsg = "Failed to delete event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/publisher/{name}")
    public Response deployEventPublisherAsString(@PathParam("name") String name,
                                                 @QueryParam("isEdited") boolean isEdited,
                                                 @Valid Adapter publisher) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub;
        try {
            String publisherDefinition = publisher.getDefinition();
            eventPublisherAdminServiceStub = DeviceMgtAPIUtils.getEventPublisherAdminServiceStub();
            if (!isEdited) {
                eventPublisherAdminServiceStub.deployEventPublisherConfiguration(publisherDefinition);
            } else {
                eventPublisherAdminServiceStub.editActiveEventPublisherConfiguration(publisherDefinition, name);
            }
            return Response.ok().build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/publisher")
    public Response deployEventPublisherAsDto(@Valid Adapter publisher) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        AdapterConfiguration adapterConfiguration = publisher.getAdapterConfiguration();
        try {
            validateAdapterProperties(adapterConfiguration.getAdapterProperties());
            boolean customMapping = adapterConfiguration.isCustomMappingEnabled();
            if (customMapping) {
                validateAdapterMapping(adapterConfiguration.getAdapterMappingConfiguration());
            }
            deployPublisher(publisher, customMapping, adapterConfiguration);
            return Response.ok().build();
        } catch (BadRequestException e) {
            String errMsg = "Failed to deploy publisher due to invalid payload";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Override
    @DELETE
    @Path("/publisher/{name}/delete")
    public Response deletePublisher(@PathParam("name") String name) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (undeployAdapter(name, "Publisher")) {
                return Response.ok().build();
            } else {
                String errMsg = String.format("Failed to undeploy the Publisher artifact of name: %s" +
                                              "for tenant domain: %s", name, tenantDomain);
                log.error(errMsg);
                return Response.serverError().entity(errMsg).build();
            }
        } catch (AxisFault e) {
            String errMsg = "Failed to delete event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    @Override
    @POST
    @Path("/siddhi-script/{name}")
    public Response deploySiddhiExecutableScript(@PathParam("name") String name,
                                                 @QueryParam("isEdited") boolean isEdited,
                                                 @Valid SiddhiExecutionPlan plan) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            deploySiddhiExecutionPlan(name, isEdited, plan.getDefinition());
            return Response.ok().build();
        } catch (InvalidExecutionPlanException e) {
            String errMsg = "Failed to deploy siddhi script due to invalid payload";
            log.error(errMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenantDomain: " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        }
    }

    @Override
    @DELETE
    @Path("/siddhi-script/{name}/delete")
    public Response deleteSiddhiScript(@PathParam("name") String name) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            undeploySiddhiScript(name);
            return Response.ok().build();
        } catch (AxisFault e) {
            String errMsg = "Failed to delete event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.serverError().entity(errMsg).build();
        }
    }

    /**
     * Deploy Stream by passing a string to a stub
     *
     * @param streamId         Stream name:version
     * @param streamDefinition Stream that should be deployed
     * @param isEdited         Create a new stream or edit an existing one
     * @return True if stream successfully created and false if not
     * @throws RemoteException                Exception that may occur during a remote method call
     * @throws UserStoreException             Exception that may occur during JWT token generation
     * @throws JWTClientException             Exception that may occur during connecting to client store
     * @throws NotFoundException              Exception that may occure if stream doesn't exist while editing
     * @throws ArtifactAlreadyExistsException Exception that may occure if stream exist while creating
     */
    private boolean deployStream(String streamId, String streamDefinition, boolean isEdited)
            throws UserStoreException, JWTClientException, RemoteException, NotFoundException,
                   ArtifactAlreadyExistsException {

        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            if (isEdited) {
                validateStreamId(streamId, eventStreamAdminServiceStub, true);
                return eventStreamAdminServiceStub
                        .editEventStreamDefinitionAsString(streamDefinition, streamId);
            } else {
                validateStreamId(streamId, eventStreamAdminServiceStub, false);
                return eventStreamAdminServiceStub.addEventStreamDefinitionAsString(streamDefinition);
            }
        } finally {
            cleanup(eventStreamAdminServiceStub);
        }
    }

    /**
     * Deploy Stream by passing a DTO object to a stub
     *
     * @param stream Stream definition
     * @return True if stream successfully created and false if not
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     * @throws NotFoundException  Exception that may occure if stream doesn't exist while editing
     */
    private boolean deployStream(EventStream stream)
            throws RemoteException, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        List<Attribute> metaData = stream.getMetaData();
        List<Attribute> payloadData = stream.getPayloadData();
        List<Attribute> correlationData = stream.getCorrelationData();
        try {
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();

            EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();
            eventStreamDefinitionDto.setName(stream.getName());
            eventStreamDefinitionDto.setVersion(stream.getVersion());
            eventStreamDefinitionDto.setNickName(stream.getNickName());
            eventStreamDefinitionDto.setDescription(stream.getDescription());
            if (metaData != null) {
                eventStreamDefinitionDto.setMetaData(addEventAttributesToDto(metaData));
            }
            if (payloadData != null) {
                eventStreamDefinitionDto.setPayloadData(addEventAttributesToDto(payloadData));
            }
            if (correlationData != null) {
                eventStreamDefinitionDto.setCorrelationData(addEventAttributesToDto(correlationData));
            }
            String streamId = stream.getName() + ":" + stream.getVersion();
            if (eventStreamAdminServiceStub.getStreamDefinitionDto(streamId) != null) {
                return eventStreamAdminServiceStub
                        .editEventStreamDefinitionAsDto(eventStreamDefinitionDto, streamId);
            } else {
                return eventStreamAdminServiceStub
                        .addEventStreamDefinitionAsDto(eventStreamDefinitionDto);
            }
        } finally {
            cleanup(eventStreamAdminServiceStub);
        }
    }

    /**
     * Undeploy a stream artifact
     *
     * @param name    Stream name
     * @param version Stream version
     * @return True if stream successfully created and false if not
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     * @throws NotFoundException Exception that may occure if stream doesn't exist
     */
    private boolean undeployStream(String name, String version)
            throws RemoteException, UserStoreException, JWTClientException, NotFoundException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            String streamId = String.format("%s:%s", name, version);
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            if (eventStreamAdminServiceStub.getStreamDefinitionDto(streamId) != null) {
                return eventStreamAdminServiceStub.removeEventStreamDefinition(name, version);
            } else {
                ErrorDTO error = new ErrorDTO();
                String msg = String.format("Stream wit id: %s not found", streamId);
                error.setMessage(msg);
                throw new NotFoundException(error);
            }
        } finally {
            cleanup(eventStreamAdminServiceStub);
        }
    }

    /**
     * Set data to a receiver dto and deploy dto through a stub
     *
     * @param receiver             Event Receiver adapter
     * @param customMapping        Is Receiver mapped
     * @param adapterConfiguration Adapter property and mapping configuration
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     */
    private void deployReceiver(Adapter receiver, boolean customMapping,
                                AdapterConfiguration adapterConfiguration)
            throws RemoteException, UserStoreException, JWTClientException {
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub = DeviceMgtAPIUtils
                .getEventReceiverAdminServiceStub();
        try {
            String receiverName = receiver.getAdapterName();
            String adapterType = receiver.getAdapterType().toStringFormatted();
            String eventStreamWithVersion = receiver.getEventStreamWithVersion();
            List<AdapterProperty> adapterProperties = adapterConfiguration.getAdapterProperties();
            EventReceiverConfigurationDto eventReceiverConfigurationDto = eventReceiverAdminServiceStub
                    .getActiveEventReceiverConfiguration(receiverName);
            if (eventReceiverConfigurationDto != null) {
                eventReceiverAdminServiceStub.undeployActiveEventReceiverConfiguration(receiverName);
            }
            BasicInputAdapterPropertyDto[] basicInputAdapterPropertyDtos = addReceiverConfigToDto(adapterProperties);

            if (customMapping) {
                AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration
                        .getAdapterMappingConfiguration();
                MessageFormat messageFormat = adapterMappingConfiguration.getMessageFormat();
                if (!messageFormat.toString().equals("wso2event")) {
                    EventMappingPropertyDto[] inputMappingPropertyDtos =
                            addReceiverMappingToDto(adapterMappingConfiguration.getInputMappingProperties());
                    if (messageFormat.toString().equals("xml")) {
                        EventMappingPropertyDto[] namespaceMappingPropertyDtos =
                                addReceiverMappingToDto(adapterMappingConfiguration.getNamespaceMappingProperties());
                        eventReceiverAdminServiceStub.deployXmlEventReceiverConfiguration(receiverName
                                , eventStreamWithVersion, adapterType, null
                                , namespaceMappingPropertyDtos, inputMappingPropertyDtos
                                , basicInputAdapterPropertyDtos, true);
                    } else {
                        if (messageFormat.toString().equals("map")) {
                            eventReceiverAdminServiceStub.deployMapEventReceiverConfiguration(receiverName
                                    , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                    , basicInputAdapterPropertyDtos, true);
                        } else if (messageFormat.toString().equals("text")) {
                            eventReceiverAdminServiceStub.deployTextEventReceiverConfiguration(receiverName
                                    , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                    , basicInputAdapterPropertyDtos, true);
                        } else {
                            eventReceiverAdminServiceStub.deployJsonEventReceiverConfiguration(receiverName
                                    , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                    , basicInputAdapterPropertyDtos, true);
                        }
                    }
                } else {
                    EventMappingPropertyDto[] correlationMappingPropertyDtos = addReceiverMappingToDto(
                            adapterMappingConfiguration.getCorrelationMappingProperties()
                    );
                    EventMappingPropertyDto[] metaMappingPropertyDtos = addReceiverMappingToDto(
                            adapterMappingConfiguration.getInputMappingProperties()
                    );
                    EventMappingPropertyDto[] payloadMappingPropertyDtos = addReceiverMappingToDto(
                            adapterMappingConfiguration.getPayloadMappingProperties()
                    );

                    eventReceiverAdminServiceStub.deployWso2EventReceiverConfiguration(receiverName
                            , eventStreamWithVersion, adapterType, metaMappingPropertyDtos
                            , correlationMappingPropertyDtos, payloadMappingPropertyDtos
                            , basicInputAdapterPropertyDtos, true
                            , eventStreamWithVersion);
                }
            } else {
                deployReceiverWithoutMapping(receiverName, eventStreamWithVersion, adapterType,
                                             eventReceiverAdminServiceStub, basicInputAdapterPropertyDtos);
            }
        } finally {
            cleanup(eventReceiverAdminServiceStub);
        }
    }

    /**
     * To deploy receiver if custom mapping is false
     *
     * @param receiverName                  Name of the receiver
     * @param eventStreamWithVersion        Attached event stream of the receiver
     * @param adapterType                   Adapter type name
     * @param eventReceiverAdminServiceStub Stub to deploy receiver
     * @param basicInputAdapterPropertyDtos DTO to attach receiver data
     * @throws RemoteException Exception that may occur during a remote method call
     */
    private void deployReceiverWithoutMapping(String receiverName, String eventStreamWithVersion
            , String adapterType, EventReceiverAdminServiceStub eventReceiverAdminServiceStub
            , BasicInputAdapterPropertyDto[] basicInputAdapterPropertyDtos)
            throws RemoteException {
        switch (adapterType) {
            case "iot-event":
            case "wso2event":
                eventReceiverAdminServiceStub.deployWso2EventReceiverConfiguration(receiverName
                        , eventStreamWithVersion, adapterType, null, null
                        , null, basicInputAdapterPropertyDtos, false
                        , eventStreamWithVersion);
                break;
            case "soap":
                eventReceiverAdminServiceStub.deployXmlEventReceiverConfiguration(receiverName
                        , eventStreamWithVersion, adapterType, null, null
                        , null, basicInputAdapterPropertyDtos, false);
                break;
            default:
                eventReceiverAdminServiceStub.deployTextEventReceiverConfiguration(receiverName
                        , eventStreamWithVersion, adapterType, null
                        , basicInputAdapterPropertyDtos, false);
        }
    }

    /**
     * Set data to a publisher dto and deploy dto through a stub
     *
     * @param publisher            Event Publisher adapter
     * @param customMapping        Is Publisher mapped
     * @param adapterConfiguration Publisher property and mapping configuration
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     */
    private void deployPublisher(Adapter publisher, boolean customMapping,
                                 AdapterConfiguration adapterConfiguration)
            throws RemoteException, UserStoreException, JWTClientException {
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = DeviceMgtAPIUtils
                .getEventPublisherAdminServiceStub();
        try {
            String publisherName = publisher.getAdapterName();
            String adapterType = publisher.getAdapterType().toStringFormatted();
            String eventStreamWithVersion = publisher.getEventStreamWithVersion();
            List<AdapterProperty> adapterProperties = adapterConfiguration.getAdapterProperties();
            EventPublisherConfigurationDto eventPublisherConfigurationDto = eventPublisherAdminServiceStub
                    .getActiveEventPublisherConfiguration(publisherName);
            if (eventPublisherConfigurationDto != null) {
                eventPublisherAdminServiceStub.undeployActiveEventPublisherConfiguration(publisherName);
            }

            BasicOutputAdapterPropertyDto[] basicOutputAdapterPropertyDtos =
                    addPublisherConfigToDto(adapterProperties);

            if (customMapping) {
                AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration
                        .getAdapterMappingConfiguration();
                MessageFormat messageFormat = adapterMappingConfiguration.getMessageFormat();
                if (!messageFormat.toString().equals("wso2event")) {
                    if (!messageFormat.toString().equals("map")) {
                        if (messageFormat.toString().equals("xml")) {
                            eventPublisherAdminServiceStub.deployXmlEventPublisherConfiguration(
                                    publisherName, eventStreamWithVersion, adapterType
                                    , adapterMappingConfiguration.getInputMappingString()
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , true);
                        } else if (messageFormat.toString().equals("text")) {
                            eventPublisherAdminServiceStub.deployTextEventPublisherConfiguration(
                                    publisherName, eventStreamWithVersion, adapterType
                                    , adapterMappingConfiguration.getInputMappingString()
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , true);
                        } else {
                            eventPublisherAdminServiceStub.deployJsonEventPublisherConfiguration(
                                    publisherName, eventStreamWithVersion, adapterType
                                    , adapterMappingConfiguration.getInputMappingString()
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , true);
                        }
                    } else {
                        org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] inputMappingPropertyDtos =
                                addPublisherMappingToDto(
                                        adapterMappingConfiguration.getInputMappingProperties()
                                );
                        eventPublisherAdminServiceStub.deployMapEventPublisherConfiguration(publisherName
                                , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                , basicOutputAdapterPropertyDtos, true);
                    }
                } else {
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[]
                            correlationMappingPropertyDtos = addPublisherMappingToDto
                            (
                                    adapterMappingConfiguration.getCorrelationMappingProperties()
                            );
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[]
                            metaMappingPropertyDtos = addPublisherMappingToDto
                            (
                                    adapterMappingConfiguration.getMetaMappingProperties()
                            );
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[]
                            payloadMappingPropertyDtos = addPublisherMappingToDto
                            (
                                    adapterMappingConfiguration.getPayloadMappingProperties()
                            );
                    eventPublisherAdminServiceStub.deployWSO2EventPublisherConfiguration(
                            publisherName, eventStreamWithVersion, adapterType, metaMappingPropertyDtos
                            , correlationMappingPropertyDtos, payloadMappingPropertyDtos
                            , basicOutputAdapterPropertyDtos, true
                            , eventStreamWithVersion);
                }
            } else {
                deployPublisherWithoutMapping(publisherName, eventStreamWithVersion, adapterType
                        , eventPublisherAdminServiceStub, basicOutputAdapterPropertyDtos);
            }
        } finally {
            cleanup(eventPublisherAdminServiceStub);
        }
    }

    /**
     * To deploy publisher if custom mapping is false
     *
     * @param publisherName                  Name of the publisher
     * @param eventStreamWithVersion         Attached event stream of the publisher
     * @param adapterType                    Adapter type name
     * @param eventPublisherAdminServiceStub Stub to deploy publisher
     * @param basicOutputAdapterPropertyDtos DTO to attach publisher data
     * @throws RemoteException Exception that may occur during a remote method call
     */
    private void deployPublisherWithoutMapping(String publisherName, String eventStreamWithVersion
            , String adapterType, EventPublisherAdminServiceStub eventPublisherAdminServiceStub
            , BasicOutputAdapterPropertyDto[] basicOutputAdapterPropertyDtos)
            throws RemoteException {
        switch (adapterType) {
            case "wso2event":
            case "ui":
            case "secured-websocket":
                eventPublisherAdminServiceStub.deployWSO2EventPublisherConfiguration(publisherName
                        , eventStreamWithVersion, adapterType, null
                        , null, null, basicOutputAdapterPropertyDtos
                        , false, eventStreamWithVersion);
                break;
            case "soap":
                eventPublisherAdminServiceStub.deployXmlEventPublisherConfiguration(publisherName
                        , eventStreamWithVersion, adapterType, null
                        , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                        , false);
                break;
            case "cassandra":
            case "rdbms":
                eventPublisherAdminServiceStub.deployMapEventPublisherConfiguration(publisherName
                        , eventStreamWithVersion, adapterType, null
                        , basicOutputAdapterPropertyDtos, false);
                break;
            default:
                eventPublisherAdminServiceStub.deployTextEventPublisherConfiguration(publisherName
                        , eventStreamWithVersion, adapterType, null
                        , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                        , false);
        }
    }

    /**
     * @param name Adapter name
     * @param type Adapter type(Receiver or Publisher)
     * @return True if Adapter successfully created and false if not
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     */
    private boolean undeployAdapter(String name, String type)
            throws RemoteException, UserStoreException, JWTClientException {
        if (type.equals("Receiver")) {
            EventReceiverAdminServiceStub eventReceiverAdminServiceStub = null;
            try {
                eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();
                return eventReceiverAdminServiceStub
                        .undeployActiveEventReceiverConfiguration(name);
            } finally {
                cleanup(eventReceiverAdminServiceStub);
            }
        } else {
            EventPublisherAdminServiceStub eventPublisherAdminServiceStub = null;
            try {
                eventPublisherAdminServiceStub = DeviceMgtAPIUtils.getEventPublisherAdminServiceStub();
                return eventPublisherAdminServiceStub
                        .undeployActiveEventPublisherConfiguration(name);
            } finally {
                cleanup(eventPublisherAdminServiceStub);
            }
        }
    }

    /**
     * Publish a siddhi execution plan using a stub
     *
     * @param name     Plan name
     * @param isEdited Is plan edited
     * @param plan     Plan data
     * @throws RemoteException               Exception that may occur during a remote method call
     * @throws UserStoreException            Exception that may occur during JWT token generation
     * @throws JWTClientException            Exception that may occur during connecting to client store
     * @throws InvalidExecutionPlanException Exception that may occur if execution plan validation fails
     */
    private void deploySiddhiExecutionPlan(String name, boolean isEdited, String plan)
            throws RemoteException, UserStoreException, JWTClientException,
                   InvalidExecutionPlanException {
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = null;
        try {
            eventProcessorAdminServiceStub = DeviceMgtAPIUtils.getEventProcessorAdminServiceStub();
            String validationResponse = eventProcessorAdminServiceStub.validateExecutionPlan(plan);
            if (validationResponse.equals("success")) {
                if (!isEdited) {
                    eventProcessorAdminServiceStub.deployExecutionPlan(plan);
                } else {
                    eventProcessorAdminServiceStub.editActiveExecutionPlan(plan, name);
                }
            } else {
                throw new InvalidExecutionPlanException(validationResponse);
            }
        } finally {
            cleanup(eventProcessorAdminServiceStub);
        }
    }

    /**
     * Undeploy a Siddhi artifact
     *
     * @param name Siddhi script name
     * @throws RemoteException    Exception that may occur during a remote method call
     * @throws UserStoreException Exception that may occur during JWT token generation
     * @throws JWTClientException Exception that may occur during connecting to client store
     */
    private void undeploySiddhiScript(String name)
            throws RemoteException, UserStoreException, JWTClientException {
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = null;
        try {
            eventProcessorAdminServiceStub = DeviceMgtAPIUtils.getEventProcessorAdminServiceStub();
            eventProcessorAdminServiceStub.undeployActiveExecutionPlan(name);
        } finally {
            cleanup(eventProcessorAdminServiceStub);
        }
    }

    /**
     * @param streamId                    Stream name:version
     * @param eventStreamAdminServiceStub stub used to mange Stream artifacts
     * @param isEdited                    Create a new stream or edit an existing one
     * @throws ArtifactAlreadyExistsException Exception that may occur if stream exist while creating
     * @throws RemoteException                Exception that may occur during a remote method call
     */
    private void validateStreamId(String streamId,
                                  EventStreamAdminServiceStub eventStreamAdminServiceStub,
                                  boolean isEdited)
            throws ArtifactAlreadyExistsException, RemoteException {
        EventStreamDefinitionDto eventStreamDefinitionDto = eventStreamAdminServiceStub
                .getStreamDefinitionDto(streamId);
        if (isEdited) {
            if (eventStreamDefinitionDto == null) {
                String errMsg = String.format("Failed to edit Stream with id: %s. " +
                                              "Stream not found", streamId);
                ErrorDTO error = new ErrorDTO();
                error.setMessage(errMsg);
                throw new NotFoundException(error);
            }
        } else {
            if (eventStreamDefinitionDto != null) {
                String errMsg = String.format("Failed to create Stream with id: %s. " +
                                              "Stream already exists.", streamId);
                throw new ArtifactAlreadyExistsException(errMsg);
            }
        }
    }

    /**
     * Validate stream properties
     *
     * @param stream EventStream object
     * @throws BadRequestException Exception that may occur if property attributes invalid
     */
    private void validateStreamProperties(EventStream stream) throws BadRequestException {
        if ((stream.getMetaData() == null || stream.getMetaData().isEmpty()) &&
            (stream.getCorrelationData() == null || stream.getCorrelationData().isEmpty()) &&
            (stream.getPayloadData() == null || stream.getPayloadData().isEmpty())) {
            String errMsg = String.format("Failed to validate Stream property attributes of %s:%s. " +
                                          "Stream mapping can't be null or empty",
                                          stream.getName(), stream.getVersion());
            throw new BadRequestException(errMsg);
        }
    }

    /**
     * Validate adapter payload attributes
     *
     * @param adapterProperties Adapter payload attributes
     * @throws BadRequestException Exception that may occur if adapter properties invalid
     */
    private void validateAdapterProperties(List<AdapterProperty> adapterProperties)
            throws BadRequestException {
        if (adapterProperties == null) {
            String errMsg = "Failed to validate adapter attributes. Adapter attributes can't be null";
            throw new BadRequestException(errMsg);
        }
    }

    /**
     * Validate adapter mapping attributes
     * <p>
     * Conditions
     * - if both inputMappingProperties and namespaceMappingProperties null check remaining property lists
     * - if all correlationMappingProperties, payloadMappingProperties, metaMappingProperties null log error
     * - if message format is null change the final result to TRUE
     * - else continue
     *
     * @param adapterMappingConfiguration Adapter mapping attributes
     * @throws BadRequestException Exception that may occur if adapter mapping properties invalid
     */
    private void validateAdapterMapping(AdapterMappingConfiguration adapterMappingConfiguration)
            throws BadRequestException {
        if (adapterMappingConfiguration == null) {
            String errMsg = "Failed to validate adapter mapping attributes. " +
                            "Adapter mapping configuration can't be null";
            throw new BadRequestException(errMsg);
        } else if (adapterMappingConfiguration.getMessageFormat() == null ||
                   ((adapterMappingConfiguration.getInputMappingString() == null)
                    && (adapterMappingConfiguration.getInputMappingProperties() == null ||
                        adapterMappingConfiguration.getInputMappingProperties().isEmpty())
                    && (adapterMappingConfiguration.getNamespaceMappingProperties() == null ||
                        adapterMappingConfiguration.getNamespaceMappingProperties().isEmpty()))
                   &&
                   ((adapterMappingConfiguration.getCorrelationMappingProperties() == null ||
                     adapterMappingConfiguration.getCorrelationMappingProperties().isEmpty())
                    && (adapterMappingConfiguration.getPayloadMappingProperties() == null ||
                        adapterMappingConfiguration.getPayloadMappingProperties().isEmpty())
                    && (adapterMappingConfiguration.getMetaMappingProperties() == null ||
                        adapterMappingConfiguration.getMetaMappingProperties().isEmpty()))
        ) {
            String errMsg = "Failed to validate adapter mapping attributes. " +
                            "Adapter mapping configuration invalid";
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(errMsg);
            throw new BadRequestException(errorDTO);
        }
    }

    /**
     * This will set payload of event attribute's mapping to the DTO
     *
     * @param attributes list of event attributes
     * @return DTO with all the event attributes
     */
    private EventStreamAttributeDto[] addEventAttributesToDto(List<Attribute> attributes) {
        EventStreamAttributeDto[] eventStreamAttributeDtos = new EventStreamAttributeDto[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(attributes.get(i).getName());
            eventStreamAttributeDto.setAttributeType(attributes.get(i).getType().toString());
            eventStreamAttributeDtos[i] = eventStreamAttributeDto;
        }
        return eventStreamAttributeDtos;
    }

    /**
     * This will set payload of receiver attributes to the DTO
     *
     * @param adapterProperties List of receiver attributes
     * @return DTO with all the receiver attributes
     */
    private BasicInputAdapterPropertyDto[] addReceiverConfigToDto(
            List<AdapterProperty> adapterProperties) {
        BasicInputAdapterPropertyDto[] basicInputAdapterPropertyDtos
                = new BasicInputAdapterPropertyDto[adapterProperties.size()];
        for (int i = 0; i < adapterProperties.size(); i++) {
            BasicInputAdapterPropertyDto basicInputAdapterPropertyDto = new BasicInputAdapterPropertyDto();
            basicInputAdapterPropertyDto.setKey(adapterProperties.get(i).getKey());
            basicInputAdapterPropertyDto.setValue(adapterProperties.get(i).getValue());
            basicInputAdapterPropertyDtos[i] = basicInputAdapterPropertyDto;
        }
        return basicInputAdapterPropertyDtos;
    }

    /**
     * This will set payload of receiver mapping attributes to the DTO
     *
     * @param mapProperties List of receiver mapping attributes
     * @return DTO with all the receiver mapping attributes
     */
    private EventMappingPropertyDto[] addReceiverMappingToDto
    (List<MappingProperty> mapProperties) {
        EventMappingPropertyDto[] eventMappingPropertyDtos = new EventMappingPropertyDto[mapProperties.size()];
        for (int i = 0; i < mapProperties.size(); i++) {
            EventMappingPropertyDto eventMappingPropertyDto = new EventMappingPropertyDto();
            eventMappingPropertyDto.setName(mapProperties.get(i).getName());
            eventMappingPropertyDto.setType(mapProperties.get(i).getType());
            eventMappingPropertyDto.setValueOf(mapProperties.get(i).getValueOf());
            eventMappingPropertyDtos[i] = eventMappingPropertyDto;
        }
        return eventMappingPropertyDtos;
    }

    /**
     * This will set payload of publisher attributes to the DTO
     *
     * @param adapterProperties List of publisher attributes
     * @return DTO with all the publisher attributes
     */
    private BasicOutputAdapterPropertyDto[] addPublisherConfigToDto(
            List<AdapterProperty> adapterProperties) {
        BasicOutputAdapterPropertyDto[] basicOutputAdapterPropertyDtos =
                new BasicOutputAdapterPropertyDto[adapterProperties.size()];
        for (int i = 0; i < adapterProperties.size(); i++) {
            BasicOutputAdapterPropertyDto basicOutputAdapterPropertyDto =
                    new BasicOutputAdapterPropertyDto();
            basicOutputAdapterPropertyDto.setKey(adapterProperties.get(i).getKey());
            basicOutputAdapterPropertyDto.setValue(adapterProperties.get(i).getValue());
            basicOutputAdapterPropertyDtos[i] = basicOutputAdapterPropertyDto;
        }
        return basicOutputAdapterPropertyDtos;
    }

    /**
     * This will set payload of publisher mapping attributes to the DTO
     *
     * @param mapProperties List of publisher mapping attributes
     * @return DTO with all the publisher mapping attributes
     */
    private org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] addPublisherMappingToDto
    (List<MappingProperty> mapProperties) {
        org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] eventMappingPropertyDtos
                = new org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[mapProperties.size()];
        for (int i = 0; i < mapProperties.size(); i++) {
            org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto eventMappingPropertyDto
                    = new org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto();
            eventMappingPropertyDto.setName(mapProperties.get(i).getName());
            eventMappingPropertyDto.setType(mapProperties.get(i).getType());
            eventMappingPropertyDto.setValueOf(mapProperties.get(i).getValueOf());
            eventMappingPropertyDtos[i] = eventMappingPropertyDto;
        }
        return eventMappingPropertyDtos;
    }

    /**
     * Clean Service client in the stub
     *
     * @param stub Stud that needs to be cleaned
     */
    private void cleanup(Stub stub) {
        if (stub != null) {
            try {
                stub.cleanup();
            } catch (AxisFault axisFault) {
                log.warn("Failed to clean the stub " + stub.getClass());
            }
        }
    }
}
