package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.lint.LintMapper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.parser.util.ParserUtil;
import org.overture.typechecker.util.TypeCheckerUtil;
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

        String targetModuleName = modelWrapper.getTargetModuleName();

        Settings.dialect = Dialect.VDM_SL; // Necessary for the parser and typechecker
        ParserUtil.ParserResult<List<AModuleModules>> parserResults = ParserUtil.parseSl(file.readdirAsIOFile());
        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> typeCheckerResults = TypeCheckerUtil.typeCheckSl(file.readdirAsIOFile());

        LintMapper mapper = new LintMapper();

        ObjectNode messages = new ObjectMapper().createObjectNode();
        messages.putPOJO("parserWarnings", mapper.messagesToJson(parserResults.warnings, targetModuleName));
        messages.putPOJO("parserErrors", mapper.messagesToJson(parserResults.errors, targetModuleName));
        messages.putPOJO("typeCheckerWarnings", mapper.messagesToJson(typeCheckerResults.warnings, targetModuleName));
        messages.putPOJO("typeCheckerErrors", mapper.messagesToJson(typeCheckerResults.errors, targetModuleName));

        return ok(messages);
    }
}
