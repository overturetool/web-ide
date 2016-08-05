package org.overture.webide.processing;

import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.webide.processing.features.Evaluator;
import org.overture.webide.processing.utils.ProcessingUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class EvaluatorMain extends ProcessingMain {
    static {
        instanceClass = EvaluatorMain.class;
    }

    @Override
    public void execute() throws Exception {
        Socket socket = ProcessingUtils.getSocket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
        ModuleList ast = new ModuleList(typeCheckerResult.result);
        ast.combineDefaults();
        ModuleInterpreter interpreter = new ModuleInterpreter(ast);
        interpreter.init(null);
        Evaluator evaluator = new Evaluator(interpreter);

        String input;

        while ((input = in.readLine()) != null) {
            if (input.equals("&exit")) {
                out.println("bye..");
                System.exit(0);
            } else {
                out.println(evaluator.evaluate(input));
            }
        }
    }
}
