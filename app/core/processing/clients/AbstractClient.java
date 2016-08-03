package core.processing.clients;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractClient<T0 extends Closeable, T1 extends Closeable> extends Thread {
    protected T0 in;
    protected T1 out;
    protected ServerSocket serverSocket;
    protected Socket socket;
    protected Process process;
    protected long timeout;
    private final Object lock = new Object();

    public AbstractClient(ServerSocket serverSocket, long timeout) {
        this.serverSocket = serverSocket;
        this.timeout = timeout;
    }

    @Override
    public synchronized void run() {
        try {
            this.socket = this.serverSocket.accept();
            init();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    protected abstract void init() throws IOException;

    public synchronized void close() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
            this.serverSocket.close();
            if (this.process != null) this.process.destroy();
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
