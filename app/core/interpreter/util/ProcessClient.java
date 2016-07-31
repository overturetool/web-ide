package core.interpreter.util;

import org.overture.webide.interpreter_util.Result;
import org.overture.webide.interpreter_util.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessClient extends Thread {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerSocket serverSocket;
    private Socket socket;
    private Process process;
    private long timeout;

    private static final Object lock = new Object();

    public ProcessClient(ServerSocket serverSocket, long timeout) {
        this.serverSocket = serverSocket;
        this.timeout = timeout;
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
        } finally {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public synchronized Result process(Task task) {
        Result result = null;
        try {
            this.out.writeObject(task);
            this.out.flush();
            result = (Result) this.in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            //e.printStackTrace();
        }
        return result;
    }

    public synchronized void close() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
            this.serverSocket.close();
            if (this.process != null)
                this.process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isProcessAlive() {
        return this.process != null && this.process.isAlive();
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public void awaitConnection() {
        synchronized (lock) {
            try {
                lock.wait(this.timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
