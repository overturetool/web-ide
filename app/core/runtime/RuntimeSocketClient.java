package core.runtime;

import org.overture.webide.processor.ProcessingResult;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class RuntimeSocketClient {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public RuntimeSocketClient(int port) throws IOException {
        this.socket = new Socket("localhost", port);
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
    }

    public ProcessingResult send(List<File> files) {
        ProcessingResult result = null;

        try {
            this.out.writeObject(files);
            this.out.flush();

            Object readObject = null;

            try {
                readObject = this.in.readObject();
            } catch (EOFException e) {
                // done
            }

            result = (ProcessingResult) readObject;

            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }
}
