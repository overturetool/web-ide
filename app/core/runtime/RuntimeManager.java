package core.runtime;

import core.utilities.SocketUtils;
import org.overture.webide.processor.ProcessingJob;
import org.overture.webide.processor.ProcessingResult;

import java.net.ServerSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RuntimeManager {
    private static Queue<RuntimeSocketClient> processQueue = new ConcurrentLinkedQueue<>();

    public ProcessingResult process(ProcessingJob job) {
        RuntimeSocketClient runtimeClient = getProcess();
        ProcessingResult processingResult = null;

        if (runtimeClient != null) {
            processingResult = runtimeClient.process(job);
            releaseProcess(runtimeClient);
        }

        return processingResult;
    }

    private RuntimeSocketClient startProcess() {
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

    private RuntimeSocketClient getProcess() {
        RuntimeSocketClient runtimeSocketClient = processQueue.poll();

        if (runtimeSocketClient != null) {
            if (!runtimeSocketClient.isProcessAlive()) {
                runtimeSocketClient.close();
                runtimeSocketClient = startProcess();
            }
        } else {
            runtimeSocketClient = startProcess();
        }

        return runtimeSocketClient;
    }

    private void releaseProcess(RuntimeSocketClient runtimeClient) {
        processQueue.add(runtimeClient);
    }
}
