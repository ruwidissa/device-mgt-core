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

import io.entgra.analytics.mgt.grafana.proxy.core.exception.GrafanaEnvVariablesNotDefined;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaConstants;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;
import javax.websocket.OnOpen;
import javax.websocket.OnMessage;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ServerEndpoint(value = "/grafana/api/live/ws")
public class GrafanaWebSocketHandler {
    private static final Log log = LogFactory.getLog(GrafanaWebSocketHandler.class);
    private GrafanaWebSocketClient grafanaClient;

    @OnOpen
    public void onOpen(Session browserSession) throws GrafanaEnvVariablesNotDefined {
        URI grafanaUri = browserSession.getRequestURI();
        String grafanaWebSocketUrl = getGrafanaWebSocketUrl(grafanaUri);
        try {
            grafanaClient = new GrafanaWebSocketClient(new URI(grafanaWebSocketUrl));
            grafanaClient.addMessageConsumer(message -> sendMessageToBrowser(browserSession, message));
        } catch (URISyntaxException e) {
            log.error("Invalid web socket uri provided", e);
        }
    }

    @OnClose
    public void onClose(CloseReason reason) throws IOException {
        log.info("Browser session closed: " + reason);
        if (grafanaClient.getGrafanaServerSession() != null) {
            grafanaClient.getGrafanaServerSession().close();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        grafanaClient.sendMessageToServer(message);
    }

    @OnError
    public void onError(Throwable t) {
        log.error("Error occurred in grafana browser session: " + t.toString());
        t.printStackTrace();
    }

    public void sendMessageToBrowser(Session browserSession, String message) {
        try {
            // Avoid grafana client sending messages when browser session is already closed
            if(browserSession.isOpen()) {
                browserSession.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending message to browser", e);
        }
    }

    private String getGrafanaWebSocketUrl(URI requestUri) throws GrafanaEnvVariablesNotDefined {
        return GrafanaUtil.getGrafanaWebSocketBase(requestUri.getScheme()) + GrafanaConstants.WS_LIVE_API;
    }
}
