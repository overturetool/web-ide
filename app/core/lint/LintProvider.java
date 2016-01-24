package core.lint;

import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Settings;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import org.overture.parser.util.ParserUtil;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LintProvider {
    private ModuleList ast;
    private ModuleInterpreter interpreter;
    private TypeCheckResult<List<AModuleModules>> typeCheckResult;
    private ParserResult<List<AModuleModules>> parserResult;

    public LintProvider(IVFS<FileObject> file) {
        Settings.dialect = Dialect.VDM_SL;
        List<File> files = new ArrayList<>();

        // TODO : Change this implementation not to use FileObjects directly
        if (file.isDirectory()) {
            List<FileObject> fileObjects = file.readdir(0);
            try {
                for (FileObject fo : fileObjects) {
                    if (fo.getType() == FileType.FILE)
                        files.add(new File(fo.getName().getPath()));
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        } else {
            files.add(file.getIOFile());
        }

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
