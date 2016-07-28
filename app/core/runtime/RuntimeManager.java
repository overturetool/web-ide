package core.runtime;

import core.utilities.SocketUtils;
import org.overture.webide.processor.ProcessingResult;
import org.overture.webide.processor.ProcessingTask;

import java.net.ServerSocket;

public class RuntimeManager {
    private static final int capacity = 2;
    private static RuntimeProcessQueue processQueue = new RuntimeProcessQueue(capacity, true, 5000);

    public ProcessingResult process(ProcessingTask task) {
        RuntimeSocketClient runtimeClient = acquireProcess();

        if (runtimeClient == null)
            return null;

        ProcessingResult processingResult = runtimeClient.process(task);
        releaseProcess(runtimeClient);

        return processingResult;
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

    private RuntimeSocketClient acquireProcess() {
        RuntimeSocketClient runtimeSocketClient = processQueue.acquire();

        if (runtimeSocketClient == null && processQueue.size() < capacity) {
            runtimeSocketClient = startNewProcess();
            processQueue.addBusyProcess(runtimeSocketClient);
        }

        return runtimeSocketClient;
    }

    private void releaseProcess(RuntimeSocketClient runtimeClient) {
        processQueue.release(runtimeClient);
    }
}
