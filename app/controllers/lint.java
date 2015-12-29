package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.lint.LintMapper;
import core.lint.LintProvider;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class lint extends Application {
    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!file.exists())
            return ok();

        LintProvider lintProvider = new LintProvider(file);
        List<VDMError> parserErrors = lintProvider.getParserErrors();
        List<VDMWarning> parserWarnings = lintProvider.getParserWarnings();
        List<VDMError> typeCheckerErrors = lintProvider.getTypeCheckerErrors();
        List<VDMWarning> typeCheckerWarnings = lintProvider.getTypeCheckerWarnings();

        LintMapper mapper = new LintMapper();

        ObjectNode messages = Json.newObject();
        messages.putPOJO("parserWarnings", mapper.messagesToJson(parserWarnings));
        messages.putPOJO("parserErrors", mapper.messagesToJson(parserErrors));
        messages.putPOJO("typeCheckerWarnings", mapper.messagesToJson(typeCheckerWarnings));
        messages.putPOJO("typeCheckerErrors", mapper.messagesToJson(typeCheckerErrors));

        return ok(messages);
    }
}
