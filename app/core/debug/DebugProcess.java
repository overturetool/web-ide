package core.debug;

import core.ServerConfigurations;
import core.processing.processes.AbstractProcess;
import org.overture.interpreter.debug.DBGPReaderV2;

import java.io.IOException;
import java.nio.file.Paths;

public class DebugProcess extends AbstractProcess {
    public DebugProcess(int port, String type, String entry, String defaultName, String file) {
        super(Paths.get("lib", "Overture-2.3.6.jar").toString(), DBGPReaderV2.class.getCanonicalName());

        if (type != null) {
            args.add("-" + type);
        }

        args.add("-h");
        args.add(ServerConfigurations.localhostByName);
        args.add("-p");
        args.add(Integer.toString(port));
        args.add("-k");
        args.add(ServerConfigurations.dbgpKey);

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
