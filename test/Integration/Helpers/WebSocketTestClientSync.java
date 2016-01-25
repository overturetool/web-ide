package Integration.Helpers;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketTestClientSync {
    private Session session = null;
    private MessageHandler messageHandler;
    private final Object token = new Object();

    public WebSocketTestClientSync(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param session
     *            the session which is opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param session
     *            the session which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     *
     * @param message
     *            The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
            //synchronized (token) { token.notify(); }
        }
    }

    public void close() throws IOException {
        if (this.session != null)
            this.session.close();
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) throws IOException {
        //synchronized (token) { token.wait(5000); }
        if (this.session != null)
            this.session.getBasicRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     */
    public static interface MessageHandler {
        public void handleMessage(String message);
    }
}
