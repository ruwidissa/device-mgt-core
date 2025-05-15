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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.apimgt.analytics.extension.AnalyticsArtifactsDeployer;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventPublisherData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventReceiverData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventStreamData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.MetaData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.Property;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventPublisherDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventReceiverDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventStreamDeployerException;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceEventManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.Attribute;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.EventAttributeList;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.TransportType;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.mapping.JSONOutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.MapOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.mapping.JSONInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.WSO2EventInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used for device type integration with DAS, to create streams and receiver dynamically and a common endpoint
 * to retrieve data.
 */
@Path("/events")
public class DeviceEventManagementServiceImpl implements DeviceEventManagementService {

    private static final Log log = LogFactory.getLog(DeviceEventManagementServiceImpl.class);

    private static final String DEFAULT_EVENT_STORE_NAME = "EVENT_STORE";
    private static final String DEFAULT_WEBSOCKET_PUBLISHER_ADAPTER_TYPE = "secured-websocket";
    private static final String OAUTH_MQTT_ADAPTER_TYPE = "oauth-mqtt";
    private static final String THRIFT_ADAPTER_TYPE = "iot-event";
    private static final String DEFAULT_DEVICE_ID_ATTRIBUTE = "deviceId";
    private static final String DEFAULT_META_DEVICE_ID_ATTRIBUTE = "meta_deviceId";
    private static final String MQTT_CONTENT_TRANSFORMER = "device-meta-transformer";
    private static final String MQTT_CONTENT_TRANSFORMER_TYPE = "contentTransformer";
    private static final String MQTT_CONTENT_VALIDATOR_TYPE = "contentValidator";
    private static final String MQTT_CONTENT_VALIDATOR = "default";
    private static final String TIMESTAMP_FIELD_NAME = "_timestamp";
    private static final String DEVICE_ID = "${deviceId}";
    private static final String DEVICE_TYPE = "${deviceType}";
    private static final String TENANT_DOMAIN = "${tenantDomain}";

    // Constants for Stream and Event Definitions
    private static final String EVENT_ADAPTER_TYPE_RDBMS = "rdbms";
    private static final String EVENT_ADAPTER_TYPE_WEBSOCKET = "websocket-local";
    private static final String MAPPING_TYPE_JSON = "json";
    private static final String MAPPING_TYPE_MAP = "map";
    private static final String MAPPING_TYPE_WSO2EVENT = "wso2event";
    private static final String EVENT_DB_DATASOURCE_NAME = "EVENT_DB";
    private static final String COMMON_EVENT_TOPIC_SUFFIX = "/+/events";
    private static final String EXECUTION_MODE_INSERT = "insert";
    private static final String TABLE_PREFIX = "table_";
    private static final String RDBMS_PUBLISHER_SUFFIX = "_rdbms_publisher";
    private static final String WS_PUBLISHER_SUFFIX = "_ws_publisher";
    private static final String RECEIVER_SUFFIX = "-receiver";

//    private static AnalyticsDataAPI getAnalyticsDataAPI() {
//        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        AnalyticsDataAPI analyticsDataAPI =
//                (AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
//        if (analyticsDataAPI == null) {
//            String msg = "Analytics api service has not initialized.";
//            log.error(msg);
//            throw new IllegalStateException(msg);
//        }
//        return analyticsDataAPI;
//    }

//    private static EventRecords getAllEventsForDevice(String tableName, String query, List<SortByField> sortByFields
//            , int offset, int limit) throws AnalyticsException {
//        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//        AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
//        EventRecords eventRecords = new EventRecords();
//        int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
//        if (eventCount == 0) {
//            eventRecords.setCount(0);
//        }
//        List<SearchResultEntry> resultEntries = analyticsDataAPI.search(tenantId, tableName, query, offset, limit,
//                sortByFields);
//        List<String> recordIds = getRecordIds(resultEntries);
//        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
//        eventRecords.setCount(eventCount);
//        List<Record> records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, response);
//        records.sort(new Comparator<Record>() {
//            @Override public int compare(Record r1, Record r2) {
//                return Long.compare(r2.getTimestamp(), r1.getTimestamp());
//            }
//        });
//        eventRecords.setList(records);
//        return eventRecords;
//    }

