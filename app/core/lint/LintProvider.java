package core.lint;

import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import org.overture.parser.util.ParserUtil;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.File;
import java.util.List;

public class LintProvider {
    private TypeCheckResult<List<AModuleModules>> typeCheckResult;
    private ParserResult<List<AModuleModules>> parserResult;

    public LintProvider(IVFS<FileObject> file) {
        Settings.dialect = Dialect.VDM_SL;
        List<File> files = file.readdirAsIOFile();
        this.parserResult = ParserUtil.parseSl(files);
        this.typeCheckResult = TypeCheckerUtil.typeCheckSl(files);
    }

    public List<VDMError> getParserErrors() {
        return parserResult.errors;
    }

    public List<VDMWarning> getParserWarnings() {
        return parserResult.warnings;
    }

    public List<VDMError> getTypeCheckerErrors() {
        return typeCheckResult.errors;
    }

    public List<VDMWarning> getTypeCheckerWarnings() {
        return typeCheckResult.warnings;
    }
}
