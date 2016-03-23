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
import org.overture.ast.messages.InternalException;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Result;

import java.io.File;
import java.util.List;

public class lint extends Application {
    private final Logger logger = LoggerFactory.getLogger(lint.class);

    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file);
        String targetModuleName = modelWrapper.getTargetModuleName();

        Settings.dialect = Dialect.VDM_SL; // Necessary for the parser and typechecker
        //Settings.release = Release.CLASSIC;
        List<File> files = file.readdirAsIOFile();
        String charset = VDMJ.filecharset;

        ParserUtil.ParserResult<List<AModuleModules>> parserResults;
        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> typeCheckerResults;
        try {
            parserResults = ParserUtil.parseSl(files, charset);
            typeCheckerResults = TypeCheckerUtil.typeCheckSl(files, charset);
        } catch (InternalException e) {
            logger.error(e.getMessage(), e);
            parserResults = ParserUtil.parseSl("");
            typeCheckerResults = TypeCheckerUtil.typeCheckSl("");
        }

        LintMapper mapper = new LintMapper();
        ObjectNode messages = new ObjectMapper().createObjectNode();
        messages.putPOJO("parserWarnings", mapper.messagesToJson(parserResults.warnings, targetModuleName));
        messages.putPOJO("parserErrors", mapper.messagesToJson(parserResults.errors, targetModuleName));
        messages.putPOJO("typeCheckerWarnings", mapper.messagesToJson(typeCheckerResults.warnings, targetModuleName));
        messages.putPOJO("typeCheckerErrors", mapper.messagesToJson(typeCheckerResults.errors, targetModuleName));

        return ok(messages);
    }
}
