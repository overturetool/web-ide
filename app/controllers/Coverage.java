package controllers;

import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.interpreter.runtime.LatexSourceFile;
import org.overture.interpreter.runtime.SourceFile;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Coverage extends Application {
    public Result coverage(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        String coverage = "/Users/kaspersaaby/Documents/projects/iha/playframework/overture_webide/workspace/111425625270532893915/BOMSL/generated/coverage.txt";
        File des = new File("workspace/111425625270532893915/BOMSL/generated/coverage.txt");
        StringWriter writer = new StringWriter();
        try {
            SourceFile sourceFile = new SourceFile(file.getIOFile());
            PrintWriter printWriter = new PrintWriter(writer);
            sourceFile.writeCoverage(printWriter);
            LatexSourceFile latex = new LatexSourceFile(sourceFile);
            latex.print(printWriter, true, true, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(writer);

        return ok();
    }
}
