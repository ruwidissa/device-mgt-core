/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.archival;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAO;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAOException;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalSourceDAOFactory;
import org.wso2.carbon.device.mgt.core.archival.dao.DataDeletionDAO;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ArchivalServiceImpl implements ArchivalService {
    private static final Log log = LogFactory.getLog(ArchivalServiceImpl.class);

    private final ArchivalDAO archivalDAO;
    private final DataDeletionDAO dataDeletionDAO;

    private static final int EXECUTION_BATCH_SIZE =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getBatchSize();

    private static final int ARCHIVAL_LOCK_INTERVAL =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getArchivalLockInterval();

    public ArchivalServiceImpl() {
        this.archivalDAO = ArchivalSourceDAOFactory.getDataPurgingDAO();
        this.dataDeletionDAO = ArchivalDestinationDAOFactory.getDataDeletionDAO();
    }

    @Override
    public void archiveTransactionalRecords() throws ArchivalException {
        try {
            beginTransactions();
            Timestamp currentTime = new Timestamp(new Date().getTime());

            //Purge the largest table, DM_DEVICE_OPERATION_RESPONSE
            if (log.isDebugEnabled()) {
                log.debug("## Archiving operation responses");
            }

            int failAttempts;

            List<Integer> nonRemovableMappings = archivalDAO.getNonRemovableOperationMappingIDs(currentTime);
            int totalLargeOpResCount = archivalDAO.getLargeOperationResponseCount(currentTime, nonRemovableMappings);

            if (totalLargeOpResCount > 0) {
                int iterationCount = totalLargeOpResCount / EXECUTION_BATCH_SIZE;
                int residualRecSize = 0;
                if ((totalLargeOpResCount % EXECUTION_BATCH_SIZE) != 0) {
                    residualRecSize = totalLargeOpResCount % EXECUTION_BATCH_SIZE;
                    iterationCount++;
                }
                failAttempts = 0;
                for (int iter = 0; iter < iterationCount; iter++) {
                    try {
                        if (iter == (iterationCount - 1)) {
                            archivalDAO.transferLargeOperationResponses(residualRecSize, currentTime, nonRemovableMappings);
                            archivalDAO.removeLargeOperationResponses(residualRecSize, currentTime, nonRemovableMappings);
                        } else {
                            archivalDAO.transferLargeOperationResponses(EXECUTION_BATCH_SIZE, currentTime, nonRemovableMappings);
                            archivalDAO.removeLargeOperationResponses(EXECUTION_BATCH_SIZE, currentTime, nonRemovableMappings);
                        }
                        commitTransactions();
                        failAttempts = 0;
                    } catch (ArchivalDAOException e) {
                        rollbackTransactions();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while trying to archive Large Operation Responses. Abort archiving.";
                            log.error(msg, e);
                            throw new ArchivalException(msg, e);
                        }
                        String msg = "Error occurred while trying to archive Large Operation Responses. " +
                                "Failed attempts: " + failAttempts + " Error: " + e.getMessage();
                        log.warn(msg);
                        iter--;
                    }
                    Thread.sleep(ARCHIVAL_LOCK_INTERVAL);
                }
            }

            int totalOpResCount = archivalDAO.getOperationResponseCount(currentTime, nonRemovableMappings);

            if (totalOpResCount > 0) {
                int iterationCount = totalOpResCount / EXECUTION_BATCH_SIZE;
                int residualRecSize = 0;
                if ((totalOpResCount % EXECUTION_BATCH_SIZE) != 0) {
                    residualRecSize = totalOpResCount % EXECUTION_BATCH_SIZE;
                    iterationCount++;
                }
                failAttempts = 0;
                for (int iter = 0; iter < iterationCount; iter++) {
                    try {
                        if (iter == (iterationCount - 1)) {
                            archivalDAO.transferOperationResponses(residualRecSize, currentTime, nonRemovableMappings);
                            archivalDAO.removeOperationResponses(residualRecSize, currentTime, nonRemovableMappings);
                        } else {
                            archivalDAO.transferOperationResponses(EXECUTION_BATCH_SIZE, currentTime, nonRemovableMappings);
                            archivalDAO.removeOperationResponses(EXECUTION_BATCH_SIZE, currentTime, nonRemovableMappings);
                        }
                        commitTransactions();
                        failAttempts = 0;
                    } catch (ArchivalDAOException e) {
                        rollbackTransactions();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while trying to archive Operation Responses. Abort archiving.";
                            log.error(msg, e);
                            throw new ArchivalException(msg, e);
                        }
                        String msg = "Error occurred while trying to archive Operation Responses. " +
                                "Failed attempts: " + failAttempts + " Error: " + e.getMessage();
                        log.warn(msg);
                        iter--;
                    }
                    Thread.sleep(ARCHIVAL_LOCK_INTERVAL);
                }
            }

            //Purge the notifications table, DM_NOTIFICATION
            if (log.isDebugEnabled()) {
                log.debug("## Archiving notifications");
            }
            archivalDAO.moveNotifications(currentTime);
            commitTransactions();
            //Purge the enrolment mappings table, DM_ENROLMENT_OP_MAPPING
            if (log.isDebugEnabled()) {
                log.debug("## Archiving enrolment mappings");
            }
            int opMappingCount = archivalDAO.getOpMappingsCount(currentTime);
            if (opMappingCount > 0) {
                int iterationCount = opMappingCount / EXECUTION_BATCH_SIZE;
                int residualRecSize = 0;
                if ((opMappingCount % EXECUTION_BATCH_SIZE) != 0) {
                    residualRecSize = opMappingCount % EXECUTION_BATCH_SIZE;
                    iterationCount++;
                }
                failAttempts = 0;
                for (int iter = 0; iter < iterationCount; iter++) {
                    try {
                        if (iter == (iterationCount - 1)) {
                            archivalDAO.transferEnrollmentOpMappings(residualRecSize, currentTime);
                            archivalDAO.removeEnrollmentOPMappings(residualRecSize, currentTime);
                        } else {
                            archivalDAO.transferEnrollmentOpMappings(EXECUTION_BATCH_SIZE, currentTime);
                            archivalDAO.removeEnrollmentOPMappings(EXECUTION_BATCH_SIZE, currentTime);
                        }
                        commitTransactions();
                        failAttempts = 0;
                    } catch (ArchivalDAOException e) {
                        rollbackTransactions();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while trying to archive Operation Enrollment Mappings. Abort archiving.";
                            log.error(msg, e);
                            throw new ArchivalException(msg, e);
                        }
                        String msg = "Error occurred while trying to archive Operation Enrollment Mappings. " +
                                "Failed attempts: " + failAttempts + " Error: " + e.getMessage();
                        log.warn(msg);
                        iter--;
                    }
                    Thread.sleep(ARCHIVAL_LOCK_INTERVAL);
                }
            }

            //Finally, purge the operations table, DM_OPERATION
            if (log.isDebugEnabled()) {
                log.debug("## Archiving operations");
            }

            archivalDAO.transferOperations();
            archivalDAO.removeOperations();
            commitTransactions();
        } catch (ArchivalDAOException e) {
            rollbackTransactions();
            String msg = "Error occurred while trying to archive data to the six tables";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
        } catch (InterruptedException e) {
            rollbackTransactions();
            String msg = "Error while halting archival thread to free up table locks.";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
        } finally {
            ArchivalSourceDAOFactory.closeConnection();
            ArchivalDestinationDAOFactory.closeConnection();
        }
    }

    private void beginTransactions() throws ArchivalException {
        try {
            ArchivalSourceDAOFactory.beginTransaction();
            ArchivalDestinationDAOFactory.beginTransaction();
        } catch (TransactionManagementException e) {
            log.error("An error occurred during starting transactions", e);
            throw new ArchivalException("An error occurred during starting transactions", e);
        }
    }

    private void commitTransactions() {
        ArchivalSourceDAOFactory.commitTransaction();
        ArchivalDestinationDAOFactory.commitTransaction();
    }

    private void rollbackTransactions() {
        ArchivalSourceDAOFactory.rollbackTransaction();
        ArchivalDestinationDAOFactory.rollbackTransaction();
    }

    @Override
    public void deleteArchivedRecords() throws ArchivalException {
        try {
            ArchivalDestinationDAOFactory.openConnection();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting Large operation responses");
            }
            dataDeletionDAO.deleteLargeOperationResponses();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting operation responses");
            }
            dataDeletionDAO.deleteOperationResponses();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting notifications ");
            }
            dataDeletionDAO.deleteNotifications();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting enrolment mappings ");
            }
            dataDeletionDAO.deleteEnrolmentMappings();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting operations ");
            }
            dataDeletionDAO.deleteOperations();
        } catch (SQLException e) {
            throw new ArchivalException("An error occurred while initialising data source for archival", e);
        } catch (ArchivalDAOException e) {
            log.error("An error occurred while executing DataDeletionTask");
        } finally {
            ArchivalDestinationDAOFactory.closeConnection();
        }
    }
}