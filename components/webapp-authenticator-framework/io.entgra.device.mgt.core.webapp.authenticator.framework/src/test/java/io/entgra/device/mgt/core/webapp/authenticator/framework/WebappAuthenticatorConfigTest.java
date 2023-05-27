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
package io.entgra.device.mgt.core.webapp.authenticator.framework;

import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticatorFrameworkException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfig;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfigService;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.WebappAuthenticatorConfig;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.impl.AuthenticatorConfigServiceImpl;

import java.util.List;

public class WebappAuthenticatorConfigTest {

    @BeforeClass
    public void init() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, "src/test/resources/config");
    }

    @Test
    public void testConfigInitialization() {
        try {
            WebappAuthenticatorConfig.init();
            WebappAuthenticatorConfig config = WebappAuthenticatorConfig.getInstance();
            Assert.assertNotNull(config);
            List<AuthenticatorConfig> authConfigs = config.getAuthenticators();
            Assert.assertNotNull(authConfigs);
        } catch (AuthenticatorFrameworkException e) {
            Assert.fail("Error occurred while testing webapp authenticator config initialization", e);
        } catch (Throwable e) {
            Assert.fail("Unexpected error has been encountered while testing webapp authenticator config " +
                    "initialization", e);
        }
    }


    @Test(description = "This method tests getAuthenticatorConfig method of AuthenticatorConfigService",
            dependsOnMethods = {"testConfigInitialization"})
    public void getAuthenticatorConfigTest() {
        AuthenticatorConfigService authenticatorConfigService = new AuthenticatorConfigServiceImpl();
        AuthenticatorConfig authenticatorConfig = authenticatorConfigService.getAuthenticatorConfig("BasicAuth");
        Assert.assertNotNull(authenticatorConfig,
                "Added authenticator config for the BasicAuth authenticator cannot be retrieved successfully");
        Assert.assertEquals(authenticatorConfig.getClassName(),
                "io.entgra.device.mgt.core.webapp.authenticator.framework" + ".authenticator.BasicAuthAuthenticator",
                "Class name related with Basic Auth does not match with "
                        + "the class name specified in the configuration");
        authenticatorConfig = authenticatorConfigService.getAuthenticatorConfig(null);
        Assert.assertNull(authenticatorConfig,
                "Authenticator is retrieved even when the authenticator name is given as null");
        authenticatorConfig = authenticatorConfigService.getAuthenticatorConfig("non-existing");
        Assert.assertNull(authenticatorConfig,
                "Authenticator is retrieved for a non-existing authenticator");

    }

    @AfterClass
    public void cleanup() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, "");
    }

}
