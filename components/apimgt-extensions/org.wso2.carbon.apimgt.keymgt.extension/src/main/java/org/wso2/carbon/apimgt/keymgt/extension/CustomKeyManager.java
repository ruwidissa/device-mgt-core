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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;

public class CustomKeyManager extends AMDefaultKeyManagerImpl {
    private static final Log log = LogFactory.getLog(CustomKeyManager.class);

    /**
     * This is used to get the metadata of the access token.
     *
     * @param accessToken AccessToken.
     * @return The meta data details of access token.
     * @throws APIManagementException This is the custom exception class for API management.
     */
    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {
        log.debug("Access Token With Prefix : "+accessToken);
        String accessTokenWithoutPrefix = accessToken.substring(accessToken.indexOf("_")+1);
        log.debug("Access Token WithOut Prefix : "+accessTokenWithoutPrefix);
        return super.getTokenMetaData(accessTokenWithoutPrefix);
    }

    @Override
    public String getType() {
        return KeyMgtConstants.CUSTOM_TYPE;
    }
}
