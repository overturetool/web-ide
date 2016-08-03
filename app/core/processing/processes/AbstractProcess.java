package core.processing.processes;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProcess {
    protected List<String> args = new ArrayList<>();

    protected AbstractProcess(String classPath, String className) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();

        args.add(javaBin);
        args.add("-cp");
//        args.add("-Xms4M");     // initial heap size
//        args.add("-Xmx32M");    // maximum heap size
//        args.add("-Xss1M");     // thread stack size
        args.add(classPath);
        args.add(className);

    }

    public Process start() {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectErrorStream(true);
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }
}
