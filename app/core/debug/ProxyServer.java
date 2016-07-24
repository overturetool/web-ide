package core.debug;

import core.utilities.SocketUtils;
import core.vfs.IVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String defaultName;
    private String absolutePath;

    private final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    public ProxyServer(int port, String entry, String defaultName, IVFS file) {
        this(port, entry, file.getExtension(), defaultName, file.getAbsoluteUrl());
    }

    public ProxyServer(int port, String entry, String type, String defaultName, IVFS dir) {
        this(port, entry, type, defaultName, dir.getAbsoluteUrl());
    }

    public ProxyServer(int port, String entry, String type, String defaultName, String absolutePath) {
        this.port = port;
        this.entry = entry;
        this.type = type;
        this.defaultName = defaultName;
        this.absolutePath = absolutePath;
    }

    public synchronized ProxyClient connect() {
        try {
            if (port == -1)
                server = SocketUtils.findAvailablePort(49152, 65535);
            else
                server = new ServerSocket(port);

            port = server.getLocalPort();
            server.setSoTimeout(timeout);
            //server.setReuseAddress(true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        ProxyClient client = new ProxyClient(server);
        client.start();

        new DebugProcess(type, host, port, key, entry, defaultName, absolutePath).start();

        return client;
    }
}
