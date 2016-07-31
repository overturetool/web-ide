package core.wrappers;

import core.interpreter.util.InterpreterManager;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.codegen.utils.GeneralCodeGenUtils;
import org.overture.codegen.vdm2java.JavaCodeGenMain;
import org.overture.config.Release;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import org.overture.webide.interpreter_util.Result;
import org.overture.webide.interpreter_util.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModelWrapper {
    private ModuleInterpreter interpreter;

    private List<File> files;
    private Dialect dialect;
    private Release release;

    public List<VDMWarning> parserWarnings;
    public List<VDMWarning> typeCheckerWarnings;
    public List<VDMError> parserErrors;
    public List<VDMError> typeCheckerErrors;

    public ModelWrapper(IVFS<FileObject> file) {
        ConfigParser configParser = new ConfigParser(file);
        this.dialect = configParser.getDialect();
        this.release = configParser.getRelease();
        this.files = JavaCodeGenMain.filterFiles(file.getProjectAsIOFile());
    }

    public String evaluate(String input) {
        Evaluator evaluator = new Evaluator(this.interpreter);
        return evaluator.evaluate(input);
    }

    public String getTargetModuleName() {
        return this.interpreter.getDefaultName();
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public Release getRelease() {
        return this.release;
    }

    public ModuleList getAST() {
        if (this.interpreter != null)
            return this.interpreter.getModules();
        else
            return new ModuleList();
    }

    public IProofObligationList getPOG() throws AnalysisException {
        if (this.interpreter != null && this.interpreter.defaultModule.getTypeChecked())
            return this.interpreter.getProofObligations();
        else
            return new ProofObligationList();
    }

    private List<File> filterFileList(IVFS<FileObject> file) {
        List<File> files = file.getProjectAsIOFile();
        List<File> filteredFiles = Collections.synchronizedList(new ArrayList<>());
        filteredFiles.addAll(files.stream().filter(GeneralCodeGenUtils::isVdmSourceFile).collect(Collectors.toList()));
        return filteredFiles;
    }

    public ModelWrapper init() {
        ModuleList ast;
        List<AModuleModules> moduleModules = null;

        Task task = new Task(this.files, this.dialect, this.release);
        Result result = new InterpreterManager().getResultAsync(task);

        if (result != null) {
            this.parserWarnings = result.getParserWarnings();
            this.parserErrors = result.getParserErrors();
            this.typeCheckerWarnings = result.getTypeCheckerWarnings();
            this.typeCheckerErrors = result.getTypeCheckerErrors();
            moduleModules = result.getModules();
        } else {
            this.parserWarnings = new ArrayList<>();
            this.parserErrors = new ArrayList<>();
            this.typeCheckerWarnings = new ArrayList<>();
            this.typeCheckerErrors = new ArrayList<>();
        }

        if (moduleModules == null) {
            ast = new ModuleList();
        } else {
            ast = new ModuleList(moduleModules);
            ast.combineDefaults();
        }

        try {
            this.interpreter = new ModuleInterpreter(ast);
            this.interpreter.defaultModule.setTypeChecked(this.parserErrors.isEmpty() && this.typeCheckerErrors.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
