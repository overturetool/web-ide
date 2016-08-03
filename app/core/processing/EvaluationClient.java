package core.processing;

import java.io.*;
import java.net.ServerSocket;

public class EvaluationClient extends AbstractClient<BufferedReader, PrintWriter> {
    public EvaluationClient(ServerSocket serverSocket, long timeout) {
        super(serverSocket, timeout, new Object());
    }

    @Override
    protected void init() throws IOException {
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
    }

    public String evaluate(String s) {
        try {
            this.out.println(s);
            return this.in.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
