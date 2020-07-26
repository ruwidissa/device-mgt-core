/*
 *   Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.device.mgt.common.general.OneTimeTokenDetails;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.Constants;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthTokenValidationException;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneTimeTokenAuthenticator implements WebappAuthenticator {
    private static final Log log = LogFactory.getLog(OneTimeTokenAuthenticator.class);


    @Override
    public void init() {

    }

    public boolean canHandle(org.apache.catalina.connector.Request request) {
        return request.getHeader(Constants.HTTPHeaders.ONE_TIME_TOKEN_HEADER) != null;
    }

    public AuthenticationInfo authenticate(org.apache.catalina.connector.Request request, Response response) {

        String token = request.getHeader(Constants.HTTPHeaders.ONE_TIME_TOKEN_HEADER);
//        DeviceMgtAPIUtils.getDeviceManagementService();//TODO: call token validate service in core
        OneTimeTokenDetails tokenDetails = new OneTimeTokenDetails();//TODO: use token details

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();

        try {
            authenticationInfo.setTenantDomain(tokenDetails.getDomain());
            authenticationInfo.setStatus(Status.CONTINUE);
            //authenticationInfo.setUsername(tokenDetails.get); //TODO: set username
            //authenticationInfo.setTenantId();//TODO: set tenant Id
        } catch (Exception e) { // TODO: remove this if not needed
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage("Could not identify tenant domain.");
        }

        return null;
    }

    public String getName() {
        return "One-Time-Token";
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
