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

package io.entgra.device.mgt.core.device.mgt.core.internal;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto.OperationConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;

public class OperationStartupHandler implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(OperationStartupHandler.class);
    private static final Gson gson = new Gson();
    private final OperationDAO operationDAO = OperationManagementDAOFactory.getOperationDAO();
    private static final String OPERATION_CONFIG = "OPERATION_CONFIG";

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        MetadataManagementService metadataManagementService = DeviceManagementDataHolder.getInstance().getMetadataManagementService();
        Metadata metadata;
        int numOfRecordsUpdated;

        try {
            metadata = metadataManagementService.retrieveMetadata(OPERATION_CONFIG);
            if (metadata != null) {
                OperationConfig operationConfiguration = gson.fromJson(metadata.getMetaValue(), OperationConfig.class);
                String[] deviceTypes = operationConfiguration.getDeviceTypes();
                String initialOperationStatus = operationConfiguration.getInitialOperationStatus();
                String requiredStatusChange = operationConfiguration.getRequiredStatusChange();

                for (String deviceType: deviceTypes) {
                    try {
                        OperationManagementDAOFactory.beginTransaction();
                        try {
                            numOfRecordsUpdated = operationDAO.updateOperationByDeviceTypeAndInitialStatus(deviceType,
                                    initialOperationStatus, requiredStatusChange);
                            log.info(numOfRecordsUpdated + " operations updated successfully for the" + deviceType);
                            OperationManagementDAOFactory.commitTransaction();
                        } catch (OperationManagementDAOException e) {
                            OperationManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred while updating operation status. DeviceType : " + deviceType + ", " +
                                    "Initial operation status: " + initialOperationStatus + ", Required status:" + requiredStatusChange;
                            log.error(msg, e);
                        }
                    } catch (TransactionManagementException e) {
                        String msg = "Transactional error occurred while updating the operation status";
                        log.error(msg, e);
                    }  finally {
                        OperationManagementDAOFactory.closeConnection();
                    }
                }
            } else {
                log.info("Operation configuration not provided");
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving the operation configuration";
            log.error(msg, e);
        }
    }
}
