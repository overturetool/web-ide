package controllers;

import core.debug.DebugFilters;
import core.debug.DebugClient;
import core.debug.DebugManager;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import org.apache.commons.codec.binary.StringUtils;
import play.mvc.LegacyWebSocket;
import play.mvc.WebSocket;
import play.mvc.WebSocket.In;
import play.mvc.WebSocket.Out;

import java.util.Base64;

public class Debug extends Application {
    public LegacyWebSocket<String> ws(String account, String path) {
        String type = request().getQueryString("type");
        String entryEncoded = request().getQueryString("entry");
        String entryDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(entryEncoded));
        String defaultName = null;
        DebugManager proxyServer;

        int containsDefault = entryDecoded.indexOf("`"); // returns -1 if the character does not occur
        if (containsDefault > 0) {
            defaultName = entryDecoded.substring(0, containsDefault);
            defaultName = Base64.getEncoder().encodeToString(defaultName.getBytes());
        }

        IVFS file = new CommonsVFS(account, path);

        if (!file.exists())
            return errorResponse("file not found");

        if (file.isDirectory()) {
            if (type == null) return errorResponse("Model type was not defined");
            proxyServer = new DebugManager(entryDecoded, type, defaultName, file);
        } else {
            proxyServer = new DebugManager(entryDecoded, defaultName, file);
        }

        DebugClient proxyClient = proxyServer.connect();
        if (proxyClient == null)
            return errorResponse("Error occurred while initiating connection");

        String initialResponse = proxyClient.read();
        if (initialResponse == null) {
            proxyClient.close();
            return errorResponse("Initial read failed");
        }

        return new LegacyWebSocket<String>() {
            // Called when the Websocket Handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                String initialResponseFiltered = DebugFilters.ConvertPathsToRelative(initialResponse.replace("\u0000", ""));
                out.write(initialResponseFiltered);

                // For each event received on the socket
                in.onMessage(request -> {
                    String filteredRequest = DebugFilters.ConvertPathToAbsolute(request);
                    String response = proxyClient.writeRead(filteredRequest);

                    if (response != null)
                        response = response.replace("\u0000", "");
                    else
                        response = ""; // TODO : return something else?

                    String filteredResponse = DebugFilters.ConvertPathsToRelative(response);
                    out.write(filteredResponse);
                });

                // When the socket is closed
                in.onClose(proxyClient::close);
            }
        };

        // TODO : Switch to new WebSocket implementation
        /*
        return WebSocket.Text.accept(request -> {
            // Called when the Websocket Handshake is done
            String initialResponseFiltered = DebugFilters.ConvertPathsToRelative(initialResponse.replace("\u0000", ""));
            Source.single(initialResponseFiltered);

            // For each event received on the socket
            return Flow.<String>create().map(msg -> {
                String filteredEvent = DebugFilters.ConvertPathToAbsolute(msg);
                String overtureResult = proxyClient.writeRead(filteredEvent).replace("\u0000", "");
                return DebugFilters.ConvertPathsToRelative(overtureResult);
            });
        });
        */
    }

    private LegacyWebSocket<String> errorResponse(String message) {
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {
                out.write(message);
                out.close();
            }
        };
    }
}
