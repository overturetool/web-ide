package core.lint;

import core.wrappers.ModelWrapper;
import org.overture.ast.modules.AModuleModules;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.util.List;

public class LintProvider {
    private TypeCheckResult<List<AModuleModules>> typeCheckResult;
    private ParserResult<List<AModuleModules>> parserResult;

    public LintProvider(ModelWrapper modelWrapper) {
        this.parserResult = modelWrapper.getParserResults();
        this.typeCheckResult = modelWrapper.getTypeCheckerResults();
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
