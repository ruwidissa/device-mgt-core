/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
//import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
//import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
//import org.wso2.carbon.analytics.dataservice.commons.SortByField;
//import org.wso2.carbon.analytics.dataservice.commons.SortType;
//import org.wso2.carbon.analytics.datasource.commons.Record;
//import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.common.geo.service.AlertAlreadyExistException;
import org.wso2.carbon.device.mgt.common.geo.service.Event;
import org.wso2.carbon.device.mgt.common.geo.service.GeoCluster;
import org.wso2.carbon.device.mgt.common.geo.service.GeoCoordinate;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFence;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeoQuery;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy.GeoHashLengthStrategy;
import org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy.ZoomGeoHashLengthStrategy;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.EventAction;
import org.wso2.carbon.device.mgt.jaxrs.beans.GeofenceWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GeoLocationBasedService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The api for
 */
public class GeoLocationBasedServiceImpl implements GeoLocationBasedService {

    private static final Log log = LogFactory.getLog(GeoLocationBasedServiceImpl.class);

    @Path("stats/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoDeviceStats(@PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @QueryParam("from") long from, @QueryParam("to") long to) {
        try {
            if (!DeviceManagerUtil.isPublishLocationResponseEnabled()) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                        .entity("Unable to retrive Geo Device stats. Geo Data publishing does not enabled.").build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage()).build();
        }
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_FUSEDSPATIALEVENT";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "id:" + deviceId + " AND type:" + deviceType;
        if (from != 0 || to != 0) {
            query += " AND timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
//        try {
//            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
//                    new DeviceIdentifier(deviceId, deviceType),
//                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
//                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
//            }
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
//            sortByFields.add(sortByField);

            // this is the user who initiates the request
//            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
//                    CarbonContext.getThreadLocalCarbonContext().getUsername());

//            try {
//                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//                int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
                AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
//                List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
//                        0,
//                        100,
//                        sortByFields);
//                List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
//                        searchResults);
//                return Response.ok().entity(events).build();
//            } catch (AnalyticsException | UserStoreException e) {
//                log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
//                throw DeviceMgtUtil.buildBadRequestException(
//                        Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
//            }
//        } catch (DeviceAccessAuthorizationException e) {
//            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
//        }
    }

