/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.task.mgt.common.constant;

public class TaskMgtConstant {
    public static final class DataSourceProperties {
        private DataSourceProperties() {
            throw new AssertionError();
        }

        public static final String DB_CHECK_QUERY = "SELECT * FROM DM_DEVICE";
        public static final String TASK_CONFIG_XML_NAME = "task-mgt-config.xml";
    }

    public static final class DataBaseTypes {
        private DataBaseTypes() {
            throw new AssertionError();
        }

        public static final String DB_TYPE_MYSQL = "MySQL";
        public static final String DB_TYPE_ORACLE = "Oracle";
        public static final String DB_TYPE_MSSQL = "Microsoft SQL Server";
        public static final String DB_TYPE_DB2 = "DB2";
        public static final String DB_TYPE_H2 = "H2";
        public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";
    }

    public static final class Task {

        public static final String DYNAMIC_TASK_TYPE = "DYNAMIC_TASK";
        public static final String NAME_SEPARATOR = "_";
        public static final String PROPERTY_KEY_COLUMN_NAME = "PROPERTY_NAME";
        public static final String PROPERTY_VALUE_COLUMN_NAME = "PROPERTY_VALUE";
        public static final String __TENANT_ID_PROP__ = "__TENANT_ID_PROP__";

    }
}
