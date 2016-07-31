package core.runtime.ClassLoaders;

import org.overture.webide.processor.IRuntimeTest;
import org.overture.webide.processor.ProcessingResult;
import org.overture.webide.processor.ProcessingTask;
import org.overture.webide.processor.RuntimeSocketServer;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ClassLoaderManager {
    public ProcessingResult processClassLoader0(ProcessingTask task) {
        Set<String> changedClasses = new HashSet<>();
        changedClasses.add("org.overture.webide.processor.IRuntimeTest");
        changedClasses.add("org.overture.config.Settings");
        changedClasses.add("org.overture.interpreter.VDMJ");
        changedClasses.add("org.overture.typechecker.util.TypeCheckerUtil");
        changedClasses.add("org.overture.typechecker.util.TypeCheckerUtil$TypeCheckResult");
        changedClasses.add("org.overture.webide.processor.ProcessingResult");
        changedClasses.add("org.overture.parser.util.ParserUtil$ParserResult");

        try {
            DynamicClassLoader classLoader = new DynamicClassLoader(changedClasses);
            Class cls = classLoader.dynamicallyLoadClass("org.overture.webide.processor.RuntimeSocketServer");
            Object newInstance = cls.newInstance();
            IRuntimeTest runtime = (IRuntimeTest) newInstance;
            return runtime.getProcessingResultNonStatic(task.getFileList(), task.getDialect(), task.getRelease());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ProcessingResult processClassLoader1(ProcessingTask task) {
        try {
            ClassLoader parentClassLoader = CustomClassLoader.class.getClassLoader();
            CustomClassLoader classLoader = new CustomClassLoader(parentClassLoader);
            Class cls = classLoader.loadClass("org.overture.webide.processor.RuntimeSocketServer");

            Object newInstance = cls.newInstance();
            IRuntimeTest runtime = (IRuntimeTest) newInstance;

//            RuntimeSocketServer.test = 10;
//            System.out.println(runtime.getTest());
//            System.out.println(RuntimeSocketServer.test);
//            System.out.println();

            return runtime.getProcessingResultNonStatic(task.getFileList(), task.getDialect(), task.getRelease());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ProcessingResult processClassLoader2(ProcessingTask task) {
        String classPath = Paths.get("lib", "OvertureProcessor-1.0-SNAPSHOT-jar-with-dependencies.jar").toAbsolutePath().toString();
        String className = RuntimeSocketServer.class.getCanonicalName();

        try {
            URL[] url = new URL[] { new URL("file:" + classPath) };
            URLClassLoader clsLoader = new URLClassLoader(url);
            //URLClassLoader clsLoader = URLClassLoader.newInstance(url);

            Class<?> cls = clsLoader.loadClass(className);
            Object newInstance = cls.newInstance();
//            IRuntimeTest runtime = (IRuntimeTest) newInstance;
//            Gson gson = new Gson();
//            IRuntimeTest runtime = gson.fromJson(gson.toJson(newInstance), IRuntimeTest.class);
            RuntimeSocketServer runtime = (RuntimeSocketServer) toObject(toByteArray(newInstance));

//            RuntimeSocketServer.test = 10;
//            System.out.println(runtime.getTest());
//            System.out.println(RuntimeSocketServer.test);
//            System.out.println();
//
            ProcessingResult result = runtime.getProcessingResultNonStatic(task.getFileList(), task.getDialect(), task.getRelease());
            clsLoader.close();

            return result;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] toByteArray(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        byte[] bytes;
        try {
            out.writeObject(obj);
            bytes = bos.toByteArray();
        } finally {
            out.close();
            bos.close();
        }
        return bytes;
    }

    private Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);
        Object object;
        try {
            object = in.readObject();
        } finally {
            in.close();
            bis.close();
        }
        return object;
    }
}
