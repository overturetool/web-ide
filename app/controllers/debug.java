package controllers;

import org.apache.commons.codec.binary.StringUtils;
import play.libs.F;
import play.mvc.WebSocket;
import utilities.ServerConfigurations;
import utilities.debug.DBGPReaderConnector;
import utilities.file_system.commons_vfs2.CommonsVF;
import utilities.file_system.ICustomVF;

import java.util.Base64;

public class debug extends Application {
    public WebSocket<String> ws(String path) {
        String type = request().getQueryString("type");
        String entryEncoded = request().getQueryString("entry");
        String entryDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(entryEncoded));

        int port = 9223;
        String relativePath = ServerConfigurations.basePath + "/" + path;

        ICustomVF file = new CommonsVF(relativePath);

        if (!file.exists())
            return new WebSocket<String>() {
                @Override
                public void onReady(In<String> in, Out<String> out) {
                    out.write("file not found");
                    out.close();
                }
            };

        DBGPReaderConnector connector;

        if (file.isDirectory())
            connector = new DBGPReaderConnector(port, entryDecoded, type, file);
        else
            connector = new DBGPReaderConnector(port, entryDecoded, file);

        connector.connect();

        return new WebSocket<String>() {
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {;
                String initialResponse = connector.initialRead().replace("\u0000", "");
                out.write(initialResponse);
                System.out.println(initialResponse);

                // For each event received on the socket,
                in.onMessage(new F.Callback<String>() {
                    public void invoke(String event) {
                        // Log events to the console
                        String overtureResult = connector.send(event).replace("\u0000", "");
                        out.write(overtureResult);
                        System.out.println(overtureResult);
                    }
                });

                // When the socket is closed.
                in.onClose(new F.Callback0() {
                    public void invoke() {
                        connector.disconnect();
                        System.out.println("Disconnected!");
                    }
                });
            }
        };
    }
}
