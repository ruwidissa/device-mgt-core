package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.sql.connection;

import io.entgra.device.mgt.core.application.mgt.core.config.Configuration;
import io.entgra.device.mgt.core.application.mgt.core.config.ConfigurationManager;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DBConnectionException;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.dao.common.ReportMgtConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class GrafanaDatasourceConnectionFactory {

    private static final ReportMgtConfiguration reportMgtConfiguration= ReportMgtConfigurationManager.getInstance().
            getConfiguration();
    private static final DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
            getDeviceManagementConfig();

    private static final Configuration applicationMgtConfig = ConfigurationManager.getInstance().getConfiguration();

    public static Connection getConnection(String databaseName) throws SQLException, DBConnectionException,
            io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException {
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
