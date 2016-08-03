package controllers;

import core.processing.clients.EvaluationClient;
import core.processing.processes.EvaluationProcess;
import core.utilities.SocketUtils;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import play.mvc.LegacyWebSocket;
import play.mvc.WebSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Base64;

public class Evaluate extends Application {
    public LegacyWebSocket<String> project(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return errorResponse("File not found");

        EvaluationClient client;

        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            client = new EvaluationClient(serverSocket, 5000);
            client.start();

            EvaluationProcess process = new EvaluationProcess(port, file.getAbsolutePath());
            process.start();

            client.awaitConnection();
        } catch (IOException e) {
            return errorResponse("Exception occurred");
        }

        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                in.onMessage(event -> {
                    String inputDecoded = new String(Base64.getDecoder().decode(event));
                    String result = client.evaluate(inputDecoded);
                    out.write(result != null ? result : "?");
                });

                // When the socket is closed
                in.onClose(client::close);
            }
        };
    }

    private LegacyWebSocket<String> errorResponse(String message) {
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                out.write(message);
                out.close();
            }
        };
    }
}
