package controllers;

import core.debug.DBGPReaderConnector;
import core.debug.DebugCommunicationFilter;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.codec.binary.StringUtils;
import play.mvc.WebSocket;

import java.util.Base64;

public class debug extends Application {
    public WebSocket<String> ws(String path) {
        String type = request().getQueryString("type");
        String entryEncoded = request().getQueryString("entry");
        String entryDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(entryEncoded));

        int port = -1;

        IVFS file = new CommonsVFS(PathHelper.JoinPath(path));

        if (!file.exists()) {
            return errorResponse("file not found");
        }

        DBGPReaderConnector connector;

        if (file.isDirectory())
            connector = new DBGPReaderConnector(port, entryDecoded, type, file);
        else
            connector = new DBGPReaderConnector(port, entryDecoded, file);

        connector.connect();

        String initialResponse = connector.read();
        if (initialResponse == null) {
            connector.disconnect();
            return errorResponse("Initial read failed");
        }

        return new WebSocket<String>() {
            // Called when the Websocket Handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                out.write(initialResponse.replace("\u0000", ""));
                System.out.println(initialResponse);

                // For each event received on the socket
                in.onMessage(event -> {
                    //String overtureResult = connector.sendAndRead(event).replace("\u0000", "");
                    String filteredEvent = DebugCommunicationFilter.ConvertPathToAbsolute(event);
                    String overtureResult = connector.sendAndRead(filteredEvent).replace("\u0000", "");
                    out.write(overtureResult);
                    System.out.println(overtureResult);
                });

                // When the socket is closed
                in.onClose(connector::disconnect);
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
