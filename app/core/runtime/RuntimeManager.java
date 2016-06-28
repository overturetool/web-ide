package core.runtime;

import core.utilities.SocketUtils;
import org.overture.webide.processor.ProcessingResult;

import java.io.File;
import java.net.ServerSocket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RuntimeManager {
    public static RuntimeManager instance;
    private final int processLimit = 3;
    private int processCount = 0;

    private Queue<RuntimeSocketClient> processQueue = new ConcurrentLinkedQueue<>();

    public static RuntimeManager getInstance() {
        if (instance == null) {
            synchronized (RuntimeManager.class) {
                instance = new RuntimeManager();
            }
        }
        return instance;
    }

    private RuntimeManager() {}

    public ProcessingResult process(List<File> list) {
        RuntimeSocketClient runtimeClient = getProcess();

        if (runtimeClient == null)
            return null;

        if (runtimeClient.isProcessAlive()) {
            ProcessingResult processingResult = runtimeClient.process(list);
            releaseProcess(runtimeClient);
            return processingResult;
        }

        processCount--;
        return null;
    }

    private RuntimeSocketClient startProcess() {
        RuntimeSocketClient runtimeClient = null;
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            runtimeClient = new RuntimeSocketClient(serverSocket);
            runtimeClient.start();

            RuntimeProcess runtimeProcess = new RuntimeProcess();
            runtimeProcess.init(port);

            // await process ready
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runtimeClient;
    }

    private RuntimeSocketClient getProcess() {
        RuntimeSocketClient runtimeSocketClient = processQueue.poll();

        if (runtimeSocketClient == null && processCount < processLimit) {
            runtimeSocketClient = startProcess();
            if (runtimeSocketClient != null) {
                processCount++;
                return runtimeSocketClient;
            }
        }

        return runtimeSocketClient;
    }

    private void releaseProcess(RuntimeSocketClient runtimeClient) {
        processQueue.add(runtimeClient);
    }
}
