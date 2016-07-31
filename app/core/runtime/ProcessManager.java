package core.runtime;

import core.utilities.SocketUtils;

import java.net.ServerSocket;

public class ProcessManager {
    private static final int capacity = 10;
    private static RuntimeProcessQueue processQueue = new RuntimeProcessQueue(capacity, true, 1000);

    public RuntimeSocketClient acquireProcess() {
        RuntimeSocketClient runtimeSocketClient = processQueue.acquire();

        if (runtimeSocketClient == null && processQueue.size() < capacity) {
            runtimeSocketClient = startNewProcess();
            processQueue.addBusyProcess(runtimeSocketClient);
        }

        return runtimeSocketClient;
    }

    public void releaseProcess(RuntimeSocketClient runtimeClient) {
        processQueue.release(runtimeClient);
    }

    private RuntimeSocketClient startNewProcess() {
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            RuntimeSocketClient runtimeClient = new RuntimeSocketClient(serverSocket, 5000);
            runtimeClient.start();

            RuntimeProcess runtimeProcess = new RuntimeProcess();
            Process process = runtimeProcess.init(port);

            runtimeClient.awaitConnection();
            runtimeClient.setProcess(process);

            return runtimeClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
