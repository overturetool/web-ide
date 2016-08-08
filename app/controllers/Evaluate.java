package controllers;

import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import core.processing.clients.EvaluationClient;
import core.processing.processes.EvaluationProcess;
import core.utilities.SocketUtils;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import play.mvc.WebSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Base64;

public class Evaluate extends Application {
    public WebSocket project(String account, String path) {
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

            if (!client.awaitConnection())
                return errorResponse("Could not connect to process");

        } catch (IOException e) {
            return errorResponse("Exception occurred");
        }

        return WebSocket.Text.accept(request -> Flow.<String>create()
                .map(msg -> new String(Base64.getDecoder().decode(msg)))
                .map(client::evaluate)
                .takeWhile(response -> !response.equals("bye.."))
                .map(response -> response != null ? response : "?"));
    }

    private WebSocket errorResponse(String message) {
        return WebSocket.Text.accept(request -> Flow.fromSinkAndSource(Sink.last(), Source.single(message)));
    }
}
