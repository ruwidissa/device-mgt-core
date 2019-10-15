/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.UnsupportedDatabaseEngineException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ReviewDAO;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.SQLServerApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.release.OracleApplicationReleaseDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.release.SQLServerApplicationReleaseDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate.OracleLifecycleStateDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate.SQLServerLifecycleStateDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.review.GenericReviewDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.release.GenericApplicationReleaseDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.OracleApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate.GenericLifecycleStateDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.review.OracleReviewDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.review.SQLServerReviewDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.subscription.GenericSubscriptionDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.subscription.OracleSubscriptionDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.subscription.SQLServerSubscriptionDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.visibility.GenericVisibilityDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.visibility.OracleVisibilityDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.visibility.SQLServerVisibilityDAOImpl;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

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
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericApplicationDAOImpl();
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
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericLifecycleStateDAOImpl();
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
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericApplicationReleaseDAOImpl();
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
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericVisibilityDAOImpl();
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
                case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new GenericSubscriptionDAOImpl();
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
            case Constants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                return new GenericReviewDAOImpl();
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
