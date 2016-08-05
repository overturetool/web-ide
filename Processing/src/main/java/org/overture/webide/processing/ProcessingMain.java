package org.overture.webide.processing;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;
import org.overture.webide.processing.features.Evaluator;
import org.overture.webide.processing.features.JavaCodeGenerator;
import org.overture.webide.processing.features.TypeChecker;
import org.overture.webide.processing.models.Result;
import org.overture.webide.processing.models.Task;
import org.overture.webide.processing.utils.Arguments;
import org.overture.webide.processing.utils.ProcessingUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ProcessingMain {
    public static void main(String args[]) throws Exception {
        String action = null;
        String host = null;
        String baseDir = null;
        int port = -1;
        boolean printInfo = false;
        List<File> fileList = new ArrayList<>();
        File baseDirFile = null;
        Settings.dialect = Dialect.VDM_PP;
        Settings.release = Release.VDM_10;

        Iterator<String> i = Arrays.asList(args).iterator();

        while(i.hasNext()) {
            String arg = i.next();
            if (arg.equals(Arguments.Actions.Evaluate) ||
                arg.equals(Arguments.Actions.TypeCheck) ||
                arg.equals(Arguments.Actions.JavaCodeGen)) {
                action = arg;
            } else if (arg.equals(Arguments.Identifiers.Host) && i.hasNext()) {
                host = i.next();
            } else if (arg.equals(Arguments.Identifiers.Port) && i.hasNext()) {
                port = Integer.parseInt(i.next());
            } else if (arg.equals(Arguments.Identifiers.BaseDir) && i.hasNext()) {
                baseDir = i.next();
                baseDirFile = new File(baseDir);
                ProcessingUtils.validateBaseDir(baseDirFile);
            } else if (arg.equals(Arguments.Identifiers.PrintInfo)) {
                printInfo = true;
            } else if (arg.equals(Arguments.Dialects.VDM_PP)) {
                Settings.dialect = Dialect.VDM_PP;
            } else if (arg.equals(Arguments.Dialects.VDM_RT)) {
                Settings.dialect = Dialect.VDM_RT;
            } else if (arg.equals(Arguments.Dialects.VDM_SL)) {
                Settings.dialect = Dialect.VDM_SL;
            } else if (arg.equals(Arguments.Release.VDM_10)) {
                Settings.release = Release.VDM_10;
            } else if (arg.equals(Arguments.Release.CLASSIC)) {
                Settings.release = Release.CLASSIC;
            } else {
                fileList.addAll(ProcessingUtils.handleFiles(arg));
            }
        }

        if (action == null)
            throw new IllegalArgumentException("Missing action argument");

        if (printInfo)
            System.out.println("process " + ProcessingUtils.getPID() + " ready");

        switch (action) {
            case Arguments.Actions.TypeCheck:
                typeCheckLoop(host, port);
                break;
            case Arguments.Actions.Evaluate:
                evaluateLoop(host, port, fileList);
                break;
            case Arguments.Actions.JavaCodeGen:
                codeGen(fileList, baseDir);
                break;
        }
    }

    @SuppressWarnings("all")
    private static void typeCheckLoop(String host, int port) throws IOException, ClassNotFoundException {
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

    private static void evaluateLoop(String host, int port, List<File> fileList) throws Exception {
        Socket socket = ProcessingUtils.getSocket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
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

    private static void codeGen(List<File> fileList, String baseDir) throws Exception {
        TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
        ModuleList ast = new ModuleList(typeCheckerResult.result);
        ast.combineDefaults();

        JavaCodeGenerator codeGenerator = new JavaCodeGenerator(ast, baseDir);

        if (!codeGenerator.generate())
            throw new Exception("Error occurred during code generation");
    }
}
