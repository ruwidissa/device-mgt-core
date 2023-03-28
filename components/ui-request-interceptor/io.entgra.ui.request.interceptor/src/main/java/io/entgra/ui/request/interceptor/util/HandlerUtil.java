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

package io.entgra.ui.request.interceptor.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.cache.LoginCache;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.SM;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.List;

public class HandlerUtil {

    private static final Log log = LogFactory.getLog(HandlerUtil.class);
    private static LoginCache loginCache = null;
    private static boolean isLoginCacheInitialized = false;
    private static AuthData authData;

    /***
     *
     * @param httpRequest - httpMethod e.g:- HttpPost, HttpGet
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    public static ProxyResponse execute(HttpRequestBase httpRequest) throws IOException {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpResponse response = client.execute(httpRequest);
            ProxyResponse proxyResponse = new ProxyResponse();

            if (response == null) {
                log.error("Received null response for http request : " + httpRequest.getMethod() + " " + httpRequest
                        .getURI().toString());
                proxyResponse.setCode(HandlerConstants.INTERNAL_ERROR_CODE);
                proxyResponse.setStatus(ProxyResponse.Status.ERROR);
                proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(
                        HandlerConstants.INTERNAL_ERROR_CODE));
                return proxyResponse;
            } else {
                int statusCode = response.getStatusLine().getStatusCode();
                String jsonString = getResponseString(response);
                if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                    proxyResponse.setCode(statusCode);
                    proxyResponse.setData(jsonString);
                    proxyResponse.setStatus(ProxyResponse.Status.SUCCESS);
                    proxyResponse.setExecutorResponse("SUCCESS");
                    proxyResponse.setHeaders(response.getAllHeaders());
                    return proxyResponse;
                } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    if (isTokenExpired(jsonString)) {
                        proxyResponse.setCode(statusCode);
                        proxyResponse.setStatus(ProxyResponse.Status.ERROR);
                        proxyResponse.setExecutorResponse(HandlerConstants.TOKEN_IS_EXPIRED);
                    } else {
                        log.error(
                                "Received " + statusCode + " response for http request : " + httpRequest.getMethod()
                                        + " " + httpRequest.getURI().toString() + ". Error message: " + jsonString);
                        proxyResponse.setCode(statusCode);
                        proxyResponse.setData(jsonString);
                        proxyResponse.setStatus(ProxyResponse.Status.ERROR);
                        proxyResponse.setExecutorResponse(
                                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                    }
                    return proxyResponse;
                }
                log.error("Received " + statusCode +
                        " response for http request : " + httpRequest.getMethod() + " " + httpRequest.getURI()
                        .toString() + ". Error message: " + jsonString);
                proxyResponse.setCode(statusCode);
                proxyResponse.setData(jsonString);
                proxyResponse.setStatus(ProxyResponse.Status.ERROR);
                proxyResponse
                        .setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                return proxyResponse;
            }
        }
    }

    public static boolean isTokenExpired(String jsonBody) {
        return jsonBody.contains("Access token expired") || jsonBody
                .contains("Invalid input. Access token validation failed");
    }

    public static String getMemeType(HttpResponse response) {
        String memeType = "";
        Header contentType = response.getEntity().getContentType();
        if (contentType != null) {
            memeType = contentType.getValue().split(";")[0].trim();
        }
        return memeType;
    }

    /***
     *
     * @param statusCode Provide status code, e.g:- 400, 401, 500 etc
     * @return relative status code key for given status code.
     */
    public static String getStatusKey(int statusCode) {
        String statusCodeKey;

        switch (statusCode) {
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                statusCodeKey = "internalServerError";
                break;
            case HttpStatus.SC_BAD_REQUEST:
                statusCodeKey = "badRequest";
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                statusCodeKey = "unauthorized";
                break;
            case HttpStatus.SC_FORBIDDEN:
                statusCodeKey = "forbidden";
                break;
            case HttpStatus.SC_NOT_FOUND:
                statusCodeKey = "notFound";
                break;
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                statusCodeKey = "methodNotAllowed";
                break;
            case HttpStatus.SC_NOT_ACCEPTABLE:
                statusCodeKey = "notAcceptable";
                break;
            case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
                statusCodeKey = "unsupportedMediaType";
                break;
            default:
                statusCodeKey = "defaultPage";
                break;
        }
        return statusCodeKey;
    }



