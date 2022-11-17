/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.defaultrole.manager;

import io.entgra.device.mgt.extensions.defaultrole.manager.bean.DefaultRolesConfig;
import io.entgra.device.mgt.extensions.defaultrole.manager.exception.DefaultRoleManagerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class DefaultRolesConfigManager {

    private static final Log log = LogFactory.getLog(DefaultRolesConfigManager.class);
    private static final String DEFAULT_ROLES_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() +
            File.separator + "default-roles-config.xml";

    private final DefaultRolesConfig defaultRolesConfig;

    public DefaultRolesConfigManager() throws DefaultRoleManagerException {
        try {
            File defaultRolesConfig = new File(DEFAULT_ROLES_CONFIG_PATH);
            Document doc = convertToDocument(defaultRolesConfig);
            JAXBContext smsContext = JAXBContext.newInstance(DefaultRolesConfig.class);
            Unmarshaller unmarshaller = smsContext.createUnmarshaller();
            this.defaultRolesConfig = (DefaultRolesConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing Default Roles config '" + DEFAULT_ROLES_CONFIG_PATH + "'";
            log.error(msg, e);
            throw new DefaultRoleManagerException(msg, e);
        }
    }

    private static Document convertToDocument(File file) throws DefaultRoleManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DefaultRoleManagerException("Error occurred while parsing " + DEFAULT_ROLES_CONFIG_PATH +
                    " file, while converting to a org.w3c.dom.Document", e);
        }
    }

    public DefaultRolesConfig getDefaultRolesConfig() {
        return this.defaultRolesConfig;
    }

}
