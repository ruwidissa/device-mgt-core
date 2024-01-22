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

package io.entgra.device.mgt.core.cea.mgt.core.internal;

import io.entgra.device.mgt.core.cea.mgt.common.service.CEAManagementService;
import io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager;
import io.entgra.device.mgt.core.cea.mgt.core.config.CEAConfigManager;
import io.entgra.device.mgt.core.cea.mgt.core.dao.factory.CEAPolicyManagementDAOFactory;
import io.entgra.device.mgt.core.cea.mgt.core.impl.CEAManagementServiceImpl;
import io.entgra.device.mgt.core.cea.mgt.core.task.CEAPolicyMonitoringTaskManager;
import io.entgra.device.mgt.core.cea.mgt.core.task.CEAPolicyMonitoringTaskManagerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="io.entgra.device.mgt.core.cea.mgt.core.CEAManagementServiceComponent" immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="io.entgra.device.mgt.core.cea.mgt.enforcementServiceManager"
 * interface="io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setEnforcementServiceManager"
 * unbind="unsetEnforcementServiceManager"
 * @scr.reference name="ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTaskService"
 * unbind="unsetTaskService"
 */

public class CEAManagementServiceComponent {
    private static final Log log = LogFactory.getLog(CEAManagementServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            CEAConfigManager ceaConfigManager = CEAConfigManager.getInstance();
            CEAPolicyManagementDAOFactory.init(ceaConfigManager.getCeaPolicyManagementRepository().getDataSourceConfig());
            CEAManagementService ceaManagementService = new CEAManagementServiceImpl();
            componentContext.getBundleContext().registerService(CEAManagementService.class.getName(),
                    ceaManagementService, null);
            CEAPolicyMonitoringTaskManager ceaPolicyMonitoringTaskManager = new CEAPolicyMonitoringTaskManagerImpl();
            CEAManagementDataHolder.getInstance().setCeaPolicyMonitoringTaskManager(ceaPolicyMonitoringTaskManager);
        } catch (Throwable t) {
            String msg = "Error occurred while activating " + CEAManagementServiceComponent.class.getName();
            log.error(msg, t);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        // This is to avoid cea management component getting initialized before the underlying datasource registered
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        // Do nothing
    }

    protected void setEnforcementServiceManager(EnforcementServiceManager enforcementServiceManager) {
        CEAManagementDataHolder.getInstance().setEnforcementServiceManager(enforcementServiceManager);
        if (log.isDebugEnabled()) {
            log.debug("Enforcement service manager is set successfully");
        }
    }

    protected void unsetEnforcementServiceManager(EnforcementServiceManager enforcementServiceManager) {
        CEAManagementDataHolder.getInstance().setEnforcementServiceManager(null);
        if (log.isDebugEnabled()) {
            log.debug("Enforcement service manager is unset successfully");
        }
    }

    protected void setTaskService(TaskService taskService) {
        CEAManagementDataHolder.getInstance().setTaskService(taskService);
        if (log.isDebugEnabled()) {
            log.debug("Task service is set successfully");
        }
    }

    protected void unsetTaskService(TaskService taskService) {
        CEAManagementDataHolder.getInstance().setTaskService(null);
        if (log.isDebugEnabled()) {
            log.debug("Task service is unset successfully");
        }
    }
}