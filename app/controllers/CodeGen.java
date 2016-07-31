package controllers;

import core.StatusCode;
import core.codegen.ProcessStrategy;
import core.codegen.ICodeGenStrategy;
import core.codegen.ThreadStrategy;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

public class CodeGen extends Application {
    public Result codeGen(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);
        ModelWrapper modelWrapper = new ModelWrapper(file).init();

        ICodeGenStrategy codeGenStrategy = new ThreadStrategy(file, modelWrapper);
        boolean exitValue = codeGenStrategy.generate();

        if (!exitValue)
            return status(StatusCode.UnprocessableEntity, "Exception occurred during code generation");

        return ok();
    }

    public Result codeGen2(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);
        ModelWrapper modelWrapper = new ModelWrapper(file);

        ICodeGenStrategy codeGenStrategy = new ProcessStrategy(file, modelWrapper);
        boolean exitValue = codeGenStrategy.generate();

        if (!exitValue)
            return status(StatusCode.UnprocessableEntity, "Exception thrown!");

        return ok();
    }
}
