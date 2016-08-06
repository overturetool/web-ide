package org.overture.webide.processing;

import org.overture.webide.processing.features.LatexGenerator.LatexUtils;

public class CoverageMain extends ProcessingMain {
    static {
        instanceClass = CoverageMain.class;
    }

    @Override
    public void execute() throws Exception {
        LatexUtils latexUtils = new LatexUtils();
        latexUtils.makeLatex(baseDirFile, fileList);
    }

//    @Override
//    public void execute() throws Exception {
//        run();
//        for (File f : fileList) {
//            String filename = f.getName().replaceAll(".(vdmsl|vdmpp|vdmrt)", "").concat(".tex");
//            CodeGenBase.emitCode(new File(baseDir, "generated/latex"), filename, write(f).toString());
//        }
//    }
//
//    private StringWriter write(File file) {
//        StringWriter writer = new StringWriter();
//        try {
//            SourceFile sourceFile = new SourceFile(file);
//            PrintWriter printWriter = new PrintWriter(writer);
//            sourceFile.printCoverage(printWriter);
//            LatexSourceFile latex = new LatexSourceFile(sourceFile);
//            latex.print(printWriter, true, true, true, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return writer;
//    }
}
