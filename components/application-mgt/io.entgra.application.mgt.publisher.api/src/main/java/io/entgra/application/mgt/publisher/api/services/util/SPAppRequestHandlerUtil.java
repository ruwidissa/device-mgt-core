/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.entgra.application.mgt.publisher.api.services.util;

import com.google.gson.Gson;
import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.config.IdentityServerDetail;
import io.entgra.application.mgt.core.util.APIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class SPAppRequestHandlerUtil {

    private static final Log log = LogFactory.getLog(SPAppRequestHandlerUtil.class);

    /**
     * Check if service provider application exists
     *
     * @param identityServerId id of the identity server
     * @param spAppId uid of the service provider
     * @return if service provider exist
     * @throws ApplicationManagementException
     */
    public static boolean isSPApplicationExist(int identityServerId, String spAppId) throws ApplicationManagementException {
        SPApplication application = retrieveSPApplication(identityServerId, spAppId);
        return application != null;
    }

    /**
     *  Get service provider by identity server id and service provider uid
     * @param identityServerId  id of the identity server
     * @param spAppId uid of service provider to be retrieved
     * @return {@link SPApplication}
     * @throws ApplicationManagementException
     */
    public static SPApplication retrieveSPApplication(int identityServerId, String spAppId)
            throws ApplicationManagementException {
        IdentityServer identityServer = getIdentityServer(identityServerId);
        HttpGet req = new HttpGet();
        URI uri = HttpUtil.createURI(getSPApplicationsAPI(identityServer));
        uri = UriBuilder.fromUri(uri).path(spAppId).build();
        req.setURI(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplication.class);
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error occurred while closing http connection");
            }
        }
    }

    /**
     * Retrieve service provider apps from identity server
     *
     * @param identityServerId id of the identity server
     * @return {@link SPApplicationListResponse}
     * @throws ApplicationManagementException
     */
    public static SPApplicationListResponse retrieveSPApplications(int identityServerId, Integer limit, Integer offset)
            throws ApplicationManagementException {
        IdentityServer identityServer = getIdentityServer(identityServerId);
        HttpGet req = new HttpGet();
        URI uri = HttpUtil.createURI(getSPApplicationsAPI(identityServer));
        UriBuilder uriBuilder = UriBuilder.fromUri(uri);
        if (limit != null) {
            uriBuilder = uriBuilder.queryParam(io.entgra.application.mgt.core.util.Constants.LIMIT_QUERY_PARAM, limit);
        }
        if (offset != null) {
            uriBuilder = uriBuilder.queryParam(io.entgra.application.mgt.core.util.Constants.OFFSET_QUERY_PARAM, offset);
        }
        uri = uriBuilder.build();
        req.setURI(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplicationListResponse.class);
            }
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error occurred while closing http connection");
            }
        }
    }

    /**
     *
     * @param identityServerId id of the identity server
     * @return {@link IdentityServer}
     * @throws ApplicationManagementException
     */
    public static IdentityServer getIdentityServer(int identityServerId) throws ApplicationManagementException {
        SPApplicationManager spApplicationManager = APIUtil.getSPApplicationManager();
        return spApplicationManager.getIdentityServer(identityServerId);
    }

    private static HttpResponse invokeISAPI(IdentityServer identityServer, HttpClient client, HttpRequestBase request) throws IOException {
        setBasicAuthHeader(identityServer, request);
        return client.execute(request);
    }

    private static void setBasicAuthHeader(IdentityServer identityServer, HttpRequestBase request) {
        String basicAuthHeader = HttpUtil.getBasicAuthBase64Header(identityServer.getUserName(),
                identityServer.getPassword());
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader);
    }

    private static String getSPApplicationsAPI(IdentityServer identityServer) {
        IdentityServerDetail identityServerDetail = ConfigurationManager.getInstance().getIdentityServerConfiguration().
                getIdentityServerDetailByProviderName(identityServer.getProviderName());
        return identityServer.getApiUrl() + identityServerDetail.getServiceProvidersAPIContextPath();
    }

}