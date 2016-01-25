package core.debug;

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

    public ProxyClient(final ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            System.out.println("Ready to connect on port " + server.getLocalPort());
            client = server.accept();
            System.out.println("Connection accepted on port " + client.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Exception thrown while initiating input and output streams");
            e.printStackTrace();
        }

        synchronized (lock) {
            lock.notify();
        }
    }

    public String read() {
        try {
            awaitInitialization();

            if (in != null) {
                return in.readLine();
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "An error occurred while reading from DBGPReader";
    }

    public String sendAndRead(String event) {
        try {
            awaitInitialization();

            out.println(event);
            out.flush();

            if (in != null) {
                return in.readLine();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception thrown while sending");
            e.printStackTrace();
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
            System.out.println("Exception thrown while disconnecting");
            e.printStackTrace();
        }
    }

    private void awaitInitialization() throws InterruptedException {
        if (in == null || out == null) {
            // Wait for input- and/or output-stream to be initialized
            synchronized (lock) {
                lock.wait(5000);
            }
        }
    }
}
