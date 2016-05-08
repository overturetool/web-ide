package controllers;

import core.StatusCode;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.ast.util.modules.ModuleList;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.utils.GeneratedModule;
import org.overture.codegen.vdm2java.JavaCodeGen;
import org.overture.interpreter.runtime.LatexSourceFile;
import org.overture.interpreter.runtime.SourceFile;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class evaluate extends Application {

    public Result expression(String input) {
        String inputDecoded = new String(Base64.getDecoder().decode(input));

        ModelWrapper modelWrapper = new ModelWrapper();

        return ok(modelWrapper.evaluate(inputDecoded));
    }

    public Result project(String input, String account, String path) {
        String inputDecoded = new String(Base64.getDecoder().decode(input));

        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file);
        String result = modelWrapper.evaluate(inputDecoded);

        return ok(result != null ? result : "?");
    }

    private void testCodeGen(ModelWrapper modelWrapper) {
        List<INode> list = new ArrayList<>();
        ModuleList ast = modelWrapper.getAst();
        list.addAll(ast.stream().collect(Collectors.toList()));

        JavaCodeGen codegen = new JavaCodeGen();
        GeneratedData generate;
        try {
            generate = codegen.generate(list);
            List<GeneratedModule> classes = generate.getClasses();
            System.out.println(classes.get(0).getContent());
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
    }

    private void testCoverage(IVFS file) {
        String coverage = "/Users/kaspersaaby/Documents/projects/iha/playframework/overture_webide/workspace/111425625270532893915/BOMSL/generated/coverage.txt";
        File des = new File("workspace/111425625270532893915/BOMSL/generated/coverage.txt");
        StringWriter writer = new StringWriter();
        try {
            SourceFile sourceFile = new SourceFile(file.getIOFile());
            PrintWriter printWriter = new PrintWriter(writer);
            sourceFile.writeCoverage(printWriter);
            LatexSourceFile latex = new LatexSourceFile(sourceFile);
            latex.print(printWriter, true, true, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(writer);
    }
}
