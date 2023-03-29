package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import io.entgra.device.mgt.core.apimgt.analytics.extension.AnalyticsArtifactsDeployer;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.*;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventReceiverDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventPublisherDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventStreamDeployerException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Attribute;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AttributeType;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.DeviceTypeEvent;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventAttributeList;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.TransportType;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceEventManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.mapping.JSONOutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.MapOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceCallbackHandler;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.mapping.JSONInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.WSO2EventInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceCallbackHandler;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationDto;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
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

    /**
     * Retrieves the stream definition from das for the given device type.
     *
     * @return dynamic event attribute list
     */
    @GET
    @Path("/{type}")
    @Override
    public Response getDeviceTypeEventDefinition(@PathParam("type") String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub = null;
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String streamName = DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain);
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            EventStreamDefinitionDto eventStreamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(
                    streamName + ":" + Constants.DEFAULT_STREAM_VERSION);
            if (eventStreamDefinitionDto == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            EventStreamAttributeDto[] eventStreamAttributeDtos = eventStreamDefinitionDto.getPayloadData();
            EventAttributeList eventAttributeList = new EventAttributeList();
            List<Attribute> attributes = new ArrayList<>();
            for (EventStreamAttributeDto eventStreamAttributeDto : eventStreamAttributeDtos) {
                attributes.add(new Attribute(eventStreamAttributeDto.getAttributeName()
                        , AttributeType.valueOf(eventStreamAttributeDto.getAttributeType().toUpperCase())));
            }
            eventAttributeList.setList(attributes);

            DeviceTypeEvent deviceTypeEvent = new DeviceTypeEvent();
            deviceTypeEvent.setEventAttributeList(eventAttributeList);
            deviceTypeEvent.setTransportType(TransportType.HTTP);
            eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();
            EventReceiverConfigurationDto eventReceiverConfigurationDto = eventReceiverAdminServiceStub
                    .getActiveEventReceiverConfiguration(getReceiverName(deviceType, tenantDomain, TransportType.MQTT));
            if (eventReceiverConfigurationDto != null) {
                deviceTypeEvent.setTransportType(TransportType.MQTT);
            }
            return Response.ok().entity(deviceTypeEvent).build();
        } catch (AxisFault e) {
            log.error("Failed to retrieve event definitions for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            cleanup(eventStreamAdminServiceStub);
            cleanup(eventReceiverAdminServiceStub);
        }
    }

    /**
     * Deploy Event Stream, Receiver, Publisher and Store Configuration.
     */
    @POST
    @Path("/{type}")
    @Override
    public Response deployDeviceTypeEventDefinition(@PathParam("type") String deviceType,
                                                    @QueryParam("skipPersist") boolean skipPersist,
                                                    @QueryParam("isSharedWithAllTenants") boolean isSharedWithAllTenants,
                                                    @Valid List<DeviceTypeEvent> deviceTypeEvents) {


        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            for (DeviceTypeEvent deviceTypeEvent : deviceTypeEvents) {
                TransportType transportType = deviceTypeEvent.getTransportType();
                EventAttributeList eventAttributes = deviceTypeEvent.getEventAttributeList();
                String eventName = deviceTypeEvent.getEventName();


                if (eventAttributes == null || eventAttributes.getList() == null || eventAttributes.getList().size() == 0 ||
                        deviceType == null || transportType == null ||
                        !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                    String errorMessage = "Invalid Payload";
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
                artifactsDeployer.deployEventStream(eventStreamData, tenantId);

                // event receiver
                String receiverName = getReceiverName(deviceType, tenantDomain, transportType, eventName);
                EventReceiverData receiverData  = new EventReceiverData();
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
                            topic = deviceTypeEvent.getEventTopicStructure().replace("${deviceId}", "+")
                                    .replace("${deviceType}", deviceType)
                                    .replace("${tenantDomain}", "+");
                        } else {
                            topic = deviceTypeEvent.getEventTopicStructure().replace("${deviceId}", "+")
                                    .replace("${deviceType}", deviceType)
                                    .replace("${tenantDomain}", tenantDomain);
                        }
                    } else {
                        if (isSharedWithAllTenants) {
                            topic = "+/" + deviceType + "/+/events";
                        } else {
                            topic = tenantDomain + "/" + deviceType + "/+/events";
                        }
                    }
                    propertyList.add(new Property("topic", topic));
                    receiverData.setCustomMappingType("json");

                } else {
                    receiverData.setEventAdapterType(THRIFT_ADAPTER_TYPE);
                    propertyList.add(new Property("events.duplicated.in.cluster", "false"));
                    receiverData.setCustomMappingType("wso2event");
                }
                receiverData.setPropertyList(propertyList);
                artifactsDeployer.deployEventReceiver(receiverData, tenantId);

                if (!skipPersist) {
                    // rdbms event publisher
                    String rdbmsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + "_rdbms_publisher";

                    EventPublisherData eventPublisherData = new EventPublisherData();
                    eventPublisherData.setName(rdbmsPublisherName);
                    eventPublisherData.setStreamName(streamName);
                    eventPublisherData.setStreamVersion(Constants.DEFAULT_STREAM_VERSION);
                    eventPublisherData.setEventAdaptorType("rdbms");
                    eventPublisherData.setCustomMappingType("map");
                    List<Property> publisherProps = new ArrayList<>();
                    publisherProps.add(new Property("datasource.name", "EVENT_DB"));
                    publisherProps.add(new Property("table.name", "table_" + rdbmsPublisherName.replace(".", "")));
                    publisherProps.add(new Property("execution.mode", "insert"));
                    eventPublisherData.setPropertyList(publisherProps);
                    artifactsDeployer.deployEventPublisher(eventPublisherData, tenantId);
                }

                // web socket event publisher
                String wsPublisherName = getPublisherName(deviceType, tenantDomain, eventName) + "_ws_publisher";
                EventPublisherData wsEventPublisherData = new EventPublisherData();
                wsEventPublisherData.setName(wsPublisherName);
                wsEventPublisherData.setStreamName(streamName);
                wsEventPublisherData.setStreamVersion(Constants.DEFAULT_STREAM_VERSION);
                wsEventPublisherData.setEventAdaptorType("websocket-local");
                wsEventPublisherData.setCustomMappingType("json");
                artifactsDeployer.deployEventPublisher(wsEventPublisherData, tenantId);

            }
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (EventStreamDeployerException e) {
            log.error("Failed while deploying event stream definition, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (EventPublisherDeployerException e) {
            log.error("Failed while deploying event publisher, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (EventReceiverDeployerException e) {
            log.error("Failed while deploying event receiver, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete device type specific artifacts from DAS.
     */
    @DELETE
    @Path("/{type}")
    @Override
    public Response deleteDeviceTypeEventDefinitions(@PathParam("type") String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventReceiverAdminServiceStub eventReceiverAdminServiceStub = null;
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = null;
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;

        EventReceiverAdminServiceStub tenantBasedEventReceiverAdminServiceStub = null;
        EventStreamAdminServiceStub tenantBasedEventStreamAdminServiceStub = null;
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            String eventPublisherName = deviceType.trim().replace(" ", "_") + "_websocket_publisher";
            String streamName = DeviceMgtAPIUtils.getStreamDefinition(deviceType, tenantDomain);
            eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
            if (eventStreamAdminServiceStub.getStreamDefinitionDto(streamName + ":" + Constants.DEFAULT_STREAM_VERSION) == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            eventStreamAdminServiceStub.removeEventStreamDefinition(streamName, Constants.DEFAULT_STREAM_VERSION);
            EventReceiverAdminServiceCallbackHandler eventReceiverAdminServiceCallbackHandler =
                    new EventReceiverAdminServiceCallbackHandler() {
                    };
            EventPublisherAdminServiceCallbackHandler eventPublisherAdminServiceCallbackHandler =
                    new EventPublisherAdminServiceCallbackHandler() {
                    };


            String eventReceiverName = getReceiverName(deviceType, tenantDomain, TransportType.MQTT);
            eventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();
            if (eventReceiverAdminServiceStub.getInactiveEventReceiverConfigurationContent(eventReceiverName) == null) {
                eventReceiverName = getReceiverName(deviceType, tenantDomain, TransportType.HTTP);
            }
            eventReceiverAdminServiceStub.startundeployInactiveEventReceiverConfiguration(eventReceiverName
                    , eventReceiverAdminServiceCallbackHandler);

            eventPublisherAdminServiceStub = DeviceMgtAPIUtils.getEventPublisherAdminServiceStub();
            eventPublisherAdminServiceStub.startundeployInactiveEventPublisherConfiguration(eventPublisherName
                    , eventPublisherAdminServiceCallbackHandler);

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    tenantBasedEventReceiverAdminServiceStub = DeviceMgtAPIUtils.getEventReceiverAdminServiceStub();
                    tenantBasedEventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
                    tenantBasedEventStreamAdminServiceStub.removeEventStreamDefinition(streamName,
                            Constants.DEFAULT_STREAM_VERSION);

                    tenantBasedEventReceiverAdminServiceStub.startundeployInactiveEventReceiverConfiguration(
                            eventReceiverName, eventReceiverAdminServiceCallbackHandler);

                }
            } finally {
                cleanup(tenantBasedEventReceiverAdminServiceStub);
                cleanup(tenantBasedEventStreamAdminServiceStub);
                PrivilegedCarbonContext.endTenantFlow();
            }
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("Failed to delete event definitions for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            cleanup(eventStreamAdminServiceStub);
            cleanup(eventPublisherAdminServiceStub);
            cleanup(eventReceiverAdminServiceStub);
            cleanup(eventReceiverAdminServiceStub);
            cleanup(eventStreamAdminServiceStub);
        }
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
            log.error("Error while publishing event receiver" , e);
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
            log.error("Error while initializing stream definition " , e);
            throw new MalformedStreamDefinitionException(e);
        } catch (EventStreamConfigurationException e) {
            log.error("Error while configuring stream definition " , e);
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
            log.error("Error while publishing to rdbms store" , e);
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
            log.error("Error while publishing to websocket-local" , e);
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
        return deviceType.replace(" ", "_").trim() + "-" + tenantDomain + "-" + transportType.toString() + "-receiver";
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
