package io.entgra.ui.request.interceptor.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.GrafanaEnvVariablesNotDefined;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaConstants;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaUtil;

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
