package core.debug;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyClient extends Thread {
    private final ServerSocket server;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(ProxyClient.class);

    public ProxyClient(final ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            logger.debug("Ready to connect on port " + server.getLocalPort());
            client = server.accept();
            logger.debug("Connection accepted on port " + client.getLocalPort());
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }

        synchronized (lock) {
            lock.notify();
        }
    }

    public String read() {
        try {
            awaitInitialization(5000);

            if (in != null) {
                return in.readLine();
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.info(e.getMessage(), e);
        }

        return "An error occurred while reading from DBGPReader";
    }

    public String sendAndRead(String event) {
        try {
            awaitInitialization(5000);

            out.println(event);
            out.flush();

            if (in != null) {
                return in.readLine();
            }
        } catch (IOException | InterruptedException e) {
            logger.info(e.getMessage(), e);
        }

        return "An error occurred while communicating with DBGPReader";
    }

    public void disconnect() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (client != null)
                client.close();
            if (server != null)
                server.close();
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
    }

    private void awaitInitialization(int timeout) throws InterruptedException {
        if (in == null || out == null) {
            // Wait for input- or output-stream to be initialized
            synchronized (lock) {
                lock.wait(timeout);
            }
        }
    }
}
