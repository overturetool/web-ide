package controllers;

import core.StatusCode;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

import java.util.Base64;

public class Evaluate extends Application {
    public Result project(String input, String account, String path) {
        String inputDecoded = new String(Base64.getDecoder().decode(input));
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file).init();
        String result = modelWrapper.evaluate(inputDecoded);

        return ok(result != null ? result : "?");
    }
}
