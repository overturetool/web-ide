package core.codegen;

import core.processing.processes.AbstractProcess;
import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.webide.processing.JavaCodeGenMain;
import org.overture.webide.processing.utils.Arguments;

import java.nio.file.Paths;

public class CodeGenProcessV2 extends AbstractProcess {
    protected CodeGenProcessV2(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        super(Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString(), JavaCodeGenMain.class.getCanonicalName());
        args.add(Arguments.Identifiers.PrintInfo);
        args.add(Arguments.Identifiers.BaseDir);
        args.add(file.getAbsolutePath());
        args.add(modelWrapper.getDialect().getArgstring());
        args.add("-" + modelWrapper.getRelease().toString());
        args.add(file.getAbsolutePath());
    }
}
