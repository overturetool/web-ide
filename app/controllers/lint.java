package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.lint.LintMapper;
import core.lint.LintProvider;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class lint extends Application {
    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper;

        if (file.isDirectory())
            modelWrapper = new ModelWrapper(file.readdirAsIOFile());
        else
            modelWrapper = new ModelWrapper(file);

        LintProvider lintProvider = new LintProvider(modelWrapper);
        List<VDMError> parserErrors = lintProvider.getParserErrors();
        List<VDMWarning> parserWarnings = lintProvider.getParserWarnings();
        List<VDMError> typeCheckerErrors = lintProvider.getTypeCheckerErrors();
        List<VDMWarning> typeCheckerWarnings = lintProvider.getTypeCheckerWarnings();

        LintMapper mapper = new LintMapper();

        String targetModuleName = modelWrapper.getTargetModuleName();

        ObjectNode messages = Json.newObject();
        messages.putPOJO("parserWarnings", mapper.messagesToJson(parserWarnings, targetModuleName));
        messages.putPOJO("parserErrors", mapper.messagesToJson(parserErrors, targetModuleName));
        messages.putPOJO("typeCheckerWarnings", mapper.messagesToJson(typeCheckerWarnings, targetModuleName));
        messages.putPOJO("typeCheckerErrors", mapper.messagesToJson(typeCheckerErrors, targetModuleName));

        return ok(messages);
    }
}
