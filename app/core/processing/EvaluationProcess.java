package core.processing;

import core.ServerConfigurations;
import org.overture.webide.processing.Arguments;
import org.overture.webide.processing.ProcessingMain;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EvaluationProcess {
    private List<String> args = new ArrayList<>();

    public EvaluationProcess(int port) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String classPath = Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString();
        String className = ProcessingMain.class.getCanonicalName();

        args.add(javaBin);
        args.add("-cp");
        args.add(classPath);
        args.add(className);

        args.add(Arguments.Actions.Evaluate);
        args.add(Arguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(Arguments.Identifiers.Port);
        args.add(Integer.toString(port));
    }

    public Process start(String absolutePath) {
        args.add(Arguments.Dialects.VDM_SL);
        args.add(Arguments.Release.VDM_10);
        args.add(Arguments.Identifiers.PrintInfo);
        args.add(Arguments.Identifiers.Dir);
        args.add(absolutePath);

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
