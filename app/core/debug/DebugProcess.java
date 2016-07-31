package core.debug;

import org.overture.interpreter.debug.DBGPReaderV2;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DebugProcess {
    private List<String> args = new ArrayList<>();

    public DebugProcess(String type, String host, int port, String key, String entry, String defaultName, String file) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String classPath = Paths.get("lib", "Overture-2.3.6.jar").toString();
        String className = DBGPReaderV2.class.getCanonicalName();

        args.add(javaBin);
        args.add("-cp");
        args.add(classPath);
        args.add(className);

        if (type != null) {
            args.add("-" + type);
        }

        if (host != null) {
            args.add("-h");
            args.add(host);
        }

        if (port != -1) {
            args.add("-p");
            args.add(Integer.toString(port));
        }

        if (key != null) {
            args.add("-k");
            args.add(key);
        }

        if (entry != null) {
            args.add("-e");
            args.add(entry);
        }

        if (defaultName != null) {
            args.add("-default64");
            args.add(defaultName);
        }

        args.add("-w"); // turn off warnings
        args.add(file);
    }

    public void start() {
        try {
            new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
