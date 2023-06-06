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

package io.entgra.device.mgt.core.operation.template.dao;

import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.operation.template.dao.impl.OperationTemplateDAOImpl;
import io.entgra.device.mgt.core.operation.template.dao.impl.OperationTemplateMySQLDAOImpl;
import io.entgra.device.mgt.core.operation.template.dao.impl.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OperationTemplateDAOFactory {

    private static final Log log = LogFactory.getLog(OperationTemplateDAOFactory.class);
    private static String databaseEngine;

    /**
     *
     * @param config
     */
    public static void init(DataSourceConfig config) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Operation Template Mgt Data Source");
        }
        ConnectionManagerUtils.resolveDataSource(config);
        databaseEngine = ConnectionManagerUtils.getDatabaseType();
    }

    /**
     *
     * @return
     */
    public static OperationTemplateDAO getOperationTemplateDAO() {
        if (databaseEngine == null) {
            throw new IllegalStateException("Database engine has not initialized properly.");
        }
        //noinspection SwitchStatementWithTooFewBranches
        switch (databaseEngine) {
            case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                return new OperationTemplateMySQLDAOImpl();
            default:
                return new OperationTemplateDAOImpl();
        }
    }

}
