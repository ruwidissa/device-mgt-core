/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package io.entgra.application.mgt.core.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.UnsupportedDatabaseEngineException;
import io.entgra.application.mgt.core.dao.ApplicationDAO;
import io.entgra.application.mgt.core.dao.ApplicationReleaseDAO;
import io.entgra.application.mgt.core.dao.LifecycleStateDAO;
import io.entgra.application.mgt.core.dao.ReviewDAO;
import io.entgra.application.mgt.core.dao.SubscriptionDAO;
import io.entgra.application.mgt.core.dao.VisibilityDAO;
import io.entgra.application.mgt.core.dao.impl.application.PostgreSQLApplicationDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.SQLServerApplicationDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.release.OracleApplicationReleaseDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.release.PostgreSQLApplicationReleaseDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.release.SQLServerApplicationReleaseDAOImpl;
import io.entgra.application.mgt.core.dao.impl.lifecyclestate.OracleLifecycleStateDAOImpl;
import io.entgra.application.mgt.core.dao.impl.lifecyclestate.PostgreSQLLifecycleStateDAOImpl;
import io.entgra.application.mgt.core.dao.impl.lifecyclestate.SQLServerLifecycleStateDAOImpl;
import io.entgra.application.mgt.core.dao.impl.review.GenericReviewDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.release.GenericApplicationReleaseDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.OracleApplicationDAOImpl;
import io.entgra.application.mgt.core.dao.impl.lifecyclestate.GenericLifecycleStateDAOImpl;
import io.entgra.application.mgt.core.dao.impl.review.OracleReviewDAOImpl;
import io.entgra.application.mgt.core.dao.impl.review.PostgreSQLReviewDAOImpl;
import io.entgra.application.mgt.core.dao.impl.review.SQLServerReviewDAOImpl;
import io.entgra.application.mgt.core.dao.impl.subscription.GenericSubscriptionDAOImpl;
import io.entgra.application.mgt.core.dao.impl.subscription.OracleSubscriptionDAOImpl;
import io.entgra.application.mgt.core.dao.impl.subscription.PostgreSQLSubscriptionDAOImpl;
import io.entgra.application.mgt.core.dao.impl.subscription.SQLServerSubscriptionDAOImpl;
import io.entgra.application.mgt.core.dao.impl.visibility.GenericVisibilityDAOImpl;
import io.entgra.application.mgt.core.dao.impl.visibility.OracleVisibilityDAOImpl;
import io.entgra.application.mgt.core.dao.impl.visibility.PostgreSQLVisibilityDAOImpl;
import io.entgra.application.mgt.core.dao.impl.visibility.SQLServerVisibilityDAOImpl;
import io.entgra.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.application.mgt.core.util.Constants;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require Application management related metadata persistence.
 */
public class ApplicationManagementDAOFactory {

    private static String databaseEngine;
    private static DataSource dataSource;
    private static final Log log = LogFactory.getLog(ApplicationManagementDAOFactory.class);

    public static void init(String datasourceName) {
        ConnectionManagerUtil.resolveDataSource(datasourceName);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static ApplicationDAO getApplicationDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericApplicationDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLApplicationDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerApplicationDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleApplicationDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static LifecycleStateDAO getLifecycleStateDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericLifecycleStateDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLLifecycleStateDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerLifecycleStateDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleLifecycleStateDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of ApplicationReleaseDAOImplementation of the particular database engine.
     *
     * @return specific ApplicationReleaseDAOImplementation
     */
    public static ApplicationReleaseDAO getApplicationReleaseDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericApplicationReleaseDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLApplicationReleaseDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleApplicationReleaseDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerApplicationReleaseDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of VisibilityDAOImplementation of the particular database engine.
     * @return specific VisibilityDAOImplementation
     */
    public static VisibilityDAO getVisibilityDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericVisibilityDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLVisibilityDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleVisibilityDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerVisibilityDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    /**
     * To get the instance of SubscriptionDAOImplementation of the particular database engine.
     * @return GenericSubscriptionDAOImpl
     */
    public static SubscriptionDAO getSubscriptionDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case Constants.DataBaseTypes.DB_TYPE_H2:
                case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericSubscriptionDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLSubscriptionDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleSubscriptionDAOImpl();
                case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerSubscriptionDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static ReviewDAO getCommentDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
            case Constants.DataBaseTypes.DB_TYPE_H2:
            case Constants.DataBaseTypes.DB_TYPE_MYSQL:
                return new GenericReviewDAOImpl();
            case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new PostgreSQLReviewDAOImpl();
            case Constants.DataBaseTypes.DB_TYPE_ORACLE:
                return new OracleReviewDAOImpl();
            case Constants.DataBaseTypes.DB_TYPE_MSSQL:
                return new SQLServerReviewDAOImpl();
            default:
                throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }
}
