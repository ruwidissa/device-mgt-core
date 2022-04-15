package io.entgra.analytics.mgt.grafana.proxy.core.sql.connection;

import io.entgra.application.mgt.core.config.Configuration;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.report.mgt.config.ReportMgtConfiguration;
import org.wso2.carbon.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.common.ReportMgtConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class GrafanaDatasourceConnectionFactory {

    private static final ReportMgtConfiguration reportMgtConfiguration= ReportMgtConfigurationManager.getInstance().
            getConfiguration();
    private static final DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
            getDeviceManagementConfig();

    private static final Configuration applicationMgtConfig = ConfigurationManager.getInstance().getConfiguration();

    public static Connection getConnection(String databaseName) throws SQLException, DBConnectionException,
            io.entgra.application.mgt.common.exception.DBConnectionException {
        if(databaseName.equals(getReportManagementDatasourceName())) {
            ReportMgtConnectionManager.openDBConnection();
            return ReportMgtConnectionManager.getDBConnection();
        } else if (databaseName.equals(getDeviceManagementDatasourceName())) {
            DeviceManagementDAOFactory.openConnection();
            return DeviceManagementDAOFactory.getConnection();
        } else if (databaseName.equals(getApplicationManagementDatasourceName())) {
            ConnectionManagerUtil.openDBConnection();
            return ConnectionManagerUtil.getDBConnection();
        } else {
            throw new RuntimeException("No such datasource with the name: " + databaseName);
        }
    }
    public static void closeConnection(String databaseName) {
        if(databaseName.equals(getReportManagementDatasourceName())) {
            ReportMgtConnectionManager.closeDBConnection();
        } else if (databaseName.equals(getDeviceManagementDatasourceName())) {
            DeviceManagementDAOFactory.closeConnection();
        } else if (databaseName.equals(getApplicationManagementDatasourceName())) {
            ConnectionManagerUtil.closeDBConnection();
        } else {
            throw new RuntimeException("No such datasource with the name: " + databaseName);
        }
    }

    private static String getReportManagementDatasourceName() {
        return reportMgtConfiguration.getDatasourceName();
    }

    private static String getDeviceManagementDatasourceName() {
        return deviceManagementConfig.getDeviceManagementConfigRepository().getDataSourceConfig().
                getJndiLookupDefinition().getJndiName();
    }

    private static String getApplicationManagementDatasourceName() {
        return applicationMgtConfig.getDatasourceName();
    }

}
