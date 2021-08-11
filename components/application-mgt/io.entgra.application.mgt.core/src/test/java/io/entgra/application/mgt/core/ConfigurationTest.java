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
package io.entgra.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.Test;
import io.entgra.application.mgt.core.config.Configuration;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.common.config.LifecycleState;

import java.util.List;

public class ConfigurationTest {

    @Test
    public void validateConfiguration() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        Assert.assertNotNull("Invalid app manager configuration", configuration);
    }

    @Test
    public void validateLifecycleStateConfiguration() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        List<LifecycleState> lifecycleStates = configuration.getLifecycleStates();
        Assert.assertNotNull("Invalid lifecycle states configuration", lifecycleStates);
        Assert.assertTrue("Invalid lifecycle states configuration. Lifecycle states cannot be empty",
                          !lifecycleStates.isEmpty());
    }
}
