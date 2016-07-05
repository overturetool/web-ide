package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RuntimeSocketServer {

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int timeoutValue = Integer.parseInt(args[2]);

        final Socket socket = new Socket(host, port);
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        final Duration timeout = Duration.ofSeconds(timeoutValue);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    out.close();
                    in.close();
                    socket.close();
                    System.exit(0);
                } catch (IOException ignored) {}
            }
        };

        executor.schedule(runnable, timeout.toMillis(), TimeUnit.MILLISECONDS);

        while (true) {
            Object inputObject = null;
            try {
                inputObject = in.readObject();
            } catch (EOFException e) {
                // done
                executor.shutdownNow();
            } catch (SocketException ignored) {
                // socket closed
            }

            if (inputObject == null)
                continue;

            List<File> fileList = new ArrayList<File>();

            if (inputObject instanceof List<?>) {
                List<?> inputList = (List<?>) inputObject;
                if (!inputList.isEmpty() && inputList.get(0) instanceof File) {
                    for (Object object : inputList)
                        fileList.add((File) object);
                }
            } else if (inputObject instanceof String) {
                out.writeObject(inputObject);
                out.flush();
                continue;
            }

            Settings.dialect = Dialect.VDM_SL;
            //Settings.release = this.release;

            TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
            ParserResult<List<AModuleModules>> parserResult = typeCheckerResult.parserResult;

            ProcessingResult result = new ProcessingResult();

            result.setParserWarnings(parserResult.warnings);
            result.setParserErrors(parserResult.errors);
            result.setTypeCheckerWarnings(typeCheckerResult.warnings);
            result.setTypeCheckerErrors(typeCheckerResult.errors);
            result.modules = typeCheckerResult.result != null ? typeCheckerResult.result : parserResult.result;

            out.writeObject(result);
            out.flush();
            executor.schedule(runnable, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
