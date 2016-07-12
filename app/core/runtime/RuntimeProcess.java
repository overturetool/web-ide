package core.runtime;

import core.ServerConfigurations;
import org.overture.webide.processor.ProcessArguments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuntimeProcess {
    public Process init(int port) {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.add("-cp");
        args.add("lib/OvertureProcessor-1.0-SNAPSHOT-jar-with-dependencies.jar");
        args.add("org.overture.webide.processor.RuntimeSocketServer");

        // Program arguments
        args.add(ProcessArguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(ProcessArguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(ProcessArguments.Identifiers.Timeout);
        args.add(Integer.toString(30));

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
