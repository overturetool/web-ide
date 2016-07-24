package core.codegen;

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

public class CodeGenProcess {
    public int init(IVFS<FileObject> file, ModelWrapper modelWrapper) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String className = JavaCodeGenMain.class.getCanonicalName();
        String classPath = Paths.get("lib", "codegen", "*").toString();

        List<String> args = new ArrayList<>();
        args.add(javaBin);
        args.add("-cp");
        args.add(classPath);
        args.add(className);

        // Program arguments
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

        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            Process process = builder.start();

            ProcessStream processInputStream = new ProcessStream(process.getInputStream());
            ProcessStream processErrorStream = new ProcessStream(process.getErrorStream());
            processInputStream.start();
            processErrorStream.start();

            process.waitFor();

            List<String> inputList = processInputStream.getList();
            List<String> errorList = processErrorStream.getList();

            return process.exitValue();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
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
