package core.wrappers;

import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.interpreter.values.Value;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import play.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ModelWrapper {
    private ModuleInterpreter interpreter;
    private String targetModuleName;

    private static final int MAX_AVAILABLE = 1;
    private static final Semaphore available = new Semaphore(MAX_AVAILABLE, true);

    public ModelWrapper(IVFS<FileObject> file) {
        available.acquireUninterruptibly();
        List<File> files = Collections.synchronizedList(new ArrayList<>());
        files.add(file.getIOFile());

        List<File> siblings = file.getSiblings();
        if (siblings != null && !siblings.isEmpty())
            files.addAll(siblings);
        else if (file.isDirectory())
            files.addAll(file.readdirAsIOFile(-1));

        init(files);
        available.release();
    }

    public ModelWrapper(List<File> files) {
        available.acquireUninterruptibly();
        init(files);
        available.release();
    }

    public synchronized String evaluate(String input) {
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
        if (this.interpreter != null)
            return this.interpreter.getModules();
        else
            return new ModuleList();
    }

    public IProofObligationList getPog() {
        try {
            if (this.interpreter != null)
                return this.interpreter.getProofObligations();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
        return new ProofObligationList();
    }

    private synchronized void init(List<File> files) {
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
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }

        // Safety-net to avoid NullPointerExceptions
        try {
            if (this.interpreter == null) {
                this.interpreter = new ModuleInterpreter(new ModuleList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
