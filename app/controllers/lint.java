package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.lint.LintMapper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.mvc.Result;

public class lint extends Application {
    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file).init();
        String targetModuleName = modelWrapper.getTargetModuleName();

        LintMapper mapper = new LintMapper();
        ObjectNode messages = new ObjectMapper().createObjectNode();
        messages.putPOJO("parserWarnings", mapper.messagesToJson(modelWrapper.parserWarnings, targetModuleName));
        messages.putPOJO("parserErrors", mapper.messagesToJson(modelWrapper.parserErrors, targetModuleName));
        messages.putPOJO("typeCheckerWarnings", mapper.messagesToJson(modelWrapper.typeCheckerWarnings, targetModuleName));
        messages.putPOJO("typeCheckerErrors", mapper.messagesToJson(modelWrapper.typeCheckerErrors, targetModuleName));

        return ok(messages);
    }
}
