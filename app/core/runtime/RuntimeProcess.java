package core.runtime;

import core.ServerConfigurations;
import org.overture.webide.processor.ProcessArguments;
import org.overture.webide.processor.RuntimeSocketServer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RuntimeProcess {
    public Process init(int port) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String classPath = Paths.get("lib", "OvertureProcessor-1.0-SNAPSHOT-jar-with-dependencies.jar").toString();
        String className = RuntimeSocketServer.class.getCanonicalName();

        List<String> args = new ArrayList<>();
        args.add(javaBin);
        args.add("-Xms4M");     // initial heap size
        args.add("-Xmx32M");    // maximum heap size
        args.add("-Xss1M");     // thread stack size
        args.add("-cp");
        args.add(classPath);
        args.add(className);

        // Program arguments
        args.add(ProcessArguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(ProcessArguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(ProcessArguments.Identifiers.Timeout);
        args.add(Integer.toString(30));
        args.add(ProcessArguments.Identifiers.PrintInfo);

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
