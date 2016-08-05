package core.codegen;

import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;

public class CodeGenProcessManager {
    private IVFS<FileObject> file;
    private ModelWrapper modelWrapper;

    public CodeGenProcessManager(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        this.file = file;
        this.modelWrapper = modelWrapper;
    }

    public boolean generate() {
        CodeGenProcess codeGenProcess = new CodeGenProcess(this.file, this.modelWrapper);
        Process process = codeGenProcess.start();
        return process != null && process.exitValue() == 0;
    }

    public boolean generateV2() {
        CodeGenProcessV2 codeGenProcess = new CodeGenProcessV2(this.file, this.modelWrapper);
        Process process = codeGenProcess.start();
        try {
            if (process != null)
                process.waitFor();
            else
                return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return process.exitValue() == 0;
    }
}
