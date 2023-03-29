/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class AnalyticsArtifactsDeployer {

    public static final String TEMPLATE_LOCATION = "repository" + File.separator + "resources" + File.separator + "iot-analytics-templates";
    public static final String EVENT_STREAM_LOCATION = "eventstreams";
    public static final String EVENT_PUBLISHER_LOCATION = "eventpublishers";
    public static final String EVENT_RECEIVER_LOCATION = "eventreceivers";
    public static final String EVENT_STREAM_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_stream.json.template";
    public static final String EVENT_PUBLISHER_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_publisher.xml.template";
    public static final String EVENT_RECEIVER_TEMPLATE = TEMPLATE_LOCATION + File.separator + "event_receiver.xml.template";


    public void deployEventStream(EventStreamData eventStreamData, int tenantId) throws EventStreamDeployerException {
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();
            Template template = ve.getTemplate(EVENT_STREAM_TEMPLATE);

            VelocityContext context = populateContextForEventStreams(eventStreamData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            String fileName = eventStreamData.getName() + "_" + eventStreamData.getVersion() + ".json";
            String fileLocation = null;
            if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                fileLocation = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "deployment"
                        + File.separator + "server" + File.separator + EVENT_STREAM_LOCATION + File.separator + fileName;
            } else {
                fileLocation = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator
                        + EVENT_STREAM_LOCATION + File.separator + fileName;
            }

            PrintWriter printWriter = new PrintWriter(fileLocation, "UTF-8");
            printWriter.println(writer.toString());
            printWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new EventStreamDeployerException("Error while persisting event stream definition ", e);
        }
    }

    public void deployEventPublisher(EventPublisherData eventPublisherData, int tenantId) throws EventPublisherDeployerException {
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();
            Template template = ve.getTemplate(EVENT_PUBLISHER_TEMPLATE);

            VelocityContext context = populateContextForEventPublisher(eventPublisherData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            String fileName = eventPublisherData.getName() + ".xml";
            String fileLocation = null;
            if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                fileLocation = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "deployment"
                        + File.separator + "server" + File.separator + EVENT_PUBLISHER_LOCATION + File.separator + fileName;
            } else {
                fileLocation = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator
                        + EVENT_PUBLISHER_LOCATION + File.separator + fileName;
            }

            PrintWriter printWriter = new PrintWriter(fileLocation, "UTF-8");
            printWriter.println(writer.toString());
            printWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new EventPublisherDeployerException("Error while persisting rdbms event publisher ", e);
        }
    }

    public void deployEventReceiver(EventReceiverData eventReceiverData, int tenantId) throws EventReceiverDeployerException {
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            ve.init();
            Template template = ve.getTemplate(EVENT_RECEIVER_TEMPLATE);

            VelocityContext context = populateContextForEventReceiver(eventReceiverData);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            String fileName = eventReceiverData.getName() + ".xml";
            String fileLocation = null;
            if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                fileLocation = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "deployment"
                        + File.separator + "server" + File.separator + EVENT_RECEIVER_LOCATION + File.separator + fileName;
            } else {
                fileLocation = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator
                        + EVENT_RECEIVER_LOCATION + File.separator + fileName;
            }

            PrintWriter printWriter = new PrintWriter(fileLocation, "UTF-8");
            printWriter.println(writer.toString());
            printWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new EventReceiverDeployerException("Error while persisting oauth mqtt event receiver ", e);
        }
    }

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
}