    /***
     * Handle error requests.
     *
     * @param resp {@link HttpServletResponse}
     * @param proxyResponse {@link ProxyResponse}
     * @throws IOException If error occurred when trying to send the error response.
     */
    public static void handleError(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        Gson gson = new Gson();
        if (proxyResponse == null) {
            proxyResponse = new ProxyResponse();
            proxyResponse.setStatus(ProxyResponse.Status.ERROR);
            proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil
                    .getStatusKey(HandlerConstants.INTERNAL_ERROR_CODE));
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

        proxyResponse.setExecutorResponse(null);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(proxyResponse));
        }
    }

    /**
     * Handle error requests with custom error codes.
     *
     * @param resp      {@link HttpServletResponse}
     * @param errorCode HTTP error status code
     * @throws IOException If error occurred when trying to send the error response.
     */
    public static void handleError(HttpServletResponse resp, int errorCode)
            throws IOException {
        ProxyResponse proxyResponse = constructProxyResponseByErrorCode(errorCode);
        HandlerUtil.handleError(resp, proxyResponse);
    }

    public static ProxyResponse constructProxyResponseByErrorCode(int errorCode) {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(errorCode);
        proxyResponse.setStatus(ProxyResponse.Status.ERROR);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(errorCode));
        return proxyResponse;
    }

    public static boolean isResponseSuccessful(ProxyResponse response) {
        return response.getStatus() == ProxyResponse.Status.SUCCESS;
    }

    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Success Response.
     */
    public static void handleSuccess(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        if (proxyResponse == null) {
            handleError(resp, null);
            return;
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());
        JSONObject response = new JSONObject();
        String responseData = proxyResponse.getData();

        if (!StringUtils.isEmpty(responseData)) {
            try {
                if (responseData.startsWith("{")) {
                    JSONObject responseDataJsonObj = new JSONObject(responseData);
                    response.put("data", responseDataJsonObj);
                } else if (responseData.startsWith("[")) {
                    JSONArray responseDataJsonArr = new JSONArray(responseData);
                    response.put("data", responseDataJsonArr);
                } else {
                    log.warn("Response data is not valid json string >> " + responseData);
                    response.put("data", responseData);
                }
            } catch (JSONException e) {
                log.error("Response data is not passable");
                response.put("data", responseData);
            }
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.write(response.toString());
        }
    }

    /**
     * Get api manager port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} api manager port
     */
    public static String getAPIManagerPort(String scheme) {
        String apiManagerPort = System.getProperty(HandlerConstants.IOT_APIM_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            apiManagerPort = System.getProperty(HandlerConstants.IOT_APIM_HTTP_PORT_ENV_VAR);
        }
        return apiManagerPort;
    }


    /**
     * Get keymanager port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} keymanager port
     */
    public static String getKeyManagerPort(String scheme) {
        String keyManagerPort = System.getProperty(HandlerConstants.IOT_KM_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            keyManagerPort = System.getProperty(HandlerConstants.IOT_KM_HTTP_PORT_ENV_VAR);
        }
        return keyManagerPort;
    }

    public static String getKeyManagerUrl(String scheme) {
        return scheme + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(scheme);
    }

    /**
     * Get gateway port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} gateway port
     */
    public static String getGatewayPort(String scheme) {
        String gatewayPort = System.getProperty(HandlerConstants.IOT_GW_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            gatewayPort = System.getProperty(HandlerConstants.IOT_GW_HTTP_PORT_ENV_VAR);
        }
        return gatewayPort;
    }

    /**
     * Get core port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} gateway port
     */
    public static String getCorePort(String scheme) {
        String productCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            productCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTP_PORT_ENV_VAR);
        }
        return productCorePort;
    }

    /**
     * Retrieve Http client based on hostname verification.
     *
     * @return {@link CloseableHttpClient} http client
     */
    public static CloseableHttpClient getHttpClient() {
        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.
                getProperty("org.wso2.ignoreHostnameVerification"));
        if (isIgnoreHostnameVerification) {
            return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        } else {
            return HttpClients.createDefault();
        }
    }

    /**
     * Send UnAuthorized Response to the user
     *
     * @param resp HttpServletResponse object
     */
    public static void sendUnAuthorizeResponse(HttpServletResponse resp)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setStatus(ProxyResponse.Status.ERROR);
        proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
        handleError(resp, proxyResponse);
    }

    /**
     * Generates the target URL for the proxy request.
     *
     * @param req         incoming {@link HttpServletRequest}
     * @param apiEndpoint API Endpoint URL
     * @return Target URL
     */
    public static String generateBackendRequestURL(HttpServletRequest req, String apiEndpoint) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiEndpoint).append(HandlerConstants.API_COMMON_CONTEXT)
                .append(req.getPathInfo().replace(" ", "%20"));
        if (StringUtils.isNotEmpty(req.getQueryString())) {
            urlBuilder.append("?").append(req.getQueryString());
        }
        return urlBuilder.toString();
    }

    public static String getIOTGatewayBase(HttpServletRequest req) {
        return req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
    }

    /**
     * Generate te request entity for POST and PUT requests from the incoming request.
     *
     * @param req          incoming {@link HttpServletRequest}.
     * @param proxyRequest proxy request instance.
     * @throws FileUploadException If unable to parse the incoming request for multipart content extraction.
     * @throws IOException         If error occurred while generating the request body.
     */
    public static void generateRequestEntity(HttpServletRequest req, HttpEntityEnclosingRequestBase proxyRequest)
            throws FileUploadException, IOException {
        if (ServletFileUpload.isMultipartContent(req)) {
            ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> fileItemList = servletFileUpload.parseRequest(req);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (FileItem item : fileItemList) {
                if (!item.isFormField()) {
                    entityBuilder.addPart(item.getFieldName(), new InputStreamBody(item.getInputStream(),
                            ContentType.create(item.getContentType()), item.getName()));
                } else {
                    entityBuilder.addTextBody(item.getFieldName(), item.getString());
                }
            }
            proxyRequest.setEntity(new BufferedHttpEntity(entityBuilder.build()));
            HandlerUtil.copyRequestHeaders(req, proxyRequest, false);
        } else {
            if (StringUtils.isNotEmpty(req.getHeader(HttpHeaders.CONTENT_LENGTH)) ||
                    StringUtils.isNotEmpty(req.getHeader(HttpHeaders.TRANSFER_ENCODING))) {
                InputStreamEntity entity = new InputStreamEntity(req.getInputStream(),
                        Long.parseLong(req.getHeader(HttpHeaders.CONTENT_LENGTH)));
                proxyRequest.setEntity(new BufferedHttpEntity(entity));
            }
            HandlerUtil.copyRequestHeaders(req, proxyRequest, true);
        }
    }

    /**
     * Generate te request entity for POST requests from the hubspot's incoming request.
     *
     * @param req          incoming {@link HttpServletRequest}.
     * @param proxyRequest proxy request instance.
     * @throws IOException         If error occurred while generating the request body.
     */
    public static void generateChatRequestEntity(HttpServletRequest req, HttpEntityEnclosingRequestBase proxyRequest)
            throws  IOException {
        if (StringUtils.isNotEmpty(req.getHeader(HttpHeaders.CONTENT_LENGTH)) ||
                StringUtils.isNotEmpty(req.getHeader(HttpHeaders.TRANSFER_ENCODING))) {
            InputStreamEntity entity = new InputStreamEntity(req.getInputStream(),
                    Long.parseLong(req.getHeader(HttpHeaders.CONTENT_LENGTH)));
            proxyRequest.setEntity(new BufferedHttpEntity(entity));
        }
    }

    /***
     * Constructs the application registration payload for DCR.
     *
     * @param tags - tags which are retrieved by reading app manager configuration
     * @param username - username provided from login form or admin username
     * @param password - password provided from login form or admin password
     * @return {@link StringEntity} of the payload to create the client application
     */
    public static StringEntity constructAppRegPayload(JsonArray tags, String appName, String username, String password) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(HandlerConstants.APP_NAME_KEY, appName);
        jsonObject.addProperty(HandlerConstants.USERNAME, username);
        jsonObject.addProperty(HandlerConstants.PASSWORD, password);
        jsonObject.addProperty(HandlerConstants.IS_ALLOWED_TO_ALL_DOMAINS_KEY, "false");
        jsonObject.add(HandlerConstants.TAGS_KEY, tags);
        String payload = jsonObject.toString();
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /***
     * Retrieves UI configuration and returns as Json.
     *
     * @param uiConfigUrl - UI configurations endpoint URL
     * @param gatewayUrl - gateway endpoint URL
     * @param httpSession - current active HttpSession
     * @param resp - HttpServletResponse
     * @return {@link JsonObject} of UI configurations
     */
    public static JsonObject getUIConfigAndPersistInSession(String uiConfigUrl, String gatewayUrl, HttpSession httpSession,
                                                            HttpServletResponse resp) throws IOException {
        HttpGet uiConfigEndpoint = new HttpGet(uiConfigUrl);
        ProxyResponse uiConfigResponse = HandlerUtil.execute(uiConfigEndpoint);
        String executorResponse = uiConfigResponse.getExecutorResponse();
        if (!StringUtils.isEmpty(executorResponse) && executorResponse
                .contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while getting UI configurations by invoking " + uiConfigUrl);
            HandlerUtil.handleError(resp, uiConfigResponse);
        }

        if (uiConfigResponse.getData() == null) {
            log.error("UI config retrieval is failed, and didn't find UI configuration for App manager.");
            HandlerUtil.handleError(resp, null);
        }
        JsonParser jsonParser = new JsonParser();

        JsonElement uiConfigJsonElement = jsonParser.parse(uiConfigResponse.getData());
        JsonObject uiConfigJsonObject = null;
        if (uiConfigJsonElement.isJsonObject()) {
            uiConfigJsonObject = uiConfigJsonElement.getAsJsonObject();
            if (uiConfigJsonObject == null) {
                log.error(
                        "Either UI config json element is not an json object or converting rom json element to json object is failed.");
                HandlerUtil.handleError(resp, null);
            }
            httpSession.setAttribute(HandlerConstants.UI_CONFIG_KEY, uiConfigJsonObject);
            httpSession.setAttribute(HandlerConstants.PLATFORM, gatewayUrl);
        }
        return uiConfigJsonObject;
    }

    /***
     * Converts scopes from JsonArray to string with space separated values.
     *
     * @param scopes - scope Json Array and it is retrieved by reading UI config.
     * @return string value of the defined scopes
     */
    public static String getScopeString(JsonArray scopes) {
        if (scopes != null && scopes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (JsonElement scope : scopes) {
                String tmpScope = scope.getAsString() + " ";
                builder.append(tmpScope);
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    /***
     * Converts xml file into string.
     *
     * @param xmlFile - xmlFile which needs to be converted into string.
     * @return string value of the xml file.
     */
    public static String xmlToString(File xmlFile) {
        String stringOutput = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            OutputFormat format = new OutputFormat(doc);
            StringWriter stringWriterOutput = new StringWriter();
            XMLSerializer serial = new XMLSerializer(stringWriterOutput, format);
            serial.serialize(doc);
            stringOutput = stringWriterOutput.toString();
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (ParserConfigurationException e) {
            log.error("Error while creating the document builder.");
        } catch (SAXException e) {
            log.error("Error while parsing xml file.", e);
        }

        return stringOutput;
    }

    /***
     * Search a key from a given json string object.
     *
     * @param jsonObjectString - json object in string format.
     * @param key - the key to be searched.
     * @return string value of the key value.
     */
    private static String searchFromJsonObjectString(String jsonObjectString, String key) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonObjectString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.get(key).getAsString();
    }

    /***
     * Initializes the login cache.
     *
     * @param httpSession - current active HttpSession.
     */
    private static void initializeLoginCache(HttpSession httpSession) {
        String uiConfig = httpSession.getAttribute(HandlerConstants.UI_CONFIG_KEY).toString();
        int capacity = Integer.parseInt(searchFromJsonObjectString(uiConfig, HandlerConstants.LOGIN_CACHE_CAPACITY_KEY));
        loginCache = new LoginCache(capacity);
    }

    /***
     * Retrieves login cache and initializes if its not done already.
     *
     * @param httpSession - current active HttpSession.
     */
    public static LoginCache getLoginCache(HttpSession httpSession) {
        if (!isLoginCacheInitialized || loginCache == null) {
            isLoginCacheInitialized = true;
            initializeLoginCache(httpSession);
        }
        return loginCache;
    }

    /**
     * Retry request again after refreshing the access token
     *
     * @param req         incoming {@link HttpServletRequest}
     * @param httpRequest subclass of {@link HttpRequestBase} related to the current request.
     * @return {@link ProxyResponse} if successful and <code>null</code> if failed.
     * @throws IOException If an error occurs when try to retry the request.
     */
    public static ProxyResponse retryRequestWithRefreshedToken(HttpServletRequest req, HttpRequestBase httpRequest,
                                                               String apiEndpoint) throws IOException {
        ProxyResponse retryResponse = refreshToken(req, apiEndpoint);
        if (isResponseSuccessful(retryResponse)) {
            HttpSession session = req.getSession(false);
            if (session == null) {
                log.error("Unauthorized, You are not logged in. Please log in to the portal");
                return constructProxyResponseByErrorCode(HttpStatus.SC_UNAUTHORIZED);
            }
            httpRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
            ProxyResponse proxyResponse = HandlerUtil.execute(httpRequest);
            if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API after refreshing the token.");
                return proxyResponse;
            }
            return proxyResponse;

        }
        return retryResponse;
    }

    /***
     * This method is responsible to get the refresh token
     *
     * @param req {@link HttpServletRequest}
     * @return If successfully renew tokens, returns TRUE otherwise return FALSE
     * @throws IOException If an error occurs while witting error response to client side or invoke token renewal API
     */
    private static ProxyResponse refreshToken(HttpServletRequest req, String keymanagerUrl)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("refreshing the token");
        }
        ProxyResponse tokenResultResponse;
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Couldn't find a session, hence it is required to login and proceed.");
            tokenResultResponse = constructProxyResponseByErrorCode(HttpStatus.SC_UNAUTHORIZED);
