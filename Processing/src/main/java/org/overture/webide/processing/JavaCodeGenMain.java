package org.overture.webide.processing;

import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMJ;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.webide.processing.features.JavaCodeGenerator;

import java.util.List;

public class JavaCodeGenMain extends ProcessingMain {
    static {
        instanceClass = JavaCodeGenMain.class;
    }

    @Override
    public void execute() throws Exception {
        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
        ModuleList ast = new ModuleList(typeCheckerResult.result);
        ast.combineDefaults();

        JavaCodeGenerator codeGenerator = new JavaCodeGenerator(ast, baseDir);

        if (!codeGenerator.generate())
            throw new Exception("Error occurred during code generation");
    }
}
