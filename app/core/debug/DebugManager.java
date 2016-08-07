package core.debug;

import core.utilities.SocketUtils;
import core.vfs.IVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class DebugManager {
    private final Logger logger = LoggerFactory.getLogger(DebugManager.class);
    private String entry;
    private String type;
    private String defaultName;
    private IVFS file;
    private boolean coverage;

    public DebugManager(String entry, String defaultName, IVFS file, boolean coverage) {
        this(entry, file.getExtension(), defaultName, file, coverage);
    }

    public DebugManager(String entry, String type, String defaultName, IVFS file, boolean coverage) {
        this.entry = entry;
        this.type = type;
        this.defaultName = defaultName;
        this.file = file;
        this.coverage = coverage;
    }

    public synchronized DebugClient connect() {
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();
            //serverSocket.setSoTimeout(10000);
            //server.setReuseAddress(true);

            DebugClient client = new DebugClient(serverSocket, 50000);
            client.start();

            DebugProcess process = new DebugProcess(port, type, entry, defaultName, file, coverage);
            process.start();

            client.awaitConnection();
            return client;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
