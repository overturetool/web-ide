package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RuntimeSocketServer {
    private static boolean printInfo;

    public static void main(String args[]) throws ClassNotFoundException, IOException {
        String host = null;
        int port = -1;
        int timeoutValue = -1;
        printInfo = false;

        Iterator<String> i = Arrays.asList(args).iterator();

        while(i.hasNext()) {
            String arg = i.next();
            if (arg.equalsIgnoreCase(ProcessArguments.Identifiers.Host) && i.hasNext()) {
                host = i.next();
            } else if (arg.equalsIgnoreCase(ProcessArguments.Identifiers.Port)  && i.hasNext()) {
                port = Integer.parseInt(i.next());
            } else if (arg.equalsIgnoreCase(ProcessArguments.Identifiers.Timeout) && i.hasNext()) {
                timeoutValue = Integer.parseInt(i.next());
            } else if (arg.equalsIgnoreCase(ProcessArguments.Identifiers.PrintInfo)) {
                printInfo = true;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (host == null || port == -1)
            throw new IllegalArgumentException("Missing required arguments: host and/or port");

        final Socket socket = new Socket(host, port);
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        print("process " + getPID() + " ready");

        while (true) {
            Object inputObject = null;

            try {
                inputObject = in.readObject();
            } catch (EOFException e) { /* ignored */ }

            if (inputObject == null)
                continue;

            ProcessingTask task = (ProcessingTask) inputObject;
            List<File> fileList = object2FileList(task.getFileList());
            ProcessingResult result = getProcessingResult(fileList, task.getDialect(), task.getRelease());

            out.writeObject(result);
            out.flush();
        }
    }

    private static ProcessingResult getProcessingResult(List<File> fileList, Dialect dialect, Release release) {
        Settings.dialect = dialect;
        Settings.release = release;

        TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
        ParserResult<List<AModuleModules>> parserResult = typeCheckerResult.parserResult;

        ProcessingResult result = new ProcessingResult();
        result.setParserWarnings(parserResult.warnings);
        result.setParserErrors(parserResult.errors);
        result.setTypeCheckerWarnings(typeCheckerResult.warnings);
        result.setTypeCheckerErrors(typeCheckerResult.errors);
        result.setModules(typeCheckerResult.result != null ? typeCheckerResult.result : parserResult.result);
        return result;
    }

    private static List<File> object2FileList(Object inputObject) {
        List<File> fileList = new ArrayList<File>();
        if (inputObject instanceof List<?>) {
            List<?> inputList = (List<?>) inputObject;
            if (!inputList.isEmpty() && inputList.get(0) instanceof File) {
                for (Object object : inputList)
                    fileList.add((File) object);
            }
        }
        return fileList;
    }

    private static int getPID() {
        try {
            String pidStr = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            return Integer.parseInt(pidStr);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void print(String s) {
        if (printInfo) {
            System.out.println(s);
        }
    }
}
