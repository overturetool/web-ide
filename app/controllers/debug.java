package controllers;

import core.debug.CommunicationFilter;
import core.debug.ProxyClient;
import core.debug.ProxyServer;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.codec.binary.StringUtils;
import play.mvc.WebSocket;

import java.util.Base64;

public class debug extends Application {
    public WebSocket<String> ws(String account, String path) {
        String type = request().getQueryString("type");
        String entryEncoded = request().getQueryString("entry");
        String entryDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(entryEncoded));
        String defaultName = null;

        int containsDefault = entryDecoded.indexOf("`"); // returns -1 if the character does not occur
        if (containsDefault > 0) {
            defaultName = entryDecoded.substring(0, containsDefault);
            defaultName = Base64.getEncoder().encodeToString(defaultName.getBytes());
        }

        int port = -1;

        IVFS file = new CommonsVFS(account, path);

        if (!file.exists())
            return errorResponse("file not found");

        ProxyServer proxyServer;

        if (file.isDirectory()) {
            if (type == null) return errorResponse("Model type was not defined");
            proxyServer = new ProxyServer(port, entryDecoded, type, defaultName, file);
        } else {
            proxyServer = new ProxyServer(port, entryDecoded, defaultName, file);
        }

        ProxyClient proxyClient = proxyServer.connect();
        if (proxyClient == null)
            return errorResponse("Error occurred while initiating connection");

        String initialResponse = proxyClient.read();
        if (initialResponse == null) {
            proxyClient.disconnect();
            return errorResponse("Initial read failed");
        }

        return new WebSocket<String>() {
            // Called when the Websocket Handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                String initialResponseFiltered = CommunicationFilter.ConvertPathsToRelative(initialResponse.replace("\u0000", ""));
                out.write(initialResponseFiltered);

                // For each event received on the socket
                in.onMessage(event -> {
                    String filteredEvent = CommunicationFilter.ConvertPathToAbsolute(event);
                    String overtureResult = proxyClient.sendAndRead(filteredEvent).replace("\u0000", "");
                    String filteredOvertureResult = CommunicationFilter.ConvertPathsToRelative(overtureResult);
                    out.write(filteredOvertureResult);
                });

                // When the socket is closed
                in.onClose(proxyClient::disconnect);
            }
        };
    }

    private WebSocket<String> errorResponse(String message) {
        return new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                out.write(message);
                out.close();
            }
        };
    }
}
