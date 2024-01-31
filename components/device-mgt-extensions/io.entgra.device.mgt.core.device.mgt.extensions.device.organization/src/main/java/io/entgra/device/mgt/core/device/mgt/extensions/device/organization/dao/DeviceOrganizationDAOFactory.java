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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao;

import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.impl.DeviceOrganizationDAOImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.impl.DeviceOrganizationMysqlDAOImpl;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.UnsupportedDatabaseEngineException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require Device Organization related metadata persistence.
 */
public class DeviceOrganizationDAOFactory {
    private static final Log log = LogFactory.getLog(DeviceOrganizationDAOFactory.class);
    private static String databaseEngine;

    /**
     * Initialize the Device Organization Data Source.
     *
     * @param dataSourceConfiguration The DataSourceConfig containing data source information.
     */
    public static void init(DataSourceConfig dataSourceConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Device Organization Data Source");
        }
        ConnectionManagerUtil.resolveDataSource(dataSourceConfiguration);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static final class DataBaseTypes {
        private DataBaseTypes() {
        }

        public static final String DB_TYPE_MYSQL = "MySQL";
        public static final String DB_TYPE_ORACLE = "Oracle";
        public static final String DB_TYPE_MSSQL = "Microsoft SQL Server";
        public static final String DB_TYPE_H2 = "H2";
        public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";
    }

    /**
     * Retrieves a DeviceOrganizationDAO implementation based on the configured database engine.
     *
     * @return a DeviceOrganizationDAO implementation
     */
    public static DeviceOrganizationDAO getDeviceOrganizationDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DataBaseTypes.DB_TYPE_H2:
                case DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DataBaseTypes.DB_TYPE_MSSQL:
                case DataBaseTypes.DB_TYPE_ORACLE:
                    return new DeviceOrganizationDAOImpl();
                case DataBaseTypes.DB_TYPE_MYSQL:
                    return new DeviceOrganizationMysqlDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }
}


