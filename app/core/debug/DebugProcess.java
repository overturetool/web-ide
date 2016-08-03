package core.debug;

import core.processing.processes.AbstractProcess;
import org.overture.interpreter.debug.DBGPReaderV2;

import java.io.IOException;
import java.nio.file.Paths;

public class DebugProcess extends AbstractProcess {
    public DebugProcess(String type, String host, int port, String key, String entry, String defaultName, String file) {
        super(Paths.get("lib", "Overture-2.3.6.jar").toString(), DBGPReaderV2.class.getCanonicalName());

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

    @Override
    public Process start() {
        try {
            new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
