package core.wrappers;

import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Settings;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.interpreter.values.Value;
import org.overture.parser.util.ParserUtil;
import org.overture.pog.pub.IProofObligationList;
import org.overture.typechecker.util.TypeCheckerUtil;
import play.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private ParserUtil.ParserResult<List<AModuleModules>> parserResult;
    private TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> typeCheckResult;
    private String targetModuleName;
    private final Object lock = new Object();

    public ModelWrapper(IVFS<FileObject> file) {
        synchronized (lock) {
            List<File> files = Collections.synchronizedList(new ArrayList<>());
            files.add(file.getIOFile());

            List<File> siblings = file.getSiblings();
            if (siblings != null && !siblings.isEmpty())
                files.addAll(siblings);
            else if (file.isDirectory())
                files.addAll(file.readdirAsIOFile(-1));

            init(files);
        }
    }

    public ModelWrapper(List<File> files) {
        synchronized (lock) {
            init(files);
        }
    }

    public String evaluate(String input) {
        try {
            Value value = this.interpreter.execute(input.trim(), null);
            return value.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public String getTargetModuleName() {
        return this.targetModuleName;
    }

    public ModuleList getAst() {
        return this.interpreter.getModules();
    }

    public IProofObligationList getPog() {
        try {
            return this.interpreter.getProofObligations();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ParserUtil.ParserResult<List<AModuleModules>> getParserResults() {
        return this.parserResult;
    }

    public TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> getTypeCheckerResults() {
        return this.typeCheckResult;
    }

    private synchronized void init(List<File> files) {
        Settings.dialect = Dialect.VDM_SL; // Necessary for the parser and typechecker

        // Look into using the VDMJ class instead
        VDMSL vdmsl = new VDMSL();
        vdmsl.setWarnings(false);
        vdmsl.setQuiet(true);

        ExitStatus parseStatus = vdmsl.parse(files);

        if (parseStatus == ExitStatus.EXIT_OK) {
            ExitStatus typeCheckStatus = vdmsl.typeCheck();

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.interpreter = vdmsl.getInterpreter();
                    this.interpreter.init(null);

                    this.targetModuleName = this.interpreter.getDefaultName();

                    // TODO : Concurrency issues there
                    this.parserResult = ParserUtil.parseSl(files);
                    this.typeCheckResult = TypeCheckerUtil.typeCheckSl(files);
                } catch (Exception e) {
                    Logger.error("Exception thrown when getting interpreter", e);
                    e.printStackTrace();
                }
            }
        }

        // Safety-net to avoid NullPointerExceptions
        try {
            if (this.interpreter == null)
                this.interpreter = new ModuleInterpreter(new ModuleList());
                this.interpreter.init(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
