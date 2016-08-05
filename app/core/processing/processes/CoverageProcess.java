package core.processing.processes;

import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.webide.processing.CoverageMain;
import org.overture.webide.processing.utils.Arguments;

import java.nio.file.Paths;

public class CoverageProcess extends AbstractProcess {
    public CoverageProcess(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        super(Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString(), CoverageMain.class.getCanonicalName());

        String absolutePath = file.getAbsolutePath();

        args.add(Arguments.Identifiers.BaseDir);
        args.add(absolutePath);

        args.add(Arguments.Identifiers.Release);
        args.add(modelWrapper.getRelease().toString());

        args.add(Arguments.Identifiers.PrintInfo);
        args.add(modelWrapper.getDialect().getArgstring());
        args.add(absolutePath);
    }
}
