package utilities.debug;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DBGPReaderConnector {
    private int port;

    private ServerSocket server;
    private Socket client;

    private BufferedReader in;
    private PrintWriter out;

    private String extension;
    private String entryPoint;
    private String rel_path;

    public DBGPReaderConnector(int port, String extension, String entryPoint, String rel_path) {
        this.port = port;
        this.extension = extension;
        this.entryPoint = entryPoint;
        this.rel_path = rel_path;
    }

    public DBGPReaderConnector(int port) {
        this.port = port;
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

            DBGPReaderServer overture = new DBGPReaderServer(extension, "localhost", port, "webIDE", entryPoint, "file://" + new File(rel_path).getAbsolutePath());
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
            System.out.println("Read failed");
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
