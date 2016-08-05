package controllers;

import core.StatusCode;
import core.processing.processes.CoverageProcess;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

public class Coverage extends Application {
    public Result coverage(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);
        ModelWrapper modelWrapper = new ModelWrapper(file);

        CoverageProcess coverageProcess = new CoverageProcess(file, modelWrapper);
        Process process = coverageProcess.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int exitValue = process.exitValue();
        if (exitValue != 0)
            return status(StatusCode.UnprocessableEntity, "Error occurred during coverage generation");

        return ok();
    }
}
