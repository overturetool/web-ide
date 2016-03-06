package core.wrappers;

import core.utilities.ResourceCache;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import play.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private static final Object lock = new Object();

    public ModelWrapper(IVFS<FileObject> file) {
        synchronized (lock) {
            if (ResourceCache.getInstance().existsAndNotModified(file)) {
                this.interpreter = ResourceCache.getInstance().get(file).getInterpreter();
            } else {
                List<File> files = Collections.synchronizedList(new ArrayList<>());
                files.add(file.getIOFile()); // TODO : should not be done if file is a directory, but overture core takes care of it.

                List<File> siblings = file.getSiblings();
                if (siblings != null && !siblings.isEmpty())
                    files.addAll(siblings);
                else if (file.isDirectory())
                    files.addAll(file.readdirAsIOFile(-1));

                List<File> filteredFiles = Collections.synchronizedList(new ArrayList<>());
                for (File f : files) {
                    boolean keep = f.getName().endsWith(".vdmsl");
                    if (keep)
                        filteredFiles.add(f);
                }

                boolean success = init(filteredFiles);

                if (success)
                    ResourceCache.getInstance().add(file, this.interpreter);
            }
        }
    }

    @Deprecated
    public ModelWrapper(List<File> files) {
        Logger.debug("Here");
        synchronized (lock) {
            init(files);
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

    private synchronized boolean init(List<File> files) {
        boolean success = false;

        // Look into using the VDMJ class instead
        VDMSL vdmsl = new VDMSL();
        vdmsl.setWarnings(false);
        vdmsl.setQuiet(true);

        ExitStatus parseStatus = vdmsl.parse(files);

        if (parseStatus == ExitStatus.EXIT_OK) {
            try {
                this.interpreter = vdmsl.getInterpreter();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ExitStatus typeCheckStatus;
            try {
                typeCheckStatus = vdmsl.typeCheck();
            } catch (ConcurrentModificationException e) {
                typeCheckStatus = ExitStatus.EXIT_ERRORS;
                Logger.error(e.getMessage());
            }

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.interpreter = vdmsl.getInterpreter();
                    this.interpreter.defaultModule.setTypeChecked(true);
                    this.interpreter.init(null);
                    success = true;
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                }
            }
        }

        // Safety-net to avoid NullPointerExceptions
        init();

        return success;
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
}
