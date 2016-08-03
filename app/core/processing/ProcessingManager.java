package core.processing;

import core.processing.classloaders.ClassLoaderManager;
import org.overture.webide.processing.Processing;
import org.overture.webide.processing.Result;
import org.overture.webide.processing.Task;

public class ProcessingManager {
    public Result getResultAsync(Task task) {
        ProcessManager processManager = new ProcessManager();

        TypeCheckClient processClient = processManager.acquireProcess();

        if (processClient == null)
            return null;

        Result result = processClient.process(task);
        processManager.releaseProcess(processClient);

        return result;
    }

    public Result getResultSync(Task task) {
        synchronized (ProcessingManager.class) {
            return new Processing().getResult(task.getFileList(), task.getDialect(), task.getRelease());
        }
    }

    public Result getResultClassLoader0(Task task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.getResultClassLoader0(task);
    }

    public Result getResultClassLoader1(Task task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.getResultClassLoader1(task);
    }

    public Result getResultClassLoader2(Task task) {
        ClassLoaderManager classLoaderManager = new ClassLoaderManager();
        return classLoaderManager.getResultClassLoader2(task);
    }
}
