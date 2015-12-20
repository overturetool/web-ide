package controllers;

import org.apache.commons.codec.binary.StringUtils;
import play.libs.F;
import play.mvc.WebSocket;
import utilities.debug.DBGPReaderConnector;
import utilities.file_system.CommonsVFS;
import utilities.file_system.ICustomVFS;

import java.util.Base64;

public class debug extends Application {
    private final String basePath = "workspace";

    public WebSocket<String> ws(String path) {
        String entry = request().getQueryString("entry");
        String entryPoint = StringUtils.newStringUtf8(Base64.getDecoder().decode(entry));

        int port = 9223;
        String rel_path = basePath + "/" + path;

        ICustomVFS vfs = new CommonsVFS();
        if (!vfs.exists(rel_path))
            return new WebSocket<String>() {
                @Override
                public void onReady(In<String> in, Out<String> out) {
                    out.write("file not found");
                    out.close();
                }
            };

        String extension = "-" + vfs.getExtension(rel_path);

        DBGPReaderConnector connector = new DBGPReaderConnector(port, extension, entryPoint, rel_path);
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
