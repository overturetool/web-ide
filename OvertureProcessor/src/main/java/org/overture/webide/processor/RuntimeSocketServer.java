package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RuntimeSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeSocketServer.class);

    public static void main(String args[]) throws ClassNotFoundException, IOException {
        String host = null;
        int port = -1;
        int timeoutValue = -1;

        String id;
        String val;
        int len = args.length;

        for (int i = 0; i < len - 1 && (id = args[i]) != null && (val = args[i + 1]) != null; i = i + 2) {
            if (id.equalsIgnoreCase(ProcessArguments.Identifiers.Host)) {
                host = val;
            } else if (id.equalsIgnoreCase(ProcessArguments.Identifiers.Port)) {
                port = Integer.parseInt(val);
            } else if (id.equalsIgnoreCase(ProcessArguments.Identifiers.Timeout)) {
                timeoutValue = Integer.parseInt(val);
            } else {
                throw new IllegalArgumentException("Unknown argument id: " + id);
            }
        }

        final Socket socket = new Socket(host, port);
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        logger.info("process " + getPID() + " ready");

        while (true) {
            Object inputObject = in.readObject();

            if (inputObject == null)
                continue;

            List<File> fileList = object2FileList(inputObject);
            ProcessingResult result = getProcessingResult(fileList);

            out.writeObject(result);
            out.flush();
        }
    }

    private static ProcessingResult getProcessingResult(List<File> fileList) {
        Settings.dialect = Dialect.VDM_SL;
        //Settings.release = this.release;

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
}
