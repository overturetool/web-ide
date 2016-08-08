package org.overture.webide.processing;

import org.overture.webide.processing.features.TypeChecker.TypeChecker;
import org.overture.webide.processing.models.Result;
import org.overture.webide.processing.models.Task;
import org.overture.webide.processing.utils.ProcessingUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class TypeCheckerMain extends ProcessingMain {
    static {
        instanceClass = TypeCheckerMain.class;
    }

    @Override
    @SuppressWarnings("all")
    public void execute() throws IOException, ClassNotFoundException {
        final Socket socket = ProcessingUtils.getSocket(host, port);
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        while (true) {
            Object inputObject = null;

            try {
                inputObject = in.readObject();
            } catch (EOFException e) { /* ignored */ }

            if (inputObject == null)
                continue;

            Task task = (Task) inputObject;
            List<File> fileList = task.getFileList();
            Result result = new TypeChecker().getResult(fileList, task.getDialect(), task.getRelease());

            out.writeObject(result);
            out.flush();
        }
    }
}
