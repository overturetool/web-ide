package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RmiRuntimeServer implements IRuntimeSocketServer {

    public String getMessage(String content) {
        return content + " 1";
    }

    public static void main(String args[]) {
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(5000);
            //System.out.println("ready");
            Socket client = serverSocket.accept();

            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());

            Object inputObject = null;

            try {
                inputObject = in.readObject();
            } catch (EOFException e) {
                // done
            }

            List<File> fileList = new ArrayList<File>();

            if (inputObject != null) {
                if (inputObject instanceof List<?>) {
                    List<?> inputList = (List<?>) inputObject;
                    if (!inputList.isEmpty() && inputList.get(0) instanceof File) {
                        for (Object object : inputList) {
                            fileList.add((File) object);
                        }
                    }
                }
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

            out.close();
            in.close();
            client.close();
            serverSocket.close();
        } catch (Exception e) {
            try {
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
