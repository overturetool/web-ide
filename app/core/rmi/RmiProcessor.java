package core.rmi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RmiProcessor {
    private static RmiProcessor instance = null;

    public static RmiProcessor getInstance() {
        if (instance == null) {
            synchronized (RmiProcessor.class) {
                instance = new RmiProcessor();
            }
        }
        return instance;
    }

    private RmiProcessor() {
    }

    public void startRmiServer() {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.add("-cp");
        args.add("OvertureProcessor/target/OvertureProcessor-1.0-SNAPSHOT.jar");

        args.add("-Djava.rmi.server.codebase=file:OvertureProcessor/target/OvertureProcessor-1.0-SNAPSHOT.jar");
        args.add("-Djava.rmi.server.hostname=localhost");
        args.add("-Djava.security.policy=java.security.AllPermission");

        args.add("org.overture.webide.processor.RmiRuntimeServer");

        //args.add("-Djava.security.manager");

        try {
            new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
