/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.handler.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.device.application.mgt.handler.exceptions.LoginException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HandlerUtil {

    private static final Log log = LogFactory.getLog(HandlerUtil.class);
    private static JsonObject uiConfigAsJsonObject;

    /***
     *
     * @param httpMethod - httpMethod e.g:- HttpPost, HttpGet
     * @param <T> - HttpPost or HttpGet class
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    public static <T> HttpResponse execute(T httpMethod) throws IOException {
        HttpResponse response = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            if (httpMethod instanceof HttpPost) {
                HttpPost method = (HttpPost) httpMethod;
                response = client.execute(method);
            } else if (httpMethod instanceof HttpGet) {
                HttpGet method = (HttpGet) httpMethod;
                response = client.execute(method);
            }

            if (response != null) {
                return response;
            }
            return null;
        }
    }

    /***
     *
     * @param response {@link HttpResponse}
     * @return {@link String} get the response payload by using Entity of the response
     * @throws IOException if error occurs while reading the content of the response or reading {@link BufferedReader}
     * object
     */
    public static String retrieveResponseString (HttpResponse response) throws  IOException{
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    /***
     *
     * @param statusCode Provide status code, e.g:- 400, 401, 500 etc
     * @return relative status code key for given status code.
     */
    public static String getStatusKey (int statusCode){
        String statusCodeKey;

        switch (statusCode) {
        case 500:
            statusCodeKey = "internalServerError";
            break;
        case 400:
            statusCodeKey = "badRequest";
            break;
        case 401:
            statusCodeKey = "unauthorized";
            break;
        case 403:
            statusCodeKey = "forbidden";
            break;
        case 404:
            statusCodeKey = "notFound";
            break;
        case 405:
            statusCodeKey = "methodNotAllowed";
            break;
        case 406:
            statusCodeKey = "notAcceptable";
            break;
        case 415:
            statusCodeKey = "unsupportedMediaType";
            break;
        default:
            statusCodeKey = "defaultPage";
            break;
        }
        return statusCodeKey;
    }

    /***
     *
     * @param uiConfigUrl - JSON string of the UI configuration
     * @return - True returns if UI config load is succeeded and False returns if it fails. Further, if call ie
     * succeeded assign values for uiConfigAsJsonObject static variable.
     * @throws LoginException IO exception could occur if an error occured when invoking end point for getting UI
     * configs
     */
    public static JsonObject loadUiConfig(String uiConfigUrl) throws LoginException {
        try {
            if (uiConfigAsJsonObject != null) {
                return uiConfigAsJsonObject;
            }
            HttpGet uiConfigEndpoint = new HttpGet(uiConfigUrl);
            JsonParser jsonParser = new JsonParser();
            HttpResponse response = execute(uiConfigEndpoint);
            if (response != null && response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String uiConfig = retrieveResponseString(response);
                JsonElement uiConfigJsonElement = jsonParser.parse(uiConfig);
                if (uiConfigJsonElement.isJsonObject()) {
                    uiConfigAsJsonObject = uiConfigJsonElement.getAsJsonObject();
                    return uiConfigAsJsonObject;
                }
            }
        } catch (IOException e) {
            throw new LoginException("Error occured while getting UI configs. ", e);
        }
        return null;
    }

}
