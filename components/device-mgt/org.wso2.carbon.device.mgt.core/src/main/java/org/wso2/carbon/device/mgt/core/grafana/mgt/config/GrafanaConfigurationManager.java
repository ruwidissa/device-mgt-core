/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.grafana.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class GrafanaConfigurationManager {

    private static final Log log = LogFactory.getLog(GrafanaConfigurationManager.class);
    private static GrafanaConfigurationManager grafanaConfigurationManager;
    private GrafanaConfiguration grafanaConfiguration;
    private static final String GRAFANA_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator
            + GrafanaConstants.CONFIG_XML_NAME;

    public static GrafanaConfigurationManager getInstance() {
        if (grafanaConfigurationManager == null) {
            synchronized (GrafanaConfigurationManager.class) {
                if (grafanaConfigurationManager == null) {
                    grafanaConfigurationManager = new GrafanaConfigurationManager();
                }
            }
        }
        return grafanaConfigurationManager;
    }

    /**
     * Initialize the Grafana Configuration through the provided configuration location
     * @param configLocation has the path of the configuration file
     * @throws GrafanaManagementException throws when there are any errors during the initialization of
     * Grafana configuration
     */
    public synchronized void initConfig(String configLocation) throws GrafanaManagementException {
        try {
            File smsConfig = new File(configLocation);
            Document doc = convertXMLToDocument(smsConfig);

            /* Un-marshaling Grafana configuration */
            JAXBContext smsContext = JAXBContext.newInstance(GrafanaConfiguration.class);
            Unmarshaller unmarshaller = smsContext.createUnmarshaller();
            this.grafanaConfiguration = (GrafanaConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing Grafana config '" + configLocation + "'";
            log.error(msg, e);
            throw new GrafanaManagementException(msg, e);
        }
    }

    /**
     * Initialize the Grafana Configuration through the grafana-config.xml file in the GRAFANA_CONFIG_PATH
     * @throws GrafanaManagementException throws when there are any errors during the initialization of config
     */
    public void initConfig() throws GrafanaManagementException {
        this.initConfig(GRAFANA_CONFIG_PATH);
    }

    public GrafanaConfiguration getGrafanaConfiguration() throws GrafanaManagementException {
        if (grafanaConfiguration != null) {
            return grafanaConfiguration;
        }
        initConfig();
        return grafanaConfiguration;
    }

    private static Document convertXMLToDocument(File file) throws GrafanaManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(GrafanaConstants.XML.FEATURES_DISALLOW_DOCTYPE_DECL, true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            String errMsg = "Error occurred while parsing file, while converting to a org.w3c.dom.Document";
            log.error(errMsg, e);
            throw new GrafanaManagementException(errMsg, e);
        }
    }

}
