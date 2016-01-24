package core.debug;

import core.vfs.IVFS;

import java.io.IOException;
import java.net.ServerSocket;

public class ProxyServer {
    private final int timeout = 10000;
    private final String host = "localhost";
    private final String key = "webIDE";

    private ServerSocket server;

    private int port;
    private String entry;
    private String type;
    private String absolutePath;

    public ProxyServer(int port, String entry, IVFS file) {
        this(port, entry, file.getExtension(), file.getAbsolutePath());
    }

    public ProxyServer(int port, String entry, String type, IVFS dir) {
        this(port, entry, type, dir.getAbsolutePath());
    }

    public ProxyServer(int port, String entry, String type, String absolutePath) {
        this.port = port;
        this.entry = entry;
        this.type = type;
        this.absolutePath = absolutePath;
    }

    public synchronized ProxyClient connect() {
        try {
            if (server == null) {
                if (port == -1)
                    server = findAvailablePort(49152, 65535);
                else
                    server = new ServerSocket(port);

                port = server.getLocalPort();
                server.setSoTimeout(timeout);
                server.setReuseAddress(true);
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            e.printStackTrace();
            return null;
        }

        ProxyClient client = new ProxyClient(server);
        client.start();

        new DBGPReaderServer(type, host, port, key, entry, absolutePath).start();

        return client;
    }

    private ServerSocket findAvailablePort(int minPort, int maxPort) throws IOException {
        for (int i = minPort; i < maxPort; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException ex) {
                // try next port
            }
        }

        // if the program gets here, no port in the range was found
        throw new IOException("no availble port found");
    }
}
