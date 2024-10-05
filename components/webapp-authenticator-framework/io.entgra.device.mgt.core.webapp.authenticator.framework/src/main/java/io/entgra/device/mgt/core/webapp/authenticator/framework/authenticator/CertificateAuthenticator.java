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

import io.entgra.device.mgt.core.certificate.mgt.core.dto.CertificateResponse;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.KeystoreException;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPException;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationException;
import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationInfo;
import io.entgra.device.mgt.core.webapp.authenticator.framework.Constants;
import io.entgra.device.mgt.core.webapp.authenticator.framework.Utils.Utils;
import io.entgra.device.mgt.core.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * This authenticator authenticates HTTP requests using certificates.
 */
public class CertificateAuthenticator implements WebappAuthenticator {

    private static final Log log = LogFactory.getLog(CertificateAuthenticator.class);
    private static final String CERTIFICATE_AUTHENTICATOR = "CertificateAuth";
    private static final String CLIENT_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    @Override
    public void init() {

    }

    @Override
    public boolean canHandle(Request request) {
        return request.getHeader(Constants.HTTPHeaders.CERTIFICATE_VERIFICATION_HEADER) != null
                || request.getHeader(Constants.HTTPHeaders.MUTUAL_AUTH_HEADER) != null ||
                request.getHeader(Constants.HTTPHeaders.PROXY_MUTUAL_AUTH_HEADER) != null;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isEmpty()) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }

        try {
            // When there is a load balancer terminating mutual SSL, it should pass this header along and
            // as the value of this header, the client certificate subject dn should be passed.
            if (request.getHeader(Constants.HTTPHeaders.PROXY_MUTUAL_AUTH_HEADER) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("PROXY_MUTUAL_AUTH_HEADER " +
                            request.getHeader(Constants.HTTPHeaders.PROXY_MUTUAL_AUTH_HEADER));
                }
                CertificateResponse certificateResponse = AuthenticatorFrameworkDataHolder.getInstance().
                        getCertificateManagementService().verifySubjectDN(request.getHeader(
                                Constants.HTTPHeaders.PROXY_MUTUAL_AUTH_HEADER));
                authenticationInfo = checkCertificateResponse(certificateResponse);
                if (log.isDebugEnabled()) {
                    log.debug("Certificate Serial : " + certificateResponse.getSerialNumber()
                            + ", CN : " + certificateResponse.getCommonName()
                            + " , username" + authenticationInfo.getUsername());
                }
            }
            else if (request.getHeader(Constants.HTTPHeaders.MUTUAL_AUTH_HEADER) != null) {
                Object object = request.getAttribute(CLIENT_CERTIFICATE_ATTRIBUTE);
                X509Certificate[] clientCertificate = null;
                if (object instanceof  X509Certificate[]) {
                    clientCertificate = (X509Certificate[]) request.
                            getAttribute(CLIENT_CERTIFICATE_ATTRIBUTE);
                }
                if (clientCertificate != null && clientCertificate[0] != null) {
                    CertificateResponse certificateResponse = AuthenticatorFrameworkDataHolder.getInstance().
                            getCertificateManagementService().verifyPEMSignature(clientCertificate[0]);
                    authenticationInfo = checkCertificateResponse(certificateResponse);
                } else {
                    authenticationInfo.setStatus(Status.FAILURE);
                    authenticationInfo.setMessage("No client certificate is present");
                }
            } else if (request.getHeader(Constants.HTTPHeaders.CERTIFICATE_VERIFICATION_HEADER) != null) {
                String certHeader = request.getHeader(Constants.HTTPHeaders.CERTIFICATE_VERIFICATION_HEADER);
                if (certHeader != null &&
                    AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                            verifySignature(certHeader)) {
                    X509Certificate certificate =
                            AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                                    extractCertificateFromSignature(certHeader);
                    String challengeToken = AuthenticatorFrameworkDataHolder.getInstance().
                            getCertificateManagementService().extractChallengeToken(certificate);

                    if (challengeToken != null) {
                        challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        deviceIdentifier.setId(challengeToken);
                        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                        Utils.validateScepDevice(deviceIdentifier, authenticationInfo);
                        authenticationInfo.setStatus(Status.CONTINUE);
                    } else {
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        String deviceId = Utils.getSubjectDnAttribute(certificate,
                                Constants.Certificate.ORGANIZATION_ATTRIBUTE);
                        if (deviceId == null) {
                            authenticationInfo.setStatus(Status.FAILURE);
                            return authenticationInfo;
                        }
                        deviceIdentifier.setId(deviceId);
                        deviceIdentifier.setType(
                                DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
                        Utils.validateScepDevice(deviceIdentifier, authenticationInfo);
                        authenticationInfo.setStatus(Status.SUCCESS);
                    }
                }
            }
        } catch (KeystoreException e) {
            String msg = "Error occurred while validating device client certificate.";
            log.error(msg, e);
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage(msg);
        } catch (SCEPException e) {
            String msg = "Error occurred while validating device identification.";
            log.error(msg, e);
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage(msg);
        }
        return authenticationInfo;
    }

    private AuthenticationInfo checkCertificateResponse(CertificateResponse certificateResponse) {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if (certificateResponse == null) {
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage("Certificate sent doesn't match any certificate in the store." +
                                          " Unauthorized access attempt.");
        } else if (certificateResponse.getCommonName() != null && !certificateResponse.getCommonName().
                isEmpty()) {
            authenticationInfo.setTenantId(certificateResponse.getTenantId());
            authenticationInfo.setStatus(Status.CONTINUE);
            authenticationInfo.setUsername(certificateResponse.getUsername());
            try {
                authenticationInfo.setTenantDomain(Utils.getTenantDomain(
                                                                        certificateResponse.getTenantId()));
            } catch (AuthenticationException e) {
                authenticationInfo.setStatus(Status.FAILURE);
                authenticationInfo.setMessage("Could not identify tenant domain.");
            }
        } else {
            authenticationInfo.setStatus(Status.FAILURE);
            authenticationInfo.setMessage("A matching certificate is found, " +
                                          "but the serial number is missing in the database.");
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return CERTIFICATE_AUTHENTICATOR;
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