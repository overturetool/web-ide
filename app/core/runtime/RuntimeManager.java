package core.runtime;

import core.utilities.SocketUtils;

import java.net.ServerSocket;

public class RuntimeManager {
    public static RuntimeManager instance;

    public static RuntimeManager getInstance() {
        if (instance == null) {
            synchronized (RuntimeManager.class) {
                instance = new RuntimeManager();
            }
        }
        return instance;
    }

    private RuntimeManager() {}

    public void getProcess() {
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            RuntimeSocketClient runtimeClient = new RuntimeSocketClient(serverSocket);
            runtimeClient.start();

            RuntimeProcess runtimeProcess = new RuntimeProcess();
            runtimeProcess.init(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
