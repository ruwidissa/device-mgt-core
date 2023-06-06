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

package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.http;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidConfigurationException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.UnknownHostException;

public class HTTPMessageExecutor implements Runnable {

    private String url;
    private String authorizationHeader;
    private String payload;
    private HostConfiguration hostConfiguration;
    private HttpClient httpClient;
    private static final String APPLIATION_JSON = "application/json";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Log log = LogFactory.getLog(HTTPMessageExecutor.class);

    public HTTPMessageExecutor(NotificationContext notificationContext, String authorizationHeader, String url
            , HostConfiguration hostConfiguration, HttpClient httpClient) {
        this.url = url;
        this.authorizationHeader = authorizationHeader;
        Gson gson = new Gson();
        this.payload = gson.toJson(notificationContext);
        this.hostConfiguration = hostConfiguration;
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        EntityEnclosingMethod method = null;

        try {
            method = new PostMethod(this.getUrl());
            method.setRequestEntity(new StringRequestEntity(this.getPayload(), APPLIATION_JSON, "UTF-8"));
            if (authorizationHeader != null && authorizationHeader.isEmpty()) {
                method.setRequestHeader(AUTHORIZATION_HEADER, authorizationHeader);
            }

            this.getHttpClient().executeMethod(hostConfiguration, method);

        } catch (UnknownHostException e) {
            log.error("Push Notification message dropped " + url, e);
            throw new InvalidConfigurationException("invalid host: url", e);
        } catch (Throwable e) {
            log.error("Push Notification message dropped ", e);
            throw new InvalidConfigurationException("Push Notification message dropped, " + e.getMessage(), e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
