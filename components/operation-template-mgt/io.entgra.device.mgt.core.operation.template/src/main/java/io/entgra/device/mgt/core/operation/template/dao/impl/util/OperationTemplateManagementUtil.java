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

package io.entgra.device.mgt.core.operation.template.dao.impl.util;

import io.entgra.device.mgt.core.operation.template.dto.OperationTemplateCacheKey;
import org.w3c.dom.Document;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class OperationTemplateManagementUtil {

    private OperationTemplateManagementUtil() {
    }

    /**
     *
     * @param file
     * @return
     * @throws DeviceManagementException
     */
    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     *
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @return
     */
    public static String setOperationTemplateCacheKey(String deviceType, String subTypeId, String operationCode) {
        return deviceType + "|" + subTypeId + "|" + operationCode;
    }

    /**
     *
     * @param subTypeId
     * @param deviceType
     * @return
     */
    public static String setOperationTemplateCacheKey(String deviceType, String subTypeId) {
        return deviceType + "|" + subTypeId;
    }

    /**
     *
     * @param key
     * @return
     */
    public static OperationTemplateCacheKey getOperationTemplateCacheKey(String key) {
        String[] keys = key.split("\\|");
        String deviceType = keys[0];;
        String subTypeId = keys[1];
        String operationCode = keys[2];

        OperationTemplateCacheKey operationTemplateCacheKey = new OperationTemplateCacheKey();
        operationTemplateCacheKey.setSubTypeId(subTypeId);
        operationTemplateCacheKey.setDeviceType(deviceType);
        operationTemplateCacheKey.setOperationCode(operationCode);

        return operationTemplateCacheKey;
    }

    /**
     *
     * @param key
     * @return
     */
    public static OperationTemplateCacheKey getOperationTemplateCodeCacheKey(String key) {

        String[] keys = key.split("\\|");
        String deviceType = keys[0];;
        String subTypeId = keys[1];

        OperationTemplateCacheKey operationTemplateCacheKey = new OperationTemplateCacheKey();
        operationTemplateCacheKey.setSubTypeId(subTypeId);
        operationTemplateCacheKey.setDeviceType(deviceType);

        return operationTemplateCacheKey;
    }

}
