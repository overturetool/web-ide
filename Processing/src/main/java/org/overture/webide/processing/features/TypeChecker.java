package org.overture.webide.processing.features;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;
import org.overture.webide.processing.models.Result;

import java.io.File;
import java.util.List;

public class TypeChecker implements ITypeChecker {
    public Result getResult(List<File> fileList, Dialect dialect, Release release) {
        Settings.dialect = dialect;
        Settings.release = release;

        TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(fileList, VDMJ.filecharset);
        ParserUtil.ParserResult<List<AModuleModules>> parserResult = typeCheckerResult.parserResult;

        Result result = new Result();
        result.setParserWarnings(parserResult.warnings);
        result.setParserErrors(parserResult.errors);
        result.setTypeCheckerWarnings(typeCheckerResult.warnings);
        result.setTypeCheckerErrors(typeCheckerResult.errors);
        result.setModules(typeCheckerResult.result != null ? typeCheckerResult.result : parserResult.result);
        return result;
    }
}
