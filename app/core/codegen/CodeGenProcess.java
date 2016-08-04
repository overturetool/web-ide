package core.codegen;

import core.processing.processes.AbstractProcess;
import core.vfs.IVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.lex.Dialect;
import org.overture.codegen.vdm2java.JavaCodeGenMain;
import org.overture.config.Release;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public class ProcessStream extends Thread {
        private BufferedReader bufferedReader;
        private List<String> list = new ArrayList<>();

        public ProcessStream(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        public List<String> getList() {
            return this.list;
        }

        @Override
        public void run() {
            String input;
            try {
                while ((input = bufferedReader.readLine()) != null) {
                    this.list.add(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
