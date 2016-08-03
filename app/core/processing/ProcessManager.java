package core.processing;

import core.utilities.SocketUtils;

import java.net.ServerSocket;

public class ProcessManager {
    private static final int capacity = 10;
    private static ProcessQueue processQueue = new ProcessQueue(capacity, true, 1000);

    public TypeCheckClient acquireProcess() {
        TypeCheckClient processClient = processQueue.acquire();

        if (processClient == null && processQueue.size() < capacity) {
            processClient = startNewProcess();
            processQueue.addBusyProcess(processClient);
        }

        return processClient;
    }

    public void releaseProcess(TypeCheckClient processClient) {
        processQueue.release(processClient);
    }

    private TypeCheckClient startNewProcess() {
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            TypeCheckClient processClient = new TypeCheckClient(serverSocket, 5000);
            processClient.start();

            TypeCheckProcess processInitiator = new TypeCheckProcess();
            Process process = processInitiator.init(port);

            processClient.awaitConnection();
            processClient.setProcess(process);

            return processClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
