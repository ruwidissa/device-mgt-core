/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.server.bootup.heartbeat.beacon;

import io.entgra.server.bootup.heartbeat.beacon.config.HeartBeatBeaconConfig;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Properties;

public class HeartBeatBeaconUtils {

    private static Log log = LogFactory.getLog(HeartBeatBeaconUtils.class);

    public static Document convertToDocument(File file)
            throws HeartBeatBeaconConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new HeartBeatBeaconConfigurationException("Error occurred while parsing file, while converting " +
                                                            "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * Lookup datasource using name and jndi properties
     *
     * @param dataSourceName Name of datasource to lookup
     * @param jndiProperties Hash table of JNDI Properties
     * @return datasource looked
     */
    public static DataSource lookupDataSource(String dataSourceName,
                                              final Hashtable<Object, Object> jndiProperties) {
        try {
            if (jndiProperties == null || jndiProperties.isEmpty()) {
                return (DataSource) InitialContext.doLookup(dataSourceName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (DataSource) context.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }


    public static ServerContext getServerDetails() throws UnknownHostException, SocketException {
        InetAddress localHost = InetAddress.getLocalHost();
        int iotsCorePort = Integer.parseInt(System.getProperty("iot.core.https.port"));
        ServerContext ctx = new ServerContext();
        ctx.setHostName(localHost.getHostName());
        ctx.setCarbonServerPort(iotsCorePort);
        return ctx;
    }

    public static void saveUUID(String text) throws IOException {
        File serverDetails = new File(HeartBeatBeaconConfig.getInstance().getServerUUIDFileLocation());
        serverDetails.createNewFile(); // if file already exists will do nothing
        FileOutputStream fos = new FileOutputStream(serverDetails, false);
        Properties prop = new Properties();
        prop.setProperty("server.uuid", text);
        prop.store(fos, null);
        fos.close();
    }

    public static String readUUID() {
        InputStream input = null;
        String uuid = null;
        try {
            input = new FileInputStream(HeartBeatBeaconConfig.getInstance().getServerUUIDFileLocation());
            Properties props = new Properties();
            props.load(input);
            uuid = props.getProperty("server.uuid");
            input.close();
        } catch (FileNotFoundException e) {
            log.info("File : server-credentials.properties does not exist, new UUID will be generated for server.");
        } catch (IOException e) {
            log.error("Error accessing server-credentials.properties to locate server.uuid.", e);
        }
        return uuid;
    }



}
