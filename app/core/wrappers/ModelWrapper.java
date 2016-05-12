package core.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.utilities.ResourceCache;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private static final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(ModelWrapper.class);

    public ModelWrapper(IVFS<FileObject> file) {
        if (ResourceCache.getInstance().existsAndNotModified(file)) {
            this.interpreter = ResourceCache.getInstance().get(file).getInterpreter();
        } else {
            List<File> filteredFiles = preprocessFiles(file);
            Release release = getRelease(file);
            if (init(filteredFiles, release))
                ResourceCache.getInstance().add(file, this.interpreter);
        }
    }

    public ModelWrapper() {
        init();
    }

    public synchronized String evaluate(String input) {
        Evaluator evaluator = new Evaluator(this.interpreter);
        return evaluator.evaluate(input);
    }

    public String getTargetModuleName() {
        return this.interpreter.getDefaultName();
    }

    public ModuleList getAst() {
        if (this.interpreter != null)
            return this.interpreter.getModules();
        else
            return new ModuleList();
    }

    public synchronized IProofObligationList getPog() throws AnalysisException {
        if (this.interpreter != null && this.interpreter.defaultModule.getTypeChecked())
            return this.interpreter.getProofObligations();
        return new ProofObligationList();
    }

    private synchronized List<File> preprocessFiles(IVFS<FileObject> file) {
        List<File> files = file.getProjectAsIOFile();
        List<File> filteredFiles = Collections.synchronizedList(new ArrayList<>());
        filteredFiles.addAll(files.stream().filter(f -> f.getName().endsWith(".vdmsl")).collect(Collectors.toList()));
        return filteredFiles;
    }

    private boolean init(List<File> files, Release release) {
        Settings.dialect = Dialect.VDM_SL;
        ParserResult<List<AModuleModules>> parserResult;
        List<AModuleModules> result;
        ModuleList ast;

        synchronized (lock) {
            Settings.release = release;
            parserResult = TypeCheckerUtil.typeCheckSl(files, VDMJ.filecharset).parserResult;
            result = parserResult.result;
        }

        if (result == null) {
            ast = new ModuleList();
        } else {
            ast = new ModuleList(result);
            ast.combineDefaults();
        }

        try {
            this.interpreter = new ModuleInterpreter(ast);
            this.interpreter.init(null);
            if (parserResult.errors.isEmpty()) {
                this.interpreter.defaultModule.setTypeChecked(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void protectedCall() {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() {
                interpreter.init(null);
                return null;
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            Object result = future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        } finally {
            future.cancel(true);
            executor.shutdownNow();
        }
    }

    private synchronized boolean init() {
        try {
            if (this.interpreter == null) {
                this.interpreter = new ModuleInterpreter(new ModuleList());
                this.interpreter.init(null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private synchronized Release getRelease(IVFS<FileObject> file) {
        try {
            String attribute = "release";
            FileObject projectRoot = file.getProjectRoot();
            if (projectRoot == null)
                return Release.DEFAULT;

            FileObject projectFile = projectRoot.getChild(".project");
            if (projectFile == null)
                return Release.DEFAULT;

            InputStream content = projectFile.getContent().getInputStream();
            JsonNode node = new ObjectMapper().readTree(content);
            Release release = null;
            if (node != null && node.hasNonNull(attribute))
                release = Release.lookup(node.get(attribute).textValue());

            return release != null ? release : Release.DEFAULT;
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return Release.DEFAULT;
    }
}
