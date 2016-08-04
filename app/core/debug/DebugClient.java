package core.debug;

import core.processing.clients.AbstractClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

public class DebugClient extends AbstractClient<BufferedReader, PrintWriter> {
    public DebugClient(final ServerSocket serverSocket, long timeout) {
        super(serverSocket, timeout);
    }

    @Override
    protected void init() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String read() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String writeRead(String event) {
        try {
            out.println(event);
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
