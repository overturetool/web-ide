package core.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.utilities.ResourceCache;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.util.modules.ModuleList;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private static final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(ModelWrapper.class);

    public ModelWrapper(IVFS<FileObject> file) {
        synchronized (lock) {
            if (ResourceCache.getInstance().existsAndNotModified(file)) {
                this.interpreter = ResourceCache.getInstance().get(file).getInterpreter();
            } else {
                List<File> filteredFiles = preprocessFiles(file);
                Release release = getRelease(file);
                if (init(filteredFiles, release))
                    ResourceCache.getInstance().add(file, this.interpreter);
            }
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

    public IProofObligationList getPog() {
        try {
            if (this.interpreter != null && this.interpreter.defaultModule.getTypeChecked())
                return this.interpreter.getProofObligations();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
        return new ProofObligationList();
    }

    private synchronized List<File> preprocessFiles(IVFS<FileObject> file) {
        List<File> files = file.getProjectAsIOFile();
        List<File> filteredFiles = Collections.synchronizedList(new ArrayList<>());
        filteredFiles.addAll(files.stream().filter(f -> f.getName().endsWith(".vdmsl")).collect(Collectors.toList()));
        return filteredFiles;
    }

    private synchronized boolean init(List<File> files, Release release) {
        Settings.dialect = Dialect.VDM_SL;
        Settings.release = release;

        VDMSL vdmsl = new VDMSL();
        vdmsl.setWarnings(false);
        vdmsl.setQuiet(true);
        vdmsl.setCharset(VDMJ.filecharset);

        ExitStatus parseStatus = vdmsl.parse(files);
        if (parseStatus == ExitStatus.EXIT_OK) {
            try {
                this.interpreter = vdmsl.getInterpreter();
            } catch (Exception e) {
                //e.printStackTrace();
            }

            ExitStatus typeCheckStatus;
            try {
                typeCheckStatus = vdmsl.typeCheck();
            } catch (ConcurrentModificationException e) {
                typeCheckStatus = ExitStatus.EXIT_ERRORS;
            }

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.interpreter = vdmsl.getInterpreter();
                    this.interpreter.defaultModule.setTypeChecked(true);
                    this.interpreter.init(null); // Needed for the evaluation part of the REPL
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        init();
        return false;
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
