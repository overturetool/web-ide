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
import java.util.ArrayList;
import java.util.List;

public class RuntimeSocketServer {

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        int port = Integer.parseInt(args[0]);

        Socket socket = new Socket("localhost", port);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        while (true) {
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
                } else if (inputObject instanceof String) {
                    //System.out.println(inputObject);
                    out.writeObject(inputObject);
                    out.flush();
                    continue;
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
        }

        /*out.close();
        in.close();
        socket.close();*/
    }
}
