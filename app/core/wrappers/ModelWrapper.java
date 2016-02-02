package core.wrappers;

import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AFromModuleImports;
import org.overture.ast.modules.AModuleImports;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Settings;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.parser.util.ParserUtil;
import org.overture.pog.pub.IProofObligationList;
import org.overture.typechecker.util.TypeCheckerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private List<File> _files;
    private String targetModuleName;

    public ModelWrapper(IVFS<FileObject> file) {
        // TODO : Possible to avoid parsing just for getting module name?
        try {
            VDMSL vdmsl = new VDMSL();
            ExitStatus parseExitStatus = vdmsl.parse(wrapInList(file.getIOFile()));

            if (parseExitStatus.equals(ExitStatus.EXIT_OK))
                this.targetModuleName = vdmsl.getInterpreter().getDefaultName();

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<File> files = wrapInList(file.getIOFile());

        List<File> importFiles = resolveImports(file);
        if (importFiles != null)
            files.addAll(importFiles);

        init(files);
    }

    public ModelWrapper(List<File> files) {
        init(files);
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
        return ParserUtil.parseSl(this._files);
    }

    public TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> getTypeCheckerResults() {
        return TypeCheckerUtil.typeCheckSl(this._files);
    }

    private void init(List<File> files) {
        this._files = files;
        Settings.dialect = Dialect.VDM_SL; // Necessary for the parser and typechecker

        // Look into using the VDMJ class instead
        VDMSL vdmsl = new VDMSL();
        ExitStatus parseStatus = vdmsl.parse(files);

        if (parseStatus == ExitStatus.EXIT_OK) {
            ExitStatus typeCheckStatus = vdmsl.typeCheck();

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.interpreter = vdmsl.getInterpreter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Safety-net to avoid NullPointerExceptions
        try {
            if (interpreter == null)
                interpreter = new ModuleInterpreter(new ModuleList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<File> resolveImports(IVFS<FileObject> file) {
        List<File> files = new ArrayList<>();

        VDMSL vdmsl = new VDMSL();
        ExitStatus parseStatus = vdmsl.parse(wrapInList(file.getIOFile()));

        if (parseStatus == ExitStatus.EXIT_ERRORS)
            return null;

        try {
            ModuleList modules = vdmsl.getInterpreter().getModules();
            LinkedList<AFromModuleImports> imports = findImports(modules);

            if (imports == null)
                return null;

            for (AFromModuleImports item : imports) {
                files.addAll(findModule(file, item.getName().getName()));
            }

            if (files.size() == 0)
                return null;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    public List<File> findModule(IVFS<FileObject> file, String module) {
        List<File> files = new ArrayList<>();
        List<File> siblings = file.getSiblings();

        for (File sibling : siblings) {
            VDMSL vdmsl = new VDMSL();

            ExitStatus parseExitStatus = vdmsl.parse(wrapInList(sibling));

            if (parseExitStatus.equals(ExitStatus.EXIT_ERRORS))
                continue;

            // TODO : Re-think this
            IVFS<FileObject> siblingFileObject = new CommonsVFS(PathHelper.RelativePath(sibling.getPath()));
            List<File> siblingImports = resolveImports(siblingFileObject);
            if (siblingImports != null)
                files.addAll(siblingImports);

            try {
                AModuleModules moduleModules = vdmsl.getInterpreter().findModule(module);

                if (moduleModules == null)
                    continue;

                files.addAll(moduleModules.getFiles());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return files;
    }

    private LinkedList<AFromModuleImports> findImports(ModuleList modules) {
        try {
            for (Object node : modules) {
                if (!(node instanceof AModuleModules))
                    continue;

                AModuleModules module = (AModuleModules) node;
                AModuleImports imports = module.getImports();

                if (imports == null)
                    return null;

                return imports.getImports();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private List<File> wrapInList(File file) {
        List<File> files = new ArrayList<>();
        files.add(file);
        return files;
    }
}
