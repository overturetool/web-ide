package core.runtime;

import core.utilities.SocketUtils;
import org.overture.webide.processor.ProcessingResult;
import org.overture.webide.processor.ProcessingTask;

import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeManager {
    private static final int processLimit = 2;
    private static AtomicInteger processCount = new AtomicInteger(0);
    private static ArrayBlockingQueue<RuntimeSocketClient> processQueue = new ArrayBlockingQueue<>(processLimit, true);

    public ProcessingResult process(ProcessingTask task) {
        RuntimeSocketClient runtimeClient = acquireProcess();

        if (runtimeClient == null)
            return null;

        ProcessingResult processingResult = runtimeClient.process(task);
        releaseProcess(runtimeClient);

        return processingResult;
    }

    private RuntimeSocketClient startNewProcess() {
        if (processCount.incrementAndGet() > processLimit) {
            processCount.decrementAndGet();
            return null;
        }

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
            processCount.decrementAndGet();
            e.printStackTrace();
        }
        return runtimeClient;
    }

    private RuntimeSocketClient acquireProcess() {
        RuntimeSocketClient runtimeSocketClient = null;

        try {
            runtimeSocketClient = processQueue.poll(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) { /* ignored */ }

        if (runtimeSocketClient == null) {
            runtimeSocketClient = startNewProcess();
        } else if (!runtimeSocketClient.isProcessAlive()) {
            runtimeSocketClient.close();
            processCount.decrementAndGet();
            runtimeSocketClient = startNewProcess();
        }

        return runtimeSocketClient;
    }

    private void releaseProcess(RuntimeSocketClient runtimeClient) {
        try {
            processQueue.add(runtimeClient);
        } catch (IllegalStateException e) {
            runtimeClient.close();
        } catch (NullPointerException e) { /* ignored */ }
    }
}