    //    private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
//        List<String> ids = new ArrayList<>();
//        for (SearchResultEntry searchResult : searchResults) {
//            ids.add(searchResult.getId());
//        }
//        return ids;
//    }
    @GET
    @Path("/{type}")
    @Override
    public Response getDeviceTypeEventDefinitions(@PathParam("type") String deviceType) {
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            List<DeviceTypeEvent> eventDefinitions = DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService().getDeviceTypeEventDefinitions(deviceType);
            return Response.status(Response.Status.OK).entity(eventDefinitions).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while fetching device type event definitions.";
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
    }

    /**
     * Deploy Event Stream, Receiver, Publisher and Store Configuration.
     */
    @POST
    @Path("/{type}")
    @Override
    public Response deployDeviceTypeEventDefinitions(@PathParam("type") String deviceType,
                                                     @QueryParam("skipPersist") boolean skipPersist,
                                                     @QueryParam("isSharedWithAllTenants") boolean isSharedWithAllTenants,
                                                     @Valid List<DeviceTypeEvent> deviceTypeEvents) {
        try {
            // Check if the device type event definitions already exist, If they already exist, return 409 Conflict
            if (DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService()
                    .isDeviceTypeMetaExist(deviceType)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Device type event definitions already exist for device type: " + deviceType)
                        .build();
            }
            if (DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService()
                    .createDeviceTypeMetaWithEvents(deviceType, deviceTypeEvents)) {
                log.info("Device type event definitions updated and metadata created successfully in the database.");
                processDeviceTypeEventDefinitions(deviceType, skipPersist, isSharedWithAllTenants, deviceTypeEvents);
            }
            return Response.ok().entity("Device type event definitions updated and metadata created successfully.").build();
        } catch (DeviceManagementException e) {
            log.error("Error while updating device type metadata with events for device type: " + deviceType, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update device type metadata with events").build();
        }
    }

    @PUT
    @Path("/{type}")
    @Override
    public Response updateDeviceTypeEventDefinitions(@PathParam("type") String deviceType,
                                                     @QueryParam("skipPersist") boolean skipPersist,
                                                     @QueryParam("isSharedWithAllTenants") boolean isSharedWithAllTenants,
                                                     @Valid List<DeviceTypeEvent> deviceTypeEvents) {
        try {
            PaginationRequest request = new PaginationRequest(0, 1);
            request.setDeviceType(deviceType);
            PaginationResult result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesByType(request);
            if (result.getRecordsTotal() == 0) {
                removeDeviceTypeEventFiles(deviceType);
                if (DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService()
                        .updateDeviceTypeMetaWithEvents(deviceType, deviceTypeEvents)) {
                    log.info("Device type event definitions updated and metadata created successfully in the database.");
                    processDeviceTypeEventDefinitions(deviceType, skipPersist, isSharedWithAllTenants, deviceTypeEvents);
                }
            } else {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Device type event definitions are not updated due to devices that are already enrolled.")
                        .build();
            }
            return Response.ok().entity("Device type event definitions updated and metadata updated successfully.").build();
        } catch (DeviceManagementException e) {
            log.error("Error while updating device type metadata with events for device type: " + deviceType, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update device type metadata with events").build();
        }
    }

    /**
     * Processes and deploys event stream, receiver, and publisher configurations for a given device type.
     *
     * <p>This method performs the following tasks for each provided {@link DeviceTypeEvent}:
     * <ul>
     *     <li>Validates event attributes and device type.</li>
     *     <li>Creates and deploys an event stream using {@link AnalyticsArtifactsDeployer}.</li>
     *     <li>Creates and deploys an event receiver based on the transport type (MQTT or Thrift).</li>
     *     <li>If {@code skipPersist} is {@code false}, creates and deploys an RDBMS event publisher.</li>
     *     <li>Always creates and deploys a WebSocket event publisher.</li>
     * </ul>
     *
     * @param deviceType           The name of the device type.
     * @param skipPersist          If {@code true}, skips deploying the RDBMS event publisher.
     * @param isSharedWithAllTenants Indicates whether the event topic is shared across all tenants.
     * @param deviceTypeEvents     A list of {@link DeviceTypeEvent} objects containing event definitions.
     * @return A JAX-RS {@link Response} indicating the result of the operation:
     *         <ul>
     *             <li>{@code 200 OK} if successful.</li>
     *             <li>{@code 400 Bad Request} if the input is invalid.</li>
     *             <li>{@code 500 Internal Server Error} if a deployment step fails.</li>
     *         </ul>
     */

    private Response processDeviceTypeEventDefinitions(String deviceType,
                                                       boolean skipPersist, boolean isSharedWithAllTenants,
                                                       List<DeviceTypeEvent> deviceTypeEvents) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            for (DeviceTypeEvent deviceTypeEvent : deviceTypeEvents) {
                TransportType transportType = deviceTypeEvent.getTransportType();
                EventAttributeList eventAttributes = deviceTypeEvent.getEventAttributeList();
                String eventName = deviceTypeEvent.getEventName();

                if (eventAttributes == null || eventAttributes.getList() == null || eventAttributes.getList().isEmpty() ||
                        deviceType == null || transportType == null ||
                        !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                    String errorMessage = String.format("Invalid Payload: deviceType=%s, eventName=%s, tenantId=%d",
                            deviceType, eventName, tenantId);
                    log.error(errorMessage);
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                // event stream
                String streamName = DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain, eventName);
                AnalyticsArtifactsDeployer artifactsDeployer = new AnalyticsArtifactsDeployer();
                List<Property> props = new ArrayList<>();
                for (Attribute attribute : eventAttributes.getList()) {
                    props.add(new Property(attribute.getName(), attribute.getType().name()));
                }
                EventStreamData eventStreamData = new EventStreamData();
                eventStreamData.setName(streamName);
                eventStreamData.setVersion(Constants.DEFAULT_STREAM_VERSION);
                eventStreamData.setMetaData(new MetaData(DEFAULT_DEVICE_ID_ATTRIBUTE, "STRING"));
                eventStreamData.setPayloadData(props);
                try {
                    artifactsDeployer.deployEventStream(eventStreamData, tenantId);
                } catch (EventStreamDeployerException e) {
                    String msg = String.format("Failed to deploy event stream for deviceType=%s, eventName=%s, tenantId=%d",
                            deviceType, eventName, tenantId);
                    log.error(msg, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }

                // event receiver
                String receiverName = getReceiverName(deviceType, tenantDomain, transportType, eventName);
                EventReceiverData receiverData = new EventReceiverData();
                receiverData.setName(receiverName);
                receiverData.setStreamName(streamName);
                receiverData.setStreamVersion(Constants.DEFAULT_STREAM_VERSION);
                List<Property> propertyList = new ArrayList<>();
                if (transportType == TransportType.MQTT) {
                    receiverData.setEventAdapterType(OAUTH_MQTT_ADAPTER_TYPE);
                    propertyList.add(new Property(MQTT_CONTENT_TRANSFORMER_TYPE, MQTT_CONTENT_TRANSFORMER));
                    propertyList.add(new Property(MQTT_CONTENT_VALIDATOR_TYPE, MQTT_CONTENT_VALIDATOR));
                    String topic;
                    if (!StringUtils.isEmpty(deviceTypeEvent.getEventTopicStructure())) {
                        if (isSharedWithAllTenants) {
                            topic = deviceTypeEvent.getEventTopicStructure().replace(DEVICE_ID, "+")
                                    .replace(DEVICE_TYPE, deviceType)
                                    .replace(TENANT_DOMAIN, "+");
                        } else {
                            topic = deviceTypeEvent.getEventTopicStructure().replace(DEVICE_ID, "+")
                                    .replace(DEVICE_TYPE, deviceType)
                                    .replace(TENANT_DOMAIN, tenantDomain);
                        }
                    } else {
                        if (isSharedWithAllTenants) {
                            topic = "+/" + deviceType + COMMON_EVENT_TOPIC_SUFFIX;
                        } else {
                            topic = tenantDomain + "/" + deviceType + COMMON_EVENT_TOPIC_SUFFIX;
                        }
                    }
                    propertyList.add(new Property("topic", topic));
                    receiverData.setCustomMappingType(MAPPING_TYPE_JSON);

                } else {
                    receiverData.setEventAdapterType(THRIFT_ADAPTER_TYPE);
                    propertyList.add(new Property("events.duplicated.in.cluster", "false"));
                    receiverData.setCustomMappingType(MAPPING_TYPE_WSO2EVENT);
                }
                receiverData.setPropertyList(propertyList);
                try {
                    artifactsDeployer.deployEventReceiver(receiverData, tenantId);
                } catch (EventReceiverDeployerException e) {
                    String msg = String.format("Failed to deploy event receiver for deviceType=%s, eventName=%s, tenantId=%d",
                            deviceType, eventName, tenantId);
                    log.error(msg, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }

                if (!skipPersist) {
                    // rdbms event publisher
                    String rdbmsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + RDBMS_PUBLISHER_SUFFIX;

                    EventPublisherData eventPublisherData = new EventPublisherData();
                    eventPublisherData.setName(rdbmsPublisherName);
                    eventPublisherData.setStreamName(streamName);
                    eventPublisherData.setStreamVersion(Constants.DEFAULT_STREAM_VERSION);
                    eventPublisherData.setEventAdaptorType(EVENT_ADAPTER_TYPE_RDBMS);
                    eventPublisherData.setCustomMappingType(MAPPING_TYPE_MAP);
                    List<Property> publisherProps = new ArrayList<>();
                    publisherProps.add(new Property("datasource.name", EVENT_DB_DATASOURCE_NAME));
                    publisherProps.add(new Property("table.name", TABLE_PREFIX + rdbmsPublisherName.replace(".", "")));
                    publisherProps.add(new Property("execution.mode", EXECUTION_MODE_INSERT));
                    eventPublisherData.setPropertyList(publisherProps);
                    try {
                        artifactsDeployer.deployEventPublisher(eventPublisherData, tenantId);
                    } catch (EventPublisherDeployerException e) {
                        String msg = String.format("Failed to deploy RDBMS event publisher for deviceType=%s, eventName=%s, tenantId=%d",
                                deviceType, eventName, tenantId);
                        log.error(msg, e);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                    }
                }

                // web socket event publisher
                String wsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + WS_PUBLISHER_SUFFIX;
                EventPublisherData wsEventPublisherData = new EventPublisherData();
                wsEventPublisherData.setName(wsPublisherName);
                wsEventPublisherData.setStreamName(streamName);
                wsEventPublisherData.setStreamVersion(Constants.DEFAULT_STREAM_VERSION);
                wsEventPublisherData.setEventAdaptorType(EVENT_ADAPTER_TYPE_WEBSOCKET);
                wsEventPublisherData.setCustomMappingType(MAPPING_TYPE_JSON);
                try {
                    artifactsDeployer.deployEventPublisher(wsEventPublisherData, tenantId);
                } catch (EventPublisherDeployerException e) {
                    String msg = String.format("Failed to deploy WebSocket event publisher for deviceType=%s, eventName=%s, tenantId=%d",
                            deviceType, eventName, tenantId);
                    log.error(msg, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }
            }
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            String msg = String.format("Failed to access device management service for tenantDomain=%s, tenantId=%d", tenantDomain, tenantId);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/{type}")
    @Override
    public Response deleteDeviceTypeEventDefinitions(@PathParam("type") String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            // Validate device type
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type for deletion";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            PaginationRequest request = new PaginationRequest(0, 1);
            request.setDeviceType(deviceType);
            PaginationResult result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesByType(request);
            if (result.getRecordsTotal() == 0) {
                // Remove artifacts from file system
                removeDeviceTypeEventFiles(deviceType);
                // Remove metadata from the database
                DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService().deleteDeviceTypeEventDefinitions(deviceType);
            } else {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Device type event definitions are not deleted due to devices that are already enrolled.")
                        .build();
            }
            return Response.ok().entity("Device type event definitions deleted successfully").build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Failed to delete device type event definitions: " + e.getMessage();
            log.error(errorMessage + ", tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }

    /**
     * Removes all event-related artifacts (event streams, receivers, and publishers) associated with a given device type.
     *
     * <p>This includes undeploying:
     * <ul>
     *     <li>WebSocket event publishers</li>
     *     <li>RDBMS event publishers</li>
     *     <li>Event receivers (based on the transport type)</li>
     *     <li>Event streams</li>
     * </ul>
     *
     * <p>If no events are defined for the device type, a {@code 404 Not Found} response is returned.</p>
     *
     * @param deviceType The device type for which the event artifacts should be removed.
     * @return A JAX-RS {@link Response} indicating the result of the operation:
     *         <ul>
     *             <li>{@code 200 OK} if all artifacts were successfully removed.</li>
     *             <li>{@code 404 Not Found} if no events are defined for the given device type.</li>
     *         </ul>
     * @throws DeviceManagementException If an error occurs while accessing the event definitions or during undeployment.
     */

    private Response removeDeviceTypeEventFiles(String deviceType) throws
            DeviceManagementException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        // Fetch existing events for the device type
        List<DeviceTypeEvent> eventDefinitions = DeviceMgtAPIUtils.getDeviceTypeEventManagementProviderService()
                .getDeviceTypeEventDefinitions(deviceType);
        if (eventDefinitions.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No events found for the given device type").build();
        }
        AnalyticsArtifactsDeployer artifactsDeployer = new AnalyticsArtifactsDeployer();

        // Loop through the events to remove associated artifacts
        for (DeviceTypeEvent eventDefinition : eventDefinitions) {
            String eventName = eventDefinition.getEventName();
            TransportType transportType = eventDefinition.getTransportType();

            String streamName = DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain, eventName);

            // Remove event publishers
            String wsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + WS_PUBLISHER_SUFFIX;
            artifactsDeployer.undeployEventPublisher(wsPublisherName, tenantId);
            log.info("Removed event publisher: " + wsPublisherName);

            String rdbmsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + RDBMS_PUBLISHER_SUFFIX;
            artifactsDeployer.undeployEventPublisher(rdbmsPublisherName, tenantId);
            log.info("Removed event publisher: " + rdbmsPublisherName);

            // Remove event receiver
            String receiverName = getReceiverName(deviceType, tenantDomain, transportType, eventName);
            artifactsDeployer.undeployEventReceiver(receiverName, tenantId);
            log.info("Removed event receiver: " + receiverName);

            // Remove event stream
            artifactsDeployer.undeployEventStream(streamName, tenantId);
            log.info("Removed event stream: " + streamName);
        }
        return Response.ok().build();
    }

    /**
     * Returns device specific data for the give period of time.
     */
//    @GET
//    @Path("/{type}/{deviceId}")
//    @Override
//    public Response getData(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
//                            @QueryParam("to") long to, @PathParam("type") String deviceType, @QueryParam("offset")
//                                    int offset, @QueryParam("limit") int limit) {
//        if (from == 0 || to == 0) {
//            String errorMessage = "Invalid values for from/to";
//            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//        }
//        if (limit == 0) {
//            String errorMessage = "Invalid values for offset/limit";
//            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//        }
//        String fromDate = String.valueOf(from);
//        String toDate = String.valueOf(to);
//        String query = DEFAULT_META_DEVICE_ID_ATTRIBUTE + ":" + deviceId
//                + " AND _timestamp : [" + fromDate + " TO " + toDate + "]";
//        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//        String sensorTableName = getTableName(DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain));
//        try {
//            if (deviceType == null ||
//                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
//                String errorMessage = "Invalid device type";
//                log.error(errorMessage);
//                return Response.status(Response.Status.BAD_REQUEST).build();
//            }
//            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
//                    new DeviceIdentifier(deviceId, deviceType))) {
//                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
//            }
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField(TIMESTAMP_FIELD_NAME, SortType.DESC);
//            sortByFields.add(sortByField);
//            EventRecords eventRecords = getAllEventsForDevice(sensorTableName, query, sortByFields, offset, limit);
//            return Response.status(Response.Status.OK.getStatusCode()).entity(eventRecords).build();
//        } catch (AnalyticsException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        } catch (DeviceAccessAuthorizationException e) {
//            log.error(e.getErrorMessage(), e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DeviceManagementException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        }
//    }

    /**
     * Returns last known data points up to the limit if limit is specified. Otherwise returns last known data point.
     * Limit parameter needs to be zero or positive.
     */

//    @GET
//    @Path("/last-known/{type}/{deviceId}")
//    @Override
//    public Response getLastKnownData(@PathParam("deviceId") String deviceId, @PathParam("type") String deviceType, @QueryParam("limit") int limit) {
//        String query = DEFAULT_META_DEVICE_ID_ATTRIBUTE + ":" + deviceId;
//        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//        String sensorTableName = getTableName(DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain));
//        try {
//            if (deviceType == null ||
//                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
//                String errorMessage = "Invalid device type";
//                log.error(errorMessage);
//                return Response.status(Response.Status.BAD_REQUEST).build();
//            }
//            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
//                    new DeviceIdentifier(deviceId, deviceType))) {
//                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
//            }
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField(TIMESTAMP_FIELD_NAME, SortType.DESC);
//            sortByFields.add(sortByField);
//            if (limit == 0) {
//                EventRecords eventRecords = getAllEventsForDevice(sensorTableName, query, sortByFields, 0, 1);
//                return Response.status(Response.Status.OK.getStatusCode()).entity(eventRecords).build();
//            } else if (limit > 0) {
//                EventRecords eventRecords = getAllEventsForDevice(sensorTableName, query, sortByFields, 0, limit);
//                return Response.status(Response.Status.OK.getStatusCode()).entity(eventRecords).build();
//            } else {
//                String errorMessage = "Invalid limit value";
//                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//            }
//        } catch (AnalyticsException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        } catch (DeviceAccessAuthorizationException e) {
//            log.error(e.getErrorMessage(), e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DeviceManagementException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        }
//    }


    /**
     * Returns the filterd device list. Devices are filterd using the paramter given and the timestamp of the record.
     * parameter should given as a range.
     */
    //todo:amalka
//    @GET
//    @Path("filter/{type}/{parameter}")
//    @Override
//    public Response getFilteredDevices(@PathParam("type") String deviceType, @PathParam("parameter") String parameter,
//                                       @QueryParam("min") double min, @QueryParam("max") double max) {
//        String query;
//        Calendar c = java.util.Calendar.getInstance();
//        long currentTimestamp = c.getTimeInMillis();
//        long previousTimestamp = currentTimestamp - 300 * 1000;
//        String fromDate = String.valueOf(previousTimestamp);
//        String toDate = String.valueOf(currentTimestamp);
//        if (min != 0 & max != 0) {
//            query = parameter + " : [" + min + " TO " + max + "]" +
//                    " AND _timestamp : [" + fromDate + " TO " + toDate + "]";
//        } else {
//            String errorMessage = "The of range values need to be given";
//            log.error(errorMessage);
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//
//        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//        String sensorTableName = getTableName(DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain));
//        try {
//            if (deviceType == null ||
//                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
//                String errorMessage = "Invalid device type";
//                log.error(errorMessage);
//                return Response.status(Response.Status.BAD_REQUEST).build();
//            }
//
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField(TIMESTAMP_FIELD_NAME, SortType.DESC);
//            sortByFields.add(sortByField);
//            EventRecords eventRecords = getAllEventsForDevice(sensorTableName, query, sortByFields, 0, 100);
//            List<Record> filterdEvents = eventRecords.getRecord();
//            List<Record> uniqueFilterdEvents = new ArrayList<Record>();
//            Set<String> devices = new HashSet<>();
//
//            for (int i = 0; i < filterdEvents.size(); i++) {
//                String deviceid = (String) filterdEvents.get(i).getValue("meta_deviceId");
//                if (!devices.contains(deviceid) && DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
//                        new DeviceIdentifier(deviceid, deviceType))) {
//                    devices.add(deviceid);
//                    uniqueFilterdEvents.add(filterdEvents.get(i));
//                }
//            }
//
//            EventRecords filterdRecords = new EventRecords();
//            filterdRecords.setList(uniqueFilterdEvents);
//            return Response.status(Response.Status.OK.getStatusCode()).entity(filterdRecords).build();
//
//        } catch (AnalyticsException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        } catch (DeviceManagementException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
//        } catch (DeviceAccessAuthorizationException e) {
//            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
//            log.error(errorMsg);
//            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
//        }
//    }
    private void publishEventReceivers(String streamName, String version, TransportType transportType
            , String requestedTenantDomain, boolean isSharedWithAllTenants, String deviceType,
                                       String eventTopicStructure, String receiverName) throws EventReceiverConfigurationException {
        EventReceiverService eventReceiverService = DeviceMgtAPIUtils.getEventReceiverService();
        try {
//            TransportType transportTypeToBeRemoved = TransportType.HTTP;
//            if (transportType == TransportType.HTTP) {
//                transportTypeToBeRemoved = TransportType.MQTT;
//            }
//            String eventRecieverNameTobeRemoved = getReceiverName(deviceType, requestedTenantDomain, transportTypeToBeRemoved);
            EventReceiverConfiguration eventReceiverConfiguration =
                    eventReceiverService.getActiveEventReceiverConfiguration(receiverName);
            if (eventReceiverConfiguration != null) {
                eventReceiverService.undeployActiveEventReceiverConfiguration(receiverName);
            }

            InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
            Map<String, String> propertyMap = new HashMap<>();
            if (transportType == TransportType.MQTT) {
                inputEventAdapterConfiguration.setType(OAUTH_MQTT_ADAPTER_TYPE);
                String topic;
                if (!StringUtils.isEmpty(eventTopicStructure)) {
                    if (isSharedWithAllTenants) {
                        topic = eventTopicStructure.replace("${deviceId}", "+")
                                .replace("${deviceType}", deviceType)
                                .replace("${tenantDomain}", "+");
                    } else {
                        topic = eventTopicStructure.replace("${deviceId}", "+")
                                .replace("${deviceType}", deviceType)
                                .replace("${tenantDomain}", requestedTenantDomain);
                    }
                } else {
                    if (isSharedWithAllTenants) {
                        topic = "+/" + deviceType + "/+/events";
                    } else {
                        topic = requestedTenantDomain + "/" + deviceType + "/+/events";
                    }
                }
                propertyMap.put("topic", topic);
                propertyMap.put(MQTT_CONTENT_TRANSFORMER_TYPE, MQTT_CONTENT_TRANSFORMER);
                propertyMap.put(MQTT_CONTENT_VALIDATOR_TYPE, MQTT_CONTENT_VALIDATOR);
            } else {
                inputEventAdapterConfiguration.setType(THRIFT_ADAPTER_TYPE);
                propertyMap.put("events.duplicated.in.cluster", "false");
            }
            inputEventAdapterConfiguration.setProperties(propertyMap);

            if (eventReceiverService.getActiveEventReceiverConfiguration(receiverName) == null) {
                EventReceiverConfiguration configuration = new EventReceiverConfiguration();
                configuration.setEventReceiverName(receiverName);
                configuration.setToStreamName(streamName);
                configuration.setToStreamVersion(version);
                configuration.setFromAdapterConfiguration(inputEventAdapterConfiguration);
                if (transportType == TransportType.MQTT) {
                    JSONInputMapping jsonInputMapping = new JSONInputMapping();
                    jsonInputMapping.setCustomMappingEnabled(false);
                    configuration.setInputMapping(jsonInputMapping);
                    eventReceiverService.deployEventReceiverConfiguration(configuration);
                } else {
                    WSO2EventInputMapping wso2EventInputMapping = new WSO2EventInputMapping();
                    wso2EventInputMapping.setCustomMappingEnabled(false);
                    configuration.setInputMapping(wso2EventInputMapping);
                    eventReceiverService.deployEventReceiverConfiguration(configuration);
                }
            }
        } catch (EventReceiverConfigurationException e) {
            log.error("Error while publishing event receiver", e);
            throw new EventReceiverConfigurationException(e);
        }

    }

    private void publishStreamDefinitons(String streamName, String version, EventAttributeList eventAttributes)
            throws MalformedStreamDefinitionException, EventStreamConfigurationException {
        EventStreamService eventStreamService = DeviceMgtAPIUtils.getEventStreamService();

        try {
            StreamDefinition streamDefinition = new StreamDefinition(streamName, version);

            List<org.wso2.carbon.databridge.commons.Attribute> payloadDataAttributes = new ArrayList<>();
            for (Attribute attribute : eventAttributes.getList()) {
                payloadDataAttributes.add(new org.wso2.carbon.databridge.commons.Attribute(attribute.getName(),
                        org.wso2.carbon.databridge.commons.AttributeType.valueOf(attribute.getType().name())));
            }
            streamDefinition.setPayloadData(payloadDataAttributes);

            List<org.wso2.carbon.databridge.commons.Attribute> metaDataAttributes = new ArrayList<>();
            metaDataAttributes.add(new org.wso2.carbon.databridge.commons.Attribute(DEFAULT_DEVICE_ID_ATTRIBUTE,
                    org.wso2.carbon.databridge.commons.AttributeType.STRING));
            streamDefinition.setMetaData(metaDataAttributes);

            if (eventStreamService.getStreamDefinition(streamDefinition.getStreamId()) != null) {
                eventStreamService.removeEventStreamDefinition(streamName, version);
                eventStreamService.addEventStreamDefinition(streamDefinition);
            } else {
                eventStreamService.addEventStreamDefinition(streamDefinition);
            }

        } catch (MalformedStreamDefinitionException e) {
            log.error("Error while initializing stream definition ", e);
            throw new MalformedStreamDefinitionException(e);
        } catch (EventStreamConfigurationException e) {
            log.error("Error while configuring stream definition ", e);
            throw new EventStreamConfigurationException(e);
        }
    }

    /*

     */

    private void publishEventStore(String streamName, String version, String publisherName)
            throws EventPublisherConfigurationException {

        EventPublisherService eventPublisherService = DeviceMgtAPIUtils.getEventPublisherService();

        try {
            if (eventPublisherService.getActiveEventPublisherConfiguration(publisherName) == null) {
                EventPublisherConfiguration configuration = new EventPublisherConfiguration();
                configuration.setEventPublisherName(publisherName);
                configuration.setFromStreamName(streamName);
                configuration.setFromStreamVersion(version);
                MapOutputMapping mapOutputMapping = new MapOutputMapping();
                mapOutputMapping.setCustomMappingEnabled(false);
                configuration.setOutputMapping(mapOutputMapping);
                OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
                outputEventAdapterConfiguration.setType("rdbms");
                Map<String, String> staticProperties = new HashMap<>();
                staticProperties.put("datasource.name", "EVENT_DB");
                staticProperties.put("execution.mode", "insert");
                staticProperties.put("table.name", "table_" + publisherName.replace(".", ""));
                outputEventAdapterConfiguration.setStaticProperties(staticProperties);
                configuration.setProcessEnabled(true);
                configuration.setToAdapterConfiguration(outputEventAdapterConfiguration);
                eventPublisherService.deployEventPublisherConfiguration(configuration);
            }

        } catch (EventPublisherConfigurationException e) {
            log.error("Error while publishing to rdbms store", e);
            throw new EventPublisherConfigurationException(e);
        }
    }

    private void publishWebsocketPublisherDefinition(String streamName, String version, String publisherName)
            throws EventPublisherConfigurationException {
        EventPublisherService eventPublisherService = DeviceMgtAPIUtils.getEventPublisherService();

        try {
            if (eventPublisherService.getActiveEventPublisherConfiguration(publisherName) == null) {
                EventPublisherConfiguration configuration = new EventPublisherConfiguration();
                configuration.setEventPublisherName(publisherName);
                configuration.setFromStreamName(streamName);
                configuration.setFromStreamVersion(version);
                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();
                jsonOutputMapping.setCustomMappingEnabled(false);
                configuration.setOutputMapping(jsonOutputMapping);
                OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
                outputEventAdapterConfiguration.setType("websocket-local");
                configuration.setToAdapterConfiguration(outputEventAdapterConfiguration);
                eventPublisherService.deployEventPublisherConfiguration(configuration);
            }
        } catch (EventPublisherConfigurationException e) {
            log.error("Error while publishing to websocket-local", e);
            throw new EventPublisherConfigurationException(e);
        }

    }

    private BasicInputAdapterPropertyDto getBasicInputAdapterPropertyDto(String key, String value) {
        BasicInputAdapterPropertyDto basicInputAdapterPropertyDto = new BasicInputAdapterPropertyDto();
        basicInputAdapterPropertyDto.setKey(key);
        basicInputAdapterPropertyDto.setValue(value);
        return basicInputAdapterPropertyDto;
    }

    private String getTableName(String streamName) {
        return streamName.toUpperCase().replace('.', '_');
    }

    private String getReceiverName(String deviceType, String tenantDomain, TransportType transportType) {
        return deviceType.replace(" ", "_").trim() + "-" + tenantDomain + "-" + transportType.toString() + RECEIVER_SUFFIX;
    }

    private String getReceiverName(String deviceType, String tenantDomain, TransportType transportType, String eventName) {
        return eventName + "-" + getReceiverName(deviceType, tenantDomain, transportType);
    }

    private String getPublisherName(String tenantDomain, String deviceType, String eventName) {
        return eventName + "_" + tenantDomain.replace(".", "_") + "_" + deviceType;
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
