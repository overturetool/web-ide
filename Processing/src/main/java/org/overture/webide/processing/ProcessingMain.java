package org.overture.webide.processing;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ProcessingMain {
    public static void main(String args[]) throws ClassNotFoundException, IOException {
        String action;
        String host = null;
        int port = -1;
        boolean printInfo = false;

        Iterator<String> i = Arrays.asList(args).iterator();

        if (i.hasNext()) {
            action = i.next();
        } else {
            throw new IllegalArgumentException("Missing action argument");
        }

        while(i.hasNext()) {
            String arg = i.next();
            if (arg.equals(Arguments.Identifiers.Host) && i.hasNext()) {
                host = i.next();
            } else if (arg.equals(Arguments.Identifiers.Port) && i.hasNext()) {
                port = Integer.parseInt(i.next());
            } else if (arg.equals(Arguments.Identifiers.PrintInfo)) {
                printInfo = true;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (host == null || port == -1)
            throw new IllegalArgumentException("Missing required arguments: host and/or port");

        final Socket socket = new Socket(host, port);

        if (printInfo)
            System.out.println("process " + ProcessingUtils.getPID() + " ready");

        if (action.equals(Arguments.Actions.TypeCheck)) {
            typeCheckLoop(socket);
        } else if (action.equals(Arguments.Actions.Evaluate)) {
            evaluateLoop(socket);
        }
    }

    @SuppressWarnings("all")
    private static void typeCheckLoop(Socket socket) throws IOException, ClassNotFoundException {
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        while (true) {
            Object inputObject = null;

            try {
                inputObject = in.readObject();
            } catch (EOFException e) { /* ignored */ }

            if (inputObject == null)
                continue;

            Task task = (Task) inputObject;
            List<File> fileList = ProcessingUtils.object2FileList(task.getFileList());
            Result result = new Processing().getResult(fileList, task.getDialect(), task.getRelease());

            out.writeObject(result);
            out.flush();
        }
    }

    private static void evaluateLoop(Socket socket) {
        // REPL feature
    }
}
