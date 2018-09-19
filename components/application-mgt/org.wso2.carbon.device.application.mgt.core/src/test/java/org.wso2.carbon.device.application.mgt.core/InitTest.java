package org.wso2.carbon.device.application.mgt.core;

import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;

import java.io.File;

/**
 * This class initializes the required configurations prior running the tests
 */
public class InitTest {

    @BeforeSuite
    public void init() throws InvalidConfigurationException {
        File configPath = new File("src/test/resources/application-mgt.xml");
        ConfigurationManager.setConfigLocation(configPath.getAbsolutePath());
    }

}
