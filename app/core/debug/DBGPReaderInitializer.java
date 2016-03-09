package core.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBGPReaderInitializer {
    private List<String> args;
    private final Logger logger = LoggerFactory.getLogger(DBGPReaderInitializer.class);

    public DBGPReaderInitializer(String type, String host, int port, String key, String entry, String defaultName, String file) {
        args = new ArrayList<>();

        args.add("java");
        args.add("-cp");
        args.add("Overture-2.3.2.jar");
        args.add("org.overture.interpreter.debug.DBGPReaderV2");

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
            logger.error(e.getMessage(), e);
        }
    }
}
