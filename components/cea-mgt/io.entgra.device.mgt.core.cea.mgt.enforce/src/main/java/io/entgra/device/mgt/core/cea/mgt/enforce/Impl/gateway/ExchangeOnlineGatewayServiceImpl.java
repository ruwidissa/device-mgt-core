/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.enforce.Impl.gateway;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.GatewayServiceException;
import io.entgra.device.mgt.core.cea.mgt.enforce.service.gateway.GatewayService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ExchangeOnlineGatewayServiceImpl implements GatewayService {
    private static final Log log = LogFactory.getLog(ExchangeOnlineGatewayServiceImpl.class);
    private static final Set<String> SCOPES = new HashSet<>(Collections.singletonList("https://outlook.office365.com/.default"));
    private static final Map<String, IConfidentialClientApplication> confidentialClientApplications = new HashMap<>();

    @Override
    public String acquireAccessToken(ActiveSyncServer activeSyncServer) throws GatewayServiceException {
        try {
            IConfidentialClientApplication confidentialClientApplication = getOrCreateConfidentialClientApplication(
                    activeSyncServer.getClient(), activeSyncServer.getSecret(), activeSyncServer.getGatewayUrl());
            ClientCredentialParameters clientCredentialParameters = ClientCredentialParameters.builder(SCOPES).build();
            IAuthenticationResult result = confidentialClientApplication.acquireToken(clientCredentialParameters).get();
            if (log.isDebugEnabled()) {
                log.debug("Access token acquiring process is successful");
            }
            return result.accessToken();
        } catch (MalformedURLException e) {
            String msg = "Error occurred while constructing confidential client application";
            log.error(msg, e);
            throw new GatewayServiceException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Error occurred while acquiring access token";
            log.error(msg, e);
            throw new GatewayServiceException(msg, e);
        } catch (ExecutionException e) {
            String msg = "Error occurred while executing token acquiring access token";
            log.error(msg, e);
            throw new GatewayServiceException(msg, e);
        }
    }

    @Override
    public boolean validate(ActiveSyncServer activeSyncServer) throws GatewayServiceException {
        try {
            IConfidentialClientApplication confidentialClientApplication = getOrCreateConfidentialClientApplication(
                    activeSyncServer.getClient(), activeSyncServer.getSecret(), activeSyncServer.getGatewayUrl());
            return confidentialClientApplication.validateAuthority();
        } catch (MalformedURLException e) {
            String msg = "Error occurred while constructing confidential client application";
            log.error(msg, e);
            throw new GatewayServiceException(msg, e);
        }
    }

    /**
     * Retrieve confidential client application if exists, otherwise create and retrieve
     * @param clientId Client ID of the Azure AD application
     * @param secret Client Secret of the Azure AD application
     * @param authority Authority URL of the tenant which Azure AD application belongs
     * @return {@link IConfidentialClientApplication}
     * @throws MalformedURLException Throws when trying to set malformed authority URL
     */
    private IConfidentialClientApplication getOrCreateConfidentialClientApplication(String clientId, String secret, String authority)
            throws MalformedURLException {
        IConfidentialClientApplication confidentialClientApplication = confidentialClientApplications.get(clientId);
        if (confidentialClientApplication == null) {
            IClientCredential credential = ClientCredentialFactory.createFromSecret(secret);
            confidentialClientApplication = ConfidentialClientApplication.
                    builder(clientId, credential).authority(authority).build();
            confidentialClientApplications.put(clientId, confidentialClientApplication);
        }
        return confidentialClientApplication;
    }
}
