/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.device.mgt.core.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    public static URI createURI(String uriString) {
        uriString = uriString.replace(" ", "%20");
        return URI.create(uriString);
    }

    public static String getRequestSubPathFromEnd(URI requestUri, int position) {
        if (requestUri.getPath() != null) {
            String[] pathList = requestUri.getPath().split("/");
            if (pathList.length - 1 >= position) {
                return pathList[pathList.length - 1 - position];
            }
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
        return EntityUtils.toString(response.getEntity());
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

    public static String getContentType(HttpResponse response) {
        ContentType contentType = ContentType.getOrDefault(response.getEntity());
        return contentType.getMimeType();
    }
}
