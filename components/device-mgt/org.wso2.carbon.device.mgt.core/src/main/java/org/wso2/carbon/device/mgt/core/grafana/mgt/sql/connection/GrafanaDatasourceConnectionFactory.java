package org.wso2.carbon.device.mgt.core.grafana.mgt.sql.connection;

import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.grafana.mgt.config.GrafanaConfiguration;
import org.wso2.carbon.device.mgt.core.grafana.mgt.config.GrafanaConfigurationManager;
import org.wso2.carbon.device.mgt.core.report.mgt.config.ReportMgtConfiguration;
import org.wso2.carbon.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;
import org.wso2.carbon.device.mgt.core.report.mgt.dao.common.ReportMgtConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class GrafanaDatasourceConnectionFactory {

    private static final ReportMgtConfiguration reportMgtConfiguration= ReportMgtConfigurationManager.getInstance().
            getConfiguration();
    private static final DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
            getDeviceManagementConfig();

    public static Connection getConnection(String databaseName) throws SQLException, DBConnectionException {
        if(databaseName.equals(getReportManagementDatasourceName())) {
            ReportMgtConnectionManager.openDBConnection();
            return ReportMgtConnectionManager.getDBConnection();
        } else if (databaseName.equals(getDeviceManagementDatasourceName())) {
            DeviceManagementDAOFactory.openConnection();
            return DeviceManagementDAOFactory.getConnection();
        } else {
            throw new RuntimeException("No such datasource with the name: " + databaseName);
        }
    }
    public static void closeConnection(String databaseName) throws SQLException, DBConnectionException {
        if(databaseName.equals(getReportManagementDatasourceName())) {
            ReportMgtConnectionManager.closeDBConnection();
        } else if (databaseName.equals(getDeviceManagementDatasourceName())) {
            DeviceManagementDAOFactory.closeConnection();
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

}
