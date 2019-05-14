package org.wso2.carbon.device.application.mgt.core.dao;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.core.BaseTestCase;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationsDTO;
import org.wso2.carbon.device.application.mgt.core.dto.DeviceTypeCreator;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;

public class ApplicationManagementDAOTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementDAOTest.class);

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing ApplicationManagementDAOTest tests");
//        super.initializeServices();
    }

    @Test
    public void testAddApplication() throws Exception {

        ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        ConnectionManagerUtil.beginDBTransaction();
        applicationDAO.createApplication(ApplicationsDTO.getApp1(), -1234);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test
    public void addDeviceType() throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            deviceTypeDAO.addDeviceType(DeviceTypeCreator.getDeviceType(), -1234, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            log.error("Error occurred while adding dummy device type", e);
            Assert.fail();
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating a transaction to add dummy device type", e);
            Assert.fail();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }


}
