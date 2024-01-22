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

package io.entgra.device.mgt.core.cea.mgt.core.task;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAConfigManagerException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAManagementException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.EnforcementServiceManagerException;
import io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager;
import io.entgra.device.mgt.core.cea.mgt.core.bean.ActiveSyncServerConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.bean.CEAConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.config.CEAConfigManager;
import io.entgra.device.mgt.core.cea.mgt.core.internal.CEAManagementDataHolder;
import io.entgra.device.mgt.core.cea.mgt.core.mgt.CEAManager;
import io.entgra.device.mgt.core.cea.mgt.core.mgt.impl.CEAManagerImpl;
import io.entgra.device.mgt.core.cea.mgt.core.util.Constants;
import io.entgra.device.mgt.core.cea.mgt.enforce.Impl.CEAPolicyOperationImpl;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.CEAPolicyOperationException;
import io.entgra.device.mgt.core.cea.mgt.enforce.service.CEAPolicyOperation;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Date;
import java.util.Objects;

public class CEAPolicyMonitoringTask extends DynamicPartitionedScheduleTask {
    private static final Log log = LogFactory.getLog(CEAPolicyMonitoringTask.class);

    private CEAManager ceaManager;
    private CEAConfigManager ceaConfigManager;
    private EnforcementServiceManager enforcementServiceManager;

    @Override
    protected void executeDynamicTask() {
        int tenantId = Integer.parseInt(Objects.requireNonNull(getProperty(Constants.TENANT_ID_KEY)));
        try {
            CEAConfiguration ceaConfiguration = ceaConfigManager.getCeaConfiguration();
            CEAPolicy ceaPolicy = ceaManager.retrieveCEAPolicy();
            ActiveSyncServerConfiguration activeSyncServerConfiguration = ceaConfiguration.
                    getActiveSyncServerConfiguration(ceaPolicy.getActiveSyncServer());
            if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                enforce(ceaPolicy, activeSyncServerConfiguration);
                return;
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                enforce(ceaPolicy, activeSyncServerConfiguration);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        } catch (CEAManagementException e) {
            log.error("Error occurred while executing dynamic partitioned task for the CEA policy monitoring", e);
        } catch (CEAConfigManagerException e) {
            log.error("Error occurred while retrieving CEA configuration", e);
        }
    }

    private void enforce(CEAPolicy ceaPolicy, ActiveSyncServerConfiguration activeSyncServerConfiguration) {
        boolean status = false;
        Date syncedStartTime = new Date();
        CEAPolicyOperation ceaPolicyOperation;
        try {
            ceaPolicyOperation = new CEAPolicyOperationImpl(enforcementServiceManager.
                    getEnforcementService(activeSyncServerConfiguration.getEnforcementService()), ceaPolicy);
            ceaPolicyOperation.enforce();
            status = true;
        } catch (EnforcementServiceManagerException | CEAPolicyOperationException e) {
            log.error("Error occurred while enforcing the CEA access policy for the tenant id" + ceaPolicy.getTenantId(), e);
        } finally {
            logbackEnforcementStatus(status, syncedStartTime);
        }
    }


    private void logbackEnforcementStatus(boolean status, Date syncedStartTime) {
        try {
            ceaManager.updateSyncStatus(status, syncedStartTime);
        } catch (CEAManagementException e) {
            log.error("Error occurred while recording sync status", e);
        }
    }

    @Override
    protected void setup() {
        ceaManager = CEAManagerImpl.getInstance();
        ceaConfigManager = CEAConfigManager.getInstance();
        enforcementServiceManager = CEAManagementDataHolder.getInstance().getEnforcementServiceManager();
    }

}
