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
package io.entgra.device.mgt.core.apimgt.analytics.extension;

import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventPublisherData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventReceiverData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.EventStreamData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.dto.MetaData;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventPublisherDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventReceiverDeployerException;
import io.entgra.device.mgt.core.apimgt.analytics.extension.exception.EventStreamDeployerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.*;

/**
 * This class is responsible for deploying and undeploying analytics artifacts such as
 * event streams, event publishers, and event receivers using Apache Velocity templates.
 */
public class AnalyticsArtifactsDeployer {

    private static final Log log = LogFactory.getLog(AnalyticsArtifactsDeployer.class);

    // Directory paths
    public static final String REPOSITORY_DIRECTORY = "repository";
    public static final String DEPLOYMENT_DIRECTORY = "deployment";
    public static final String SERVER_DIRECTORY = "server";

    // Template locations
    public static final String TEMPLATE_LOCATION = REPOSITORY_DIRECTORY + File.separator + "resources" + File.separator + "iot-analytics-templates";
    public static final String EVENT_STREAM_LOCATION = "eventstreams";
    public static final String EVENT_PUBLISHER_LOCATION = "eventpublishers";
    public static final String EVENT_RECEIVER_LOCATION = "eventreceivers";

    // Template files
    public static final String EVENT_STREAM_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_stream.json.template";
    public static final String EVENT_PUBLISHER_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_publisher.xml.template";
    public static final String EVENT_RECEIVER_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_receiver.xml.template";

    // Default version and other constants
    public static final String DEFAULT_STREAM_VERSION = "1.0.0";

    // General constants for file handling
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String JSON_EXTENSION = ".json";
    public static final String XML_EXTENSION = ".xml";
    public static final String UNDERSCORE = "_";

