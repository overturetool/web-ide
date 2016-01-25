package core.debug;

import java.io.IOException;

public class DBGPReaderInitializer {
    private String[] args;

    public DBGPReaderInitializer(String type, String host, int port, String key, String entry, String file) {
        this.args = new String[] {
            "java",
            "-cp",
            "Overture-2.3.0.jar",
            "org.overture.interpreter.debug.DBGPReaderV2",
            "-" + type,
            "-h", host,
            "-p", Integer.toString(port),
            "-k", key,
            "-e", entry,
            file
        };
    }

    public void start() {
        try {
            new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
