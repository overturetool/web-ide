package core.runtime;

import org.overture.webide.processor.ProcessingResult;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class RuntimeSocketClient extends Thread {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerSocket serverSocket;
    private Socket socket;

    public RuntimeSocketClient(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public synchronized void run() {
        init();
    }

    private void init() {
        try {
            this.socket = this.serverSocket.accept();
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(Object object) throws IOException {
        this.out.writeObject(object);
        this.out.flush();
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        Object readObject = null;
        try {
            readObject = this.in.readObject();
        } catch (EOFException e) {
            // done
        }
        return readObject;
    }

    public synchronized ProcessingResult process(List<File> files) {
        ProcessingResult result = null;

        try {
            writeObject(files);
            result = (ProcessingResult) readObject();

            /*this.in.close();
            this.out.close();
            this.socket.close();
            this.serverSocket.close();*/
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public synchronized boolean isProcessAlive() {
        try {
            String test = "isProcessAlive?";
            writeObject(test);
            return readObject().equals(test);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
