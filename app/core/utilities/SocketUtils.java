package core.utilities;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtils {
    private static final Object lock = new Object();

    public static ServerSocket findAvailablePort(int minPort, int maxPort) throws IOException {
        synchronized (lock) {
            for (int i = minPort; i < maxPort; i++) {
                try {
                    return new ServerSocket(i);
                } catch (IOException ex) {
                    // try next port
                }
            }
        }

        // if the program gets here, no port in the range was found
        throw new IOException("no available port found");
    }
}
