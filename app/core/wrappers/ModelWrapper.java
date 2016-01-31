package core.wrappers;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.pog.pub.IProofObligationList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelWrapper {
    private VDMSL vdmsl;
    private ModuleInterpreter interpreter;

    public ModelWrapper(File file) {
        List<File> files = new ArrayList<>();
        files.add(file);
        init(files);
    }

    public ModelWrapper(List<File> files) {
        init(files);
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

    private void init(List<File> files) {
        // Look into using the VDMJ class instead
        this.vdmsl = new VDMSL();
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
    }
}
