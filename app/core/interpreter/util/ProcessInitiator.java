package core.interpreter.util;

import core.ServerConfigurations;
import org.overture.webide.interpreter_util.InterpreterArguments;
import org.overture.webide.interpreter_util.InterpreterUtilMain;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProcessInitiator {
    public Process init(int port) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String classPath = Paths.get("lib", "interpreter-util-1.0-SNAPSHOT-jar-with-dependencies.jar").toString();
        String className = InterpreterUtilMain.class.getCanonicalName();

        List<String> args = new ArrayList<>();
        args.add(javaBin);
//        args.add("-Xms4M");     // initial heap size
//        args.add("-Xmx32M");    // maximum heap size
//        args.add("-Xss1M");     // thread stack size
        args.add("-cp");
        args.add(classPath);
        args.add(className);

        // Program arguments
        args.add(InterpreterArguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(InterpreterArguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(InterpreterArguments.Identifiers.PrintInfo);

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
