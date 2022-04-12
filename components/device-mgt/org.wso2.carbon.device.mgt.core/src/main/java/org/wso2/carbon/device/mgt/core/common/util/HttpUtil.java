/*
 *  Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.core.common.Constants;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    /**
     * Use to create {@link URI} from string
     * This does encoding of the provided uri string before creating the URI
     *
     * @param uriString uri string to be used to create the URI
     * @return created URI
     */
    public static URI createURI(String uriString) {
        uriString = uriString.replace(" ", "%20");
        return URI.create(uriString);
    }

    /**
     * Use to create basic auth header string for provided credentials
     *
     * @param userName username credential
     * @param password password credential
     * @return Basic auth header
     */
    public static String getBasicAuthBase64Header(String userName, String password) {
        return Constants.BASIC_AUTH_HEADER_PREFIX + getBase64Encode(userName, password);
    }

    /**
     * Use to get the base64 encoded string of key value pair.
     * For example this can be useful when creating basic auth header
     *
     * @return base64 encoded string of provided key and value
     */
    public static String getBase64Encode(String key, String value) {
        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
    }

    /**
     * Useful to get the sub path in a position from the given uri starting from end of the uri
     *
     * @param requestUri {@link URI}
     * @param position of which the sub path is needed
     * @return Sub path of the uri
     */
    public static String extractRequestSubPathFromEnd(URI requestUri, int position) {
        if (requestUri.getPath() != null) {
            String[] pathList = requestUri.getPath().split("/");
            if (pathList.length - 1 >= position) {
                return pathList[pathList.length - 1 - position];
            }
        }
        return null;
    }

    /**
     * Useful to get the sub path in a position from the given uri
     *
     * @param uri uri string from which the sub path should be extracted
     * @param position of which the sub path is needed
     * @return Sub path of the uri
     */
    public static String extractRequestSubPath(String uri, int position) {
        String[] pathList = uri.split("/");
        if (pathList.length - 1 > position) {
            String path = pathList[position + 1];
            if(path.contains(Constants.URI_QUERY_SEPARATOR)) {
                path = path.substring(0, path.indexOf(Constants.URI_QUERY_SEPARATOR));
            }
            return path;
        }
        return null;
    }

    /**
     * This returns the response body as string
     *
     * @param response {@link HttpResponse}
     * @return Response body string
     * @throws IOException if errors while converting response body to string
     */
    public static String getResponseString(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Useful to check if a given query param exists in uri
     *
     * @param param query param to be checked
     * @param uri in which query param should be checked
     * @return if the provided query parameter exists in uri
     */
    public static boolean isQueryParamExist(String param, URI uri) {
        Map<String, List<String>> queryMap = getQueryMap(uri);
        return queryMap.containsKey(param);
    }

    /**
     * This is useful to get first query parameter value from a query map.
     * For example a query parameter may have multiple values.
     *
     * @param param query parameter of which the value is needed
     * @param queryMap query map which contains query paramters and it's values
     * @return First value of provided query parameter
     */
    public static String getFirstQueryValue(String param, Map<String, List<String>> queryMap) {
        List<String> valueList = queryMap.get(param);
        String firstValue = null;
        if(valueList != null) {
            firstValue = valueList.get(0);
        }
        return firstValue;
    }

    /**
     * This constructs a key/value Map from query string of the provided uri.
     * In other words this will create a map with query parameters as keys and their
     * values as values.
     *
     * For example if the uri contains "?bar=1&foo=2" this will return a map
     * with bar and foo as keys and 1 and 2 as their values
     *
     * This is similar to getQueryMap(URI uri) method except that this accepts uri string
     *
     * @param uri of which the query map to be created
     * @return Query map of the provided uri
     */
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

    /**
     * This constructs a key/value Map from query string of the provided uri.
     * In other words this will create a map with query parameters as keys and their
     * values as values.
     *
     * For example if the uri contains "?bar=1&foo=2" this will return a map
     * with bar and foo as keys and 1 and 2 as their values
     *
     * @param uri of which the query map to be created
     * @return Query map of the provided uri
     */
    public static Map<String, List<String>> getQueryMap(URI uri) {
        String query = uri.getQuery();
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

    /**
     * Get query string from uri path. Return null if no query string exists
     *
     * @param uri of which query string should be taken
     * @return Query string of the provided uri
     */
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

    /**
     * Get content type of http response
     *
     * @param response {@link HttpResponse}
     * @return Content type
     */
    public static String getContentType(HttpResponse response) {
        ContentType contentType = ContentType.getOrDefault(response.getEntity());
        return contentType.getMimeType();
    }
}
