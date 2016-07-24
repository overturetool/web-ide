package core.codegen;

import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.util.modules.ModuleList;
import org.overture.codegen.ir.CodeGenBase;
import org.overture.codegen.ir.IRSettings;
import org.overture.codegen.logging.ILogger;
import org.overture.codegen.logging.Logger;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.vdm2java.JavaCodeGen;
import org.overture.codegen.vdm2java.JavaCodeGenMain;
import org.overture.codegen.vdm2java.JavaSettings;
import org.overture.config.Settings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ThreadStrategy implements ICodeGenStrategy {
    private IVFS<FileObject> file;
    private ModelWrapper modelWrapper;

    public ThreadStrategy(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        this.file = file;
        this.modelWrapper = modelWrapper;
    }

    @Override
    public boolean generate() {
        ModuleList ast = this.modelWrapper.getAST();
        Settings.dialect = this.modelWrapper.getDialect();
        Settings.release = this.modelWrapper.getRelease();

        String rootPackage = Paths.get(this.file.getRelativePath()).getName(0).toString();
        Path output = Paths.get(this.file.getAbsolutePath(), "generated");

        try {
            IRSettings irSettings = new IRSettings();
            irSettings.setCharSeqAsString(true);
            irSettings.setGenerateConc(false);
            irSettings.setGenerateInvariants(false);
            irSettings.setGeneratePostCondChecks(false);
            irSettings.setGeneratePostConds(false);
            irSettings.setGeneratePreCondChecks(false);
            irSettings.setGeneratePreConds(false);
            irSettings.setGenerateTraces(false);

            JavaSettings javaSettings = new JavaSettings();
            javaSettings.setJavaRootPackage(rootPackage);

            JavaCodeGen codeGen = new JavaCodeGen();
            codeGen.setSettings(irSettings);
            codeGen.setJavaSettings(javaSettings);

            ListLogger logger = new ListLogger();

            synchronized (ThreadStrategy.class) {
                Logger.setLog(logger);
                GeneratedData data = codeGen.generate(CodeGenBase.getNodes(ast));
                JavaCodeGenMain.processData(false, output.toFile(), codeGen, data, true);
                // TODO : Use logger data
            }
        } catch (AnalysisException | NullPointerException e) {
            return false;
        }

        return true;
    }

    public class ListLogger implements ILogger {
        private boolean silent = false;
        private LinkedList<String> list = new LinkedList<>();

        @Override
        public void setSilent(boolean b) {
            this.silent = b;
        }

        @Override
        public void println(String s) {
            if (!this.silent) {
                this.list.add(s);
            }
        }

        @Override
        public void print(String s) {
            if (!this.silent) {
                String lastElement = this.list.removeLast();
                this.list.add(String.join(" ", lastElement, s));
            }
        }

        @Override
        public void printErrorln(String s) {
            println(s);
        }

        @Override
        public void printError(String s) {
            print(s);
        }

        public List<String> getList() {
            return this.list;
        }
    }

    public class StringLogger implements ILogger {
        private String content = "";
        private boolean silent = false;

        @Override
        public void setSilent(boolean b) {
            this.silent = b;
        }

        @Override
        public void println(String s) {
            if (!this.silent)
                this.content += s + "\n";
        }

        @Override
        public void print(String s) {
            if (!this.silent)
                this.content += s;
        }

        @Override
        public void printErrorln(String s) {
            println(s);
        }

        @Override
        public void printError(String s) {
            print(s);
        }

        public String getContent() {
            return this.content;
        }
    }
}
