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

package io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper;

import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.bean.UserStoreRoleMappingConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.exception.UserStoreRoleMapperException;
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

public class UserStoreRoleMappingConfigManager {

    private static final Log log = LogFactory.getLog(UserStoreRoleMappingConfigManager.class);
    private static final String USERSTORE_ROLE_MAPPING_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() +
            File.separator + "user-store-role-mapping-config.xml";

    private final UserStoreRoleMappingConfig userStoreRoleMappingConfig;

    public UserStoreRoleMappingConfigManager() throws UserStoreRoleMapperException {
        try {
            File UserStoreRoleMappingConfig = new File(USERSTORE_ROLE_MAPPING_CONFIG_PATH);
            Document doc = convertToDocument(UserStoreRoleMappingConfig);
            JAXBContext smsContext = JAXBContext.newInstance(UserStoreRoleMappingConfig.class);
            Unmarshaller unmarshaller = smsContext.createUnmarshaller();
            this.userStoreRoleMappingConfig = (UserStoreRoleMappingConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing config '" + USERSTORE_ROLE_MAPPING_CONFIG_PATH + "'";
            log.error(msg, e);
            throw new UserStoreRoleMapperException(msg, e);
        }
    }

    private static Document convertToDocument(File file) throws UserStoreRoleMapperException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new UserStoreRoleMapperException("Error occurred while parsing " + USERSTORE_ROLE_MAPPING_CONFIG_PATH +
                    " file, while converting to a org.w3c.dom.Document", e);
        }
    }

    public UserStoreRoleMappingConfig getUserStoreRoleMappingConfig() {
        return this.userStoreRoleMappingConfig;
    }

}
