package core.runtime;

import core.utilities.SocketUtils;
import org.overture.webide.processor.ProcessingResult;
import org.overture.webide.processor.ProcessingTask;

import java.net.ServerSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RuntimeManager {
    private static Queue<RuntimeSocketClient> processQueue = new ConcurrentLinkedQueue<>();

    public ProcessingResult process(ProcessingTask task) {
        RuntimeSocketClient runtimeClient = acquireProcess();
        ProcessingResult processingResult = null;

        if (runtimeClient != null) {
            processingResult = runtimeClient.process(task);
            releaseProcess(runtimeClient);
        }

        return processingResult;
    }

    private RuntimeSocketClient startNewProcess() {
        RuntimeSocketClient runtimeClient = null;
        try {
            ServerSocket serverSocket = SocketUtils.findAvailablePort(49152, 65535);
            int port = serverSocket.getLocalPort();

            runtimeClient = new RuntimeSocketClient(serverSocket);
            runtimeClient.start();

            RuntimeProcess runtimeProcess = new RuntimeProcess();
            Process process = runtimeProcess.init(port);
            runtimeClient.setProcess(process);

            // TODO : await process ready?
        } catch (Exception e) {
            e.printStackTrace();
        }
        return runtimeClient;
    }

    private RuntimeSocketClient acquireProcess() {
        RuntimeSocketClient runtimeSocketClient = processQueue.poll();

        if (runtimeSocketClient != null) {
            if (!runtimeSocketClient.isProcessAlive()) {
                runtimeSocketClient.close();
                runtimeSocketClient = startNewProcess();
            }
        } else {
            runtimeSocketClient = startNewProcess();
        }

        return runtimeSocketClient;
    }

    private void releaseProcess(RuntimeSocketClient runtimeClient) {
        processQueue.add(runtimeClient);
    }
}