//            handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return tokenResultResponse;
        }

        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        tokenResultResponse = getTokenResult(authData, keymanagerUrl);
        if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while refreshing access token.");
//            HandlerUtil.handleError(resp, tokenResultResponse);
            return tokenResultResponse;
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement jTokenResult = jsonParser.parse(tokenResultResponse.getData());

        if (jTokenResult.isJsonObject()) {
            setNewAuthData(constructAuthDataFromTokenResult(jTokenResult, authData), session);
            return tokenResultResponse;
        }

        log.error("Error Occurred in token renewal process.");
        tokenResultResponse = constructProxyResponseByErrorCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//        handleError(resp, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        return tokenResultResponse;
    }
    public static ProxyResponse getTokenResult(AuthData authData, String keymanagerUrl) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(keymanagerUrl + HandlerConstants.OAUTH2_TOKEN_ENDPOINT);
        StringEntity tokenEndpointPayload = new StringEntity(
                "grant_type=refresh_token&refresh_token=" + authData.getRefreshToken(),
                ContentType.APPLICATION_FORM_URLENCODED);

        tokenEndpoint.setEntity(tokenEndpointPayload);
        String encodedClientApp = authData.getEncodedClientApp();
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        return HandlerUtil.execute(tokenEndpoint);
    }

    public static void setNewAuthData(AuthData newAuthData, HttpSession session) {
        authData = newAuthData;
        session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, newAuthData);
    }

    public static AuthData constructAuthDataFromTokenResult(JsonElement tokenResult, AuthData authData) {
        JsonObject jTokenResultAsJsonObject = tokenResult.getAsJsonObject();
        AuthData newAuthData = new AuthData();
        newAuthData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
        newAuthData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
        newAuthData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
        newAuthData.setClientId(authData.getClientId());
        newAuthData.setClientSecret(authData.getClientSecret());
        newAuthData.setEncodedClientApp(authData.getEncodedClientApp());
        newAuthData.setUsername(authData.getUsername());
        return newAuthData;
    }

    /**
     * Copy incoming request headers to the proxy request.
     *
     * @param req                 incoming {@link HttpServletRequest}
     * @param httpRequest         proxy request instance.
     * @param preserveContentType <code>true</code> if content type header needs to be preserved.
     *                            This should be set to <code>false</code> when handling multipart requests as Http
     *                            client will generate the Content-Type header automatically.
     */
    public static void copyRequestHeaders(HttpServletRequest req, HttpRequestBase httpRequest, boolean preserveContentType) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) ||
                    headerName.equalsIgnoreCase(SM.COOKIE) ||
                    (!preserveContentType && headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE))) {
                continue;
            }
            Enumeration<String> headerValues = req.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                httpRequest.setHeader(headerName, headerValues.nextElement());
            }
        }
    }

    public static String getHeaderValue(String headerName, Header[] headers) {
        String headerValue = null;
        for(Header header : headers) {
            if (header.getName().equalsIgnoreCase(headerName)) {
                headerValue = header.getValue();
            }
        }
        return headerValue;
    }

    public static String getResponseString(HttpResponse response) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                responseBuilder.append(line);
            }
            return responseBuilder.toString();
        }
    }


    public static boolean isPropertyDefined(String property) {
        return StringUtils.isEmpty(System.getProperty(property));
    }
}
