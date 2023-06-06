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
package io.entgra.device.mgt.core.webapp.authenticator.framework.util;

import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationInfo;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import java.util.Properties;

public class TestWebappAuthenticator implements WebappAuthenticator {

    private static final String NAME = "TestAuthenticator";

    @Override
    public void init() {

    }

    @Override
    public boolean canHandle(Request request) {
        return false;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

}
