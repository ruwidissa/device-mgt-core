/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.core.identityserver.serviceprovider.impl;

import com.google.gson.Gson;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.IdentityServerManagementException;
import io.entgra.application.mgt.core.identityserver.serviceprovider.ISServiceProviderApplicationService;
import io.entgra.application.mgt.core.util.Constants;
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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class WSO2IAMSPApplicationService implements ISServiceProviderApplicationService {

    private static final List<String> REQUIRED_API_PARAM_LIST;
    private static final String TENANT_DOMAIN = "Tenant domain";
    private static final String SP_APPLICATION_API_CONTEXT = "/t/%s/api/server/v1/applications";
    private static final Log log = LogFactory.getLog(WSO2IAMSPApplicationService.class);

    static {
        REQUIRED_API_PARAM_LIST = Collections.singletonList(TENANT_DOMAIN);
    }

    public List<String> getRequiredApiParams() {
        return REQUIRED_API_PARAM_LIST;
    }

    public boolean isSPApplicationExist(IdentityServerDTO identityServer, String spAppId) throws ApplicationManagementException {
        SPApplication application = retrieveSPApplication(identityServer, spAppId);
        return application != null;
    }

    public SPApplication retrieveSPApplication(IdentityServerDTO identityServer, String spAppId) throws ApplicationManagementException {
        HttpGet req = new HttpGet();
        String uriString = constructAPIUrl(identityServer);
        uriString += Constants.FORWARD_SLASH + spAppId;
        req.setURI(HttpUtil.createURI(uriString));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplication.class);
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            String msg = constructErrorMessage(response);
            log.error(msg);
            throw new IdentityServerManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API. Make sure identity server is up and running";
            log.error(msg, e);
            throw new IdentityServerManagementException(msg, e);
        }
    }

    /**
     * Construct error message string depending on service providers api response
     * (I.E If unauthorized a different message will be returned)
     *
     * @param response of the service provider api call
     * @return constructed error message
     */
    private String constructErrorMessage(HttpResponse response) {
        String msg = "Error occurred while calling SP Applications API";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            msg += ". Make sure provided identity Server credentials are valid";
        }
        return msg;
    }

    public SPApplicationListResponse retrieveSPApplications(IdentityServerDTO identityServer, Integer limit, Integer offset)
            throws ApplicationManagementException {
        HttpGet req = new HttpGet();
        String uriString = constructAPIUrl(identityServer);
        uriString += Constants.URI_QUERY_SEPARATOR + Constants.LIMIT_QUERY_PARAM + Constants.QUERY_KEY_VALUE_SEPARATOR
                + limit;
        uriString += Constants.QUERY_STRING_SEPARATOR + Constants.OFFSET_QUERY_PARAM + Constants.QUERY_KEY_VALUE_SEPARATOR
                + offset;
        req.setURI(HttpUtil.createURI(uriString));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = invokeISAPI(identityServer, client, req);
            String responseBody = HttpUtil.getResponseString(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new Gson().fromJson(responseBody,
                        SPApplicationListResponse.class);
            }
            String msg = constructErrorMessage(response);
            log.error(msg);
            throw new IdentityServerManagementException(msg);
        } catch (IOException e) {
            String msg = "Error occurred while calling SP Applications API. Make sure identity server is up and running";
            log.error(msg, e);
            throw new IdentityServerManagementException(msg, e);
        }
    }

    /**
     * Takes essential prerequisite steps (I.E setting authorization header),
     * invokes provided GET request and returns the response
     *
     * @param identityServer {@link IdentityServerDTO}
     * @param client httpClient that should be used to invoke
     * @param request GET request to be invoked
     * @return response of the invoked api
     */
    private HttpResponse invokeISAPI(IdentityServerDTO identityServer, HttpClient client, HttpGet request) throws IOException {
        setBasicAuthHeader(identityServer, request);
        return client.execute(request);
    }

    /**
     * Add basic auth header to provided service provider api request by getting the username and password
     * from identity server bean
     *
     * @param identityServer {@link IdentityServerDTO}
     * @param request service provider api request
     */
    private void setBasicAuthHeader(IdentityServerDTO identityServer, HttpRequestBase request) {
        String basicAuthHeader = HttpUtil.getBasicAuthBase64Header(identityServer.getUsername(),
                identityServer.getPassword());
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader);
    }

    /**
     * Helps to construct service provider api base url
     *
     * @param identityServer {@link IdentityServerDTO}
     * @return constructed service providers api url
     */
    private String constructAPIUrl(IdentityServerDTO identityServer) {
        String identityServerUrl = identityServer.getUrl();
        // add "/" at the end, if the server url doesn't contain "/" at the end
        if (identityServerUrl.charAt(identityServerUrl.length() - 1) != Constants.FORWARD_SLASH.charAt(0)) {
            identityServerUrl += Constants.FORWARD_SLASH;
        }
        return identityServerUrl + String.format(SP_APPLICATION_API_CONTEXT, identityServer.getApiParams().get(TENANT_DOMAIN));
    }
}