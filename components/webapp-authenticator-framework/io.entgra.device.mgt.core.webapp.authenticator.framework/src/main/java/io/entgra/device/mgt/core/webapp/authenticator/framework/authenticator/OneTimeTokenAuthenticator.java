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
package io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator;

import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationInfo;
import io.entgra.device.mgt.core.webapp.authenticator.framework.Constants;
import io.entgra.device.mgt.core.webapp.authenticator.framework.Utils.Utils;
import io.entgra.device.mgt.core.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

public class OneTimeTokenAuthenticator implements WebappAuthenticator {
    private static final Log log = LogFactory.getLog(OneTimeTokenAuthenticator.class);


    @Override
    public void init() {

    }

    public boolean canHandle(org.apache.catalina.connector.Request request) {
        return request.getHeader(Constants.HTTPHeaders.ONE_TIME_TOKEN_HEADER) != null;
    }

    public AuthenticationInfo authenticate(org.apache.catalina.connector.Request request, Response response) {

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();

        try {
            OTPManagementService otpManagementService = AuthenticatorFrameworkDataHolder.getInstance()
                    .getOtpManagementService();
            OneTimePinDTO validOTP;
            if (request.getRequestURI().toString().endsWith("cloud/download-url")
                    || request.getRequestURI().toString().endsWith("cloud/tenant")) {
                validOTP = otpManagementService.isValidOTP(request.getHeader(Constants.HTTPHeaders
                        .ONE_TIME_TOKEN_HEADER), true);
            } else {
                log.info("Validating OTP for enrollments PIN: " + request.getHeader(Constants
                        .HTTPHeaders.ONE_TIME_TOKEN_HEADER));
                validOTP = otpManagementService.isValidOTP(request.getHeader(Constants.HTTPHeaders
                        .ONE_TIME_TOKEN_HEADER), false);
            }

            if (validOTP != null) {
                authenticationInfo.setStatus(Status.CONTINUE);
                authenticationInfo.setTenantId(validOTP.getTenantId());
                authenticationInfo.setTenantDomain(Utils.getTenantDomain(validOTP.getTenantId()));
                authenticationInfo.setUsername(validOTP.getUsername());
            } else {
                authenticationInfo.setStatus(Status.FAILURE);
                authenticationInfo.setMessage("Invalid OTP token.");
            }
        } catch (Exception e) {
            String msg = "OTP Token Validation Failed.";
            log.error(msg, e);
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage(msg);
        }
        return authenticationInfo;
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
