package core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RuntimeProcess {
    public Process init(int port) {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.add("-cp");
        args.add("lib/original-OvertureProcessor-1.0-SNAPSHOT.jar");
        args.add("org.overture.webide.processor.RuntimeSocketServer");

        args.add("localhost");
        args.add(Integer.toString(port));
        args.add(Integer.toString(30));

        Process process = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectErrorStream(true);
            process = builder.start();

            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            new ProcessStream(inputStream).start();
            new ProcessStream(errorStream).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    public class ProcessStream extends Thread {
        private BufferedReader bufferedReader;

        public ProcessStream(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            try {
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    System.out.println(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
