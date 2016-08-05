package controllers;

import core.StatusCode;
import core.codegen.CodeGenProcessManager;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

public class CodeGen extends Application {
    public Result codeGen(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);
        ModelWrapper modelWrapper = new ModelWrapper(file).init();

        CodeGenProcessManager codeGenStrategy = new CodeGenProcessManager(file, modelWrapper);
        boolean exitValue = codeGenStrategy.generate();

        if (!exitValue)
            return status(StatusCode.UnprocessableEntity, "Exception occurred during code generation");

        return ok();
    }

    public Result codeGen2(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);
        ModelWrapper modelWrapper = new ModelWrapper(file);

        CodeGenProcessManager codeGenStrategy = new CodeGenProcessManager(file, modelWrapper);
        boolean exitValue = codeGenStrategy.generateV2();

        if (!exitValue)
            return status(StatusCode.UnprocessableEntity, "Exception occurred during code generation");

        return ok();
    }
}
