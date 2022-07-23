/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.extension;

import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.DefaultKeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.apimgt.keymgt.extension.customKeyManagerConfigComponent" immediate="true"
 */
public class KeyManagerConnectorConfiguration extends DefaultKeyManagerConnectorConfiguration {

    @Override
    public String getImplementation() {
        return CustomKeyManager.class.getName();
    }

    @Override
    public String getJWTValidator() {
        return JWTValidatorImpl.class.getName();
    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {
        return super.getApplicationConfigurations();
    }

    @Override
    public String getType() {
        return KeyMgtConstants.CUSTOM_TYPE;
    }

    @Override
    public String getDefaultScopesClaim() {
        return APIConstants.JwtTokenConstants.SCOPE;
    }

    @Override
    public String getDefaultConsumerKeyClaim() {
        return APIConstants.JwtTokenConstants.AUTHORIZED_PARTY;
    }

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {
        List<ConfigurationDto> configurationDtoList = new ArrayList<>();
        configurationDtoList.add(new ConfigurationDto("Username", "Username", "input", "Username of admin user", "", true, false, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto("Password", "Password", "input", "Password of Admin user", "", true, true, Collections.emptyList(), false));
        configurationDtoList.addAll(super.getConnectionConfigurations());
        return configurationDtoList;
    }
}
