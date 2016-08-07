package core.codegen;

import core.processing.processes.AbstractProcess;
import core.processing.processes.utils.ProcessStream;
import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.lex.Dialect;
import org.overture.codegen.vdm2java.JavaCodeGenMain;
import org.overture.config.Release;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class CodeGenProcess extends AbstractProcess {
    public CodeGenProcess(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        super(Paths.get("lib", "*").toString(), JavaCodeGenMain.class.getCanonicalName());

        String path = file.getAbsolutePath();
        String rootPackage = Paths.get(file.getRelativePath()).getName(0).toString();
        Dialect dialect = modelWrapper.getDialect();
        Release release = modelWrapper.getRelease();

        args.add(dialect.getArgstring().replace("vdm", ""));
        args.add("-" + release.toString());

        args.add("-package");
        args.add(rootPackage);

        args.add("-folder");
        args.add(path);

        args.add("-output");
        args.add(Paths.get(path, "generated").toString());
    }

    public Process start() {
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            Process process = builder.start();

            ProcessStream processInputStream = new ProcessStream(process.getInputStream());
            ProcessStream processErrorStream = new ProcessStream(process.getErrorStream());
            processInputStream.start();
            processErrorStream.start();

            process.waitFor();

            // TODO : use lists information
            List<String> inputList = processInputStream.getList();
            List<String> errorList = processErrorStream.getList();

            return process;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
