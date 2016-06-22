package core.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RuntimeProcess {
    public boolean init(int port) {
        List<String> args = new ArrayList<>();
        args.add("java");
        args.add("-cp");
        args.add("OvertureProcessor/target/OvertureProcessor-1.0-SNAPSHOT.jar");
        args.add("org.overture.webide.processor.RmiRuntimeServer");
        args.add(port + "");

        try {
            Process start = new ProcessBuilder(args).start();

            InputStream inputStream = start.getInputStream();
            InputStream errorStream = start.getErrorStream();

            new ProcessStream(inputStream).start();
            new ProcessStream(errorStream).start();

//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            String status = bufferedReader.readLine();
//
//            if (status.equals("ready"))
//                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class ProcessStream extends Thread {
        private BufferedReader bufferedReader;

        public ProcessStream(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            String input;

            try {
                while ((input = bufferedReader.readLine()) != null) {
                    System.out.println(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
