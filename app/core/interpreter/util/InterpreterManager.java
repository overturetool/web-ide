package core.interpreter.util;

import core.interpreter.util.classloaders.ClassLoaderManager;
import org.overture.webide.interpreter_util.*;

public class InterpreterManager {
    public Result getResultAsync(Task task) {
        ProcessManager processManager = new ProcessManager();

        ProcessClient processClient = processManager.acquireProcess();

        if (processClient == null)
            return null;

        Result result = processClient.process(task);
        processManager.releaseProcess(processClient);

        return result;
    }

    public Result getResultSync(Task task) {
        synchronized (InterpreterManager.class) {
            return new InterpreterUtil().getResult(task.getFileList(), task.getDialect(), task.getRelease());
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
