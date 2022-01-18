/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.device.mgt.common.exceptions.EventPublishingException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;

import javax.ws.rs.core.PathSegment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpReportingUtil {

    private static final Log log = LogFactory.getLog(HttpReportingUtil.class);
    private static final String IS_EVENT_PUBLISHING_ENABLED = "isEventPublishingEnabled";

    public static String getReportingHost() {
        return System.getProperty(DeviceManagementConstants.Report.REPORTING_EVENT_HOST);
    }

    public static int invokeApi(String payload, String endpoint) throws EventPublishingException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost apiEndpoint = new HttpPost(endpoint);
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            StringEntity requestEntity = new StringEntity(
                    payload, ContentType.APPLICATION_JSON);
            apiEndpoint.setEntity(requestEntity);
            HttpResponse response = client.execute(apiEndpoint);
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new EventPublishingException("Error occurred when " +
                    "invoking API. API endpoint: " + endpoint, e);
        }
    }
    public static String getRequestSubPathFromEnd(URI requestUri, int position) {
        String[] pathList = requestUri.getPath().split("/");
        if (pathList.length - 1 >= position) {
            return pathList[pathList.length - 1 - position];
        }
        return null;
    }

    public static String getRequestSubPath(String fullPath, int position) {
        String[] pathList = fullPath.split("/");
        if (pathList.length - 1 > position) {
            String path = pathList[position + 1];
            if(path.contains(Constants.URI_QUERY_SEPARATOR)) {
                path = path.substring(0, path.indexOf(Constants.URI_QUERY_SEPARATOR));
            }
            return path;
        }
        return null;
    }

    public static String getResponseString(HttpResponse response) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }
    public static boolean isQueryParamExist(String param, URI request) {
        Map<String, List<String>> queryMap = getQueryMap(request);
        return queryMap.containsKey(param);
    }
    public static String getFirstQueryValue(String param, Map<String, List<String>> queryMap) {
        List<String> valueList = queryMap.get(param);
        String firstValue = null;
        if(valueList != null) {
            firstValue = valueList.get(0);
        }
        return firstValue;
    }
    public static Map<String, List<String>> getQueryMap(String uri) {
        String query = getQueryFromURIPath(uri);
        Map<String, List<String>> map = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] paramArr = param.split("=");
                if (paramArr.length == 2) {
                    String name = paramArr[0];
                    String value = paramArr[1];
                    if (!map.containsKey(name)) {
                        List<String> valueList = new ArrayList<>();
                        map.put(name, valueList);
                    }
                    map.get(name).add(value);
                }
            }
        }
        return map;
    }
    public static Map<String, List<String>> getQueryMap(URI request) {
        String query = request.getQuery();
        Map<String, List<String>> map = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] paramArr = param.split("=");
                if (paramArr.length == 2) {
                    String name = paramArr[0];
                    String value = paramArr[1];
                    if (!map.containsKey(name)) {
                        List<String> valueList = new ArrayList<>();
                        map.put(name, valueList);
                    }
                    map.get(name).add(value);
                }
            }
        }
        return map;
    }
    public static String getQueryFromURIPath(String uri) {
        String query = null;
        if (uri.length() > "?".length() && uri.contains("?")) {
            query = uri.substring(uri.lastIndexOf("?") + "?".length());
        }
        if (StringUtils.isEmpty(query)) {
            query = null;
        }
        return query;
    }

    public static String getMemeType(HttpResponse response) {
        String memeType = "";
        Header contentType = response.getEntity().getContentType();
        if (contentType != null) {
            memeType = contentType.getValue().split(";")[0].trim();
        }
        return memeType;
    }
    public static String getGrafanaBaseUrl(String scheme) {
        String host = "localhost";
        String port = "3000";
        if (scheme.equals(Constants.HTTP_PROTOCOL)) {
            port = "3000";
        }
        return host + Constants.COLON + port;
    }

    public static boolean isPublishingEnabledForTenant() {
        Object configuration = DeviceManagerUtil.getConfiguration(IS_EVENT_PUBLISHING_ENABLED);
        if (configuration != null) {
            return Boolean.valueOf(configuration.toString());
        }
        return false;
    }
}
