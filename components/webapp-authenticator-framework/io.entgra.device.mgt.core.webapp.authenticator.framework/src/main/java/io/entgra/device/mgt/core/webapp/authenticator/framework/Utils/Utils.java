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

package io.entgra.device.mgt.core.webapp.authenticator.framework.Utils;

import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPException;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.SCEPManager;
import io.entgra.device.mgt.core.certificate.mgt.core.scep.TenantedDeviceWrapper;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationException;
import io.entgra.device.mgt.core.webapp.authenticator.framework.AuthenticationInfo;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;
import io.entgra.device.mgt.core.webapp.authenticator.framework.authenticator.oauth.OAuthValidatorFactory;
import io.entgra.device.mgt.core.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static int getTenantIdOFUser(String username) throws AuthenticationException {
        int tenantId = 0;
        String domainName = MultitenantUtils.getTenantDomain(username);
        if (domainName != null) {
            try {
                TenantManager tenantManager = AuthenticatorFrameworkDataHolder.getInstance().getRealmService()
                        .getTenantManager();
                tenantId = tenantManager.getTenantId(domainName);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " +
                        domainName;
                log.error(errorMsg, e);
                throw new AuthenticationException(errorMsg, e);
            }
        }
        return tenantId;
    }

    public static String getTenantDomain(int tenantId) throws AuthenticationException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            RealmService realmService = AuthenticatorFrameworkDataHolder.getInstance().getRealmService();
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                throw new AuthenticationException(msg);
            }

            return realmService.getTenantManager().getDomain(tenantId);

        } catch (UserStoreException e) {
            String msg = "User store not initialized";
            log.error(msg);
            throw new AuthenticationException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * To init BST and Oauth authenticators
     *
     * @param properties Properties of authenticators
     * @return token validator, if all the required parameters satisfied
     */
    public static OAuth2TokenValidator initAuthenticators(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException(
                    "Required properties needed to initialize OAuthAuthenticator are not provided");
        }
        String tokenValidationEndpointUrl = properties.getProperty("TokenValidationEndpointUrl");
        if (tokenValidationEndpointUrl == null || tokenValidationEndpointUrl.isEmpty()) {
            throw new IllegalArgumentException("OAuth token validation endpoint url is not provided");
        }
        String url = Utils.replaceSystemProperty(tokenValidationEndpointUrl);
        if ((url == null) || (url.isEmpty())) {
            throw new IllegalArgumentException("OAuth token validation endpoint url is not provided");
        }
        String adminUsername = DeviceManagerUtil.replaceSystemProperty(properties.getProperty("Username"));
        if (adminUsername == null) {
            throw new IllegalArgumentException(
                    "Username to connect to the OAuth token validation endpoint is not provided");
        }
        String adminPassword = DeviceManagerUtil.replaceSystemProperty(properties.getProperty("Password"));
        if (adminPassword == null) {
            throw new IllegalArgumentException(
                    "Password to connect to the OAuth token validation endpoint is not provided");
        }
        boolean isRemote = Boolean.parseBoolean(properties.getProperty("IsRemote"));
        Properties validatorProperties = new Properties();
        String maxTotalConnections = properties.getProperty("MaxTotalConnections");
        String maxConnectionsPerHost = properties.getProperty("MaxConnectionsPerHost");
        if (maxTotalConnections != null) {
            validatorProperties.setProperty("MaxTotalConnections", maxTotalConnections);
        }
        if (maxConnectionsPerHost != null) {
            validatorProperties.setProperty("MaxConnectionsPerHost", maxConnectionsPerHost);
        }
        return OAuthValidatorFactory.getValidator(url, adminUsername, adminPassword, isRemote, validatorProperties);
    }

    /**
     * To set the authentication info based on the OauthValidationResponse.
     *
     * @return Updated Authentication info based on OauthValidationResponse
     */
    public static AuthenticationInfo setAuthenticationInfo(OAuthValidationResponse oAuthValidationResponse,
            AuthenticationInfo authenticationInfo) throws AuthenticationException {
        if (oAuthValidationResponse.isValid()) {
            String username = oAuthValidationResponse.getUserName();
            String tenantDomain = oAuthValidationResponse.getTenantDomain();
            authenticationInfo.setUsername(username);
            authenticationInfo.setTenantDomain(tenantDomain);
            authenticationInfo.setTenantId(getTenantIdOFUser(username + "@" + tenantDomain));
            authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
        } else {
            authenticationInfo.setMessage(oAuthValidationResponse.getErrorMsg());
            authenticationInfo.setStatus(WebappAuthenticator.Status.FAILURE);
        }
        return authenticationInfo;
    }

    private static String replaceSystemProperty(String urlWithPlaceholders)  {
        String regex = "\\$\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matchPattern = pattern.matcher(urlWithPlaceholders);
        while (matchPattern.find()) {
            String sysPropertyName = matchPattern.group(1);
            String sysPropertyValue = System.getProperty(sysPropertyName);
            if (sysPropertyValue != null && !sysPropertyName.isEmpty()) {
                urlWithPlaceholders = urlWithPlaceholders.replaceAll("\\$\\{(" + sysPropertyName + ")\\}",
                        sysPropertyValue);
            }
        }
        return urlWithPlaceholders;
    }

    /**
     * Returns the value of the given attribute from the subject distinguished name. eg: "entgra.net"
     * from "CN=entgra.net"
     * @param requestCertificate {@link X509Certificate} that needs to extract an attribute from
     * @param attribute the attribute name that needs to be extracted from the cert. eg: "CN="
     * @return the value of the attribute
     */
    public static String getSubjectDnAttribute(X509Certificate requestCertificate, String attribute) {
        String distinguishedName = requestCertificate.getSubjectDN().getName();
        if (StringUtils.isNotEmpty(distinguishedName)) {
            String[] dnSplits = distinguishedName.split(",");
            for (String dnSplit : dnSplits) {
                if (dnSplit.contains(attribute)) {
                    String[] cnSplits = dnSplit.split("=");
                    if (StringUtils.isNotEmpty(cnSplits[1])) {
                        return cnSplits[1];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if the device identifier is valid and set the authentication info such as the tenant domain,
     * tenant id and username of the enrolled device.
     * @param deviceIdentifier {@link DeviceIdentifier} containing device id and type
     * @param authenticationInfo {@link AuthenticationInfo} containing tenant and user details
     * @throws SCEPException if the device or tenant does not exist
     */
    public static void validateScepDevice(DeviceIdentifier deviceIdentifier, AuthenticationInfo authenticationInfo)
            throws SCEPException {
        SCEPManager scepManager = AuthenticatorFrameworkDataHolder.getInstance().getScepManager();
        TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);
        authenticationInfo.setTenantDomain(tenantedDeviceWrapper.getTenantDomain());
        authenticationInfo.setTenantId(tenantedDeviceWrapper.getTenantId());

        // To make sure the tenant flow is not initiated in the valve as the
        // tenant flows are initiated at the API level on iOS
        if (deviceIdentifier.getType().equals(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS)) {
            authenticationInfo.setTenantId(-1);
        }

        if (tenantedDeviceWrapper.getDevice() != null &&
                tenantedDeviceWrapper.getDevice().getEnrolmentInfo() != null) {
            EnrolmentInfo enrolmentInfo = tenantedDeviceWrapper.getDevice().getEnrolmentInfo();
            authenticationInfo.setUsername(enrolmentInfo.getOwner());
        }
    }
}
