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
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventPublisher;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventReceiver;
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
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This class is used to create endpoints to serve the deployment of streams, publishers, receivers,
 * siddhi scripts to the Analytics server as Artifacts
 */
@Path("/analytics/artifacts")
public class AnalyticsArtifactsManagementServiceImpl implements AnalyticsArtifactsManagementService {
    private static final Log log = LogFactory.getLog(AnalyticsArtifactsManagementServiceImpl.class);

    /**
     * @param stream EventStream object with the properties of the stream
     * @return A status code depending on the code result
     * Function - Used to deploy stream as an artifact using a String
     */
    @Override
    @POST
    @Path("/stream/{id}")
    public Response deployEventDefinitionAsString(@PathParam("id") String id,
                                                  @QueryParam("isEdited") boolean isEdited,
                                                  @Valid EventStream stream) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            String streamDefinition = new String(stream.getDefinition().getBytes(), StandardCharsets.UTF_8);
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            if (!isEdited) {
                eventStreamAdminServiceStub.addEventStreamDefinitionAsString(streamDefinition);
            } else {
                // Find and edit stream
                if (eventStreamAdminServiceStub.getStreamDetailsForStreamId(id) != null) {
                    eventStreamAdminServiceStub.editEventStreamDefinitionAsString(streamDefinition, id);
                }
            }
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            cleanup(eventStreamAdminServiceStub);
        }
    }

    /**
     * @param stream EventStream object with the properties of the stream
     * @return A status code depending on the code result
     * Function - Used to deploy stream as an artifact using a DTO
     */
    @Override
    @POST
    @Path("/stream")
    public Response deployEventDefinitionAsDto(@Valid EventStream stream) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        // Categorize attributes to three lists depending on their type
        List<Attribute> metaData = stream.getMetaData();
        List<Attribute> payloadData = stream.getPayloadData();
        List<Attribute> correlationData = stream.getCorrelationData();

        try {
            /* Conditions
             * - At least one list should always be not null
             */
            if (metaData == null && correlationData == null && payloadData == null) {
                log.error("Invalid payload: event attributes");
                return Response.status(Response.Status.BAD_REQUEST).build();

            } else {
                // Publish the event stream
                publishStream(stream, metaData, correlationData, payloadData);
                return Response.ok().build();
            }
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param name     Receiver name
     * @param isEdited If receiver is created or edited
     * @param receiver Receiver object with the properties of the receiver
     * @return A status code depending on the code result
     * Function - Used to deploy receiver as an artifact using a String
     */
    @Override
    @POST
    @Path("/receiver/{name}")
    public Response deployEventReceiverAsString(@PathParam("name") String name,
                                                @QueryParam("isEdited") boolean isEdited,
                                                @Valid EventReceiver receiver) {
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
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().entity(name).build();
    }

    /**
     * @param receiver Receiver object with the properties of the receiver
     * @return A status code depending on the code result
     * Function - Used to deploy receiver as an artifact using a DTO
     */
    @Override
    @POST
    @Path("/receiver")
    public Response deployEventReceiverAsDto(@Valid Adapter receiver) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String receiverName = receiver.getAdapterName();
        String adapterType = receiver.getAdapterType().toStringFormatted();
        AdapterConfiguration adapterConfiguration = receiver.getAdapterConfiguration();
        AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration.getAdapterMappingConfiguration();

        try {
            List<AdapterProperty> adapterProperties = adapterConfiguration.getAdapterProperties();
            if (adapterProperties == null) {
                log.error("Invalid attribute payload");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            boolean customMapping = adapterConfiguration.isCustomMappingEnabled();
            List<MappingProperty> inputMappingProperties = adapterMappingConfiguration.getInputMappingProperties();
            List<MappingProperty> namespaceMappingProperties = adapterMappingConfiguration.getNamespaceMappingProperties();
            List<MappingProperty> correlationMappingProperties = adapterMappingConfiguration.getCorrelationMappingProperties();
            List<MappingProperty> payloadMappingProperties = adapterMappingConfiguration.getPayloadMappingProperties();
            List<MappingProperty> metaMappingProperties = adapterMappingConfiguration.getMetaMappingProperties();
            MessageFormat messageFormat = adapterMappingConfiguration.getMessageFormat();
            /*
             * Conditions
             * - if CustomMappingEnabled check validity of property lists
             * - if both inputMappingProperties and namespaceMappingProperties null check remaining property lists
             * - if all correlationMappingProperties, payloadMappingProperties, metaMappingProperties null log error
             * - if message format is null change the final result to TRUE
             * - else continue
             * */
            if ((customMapping &&
                 (inputMappingProperties == null && namespaceMappingProperties == null) &&
                 (correlationMappingProperties == null && payloadMappingProperties == null &&
                  metaMappingProperties == null)) || messageFormat == null) {
                String errMsg = "Invalid mapping payload";
                log.error(errMsg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
            }
            String eventStreamWithVersion = receiver.getEventStreamWithVersion();

            publishReceiver(receiverName, adapterType, adapterProperties, customMapping, inputMappingProperties,
                            namespaceMappingProperties, correlationMappingProperties, payloadMappingProperties,
                            metaMappingProperties, messageFormat, eventStreamWithVersion);
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param name      Publisher name
     * @param isEdited  If receiver is created or edited
     * @param publisher Publisher object with the properties of the publisher
     * @return A status code depending on the code result
     * Function - Used to deploy publisher as an artifact using a String
     */
    @Override
    @POST
    @Path("/publisher/{name}")
    public Response deployEventPublisherAsString(@PathParam("name") String name,
                                                 @QueryParam("isEdited") boolean isEdited,
                                                 @Valid EventPublisher publisher) {
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
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().entity(publisher).build();
    }

    /**
     * @param publisher Publisher object with the properties of the publisher
     * @return A status code depending on the code result
     * Function - Used to deploy publisher as an artifact using a DTO
     */
    @Override
    @POST
    @Path("/publisher")
    public Response deployEventPublisherAsDto(@Valid Adapter publisher) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String publisherName = publisher.getAdapterName();
        String adapterType = publisher.getAdapterType().toStringFormatted();
        AdapterConfiguration adapterConfiguration = publisher.getAdapterConfiguration();
        AdapterMappingConfiguration adapterMappingConfiguration = adapterConfiguration.getAdapterMappingConfiguration();

        try {
            List<AdapterProperty> adapterProperties = adapterConfiguration.getAdapterProperties();
            if (adapterProperties == null) {
                log.error("Invalid attribute payload");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            boolean customMapping = adapterConfiguration.isCustomMappingEnabled();
            String inputMappingString = adapterMappingConfiguration.getInputMappingString();
            List<MappingProperty> inputMappingProperties = adapterMappingConfiguration.getInputMappingProperties();
            List<MappingProperty> correlationMappingProperties = adapterMappingConfiguration.getCorrelationMappingProperties();
            List<MappingProperty> payloadMappingProperties = adapterMappingConfiguration.getPayloadMappingProperties();
            List<MappingProperty> metaMappingProperties = adapterMappingConfiguration.getMetaMappingProperties();
            MessageFormat messageFormat = adapterMappingConfiguration.getMessageFormat();
            /*
             * Conditions
             * - if CustomMappingEnabled check validity of property lists
             * - if all correlationMappingProperties, payloadMappingProperties, metaMappingProperties null log error
             * - if message format is null change the final result to TRUE
             * - else continue
             */
            if ((customMapping &&
                 (inputMappingProperties == null && inputMappingString == null) &&
                 (correlationMappingProperties == null && payloadMappingProperties == null &&
                  metaMappingProperties == null)) || messageFormat == null) {
                String errMsg = "Invalid mapping payload";
                log.error(errMsg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errMsg).build();
            }
            String eventStreamWithVersion = publisher.getEventStreamWithVersion();

            publishPublisher(publisherName, adapterType, adapterProperties, customMapping
                    , inputMappingString, inputMappingProperties, correlationMappingProperties
                    , payloadMappingProperties, metaMappingProperties, messageFormat
                    , eventStreamWithVersion);
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param name     Siddhi plan name
     * @param isEdited If receiver is created or edited
     * @param plan     Siddhi plan definition
     * @return a status code depending on the code execution
     * Function - Used to deploy Siddhi script as an artifact using a String
     */
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
            log.error("Failed to create event definitions for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (InvalidExecutionPlanException e) {
            log.error("Invalid Execution plan: " + tenantDomain, e);
            return e.getResponse();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store for tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param stream          Stream definition
     * @param metaData        Meta attributes of the stream
     * @param correlationData Correlation attributes of the stream
     * @param payloadData     Payload attributes of the stream
     * @throws RemoteException    exception that may occur during a remote method call
     * @throws UserStoreException exception that may occur during JWT token generation
     * @throws JWTClientException exception that may occur during connecting to client store
     */
    private void publishStream(EventStream stream, List<Attribute> metaData,
                               List<Attribute> correlationData, List<Attribute> payloadData)
            throws RemoteException, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub =
                DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
        try {
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
     * @param receiverName                 Receiver name
     * @param adapterType                  Receiver type
     * @param adapterProperties            Receiver properties
     * @param customMapping                Is receiver mapped
     * @param inputMappingProperties       Receiver input attribute mapping
     * @param namespaceMappingProperties   Receiver name-scape attribute mapping
     * @param correlationMappingProperties Receiver correlation attribute mapping
     * @param payloadMappingProperties     Receiver payload attribute mapping
     * @param metaMappingProperties        Receiver meta attribute mapping
     * @param messageFormat                Receiver mapping format
     * @param eventStreamWithVersion       Attached stream
     * @throws RemoteException    exception that may occur during a remote method call
     * @throws UserStoreException exception that may occur during JWT token generation
     * @throws JWTClientException exception that may occur during connecting to client store
     */
    private void publishReceiver(String receiverName, String adapterType,
                                 List<AdapterProperty> adapterProperties, boolean customMapping,
                                 List<MappingProperty> inputMappingProperties,
                                 List<MappingProperty> namespaceMappingProperties,
                                 List<MappingProperty> correlationMappingProperties,
                                 List<MappingProperty> payloadMappingProperties,
                                 List<MappingProperty> metaMappingProperties,
                                 MessageFormat messageFormat,
                                 String eventStreamWithVersion)
            throws RemoteException, UserStoreException, JWTClientException {
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();

        try {
            EventReceiverConfigurationDto eventReceiverConfigurationDto = eventReceiverAdminServiceStub
                    .getActiveEventReceiverConfiguration(receiverName);

            // Check if adapter already exists, if so un-deploy it
            if (eventReceiverConfigurationDto != null) {
                eventReceiverAdminServiceStub.undeployActiveEventReceiverConfiguration(receiverName);
            }

            // Adding attribute properties to DTOs
            BasicInputAdapterPropertyDto[] basicInputAdapterPropertyDtos =
                    addReceiverConfigToDto(adapterProperties);

            if (eventReceiverAdminServiceStub.getActiveEventReceiverConfiguration(receiverName) == null) {
                // Call stub deploy methods according to the message format
                if (!messageFormat.toString().equals("wso2event")) {
                    EventMappingPropertyDto[] inputMappingPropertyDtos =
                            addReceiverMappingToDto(inputMappingProperties);
                    if (messageFormat.toString().equals("xml")) {
                        EventMappingPropertyDto[] namespaceMappingPropertyDtos =
                                addReceiverMappingToDto(namespaceMappingProperties);

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
                    EventMappingPropertyDto[] correlationMappingPropertyDtos = addReceiverMappingToDto(correlationMappingProperties);
                    EventMappingPropertyDto[] metaMappingPropertyDtos = addReceiverMappingToDto(metaMappingProperties);
                    EventMappingPropertyDto[] payloadMappingPropertyDtos = addReceiverMappingToDto(payloadMappingProperties);

                    eventReceiverAdminServiceStub.deployWso2EventReceiverConfiguration(receiverName
                            , eventStreamWithVersion, adapterType, metaMappingPropertyDtos
                            , correlationMappingPropertyDtos, payloadMappingPropertyDtos
                            , basicInputAdapterPropertyDtos, customMapping
                            , eventStreamWithVersion);
                }

            }
        } finally {
            cleanup(eventReceiverAdminServiceStub);
        }
    }

    /**
     * @param publisherName                Publisher name
     * @param adapterType                  Publisher type
     * @param adapterProperties            Publisher properties
     * @param customMapping                Is publisher mapped
     * @param correlationMappingProperties Publisher correlation attribute mapping
     * @param payloadMappingProperties     Publisher payload attribute mapping
     * @param metaMappingProperties        Publisher meta attribute mapping
     * @param messageFormat                Publisher mapping format
     * @param eventStreamWithVersion       Attached stream
     * @throws RemoteException    exception that may occur during a remote method call
     * @throws UserStoreException exception that may occur during JWT token generation
     * @throws JWTClientException exception that may occur during connecting to client store
     */
    private void publishPublisher(String publisherName, String adapterType,
                                  List<AdapterProperty> adapterProperties,
                                  boolean customMapping,
                                  String inputMappingString,
                                  List<MappingProperty> inputMappingProperties,
                                  List<MappingProperty> correlationMappingProperties,
                                  List<MappingProperty> payloadMappingProperties,
                                  List<MappingProperty> metaMappingProperties,
                                  MessageFormat messageFormat,
                                  String eventStreamWithVersion)
            throws RemoteException, UserStoreException, JWTClientException {
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = DeviceMgtAPIUtils.getEventPublisherAdminServiceStub();
        // Check if adapter already exists, if so un-deploy it
        try {
            EventPublisherConfigurationDto eventPublisherConfigurationDto = eventPublisherAdminServiceStub
                    .getActiveEventPublisherConfiguration(publisherName);
            if (eventPublisherConfigurationDto != null) {
                eventPublisherAdminServiceStub.undeployActiveEventPublisherConfiguration(publisherName);
            }

            // Adding attribute properties to DTOs
            BasicOutputAdapterPropertyDto[] basicOutputAdapterPropertyDtos =
                    addPublisherConfigToDto(adapterProperties);

            if (eventPublisherAdminServiceStub.getActiveEventPublisherConfiguration(publisherName) == null) {
                // Call stub deploy methods according to the message format
                if (!messageFormat.toString().equals("wso2event")) {
                    if (!messageFormat.toString().equals("map")) {
                        if (messageFormat.toString().equals("xml")) {
                            eventPublisherAdminServiceStub.deployXmlEventPublisherConfiguration(publisherName
                                    , eventStreamWithVersion, adapterType, inputMappingString
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , customMapping);
                        } else if (messageFormat.toString().equals("text")) {
                            eventPublisherAdminServiceStub.deployTextEventPublisherConfiguration(publisherName
                                    , eventStreamWithVersion, adapterType, inputMappingString
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , customMapping);
                        } else {
                            eventPublisherAdminServiceStub.deployJsonEventPublisherConfiguration(publisherName
                                    , eventStreamWithVersion, adapterType, inputMappingString
                                    , basicOutputAdapterPropertyDtos, eventStreamWithVersion
                                    , customMapping);
                        }
                    } else {
                        org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] inputMappingPropertyDtos =
                                addPublisherMappingToDto(inputMappingProperties);
                        eventPublisherAdminServiceStub.deployMapEventPublisherConfiguration(publisherName
                                , eventStreamWithVersion, adapterType, inputMappingPropertyDtos
                                , basicOutputAdapterPropertyDtos, customMapping);
                    }
                } else {
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] correlationMappingPropertyDtos =
                            addPublisherMappingToDto(correlationMappingProperties);
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] metaMappingPropertyDtos =
                            addPublisherMappingToDto(metaMappingProperties);
                    org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto[] payloadMappingPropertyDtos =
                            addPublisherMappingToDto(payloadMappingProperties);

                    eventPublisherAdminServiceStub.deployWSO2EventPublisherConfiguration(publisherName
                            , eventStreamWithVersion, adapterType, metaMappingPropertyDtos
                            , correlationMappingPropertyDtos, payloadMappingPropertyDtos
                            , basicOutputAdapterPropertyDtos, customMapping
                            , eventStreamWithVersion);
                }

            }
        } finally {
            cleanup(eventPublisherAdminServiceStub);
        }
    }

    /**
     * @param name     plan name
     * @param isEdited is plan edited
     * @param plan     plan data
     * @throws RemoteException               exception that may occur during a remote method call
     * @throws UserStoreException            exception that may occur during JWT token generation
     * @throws JWTClientException            exception that may occur during connecting to client store
     * @throws InvalidExecutionPlanException exception that may occur if execution plan validation fails
     */
    private void publishSiddhiExecutionPlan(String name, boolean isEdited,
                                            String plan)
            throws RemoteException, UserStoreException, JWTClientException,
                   InvalidExecutionPlanException {
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = null;
        try {
            eventProcessorAdminServiceStub = DeviceMgtAPIUtils.getEventProcessorAdminServiceStub();
            // Validate the plan code
            String validationResponse = eventProcessorAdminServiceStub.validateExecutionPlan(plan);
            if (validationResponse.equals("success")) {
                if (!isEdited) {
                    // Create a new plan
                    eventProcessorAdminServiceStub.deployExecutionPlan(plan);
                } else {
                    // Edit plan
                    eventProcessorAdminServiceStub.editActiveExecutionPlan(plan, name);
                }
            } else {
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setMessage(validationResponse);
                throw new InvalidExecutionPlanException(errorDTO);
            }
        } finally {
            cleanup(eventProcessorAdminServiceStub);
        }
    }

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

    private EventMappingPropertyDto[] addReceiverMappingToDto(List<MappingProperty> mapProperties) {
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
