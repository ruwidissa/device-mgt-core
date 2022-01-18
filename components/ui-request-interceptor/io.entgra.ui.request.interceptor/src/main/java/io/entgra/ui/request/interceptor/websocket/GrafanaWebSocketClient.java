package io.entgra.ui.request.interceptor.websocket;

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
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaUtil;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.ws.rs.core.HttpHeaders;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
