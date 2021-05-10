/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.ui.request.interceptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.ui.request.interceptor.exceptions.LoginException;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderServiceImpl;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@MultipartConfig
@WebServlet("/login")
public class LoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    private static String username;
    private static String password;
    private static String gatewayUrl;
    private static String uiConfigUrl;
    private static String keyManagerUrl;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            validateLoginRequest(req);
            HttpSession httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
            httpSession = req.getSession(true);
            //setting session to expiry in 5 minutes
            httpSession.setMaxInactiveInterval(Math.toIntExact(HandlerConstants.TIMEOUT));
            //todo: amalka do we need this remote call?
            JsonObject uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(
                    uiConfigUrl, gatewayUrl, httpSession, resp);

            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();

            List<String> list = new ArrayList<String>();
            for(int i=0; i < tags.size(); i++) {
                list.add(tags.get(i).getAsString());
            }

            String[] tagsAsStringArray = list.toArray(new String[list.size()]);

            String scopeString = HandlerUtil.getScopeString(scopes);

            if (scopeString != null) {
                scopeString = scopeString.trim();
            } else {
                scopeString = "default";
            }

            APIManagementProviderService apiManagementProviderService = new APIManagementProviderServiceImpl();
            ApiApplicationKey apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    HandlerConstants.PUBLISHER_APPLICATION_NAME,
                    tagsAsStringArray, HandlerConstants.PRODUCTION_KEY, username, false,
                    ApiApplicationConstants.DEFAULT_VALIDITY_PERIOD, scopeString);

            if (apiApplicationKey != null && getTokenAndPersistInSession(apiApplicationKey.getConsumerKey(),
                    apiApplicationKey.getConsumerSecret(), req, resp, scopes)) {
                log.info("tenantDomain : " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
                log.info("username : " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
                ProxyResponse proxyResponse = new ProxyResponse();
                proxyResponse.setCode(HttpStatus.SC_OK);
                HandlerUtil.handleSuccess(resp, proxyResponse);
                return;
            }
            HandlerUtil.handleError(resp, null);
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        } catch (LoginException e) {
            log.error("Error occurred while getting token data. ", e);
        } catch (APIManagerException e) {
            log.error("Error occurred while creating application. ", e);
        }
    }

    /***
     * Generates token from token endpoint and persists them inside the session
     *
     * @param req - {@link HttpServletRequest}
//     * @param clientAppResult - clientAppResult
     * @param scopes - scopes defied in the application-mgt.xml
     * @throws LoginException - login exception throws when getting token result
     */
    private boolean getTokenAndPersistInSession(String clientId, String clientSecret, HttpServletRequest req,
            HttpServletResponse resp, JsonArray scopes) throws LoginException {
        JsonParser jsonParser = new JsonParser();
        try {
            if (clientId != null && clientSecret != null) {
                String encodedClientApp = Base64.getEncoder()
                        .encodeToString((clientId + HandlerConstants.COLON + clientSecret).getBytes());

                ProxyResponse tokenResultResponse = getTokenResult(encodedClientApp, scopes);

                if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API to get token data.");
                    HandlerUtil.handleError(resp, tokenResultResponse);
                    return false;
                }
                String tokenResult = tokenResultResponse.getData();
                if (tokenResult == null) {
                    log.error("Invalid token response is received.");
                    HandlerUtil.handleError(resp, tokenResultResponse);
                    return false;
                }

                JsonElement jTokenResult = jsonParser.parse(tokenResult);
                if (jTokenResult.isJsonObject()) {
                    JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
                    HttpSession session = req.getSession(false);
                    if (session == null) {
                        return false;
                    }
                    AuthData authData = new AuthData();
                    authData.setClientId(clientId);
                    authData.setClientSecret(clientSecret);
                    authData.setEncodedClientApp(encodedClientApp);
                    authData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
                    authData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
                    authData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
                    session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new LoginException("Error occurred while sending the response into the socket", e);
        }
    }

    /***
     *
     * @param req - {@link HttpServletRequest}
     * Define username and password static parameters.
     */
    private static void validateLoginRequest(HttpServletRequest req) throws LoginException {
        String iotsCorePort = System.getProperty("iot.core.https.port");
        if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
            iotsCorePort = System.getProperty("iot.core.http.port");
        }

        String keyManagerPort = System.getProperty("iot.keymanager.https.port");

        username = req.getParameter("username");
        password = req.getParameter("password");
        gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.gateway.host")
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
        uiConfigUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.core.host")
                + HandlerConstants.COLON + iotsCorePort + HandlerConstants.UI_CONFIG_ENDPOINT;
        keyManagerUrl = HandlerConstants.HTTPS_PROTOCOL + HandlerConstants.SCHEME_SEPARATOR +
                System.getProperty("iot.keymanager.host") + HandlerConstants.COLON + keyManagerPort;

        if (username == null || password == null) {
            String msg = "Invalid login request. Username or Password is not received for login request.";
            log.error(msg);
            throw new LoginException(msg);
        }
    }

    /***
     * Generates tokens by invoking token endpoint
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes - Scopes which are retrieved by reading application-mgt configuration
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private ProxyResponse getTokenResult(String encodedClientApp, JsonArray scopes) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(keyManagerUrl + HandlerConstants.TOKEN_ENDPOINT);
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        String scopeString = HandlerUtil.getScopeString(scopes);

        if (scopeString != null) {
            scopeString = scopeString.trim();
        } else {
            scopeString = "default";
        }

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=" + HandlerConstants.PASSWORD_GRANT_TYPE + "&username=" + username + "&password=" +
                        password + "&scope=" + scopeString,
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenEndpoint.setEntity(tokenEPPayload);
        return HandlerUtil.execute(tokenEndpoint);
    }
}
