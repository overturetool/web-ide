package core.runtime;

import core.runtime.ClassLoaders.ClassLoaderManager;
import org.overture.webide.processor.ProcessingResult;
import org.overture.webide.processor.ProcessingTask;
import org.overture.webide.processor.RuntimeSocketServer;

public class RuntimeManager {
    public ProcessingResult processAsync(ProcessingTask task) {
        ProcessManager processManager = new ProcessManager();

        RuntimeSocketClient runtimeClient = processManager.acquireProcess();

        if (runtimeClient == null)
            return null;

        ProcessingResult processingResult = runtimeClient.process(task);
        processManager.releaseProcess(runtimeClient);

        return processingResult;
    }

    public ProcessingResult processSync(ProcessingTask task) {
        synchronized (RuntimeManager.class) {
            return RuntimeSocketServer.getProcessingResult(task.getFileList(), task.getDialect(), task.getRelease());
        }
    }

    public ProcessingResult processClassLoader0(ProcessingTask task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.processClassLoader0(task);
    }

    public ProcessingResult processClassLoader1(ProcessingTask task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.processClassLoader1(task);
    }

    public ProcessingResult processClassLoader2(ProcessingTask task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.processClassLoader2(task);
    }
}
