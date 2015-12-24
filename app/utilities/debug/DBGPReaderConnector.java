package utilities.debug;

import utilities.file_system.IVF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DBGPReaderConnector {
    private final int timeout = 10000;
    private final String host = "localhost";
    private final String key = "webIDE";

    private ServerSocket server;
    private Socket client;

    private BufferedReader in;
    private PrintWriter out;

    private int port;
    private String entry;
    private String type;
    private String absolutePath;

    public DBGPReaderConnector(int port, String entry, IVF file) {
        this(port, entry, file.getExtension(), file.getAbsolutePath());
    }

    public DBGPReaderConnector(int port, String entry, String type, IVF dir) {
        this(port, entry, type, dir.getAbsolutePath());
    }

    public DBGPReaderConnector(int port, String entry, String type, String absolutePath) {
        this.port = port;
        this.entry = entry;
        this.type = type;
        this.absolutePath = absolutePath;
    }

    public void connect() {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(timeout);
            server.setReuseAddress(true);
        }
        catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            e.printStackTrace();
            return;
        }

        try {
            System.out.println("Ready to connect");

            DBGPReaderServer overture = new DBGPReaderServer(type, host, port, key, entry, absolutePath);
            overture.start();

            client = server.accept();
            System.out.println("Connection accepted!");
        }
        catch (IOException e) {
            System.out.println("Accept failed on port " + port);
            e.printStackTrace();
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        }
        catch (IOException e) {
            System.out.println("Exception thrown while initiating input and output streams");
            e.printStackTrace();
        }
    }

    public String read() {
        try {
            if (in != null) {
                return in.readLine();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "An error occurred while reading from DBGPReader";
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null) client.close();
            if (server != null) server.close();
        } catch (IOException e) {
            System.out.println("Exception thrown while disconnecting");
            e.printStackTrace();
        }
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
}
