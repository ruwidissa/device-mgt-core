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
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Attribute;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterMappingConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.MappingProperty;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AdapterProperty;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.MessageFormat;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.SiddhiExecutionPlan;
import org.wso2.carbon.device.mgt.jaxrs.exception.BadRequestException;
import org.wso2.carbon.device.mgt.jaxrs.exception.ErrorDTO;
import org.wso2.carbon.device.mgt.jaxrs.exception.InvalidExecutionPlanException;
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
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            String streamDefinition = stream.getDefinition();
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            if (!isEdited) {
                eventStreamAdminServiceStub.addEventStreamDefinitionAsString(streamDefinition);
            } else {
                if (eventStreamAdminServiceStub.getStreamDetailsForStreamId(id) != null) {
                    eventStreamAdminServiceStub.editEventStreamDefinitionAsString(streamDefinition, id);
                }
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
        } finally {
            cleanup(eventStreamAdminServiceStub);
        }
    }

    @Override
    @POST
    @Path("/stream")
    public Response deployEventDefinitionAsDto(@Valid EventStream stream) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            deployStream(stream);
            return Response.ok().build();
        } catch (AxisFault e) {
            String errMsg = "Failed to create event definitions for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (RemoteException e) {
            String errMsg = "Failed to connect with the remote services for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (JWTClientException e) {
            String errMsg = "Failed to generate jwt token for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
        } catch (UserStoreException e) {
            String errMsg = "Failed to connect with the user store for tenant " + tenantDomain;
            log.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
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
    @Path("/siddhi-script/{name}")
    public Response deploySiddhiExecutableScript(@PathParam("name") String name,
                                                 @QueryParam("isEdited") boolean isEdited,
                                                 @Valid SiddhiExecutionPlan plan) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            publishSiddhiExecutionPlan(name, isEdited, plan.getDefinition());
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

    /**
     * Set data to a Stream dto and deploy dto through a stub
     *
     * @param stream Stream definition
     */
    private void deployStream(EventStream stream)
            throws RemoteException, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        List<Attribute> metaData = stream.getMetaData();
        List<Attribute> payloadData = stream.getPayloadData();
        List<Attribute> correlationData = stream.getCorrelationData();
        if (metaData.isEmpty() && correlationData.isEmpty() && payloadData.isEmpty()) {
            String errMsg = String.format("Failed to validate Stream property attributes of %s:%s",
                                          stream.getName(), stream.getVersion());
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(errMsg);
            log.error(errMsg);
            throw new BadRequestException(errorResponse);
        }
        try {
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();

            EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();
            eventStreamDefinitionDto.setName(stream.getName());
            eventStreamDefinitionDto.setVersion(stream.getVersion());
            eventStreamDefinitionDto.setNickName(stream.getNickName());
            eventStreamDefinitionDto.setDescription(stream.getDescription());
            eventStreamDefinitionDto.setMetaData(addEventAttributesToDto(metaData));
            eventStreamDefinitionDto.setPayloadData(addEventAttributesToDto(payloadData));
            eventStreamDefinitionDto.setCorrelationData(addEventAttributesToDto(correlationData));

            String streamId = stream.getName() + ":" + stream.getVersion();
            if (eventStreamAdminServiceStub.getStreamDefinitionDto(streamId) != null) {
                eventStreamAdminServiceStub.editEventStreamDefinitionAsDto(eventStreamDefinitionDto, streamId);
            } else {
                eventStreamAdminServiceStub.addEventStreamDefinitionAsDto(eventStreamDefinitionDto);
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
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();

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

            AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration.getAdapterMappingConfiguration();
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
                            , basicInputAdapterPropertyDtos, customMapping);
                } else {
                    if (messageFormat.toString().equals("map")) {
                        eventReceiverAdminServiceStub.deployMapEventReceiverConfiguration(receiverName
                                , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                , basicInputAdapterPropertyDtos, customMapping);
                    } else if (messageFormat.toString().equals("text")) {
                        eventReceiverAdminServiceStub.deployTextEventReceiverConfiguration(receiverName
                                , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                , basicInputAdapterPropertyDtos, customMapping);
                    } else {
                        eventReceiverAdminServiceStub.deployJsonEventReceiverConfiguration(receiverName
                                , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                , basicInputAdapterPropertyDtos, customMapping);
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
                        , basicInputAdapterPropertyDtos, customMapping
                        , eventStreamWithVersion);
            }
        } finally {
            cleanup(eventReceiverAdminServiceStub);
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
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = DeviceMgtAPIUtils.getEventPublisherAdminServiceStub();
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

            AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration.getAdapterMappingConfiguration();
            MessageFormat messageFormat = adapterMappingConfiguration.getMessageFormat();
            if (!messageFormat.toString().equals("wso2event")) {
                if (!messageFormat.toString().equals("map")) {
                    if (messageFormat.toString().equals("xml")) {
                        eventPublisherAdminServiceStub.deployXmlEventPublisherConfiguration(publisherName
                                , eventStreamWithVersion, adapterType, adapterMappingConfiguration.getInputMappingString()
                                , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                , customMapping);
                    } else if (messageFormat.toString().equals("text")) {
                        eventPublisherAdminServiceStub.deployTextEventPublisherConfiguration(publisherName
                                , eventStreamWithVersion, adapterType, adapterMappingConfiguration.getInputMappingString()
                                , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                , customMapping);
                    } else {
                        eventPublisherAdminServiceStub.deployJsonEventPublisherConfiguration(publisherName
                                , eventStreamWithVersion, adapterType, adapterMappingConfiguration.getInputMappingString()
                                , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                , customMapping);
                    }
                } else {
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] inputMappingPropertyDtos =
                            addPublisherMappingToDto(
                                    adapterMappingConfiguration.getInputMappingProperties()
                            );
                    eventPublisherAdminServiceStub.deployMapEventPublisherConfiguration(publisherName
                            , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                            , basicOutputAdapterPropertyDtos, customMapping);
                }
            } else {
                org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] correlationMappingPropertyDtos =
                        addPublisherMappingToDto(
                                adapterMappingConfiguration.getCorrelationMappingProperties()
                        );
                org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] metaMappingPropertyDtos =
                        addPublisherMappingToDto(
                                adapterMappingConfiguration.getMetaMappingProperties()
                        );
                org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] payloadMappingPropertyDtos =
                        addPublisherMappingToDto(
                                adapterMappingConfiguration.getPayloadMappingProperties()
                        );
                eventPublisherAdminServiceStub.deployWSO2EventPublisherConfiguration(publisherName
                        , eventStreamWithVersion, adapterType, metaMappingPropertyDtos
                        , correlationMappingPropertyDtos, payloadMappingPropertyDtos
                        , basicOutputAdapterPropertyDtos, customMapping
                        , eventStreamWithVersion);
            }

        } finally {
            cleanup(eventPublisherAdminServiceStub);
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
    private void publishSiddhiExecutionPlan(String name, boolean isEdited,
                                            String plan)
            throws RemoteException, UserStoreException, JWTClientException {
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
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setMessage(validationResponse);
                log.error(validationResponse);
                throw new InvalidExecutionPlanException(errorDTO);
            }
        } finally {
            cleanup(eventProcessorAdminServiceStub);
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
        for (Attribute attribute : attributes) {
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(attribute.getName());
            eventStreamAttributeDto.setAttributeType(attribute.getType().toString());

        }
        return eventStreamAttributeDtos;
    }

    /**
     * Validate adapter payload attributes
     *
     * @param adapterProperties Adapter payload attributes
     */
    private void validateAdapterProperties(List<AdapterProperty> adapterProperties) {
        if (adapterProperties.isEmpty()) {
            String errMsg = "Invalid payload: event property attributes invalid!!!";
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(errMsg);
            log.error(errMsg);
            throw new BadRequestException(errorResponse);
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
     */
    private void validateAdapterMapping(AdapterMappingConfiguration adapterMappingConfiguration) {

        if (adapterMappingConfiguration.getInputMappingString() == null &&
            (adapterMappingConfiguration.getInputMappingProperties().isEmpty() &&
             adapterMappingConfiguration.getNamespaceMappingProperties().isEmpty()) &&
            (
                    adapterMappingConfiguration.getCorrelationMappingProperties().isEmpty() &&
                    adapterMappingConfiguration.getPayloadMappingProperties().isEmpty() &&
                    adapterMappingConfiguration.getMetaMappingProperties().isEmpty()
            )
            || adapterMappingConfiguration.getMessageFormat() == null) {
            String errMsg = "Invalid payload: event mapping attributes invalid!!!";
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage(errMsg);
            log.error(errMsg);
            throw new BadRequestException(errorResponse);
        }
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
