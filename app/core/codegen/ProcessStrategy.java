package core.codegen;

import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;

public class ProcessStrategy implements ICodeGenStrategy {
    private IVFS<FileObject> file;
    private ModelWrapper modelWrapper;

    public ProcessStrategy(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        this.file = file;
        this.modelWrapper = modelWrapper;
    }

    @Override
    public boolean generate() {
        CodeGenProcess codeGenProcess = new CodeGenProcess(this.file, this.modelWrapper);
        Process process = codeGenProcess.start();
        return process != null && process.exitValue() == 0;
    }
}
