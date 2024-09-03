/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto.OperationConfig;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigException;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationConfigurationService {

    private static final Log log = LogFactory.getLog(OperationConfigurationService.class);
    private static final Gson gson = new Gson();
    private static final String STRING = "STRING";
    private static final String OPERATION_CONFIG = "OPERATION_CONFIG";
    static MetadataManagementService metadataManagementService = DeviceManagementDataHolder.getInstance().getMetadataManagementService();


    public static OperationConfig getOperationConfig() throws OperationConfigException {

        Metadata metadata;
        try {
            metadata = metadataManagementService.retrieveMetadata(OPERATION_CONFIG);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving operation configuration";
            log.error(msg, e);
            throw new OperationConfigException(msg, e);
        }
        if (metadata != null) {
            return gson.fromJson(metadata.getMetaValue(), OperationConfig.class);
        } else {
            return null;
        }
    }

    public static void addOperationConfiguration(OperationConfig config) throws OperationConfigException,
            OperationConfigAlreadyExistsException {

        Metadata metadata = new Metadata();
        metadata.setDataType(STRING);
        metadata.setMetaKey(OPERATION_CONFIG);
        metadata.setMetaValue(gson.toJson(config));

        try {
            metadataManagementService.createMetadata(metadata);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while adding operation configuration";
            log.error(msg, e);
            throw new OperationConfigException(msg, e);
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Operation configuration already exists";
            log.error(msg, e);
            throw new OperationConfigAlreadyExistsException(msg, e);
        }
    }

    public static void updateOperationConfiguration(OperationConfig config) throws OperationConfigException {

        Metadata metadata = new Metadata();
        metadata.setDataType(STRING);
        metadata.setMetaKey(OPERATION_CONFIG);
        metadata.setMetaValue(gson.toJson(config));

        try {
            metadataManagementService.updateMetadata(metadata);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while updating operation configuration";
            log.error(msg, e);
            throw new OperationConfigException(msg, e);
        }
    }

    public static void deleteOperationConfiguration() throws OperationConfigException, OperationConfigNotFoundException {

        try {
            metadataManagementService.deleteMetadata(OPERATION_CONFIG);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while deleting operation configuration";
            log.error(msg, e);
            throw new OperationConfigException(msg, e);
        } catch (MetadataKeyNotFoundException e) {
            String msg = "Operation configuration already exists";
            log.error(msg, e);
            throw new OperationConfigNotFoundException(msg, e);
        }
    }

}
