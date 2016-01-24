package core.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyClient extends Thread {
    private ServerSocket server;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private final Object lock;

    public ProxyClient(ServerSocket server) {
        this.lock = new Object();
        this.server = server;
    }

    @Override
    public void run() {
        try {
            System.out.println("Ready to connect");
            client = server.accept();
            System.out.println("Connection accepted!");
        } catch (IOException e) {
            e.printStackTrace();
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
            if (in == null) {
                // Wait for input stream to be initialized
                synchronized (lock) {
                    lock.wait(5000);
                }
            }

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
        out.println(event);
        out.flush();

        try {
            if (in != null) {
                return in.readLine();
            }
        } catch (IOException e) {
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
}
