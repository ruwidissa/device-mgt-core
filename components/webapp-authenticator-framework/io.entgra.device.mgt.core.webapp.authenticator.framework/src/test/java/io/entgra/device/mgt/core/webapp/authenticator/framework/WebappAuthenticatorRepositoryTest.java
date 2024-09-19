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

import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import io.entgra.device.mgt.core.webapp.authenticator.framework.util.MalformedAuthenticator;
import io.entgra.device.mgt.core.webapp.authenticator.framework.util.TestWebappAuthenticator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebappAuthenticatorRepositoryTest {

    @Test
    public void testAddAuthenticator() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();

        WebappAuthenticator addedAuthenticator = new TestWebappAuthenticator();
        repository.addAuthenticator(addedAuthenticator);

        WebappAuthenticator retriedAuthenticator = repository.getAuthenticator(addedAuthenticator.getName());
        Assert.assertEquals(addedAuthenticator.getName(), retriedAuthenticator.getName());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddMalformedAuthenticator() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        WebappAuthenticator malformedAuthenticator = new MalformedAuthenticator();
        repository.addAuthenticator(malformedAuthenticator);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddAuthenticatorWithNull() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        repository.addAuthenticator(null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddAuthenticatorWithEmptyString() {
        WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
        repository.addAuthenticator(null);
    }

}
