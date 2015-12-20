package controllers;

import org.apache.commons.codec.binary.StringUtils;
import play.libs.F;
import play.mvc.WebSocket;
import utilities.DBGPReaderConnector;
import utilities.DBGPReaderServer;
import utilities.file_system.CommonsVFS;
import utilities.file_system.ICustomVFS;

import java.io.File;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class debug extends Application {
    private final String basePath = "workspace";

    public WebSocket<String> ws(String path) {
        String entry = request().getQueryString("entry");
        String entryPoint = StringUtils.newStringUtf8(Base64.getDecoder().decode(entry));

        int port = 37123;
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

        DBGPReaderConnector connector = new DBGPReaderConnector(port);
        connector.start();

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DBGPReaderServer overture = new DBGPReaderServer(extension, "localhost", port, "webIDE", entryPoint, "file://" + new File(rel_path).getAbsolutePath());
        overture.start();

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
