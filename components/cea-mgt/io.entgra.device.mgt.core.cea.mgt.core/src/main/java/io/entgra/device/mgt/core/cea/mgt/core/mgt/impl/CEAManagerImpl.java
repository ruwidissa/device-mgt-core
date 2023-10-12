/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.core.mgt.impl;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.CEAPolicyUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAConfigManagerException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAManagementException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyAlreadyExistsException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyNotFoundException;
import io.entgra.device.mgt.core.cea.mgt.core.bean.CEAConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.config.CEAConfigManager;
import io.entgra.device.mgt.core.cea.mgt.core.dao.CEAPolicyDAO;
import io.entgra.device.mgt.core.cea.mgt.core.dao.factory.CEAPolicyManagementDAOFactory;
import io.entgra.device.mgt.core.cea.mgt.core.exception.CEAPolicyManagementDAOException;
import io.entgra.device.mgt.core.cea.mgt.core.exception.CEAPolicyMonitoringTaskManagerException;
import io.entgra.device.mgt.core.cea.mgt.core.internal.CEAManagementDataHolder;
import io.entgra.device.mgt.core.cea.mgt.core.mgt.CEAManager;
import io.entgra.device.mgt.core.cea.mgt.core.task.CEAPolicyMonitoringTaskManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;

public class CEAManagerImpl implements CEAManager {
    private static final Log log = LogFactory.getLog(CEAManagerImpl.class);
    private final CEAPolicyDAO ceaPolicyDAO;

    private CEAManagerImpl() {
        ceaPolicyDAO = CEAPolicyManagementDAOFactory.getCEAPolicyDAO();
    }

    public static CEAManagerImpl getInstance() {
        return CEAManagerHolder.INSTANCE;
    }

    @Override
    public CEAPolicyUIConfiguration getCEAPolicyUIConfiguration() throws CEAManagementException {
        CEAPolicyUIConfiguration ceaPolicyUIConfiguration;
        try {
            ceaPolicyUIConfiguration = CEAConfigManager.getInstance().getCeaPolicyUIConfiguration();
        } catch (CEAConfigManagerException e) {
            String msg = "Error occurred while retrieving CEA ui configs";
            throw new CEAManagementException(msg, e);
        }
        return ceaPolicyUIConfiguration;
    }

    @Override
    public void syncNow() throws CEAManagementException {
        try {
            CEAPolicyMonitoringTaskManager ceaPolicyMonitoringTaskManager = CEAManagementDataHolder.
                    getInstance().getCeaPolicyMonitoringTaskManager();
            if (ceaPolicyMonitoringTaskManager == null) {
                throw new IllegalStateException("CEA policy monitoring task manager not initialized properly");
            }
            CEAConfigManager ceaConfigManager = CEAConfigManager.getInstance();
            CEAConfiguration ceaConfiguration = ceaConfigManager.getCeaConfiguration();
            ceaPolicyMonitoringTaskManager.stopTask();
            ceaPolicyMonitoringTaskManager.startTask(ceaConfiguration.getMonitoringConfiguration().getMonitoringFrequency());
        } catch (CEAConfigManagerException e) {
            String msg = "Error occurred while retrieving CEA configurations";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } catch (CEAPolicyMonitoringTaskManagerException e) {
            String msg = "Error occurred while triggering CEA policy monitoring task";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        }
    }

    @Override
    public CEAPolicy createCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException,
            CEAPolicyAlreadyExistsException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            if (ceaPolicyDAO.retrieveCEAPolicy() != null) {
                throw new CEAPolicyAlreadyExistsException("CEA policy already exists");
            }
            return ceaPolicyDAO.createCEAPolicy(ceaPolicy);
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while creating CEA policy";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public CEAPolicy retrieveCEAPolicy() throws CEAManagementException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            return ceaPolicyDAO.retrieveCEAPolicy();
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while retrieving CEA policy";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<CEAPolicy> retrieveAllCEAPolicies() throws CEAManagementException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            return ceaPolicyDAO.retrieveAllCEAPolicies();
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while retrieving CEA policies";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public CEAPolicy updateCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException, CEAPolicyNotFoundException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            CEAPolicy existingCeaPolicy = ceaPolicyDAO.retrieveCEAPolicy();
            if (existingCeaPolicy == null) {
                throw new CEAPolicyNotFoundException("CEA policy not found");
            }
            return ceaPolicyDAO.updateCEAPolicy(existingCeaPolicy, ceaPolicy);
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while updating CEA policy";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteCEAPolicy() throws CEAManagementException, CEAPolicyNotFoundException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            CEAPolicyMonitoringTaskManager ceaPolicyMonitoringTaskManager = CEAManagementDataHolder.
                    getInstance().getCeaPolicyMonitoringTaskManager();
            if (ceaPolicyMonitoringTaskManager == null) {
                String msg = "CEA policy monitoring task manager not initialized properly, " +
                        "hence aborting CEA policy deleting procedure";
                throw new IllegalStateException(msg);
            }
            CEAPolicy existingCeaPolicy = ceaPolicyDAO.retrieveCEAPolicy();
            if (existingCeaPolicy == null) throw new CEAPolicyNotFoundException("CEA policy not found");
            ceaPolicyDAO.deleteCEAPolicy();
            ceaPolicyMonitoringTaskManager.stopTask();
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while deleting CEA policy";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } catch (CEAPolicyMonitoringTaskManagerException e) {
            String msg = "Error occurred while stopping CEA policy monitoring task";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateSyncStatus(boolean status, Date syncedTime) throws CEAManagementException {
        try {
            CEAPolicyManagementDAOFactory.openConnection();
            ceaPolicyDAO.updateLastSyncedTime(status, syncedTime);
        } catch (CEAPolicyManagementDAOException e) {
            String msg = "Error occurred while updating sync status";
            log.error(msg, e);
            throw new CEAManagementException(msg, e);
        } finally {
            CEAPolicyManagementDAOFactory.closeConnection();
        }
    }

    private static class CEAManagerHolder {
        public static final CEAManagerImpl INSTANCE = new CEAManagerImpl();
    }
}