    @Path("stats/device-locations")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Deprecated
    public Response getGeoDeviceLocations(
            @QueryParam("deviceType") String deviceType,
            @QueryParam("minLat") double minLat,
            @QueryParam("maxLat") double maxLat,
            @QueryParam("minLong") double minLong,
            @QueryParam("maxLong") double maxLong,
            @QueryParam("zoom") int zoom) {
        GeoHashLengthStrategy geoHashLengthStrategy = new ZoomGeoHashLengthStrategy();
        GeoCoordinate southWest = new GeoCoordinate(minLat, minLong);
        GeoCoordinate northEast = new GeoCoordinate(maxLat, maxLong);
        int geohashLength = geoHashLengthStrategy.getGeohashLength(southWest, northEast, zoom);
        DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
        GeoQuery geoQuery = new GeoQuery(southWest, northEast, geohashLength);
        if (deviceType != null) {
            geoQuery.setDeviceTypes(Collections.singletonList(deviceType));
        }
        List<org.wso2.carbon.device.mgt.jaxrs.beans.GeoCluster> geoClusters = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            List<GeoCluster> newClusters = deviceManagementService.findGeoClusters(geoQuery);
            org.wso2.carbon.device.mgt.jaxrs.beans.GeoCluster geoCluster;
            String deviceIdentification = null;
            String deviceName = null;
            String lastSeen = null;
            for (GeoCluster gc : newClusters) {
                if (gc.getDevice() != null) {
                    deviceIdentification = gc.getDevice().getDeviceIdentifier();
                    deviceName = gc.getDevice().getName();
                    deviceType = gc.getDevice().getType();
                    lastSeen = simpleDateFormat.format(new Date(gc.getDevice()
                            .getEnrolmentInfo().getDateOfLastUpdate()));
                }
                geoCluster = new org.wso2.carbon.device.mgt.jaxrs.beans.GeoCluster(gc.getCoordinates(),
                        gc.getSouthWestBound(), gc.getNorthEastBound(), gc.getCount(), gc.getGeohashPrefix(),
                        deviceIdentification, deviceName, deviceType, lastSeen);
                geoClusters.add(geoCluster);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving geo clusters query: " + new Gson().toJson(geoQuery);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(geoClusters).build();
    }

    @Path("stats/geo-view")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoDeviceView(
            @QueryParam("minLat") double minLat,
            @QueryParam("maxLat") double maxLat,
            @QueryParam("minLong") double minLong,
            @QueryParam("maxLong") double maxLong,
            @QueryParam("zoom") int zoom,
            @QueryParam("deviceType") List<String> deviceTypes,
            @QueryParam("deviceIdentifier") List<String> deviceIdentifiers,
            @QueryParam("status") List<EnrolmentInfo.Status> statuses,
            @QueryParam("ownership") List<String> ownerships,
            @QueryParam("owner") List<String> owners,
            @QueryParam("noClusters") boolean noClusters,
            @QueryParam("createdBefore") long createdBefore,
            @QueryParam("createdAfter") long createdAfter,
            @QueryParam("updatedBefore") long updatedBefore,
            @QueryParam("updatedAfter") long updatedAfter) {

        GeoHashLengthStrategy geoHashLengthStrategy = new ZoomGeoHashLengthStrategy();
        GeoCoordinate southWest = new GeoCoordinate(minLat, minLong);
        GeoCoordinate northEast = new GeoCoordinate(maxLat, maxLong);
        int geohashLength = geoHashLengthStrategy.getGeohashLength(southWest, northEast, zoom);
        DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
        List<GeoCluster> geoClusters;
        GeoQuery geoQuery = new GeoQuery(southWest, northEast, geohashLength);
        geoQuery.setDeviceTypes(deviceTypes);
        geoQuery.setDeviceIdentifiers(deviceIdentifiers);
        geoQuery.setStatuses(statuses);
        geoQuery.setOwners(owners);
        geoQuery.setOwnerships(ownerships);
        geoQuery.setNoClusters(noClusters);
        geoQuery.setCreatedBefore(createdBefore);
        geoQuery.setCreatedAfter(createdAfter);
        geoQuery.setUpdatedBefore(updatedBefore);
        geoQuery.setUpdatedAfter(updatedAfter);
        try {
            geoClusters = deviceManagementService.findGeoClusters(geoQuery);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving geo clusters for query: " + new Gson().toJson(geoQuery);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(geoClusters).build();
    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGeoAlerts(Alert alert, @PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.createGeoAlert(alert, identifier, alertType, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }


    @Path("alerts/{alertType}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGeoAlertsForGeoClusters(Alert alert, @PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.createGeoAlert(alert, alertType);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating " + alertType + " alert";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }


    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGeoAlerts(Alert alert, @PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.updateGeoAlert(alert, identifier, alertType, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGeoAlertsForGeoClusters(Alert alert, @PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.updateGeoAlert(alert, alertType);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while updating the geo alert for geo clusters";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public Response removeGeoAlerts(@PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @PathParam("alertType") String alertType,
            @QueryParam("queryName") String queryName) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.removeGeoAlert(alertType, identifier, queryName, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while removing the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public Response removeGeoAlertsForGeoClusters(@PathParam("alertType") String alertType, @QueryParam("queryName") String queryName) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.removeGeoAlert(alertType, queryName);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while removing the geo alert for geo clusters";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlerts(@PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();

            if (GeoServices.ALERT_TYPE_WITHIN.equals(alertType)) {
                List<GeoFence> alerts = geoService.getWithinAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_EXIT.equals(alertType)) {
                List<GeoFence> alerts = geoService.getExitAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_SPEED.equals(alertType)) {
                String result = geoService.getSpeedAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_PROXIMITY.equals(alertType)) {
                String result = geoService.getProximityAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_STATIONARY.equals(alertType)) {
                List<GeoFence> alerts = geoService.getStationaryAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_TRAFFIC.equals(alertType)) {
                List<GeoFence> alerts = geoService.getTrafficAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            }
            return null;
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while getting the geo alerts for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsForGeoClusters(@PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            List<GeoFence> alerts = null;
            String result = null;

            switch (alertType) {
            case GeoServices.ALERT_TYPE_WITHIN:
                alerts = geoService.getWithinAlerts();
                break;
            case GeoServices.ALERT_TYPE_EXIT:
                alerts = geoService.getExitAlerts();
                break;
            case GeoServices.ALERT_TYPE_STATIONARY:
                alerts = geoService.getStationaryAlerts();
                break;
            case GeoServices.ALERT_TYPE_TRAFFIC:
                alerts = geoService.getTrafficAlerts();
                break;
            case GeoServices.ALERT_TYPE_SPEED:
                result = geoService.getSpeedAlerts();
                return Response.ok().entity(result).build();
            case GeoServices.ALERT_TYPE_PROXIMITY:
                result = geoService.getProximityAlerts();
                return Response.ok().entity(result).build();
            default:
                throw new GeoLocationBasedServiceException("Invalid Alert Type");
            }
            return Response.ok().entity(alerts).build();

        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while getting the geo alerts for " + alertType + " alert";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Path("alerts/history/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsHistory(@PathParam("deviceId") String deviceId,
            @PathParam("deviceType") String deviceType,
            @QueryParam("from") long from, @QueryParam("to") long to) {
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_ALERTNOTIFICATIONS";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "id:" + deviceId + " AND type:" + deviceType;
        if (from != 0 || to != 0) {
            query += " AND timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
//        try {
//            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
//                    new DeviceIdentifier(deviceId, deviceType),
//                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
//                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
//            }
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
//            sortByFields.add(sortByField);

            // this is the user who initiates the request
//            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
//                    CarbonContext.getThreadLocalCarbonContext().getUsername());

//            try {
//                String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
//                int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
//                AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
//                List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
//                        0,
//                        100,
//                        sortByFields);
//                List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
//                        searchResults);
//                return Response.ok().entity(events).build();
//            } catch (AnalyticsException | UserStoreException e) {
//                log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
//                throw DeviceMgtUtil.buildBadRequestException(
//                        Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
//            }
//        } catch (DeviceAccessAuthorizationException e) {
//            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
//        }
    }

    @Path("alerts/history")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsHistoryForGeoClusters(@QueryParam("from") long from, @QueryParam("to") long to) {
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_ALERTNOTIFICATIONS";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "";
        if (from != 0 || to != 0) {
            query = "timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
//        try {
//            List<SortByField> sortByFields = new ArrayList<>();
//            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
//            sortByFields.add(sortByField);
//
//            // this is the user who initiates the request
//            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
//                    CarbonContext.getThreadLocalCarbonContext().getUsername());
//
//            String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
//            int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
//            AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
//            List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
//                    0,
//                    100,
//                    sortByFields);
//            List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
//                    searchResults);
//            return Response.ok().entity(events).build();
//
//        } catch (AnalyticsException | UserStoreException e) {
//            log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
            throw DeviceMgtUtil.buildBadRequestException(
                    Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
//        }
    }

//    private List<Event> getEventBeans(AnalyticsDataAPI analyticsDataAPI, int tenantId, String tableName,
//            List<String> columns,
//            List<SearchResultEntry> searchResults) throws AnalyticsException {
//        List<String> ids = getIds(searchResults);
//        List<String> requiredColumns = (columns == null || columns.isEmpty()) ? null : columns;
//        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, requiredColumns, ids);
//        List<Record> records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, response);
//        Map<String, Event> eventBeanMap = getEventBeanKeyedWithIds(records);
//        return getSortedEventBeans(eventBeanMap, searchResults);
//    }
//
//    private List<Event> getSortedEventBeans(Map<String, Event> eventBeanMap,
//            List<SearchResultEntry> searchResults) {
//        List<Event> sortedRecords = new ArrayList<>();
//        for (SearchResultEntry entry : searchResults) {
//            sortedRecords.add(eventBeanMap.get(entry.getId()));
//        }
//        return sortedRecords;
//    }
//
//    private Map<String, Event> getEventBeanKeyedWithIds(List<Record> records) {
//        Map<String, Event> eventBeanMap = new HashMap<>();
//        for (Record record : records) {
//            Event event = getEventBean(record);
//            eventBeanMap.put(event.getId(), event);
//        }
//        return eventBeanMap;
//    }
//
//    private List<String> getIds(List<SearchResultEntry> searchResults) {
//        List<String> ids = new ArrayList<>();
//        if (searchResults != null) {
//            for (SearchResultEntry resultEntry : searchResults) {
//                ids.add(resultEntry.getId());
//            }
//        }
//        return ids;
//    }
//
//    private static Event getEventBean(Record record) {
//        Event eventBean = new Event();
//        eventBean.setId(record.getId());
//        eventBean.setTableName(record.getTableName());
//        eventBean.setTimestamp(record.getTimestamp());
//        eventBean.setValues(record.getValues());
//        return eventBean;
//    }

    @Path("/geo-fence")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGeofence(GeofenceWrapper geofenceWrapper) {
        RequestValidationUtil.validateGeofenceData(geofenceWrapper);
        RequestValidationUtil.validateEventConfigurationData(geofenceWrapper.getEventConfig());
        try {
            GeofenceData geofenceData = mapRequestGeofenceData(geofenceWrapper);
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            if (!geoService.createGeofence(geofenceData)) {
                String msg = "Failed to create geofence";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            return Response.status(Response.Status.CREATED).entity("Geo Fence record created successfully").build();
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Failed to create geofence";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (EventConfigurationException e) {
            String msg = "Failed to create event configuration for Geo Fence";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Extract request event data from the payload and attach it to the DTO
     * @param eventConfig request event payload
     * @return generated event beans list according to the payload data
     */
    private List<EventConfig> mapRequestEvent(List<org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig> eventConfig) {
        List<EventConfig> savingEventList = new ArrayList<>();
        for (org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig event : eventConfig) {
            EventConfig savingConfig = new EventConfig();
            if (event.getId() > 0) {
                savingConfig.setEventId(event.getId());
            } else {
                savingConfig.setEventId(-1);
            }
            savingConfig.setEventLogic(event.getEventLogic());
            String eventJson = new Gson().toJson(event.getActions());
            savingConfig.setActions(eventJson);
            savingEventList.add(savingConfig);
        }
        return savingEventList;
    }

    @Path("/geo-fence/{fenceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeofence(@PathParam("fenceId") int fenceId,
            @QueryParam("requireEventData") boolean requireEventData) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            GeofenceData geofenceData = geoService.getGeoFences(fenceId);
            if (geofenceData == null) {
                String msg = "No valid Geofence found for ID " + fenceId;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (requireEventData) {
                List<EventConfig> eventsOfGeoFence = geoService.getEventsOfGeoFence(geofenceData.getId());
                geofenceData.setEventConfig(eventsOfGeoFence);
            }
            return Response.status(Response.Status.OK).entity(getMappedResponseBean(geofenceData)).build();
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Server error occurred while retrieving Geofence for Id " + fenceId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Wrap geofence data retrieved from DB into Response Bean
     * @param geofenceData retrieved data fromDB
     * @return Response bean with geofence data
     */
    private GeofenceWrapper getMappedResponseBean(GeofenceData geofenceData) {
        GeofenceWrapper geofenceWrapper = new GeofenceWrapper();
        geofenceWrapper.setId(geofenceData.getId());
        geofenceWrapper.setFenceName(geofenceData.getFenceName());
        geofenceWrapper.setDescription(geofenceData.getDescription());
        geofenceWrapper.setLatitude(geofenceData.getLatitude());
        geofenceWrapper.setLongitude(geofenceData.getLongitude());
        geofenceWrapper.setRadius(geofenceData.getRadius());
        geofenceWrapper.setGeoJson(geofenceData.getGeoJson());
        geofenceWrapper.setFenceShape(geofenceData.getFenceShape());
        if (geofenceData.getGroupIds() != null && !geofenceData.getGroupIds().isEmpty()) {
            geofenceWrapper.setGroupIds(geofenceData.getGroupIds());
        }
        if (geofenceData.getGroupData() != null && !geofenceData.getGroupData().isEmpty()) {
            geofenceWrapper.setGroupNames(geofenceData.getGroupData());
        }
        if (geofenceData.getEventConfig() != null) {
            geofenceWrapper.setEventConfig(getEventConfigBean(geofenceData.getEventConfig()));
        }
        return geofenceWrapper;
    }

    /**
     * Get event list to send with the response
     * @param eventConfig event list retrieved
     * @return list of response event beans
     */
    private List<org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig> getEventConfigBean(List<EventConfig> eventConfig) {
        List<org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig> eventList = new ArrayList<>();
        org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig eventData;
        for (EventConfig event : eventConfig) {
            eventData = new org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig();
            eventData.setId(event.getEventId());
            eventData.setEventLogic(event.getEventLogic());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try {
                List<EventAction> eventActions = mapper.readValue(event.getActions(), mapper.getTypeFactory().
                        constructCollectionType(List.class, EventAction.class));
                eventData.setActions(eventActions);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.warn("Error occurred while parsing event actions of the event with ID " + event.getEventId());
                }
                continue;
            }
            eventList.add(eventData);
        }
        return eventList;
    }

    @Path("/geo-fence")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeofence(@QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("name") String name,
            @QueryParam("requireEventData") boolean requireEventData) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            if (offset >= 0 && limit != 0) {
                PaginationRequest request = new PaginationRequest(offset, limit);
                if (name != null && !name.isEmpty()) {
                    request.setProperty(DeviceManagementConstants.GeoServices.FENCE_NAME, name);
                }
                List<GeofenceData> geoFences = geoService.getGeoFences(request);
                if (!geoFences.isEmpty() && requireEventData) {
                    geoFences = geoService.attachEventObjects(geoFences);
                }
                return buildResponse(geoFences);
            }
            if (name != null && !name.isEmpty()) {
                List<GeofenceData> geoFences = geoService.getGeoFences(name);
                if (requireEventData) {
                    geoFences = geoService.attachEventObjects(geoFences);
                }
                return buildResponse(geoFences);
            }
            return buildResponse(geoService.getGeoFences());
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Failed to retrieve geofence data";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Build the response payload from the data retrieved from the database
     * @param fencesList retrieved geofence data to send in response
     * @return HttpResponse object
     */
    private Response buildResponse(List<GeofenceData> fencesList) {
        List<GeofenceWrapper> geofenceList = new ArrayList<>();
        for (GeofenceData geofenceData : fencesList) {
            geofenceList.add(getMappedResponseBean(geofenceData));
        }
        PaginationResult paginationResult = new PaginationResult();
        paginationResult.setData(geofenceList);
        paginationResult.setRecordsTotal(geofenceList.size());
        return Response.status(Response.Status.OK).entity(paginationResult).build();
    }

    @DELETE
    @Override
    @Path("/geo-fence/{fenceId}")
    public Response deleteGeofence(@PathParam("fenceId") int fenceId) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            if (!geoService.deleteGeofenceData(fenceId)) {
                String msg = "No valid Geofence found for ID " + fenceId;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).build();
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Failed to delete geofence data";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Path("/geo-fence/{fenceId}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGeofence(GeofenceWrapper geofenceWrapper,
            @PathParam("fenceId") int fenceId,
            @QueryParam("eventIds") int[] eventIds) {
        RequestValidationUtil.validateGeofenceData(geofenceWrapper);
        RequestValidationUtil.validateEventConfigurationData(geofenceWrapper.getEventConfig());
        try {
            GeofenceData geofenceData = mapRequestGeofenceData(geofenceWrapper);
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            if (!geoService.updateGeofence(geofenceData, fenceId)) {
                String msg = "No valid Geofence found for ID " + fenceId;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            List<Integer> eventsToRemove = new ArrayList<>();
            for (int eventId : eventIds) {
                eventsToRemove.add(eventId);
            }
            geoService.updateGeoEventConfigurations(geofenceData, eventsToRemove,
                    geofenceData.getGroupIds(), fenceId);
            return Response.status(Response.Status.CREATED).entity("Geo Fence update successfully").build();
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Failed to update geofence";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (EventConfigurationException e) {
            String msg = "Failed to update geofence events";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Parse geofence data from the request payload to the GeofenceData DTO
     * @param geofenceWrapper request payload data
     * @return GeofenceData object built from the request data
     */
    private GeofenceData mapRequestGeofenceData(GeofenceWrapper geofenceWrapper) {
        GeofenceData geofenceData = new GeofenceData();
        geofenceData.setFenceName(geofenceWrapper.getFenceName());
        geofenceData.setDescription(geofenceWrapper.getDescription());
        geofenceData.setLatitude(geofenceWrapper.getLatitude());
        geofenceData.setLongitude(geofenceWrapper.getLongitude());
        geofenceData.setRadius(geofenceWrapper.getRadius());
        geofenceData.setFenceShape(geofenceWrapper.getFenceShape());
        geofenceData.setGeoJson(geofenceWrapper.getGeoJson());
        if (geofenceWrapper.getGroupIds() == null || geofenceWrapper.getGroupIds().isEmpty()) {
            String msg = "Group ID / IDs are mandatory, since cannot be null or empty";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST)
                            .setMessage(msg).build());
        }
        geofenceData.setGroupIds(geofenceWrapper.getGroupIds());
        geofenceData.setEventConfig(mapRequestEvent(geofenceWrapper.getEventConfig()));
        return geofenceData;
    }
}
