package utilities.debug;

import utilities.file_system.ICustomVF;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DBGPReaderConnector {
    private int port;

    private ServerSocket server;
    private Socket client;

    private BufferedReader in;
    private PrintWriter out;

    private String entry;
    private String type;
    private String absolutePath;

    public DBGPReaderConnector(int port, String entry, ICustomVF file) {
        this(port, entry, file.getExtension(), file.getAbsolutePath());
    }

    public DBGPReaderConnector(int port, String entry, String type, ICustomVF dir) {
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
            server.setSoTimeout(10000);
            server.setReuseAddress(true);
        }
        catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            e.printStackTrace();
            return;
        }

        try {
            System.out.println("Ready to connect");

            DBGPReaderServer overture = new DBGPReaderServer(type, "localhost", port, "webIDE", entry, absolutePath);
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

    public String initialRead() {
        String line = null;
        while (line == null) {
            try {
                if (in != null)
                    line = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return line;
    }

    public void disconnect() {
        try {
            server.close();
        } catch (IOException e) {
            System.out.println("Exception thrown while disconnecting");
            e.printStackTrace();
        }
    }

    public String send(String event) {
        String line = "";

        out.println(event);
        out.flush();

        try {
            line = in.readLine();
        } catch (IOException e) {
            System.out.println("Exception thrown while sending");
            e.printStackTrace();
        }

        return line;
    }
}
