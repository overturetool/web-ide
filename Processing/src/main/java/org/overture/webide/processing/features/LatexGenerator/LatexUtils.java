package org.overture.webide.processing.features.LatexGenerator;

import org.overture.ast.lex.Dialect;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.runtime.LatexSourceFile;
import org.overture.parser.lex.LexTokenReader;
import org.overture.parser.syntax.ModuleReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

public class LatexUtils {
    public void makeLatex(File baseDirFile, List<File> fileList) throws IOException {
        File outputDirForGeneratedModelFiles = Paths.get(baseDirFile.getAbsolutePath(), "generated", "latex", "specification").toFile();
        if (!outputDirForGeneratedModelFiles.exists())
            outputDirForGeneratedModelFiles.mkdirs();

        ModuleList modules = parseModules(fileList);
        LatexBuilder latexBuilder = new LatexBuilder();
        LexLocation.resetLocations();

        for (AModuleModules classDefinition : modules) {
            for (File moduleFile : classDefinition.getFiles()) {
                createCoverage(latexBuilder, outputDirForGeneratedModelFiles, fileList, moduleFile, true, true, true);
            }
        }

        latexBuilder.saveDocument(baseDirFile, baseDirFile.getName());
    }

    private ModuleList parseModules(List<File> fileList) {
        ModuleReader reader;
        ModuleList modules = new ModuleList();
        for (File source : fileList) {
            LexTokenReader ltr = new LexTokenReader(source, Dialect.VDM_SL, VDMJ.filecharset);
            reader = new ModuleReader(ltr);
            modules.addAll(reader.readModules());
        }
        return modules;
    }

    private void createCoverage(LatexBuilder latexBuilder, File outputFolderForGeneratedModelFiles, List<File> outputFiles, File moduleFile,
                                boolean modelOnly, boolean markCoverage, boolean includeCoverageTable) throws IOException {
        File texFile = new File(outputFolderForGeneratedModelFiles, moduleFile.getName().replace(".vdmsl", "") + ".tex");
        if (!texFile.exists())
            texFile.createNewFile();

        if (markCoverage || includeCoverageTable) {
            for (File file : outputFiles) {
                if (file.getName().toLowerCase().endsWith(".covtbl") && moduleFile.getName().equals(getFileName(file))) {
                    LexLocation.mergeHits(moduleFile, file);
                    outputFiles.remove(file);
                }
            }
        }

        String charset = VDMJ.filecharset;
        latexBuilder.addInclude(texFile.getAbsolutePath());

        PrintWriter pw = new PrintWriter(texFile, charset);

        //new LatexSourceFile(moduleFile, charset).printCoverage(pw, false, modelOnly, true);
        LatexSourceFile latexSourceFile = new LatexSourceFile(moduleFile, charset);
        latexSourceFile.print(pw, false, modelOnly, false, false);

        pw.close();
    }

    public String getFileName(File file) {
        int index = file.getName().lastIndexOf('.');
        return file.getName().substring(0, index);
    }
}