    /**
     * Deploys an event stream configuration file using the Velocity template engine.
     *
     * @param eventStreamData Data required to generate the event stream configuration.
     * @param tenantId        ID of the tenant for whom the event stream is deployed.
     * @throws EventStreamDeployerException if there is an error during deployment.
     */
    public void deployEventStream(EventStreamData eventStreamData, int tenantId) throws EventStreamDeployerException {
        try {
            // Initialize Velocity Engine
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();

            // Load template
            Template template = ve.getTemplate(EVENT_STREAM_TEMPLATE);

            // Populate context and merge it with the template
            VelocityContext context = populateContextForEventStreams(eventStreamData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            // Generate the file name and location
            String fileName = eventStreamData.getName() + UNDERSCORE + eventStreamData.getVersion() + JSON_EXTENSION;
            String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_STREAM_LOCATION);

            // Write the output to the file
            try (PrintWriter printWriter = new PrintWriter(fileLocation, UTF_8_ENCODING)) {
                printWriter.println(writer.toString());
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            String msg = "Error while persisting event stream definition. Event Stream: " + eventStreamData.getName() +
                    ", Version: " + eventStreamData.getVersion() + ", Tenant ID: " + tenantId;
            log.error(msg, e);
            throw new EventStreamDeployerException(msg, e);
        }
    }

    /**
     * Deploys an event publisher configuration file using the Velocity template engine.
     *
     * @param eventPublisherData Data required to generate the event publisher configuration.
     * @param tenantId           ID of the tenant for whom the event publisher is deployed.
     * @throws EventPublisherDeployerException if there is an error during deployment.
     */
    public void deployEventPublisher(EventPublisherData eventPublisherData, int tenantId) throws EventPublisherDeployerException {
        try {
            // Initialize Velocity Engine
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();

            // Load template
            Template template = ve.getTemplate(EVENT_PUBLISHER_TEMPLATE);

            // Populate context and merge it with the template
            VelocityContext context = populateContextForEventPublisher(eventPublisherData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            // Generate the file name and location
            String fileName = eventPublisherData.getName() + XML_EXTENSION;
            String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_PUBLISHER_LOCATION);

            // Write the output to the file
            try (PrintWriter printWriter = new PrintWriter(fileLocation, UTF_8_ENCODING)) {
                printWriter.println(writer.toString());
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            String msg = String.format("Error while persisting event publisher definition. Event Publisher: %s, Tenant ID: %d",
                    eventPublisherData.getName(), tenantId);
            log.error(msg, e);
            throw new EventPublisherDeployerException(msg, e);
        }
    }

    /**
     * Deploys an event receiver configuration file using the Velocity template engine.
     *
     * @param eventReceiverData Data required to generate the event receiver configuration.
     * @param tenantId          ID of the tenant for whom the event receiver is deployed.
     * @throws EventReceiverDeployerException if there is an error during deployment.
     */
    public void deployEventReceiver(EventReceiverData eventReceiverData, int tenantId) throws EventReceiverDeployerException {
        try {
            // Initialize Velocity Engine
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();

            // Load template
            Template template = ve.getTemplate(EVENT_RECEIVER_TEMPLATE);

            // Populate context and merge it with the template
            VelocityContext context = populateContextForEventReceiver(eventReceiverData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            // Generate the file name and location
            String fileName = eventReceiverData.getName() + XML_EXTENSION;
            String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_RECEIVER_LOCATION);

            // Write the output to the file
            try (PrintWriter printWriter = new PrintWriter(fileLocation, UTF_8_ENCODING)) {
                printWriter.println(writer.toString());
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            String msg = String.format("Error while persisting oauth mqtt event receiver definition. " +
                            "Event Receiver: %s, Tenant ID: %d",
                    eventReceiverData.getName(), tenantId);
            log.error(msg, e);
            throw new EventReceiverDeployerException(msg, e);
        }
    }

    /**
     * Populates the Velocity context for event stream generation.
     *
     * @param eventStreamData Event stream data model.
     * @return A populated VelocityContext instance.
     */
    private VelocityContext populateContextForEventStreams(EventStreamData eventStreamData) {
        VelocityContext context = new VelocityContext();
        context.put("name", eventStreamData.getName());
        context.put("version", eventStreamData.getVersion());
        context.put("metaData",
                eventStreamData.getMetaData() != null ? eventStreamData.getMetaData() : new MetaData("deviceId", "STRING"));
        if (eventStreamData.getPayloadData() != null) {
            context.put("properties", eventStreamData.getPayloadData());
        }
        return context;
    }

    /**
     * Populates the Velocity context for event publisher generation.
     *
     * @param eventPublisherData Event publisher data model.
     * @return A populated VelocityContext instance.
     */
    private VelocityContext populateContextForEventPublisher(EventPublisherData eventPublisherData) {
        VelocityContext context = new VelocityContext();
        context.put("name", eventPublisherData.getName());
        context.put("streamName", eventPublisherData.getStreamName());
        context.put("streamVersion", eventPublisherData.getStreamVersion());
        context.put("properties", eventPublisherData.getPropertyList());
        context.put("eventAdapterType", eventPublisherData.getEventAdaptorType());
        context.put("customMappingType", eventPublisherData.getCustomMappingType());
        return context;
    }

    /**
     * Populates the Velocity context for event receiver generation.
     *
     * @param eventReceiverData Event receiver data model.
     * @return A populated VelocityContext instance.
     */
    private VelocityContext populateContextForEventReceiver(EventReceiverData eventReceiverData) {
        VelocityContext context = new VelocityContext();
        context.put("name", eventReceiverData.getName());
        context.put("streamName", eventReceiverData.getStreamName());
        context.put("streamVersion", eventReceiverData.getStreamVersion());
        context.put("properties", eventReceiverData.getPropertyList());
        context.put("eventAdapterType", eventReceiverData.getEventAdapterType());
        context.put("customMappingType", eventReceiverData.getCustomMappingType());
        return context;
    }

    /**
     * Undeploys an event stream configuration file.
     *
     * @param streamName Name of the event stream.
     * @param tenantId   ID of the tenant from whom the event stream is to be removed.
     */
    public void undeployEventStream(String streamName, int tenantId) {
        String fileName = streamName + UNDERSCORE + DEFAULT_STREAM_VERSION + JSON_EXTENSION;
        String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_STREAM_LOCATION);
        try {
            deleteFile(fileLocation, "event stream");
        } catch (FileNotFoundException e) {
            log.warn("Event stream file not found or could not be deleted: " + fileLocation, e);
        }
    }

    /**
     * Undeploys an event publisher configuration file.
     *
     * @param publisherName Name of the event publisher.
     * @param tenantId      ID of the tenant from whom the event publisher is to be removed.
     */
    public void undeployEventPublisher(String publisherName, int tenantId) {
        String fileName = publisherName + XML_EXTENSION;
        String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_PUBLISHER_LOCATION);
        try {
            deleteFile(fileLocation, "event publisher");
        } catch (FileNotFoundException e) {
            log.warn("Event publisher file not found or could not be deleted: " + fileLocation, e);
        }
    }

    /**
     * Undeploys an event receiver configuration file.
     *
     * @param receiverName Name of the event receiver.
     * @param tenantId     ID of the tenant from whom the event receiver is to be removed.
     */
    public void undeployEventReceiver(String receiverName, int tenantId) {
        String fileName = receiverName + XML_EXTENSION;
        String fileLocation = getFileLocationForTenant(fileName, tenantId, EVENT_RECEIVER_LOCATION);
        try {
            deleteFile(fileLocation, "event receiver");
        } catch (FileNotFoundException e) {
            log.warn("Event receiver file not found or could not be deleted: " + fileLocation, e);
        }
    }

    /**
     * Constructs the file location for tenant-specific deployment directory.
     *
     * @param tenantId Tenant ID for multi-tenancy support.
     * @param fileName The name of the file being deployed.
     * @param artifactLocation The specific directory where the artifact is located (eventstreams, eventpublishers, etc.).
     * @return The full file path as a string.
     */
    private String getFileLocationForTenant(String fileName, int tenantId, String artifactLocation) {
        String fileLocation;

        if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
            // Super tenant case
            fileLocation = CarbonUtils.getCarbonHome() + File.separator + REPOSITORY_DIRECTORY + File.separator
                    + DEPLOYMENT_DIRECTORY + File.separator + SERVER_DIRECTORY + File.separator
                    + artifactLocation + File.separator + fileName;
        } else {
            // Tenant-specific case
            fileLocation = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator
                    + artifactLocation + File.separator + fileName;
        }

        return fileLocation;
    }

    /**
     * Deletes a file from the file system.
     *
     * @param filePath     The full path to the file to be deleted.
     * @param artifactType The type of artifact (used in logging).
     * @throws FileNotFoundException if the file does not exist or cannot be deleted.
     */
    private void deleteFile(String filePath, String artifactType) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                log.info("Successfully deleted " + artifactType + " file: " + filePath);
            } else {
                String msg = String.format("Error while deleting %s file: %s - Failed to delete the file.", artifactType, filePath);
                log.error(msg);
                throw new FileNotFoundException(msg);
            }
        } else {
            String msg = String.format("Error while deleting %s file: %s - File not found.", artifactType, filePath);
            log.error(msg);
            throw new FileNotFoundException(msg);
        }
    }

}
