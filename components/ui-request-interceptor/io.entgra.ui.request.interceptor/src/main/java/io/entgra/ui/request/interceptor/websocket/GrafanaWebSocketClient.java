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

package io.entgra.ui.request.interceptor.websocket;

import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.CloseReason;
import javax.websocket.WebSocketContainer;
import java.net.URI;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ClientEndpoint
public class GrafanaWebSocketClient extends Endpoint {

    private static final Log log = LogFactory.getLog(GrafanaWebSocketClient.class);
    private Session grafanaServerSession;
    private Consumer<String> messageConsumer;

    public GrafanaWebSocketClient(URI endpointURI) {
        try {
            ClientEndpointConfig clientEndpointConfig = handShakeRequestConfig();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientEndpointConfig, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ClientEndpointConfig handShakeRequestConfig() {
        ClientEndpointConfig.Configurator clientEndpointConfigConfigurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                try {
                    headers.put(HttpHeaders.AUTHORIZATION,
                            Collections.singletonList(GrafanaUtil.getBasicAuthBase64Header()));
                } catch (GrafanaManagementException e) {
                    log.error(e);
                }
            }


        };
        return ClientEndpointConfig.Builder.create().
                configurator(clientEndpointConfigConfigurator).build();
    }

    @OnOpen
    @Override
    public void onOpen(Session grafanaServerSession, EndpointConfig endpointConfig) {
        // Due to a bug (https://bz.apache.org/bugzilla/show_bug.cgi?format=multiple&id=57788)
        // in the tomcat version used, this has to coded like this
        grafanaServerSession.addMessageHandler(String.class, message -> messageConsumer.accept(message));
        this.grafanaServerSession = grafanaServerSession;
    }

    @OnClose
    @Override
    public void onClose(Session session, CloseReason reason) {
        log.info("Server session closed: " + reason);
        this.grafanaServerSession = null;
    }

    @OnError
    @Override
    public void onError(Session session, Throwable t) {
        log.error("Error occurred in grafana server session: " + t.toString());
        t.printStackTrace();
    }

    public void sendMessageToServer(String message) {
        if (grafanaServerSession.getAsyncRemote() != null) {
            grafanaServerSession.getAsyncRemote().sendText(message);
        }
    }

    public Session getGrafanaServerSession() {
        return grafanaServerSession;
    }

    public void addMessageConsumer(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
}
