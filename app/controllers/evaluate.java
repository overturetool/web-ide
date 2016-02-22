package controllers;

import core.StatusCode;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

import java.util.Base64;

public class evaluate extends Application {

    public Result expression(String input) {
        String inputDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(input));

        ModelWrapper modelWrapper = new ModelWrapper();

        return ok(modelWrapper.evaluate(inputDecoded));
    }

    public Result project(String input, String account, String path) {
        String inputDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(input));

        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file);

        return ok(modelWrapper.evaluate(inputDecoded));
    }
}
