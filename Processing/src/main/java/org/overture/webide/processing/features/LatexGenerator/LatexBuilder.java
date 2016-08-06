package org.overture.webide.processing.features.LatexGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

public class LatexBuilder {
    private List<String> includes = new Vector<>();
    private final String PROJECT_INCLUDE_MODEL_FILES = "%PROJECT_INCLUDE_MODEL_FILES";
    private final String TITLE = "%TITLE";
    private String document =
            "\\documentclass{article}\n" +
            "\\usepackage{fullpage}\n" +
            "\\usepackage[color]{vdmlisting}\n" +
            "\\usepackage[hidelinks]{hyperref}\n" +
            "\\usepackage{longtable}\n" +
            "\\begin{document}\n" +
            "\\title{%TITLE}\n" +
            "\\author{}\n" +
            "\\maketitle\n" +
            "\\tableofcontents\n" +
            "%PROJECT_INCLUDE_MODEL_FILES" +
            "\\end{document}\n";

    public void saveDocument(File projectRoot, String name) throws IOException {
        File latexRoot = Paths.get(projectRoot.getPath(), "generated", "latex").toFile();

        if (!latexRoot.exists())
            latexRoot.mkdirs();

        StringBuilder sb = new StringBuilder();
        String title = projectRoot.getName().replace('\\', '/').substring(0, projectRoot.getName().length());

        for (String path : includes) {
            String includeName = path;
            includeName = includeName.substring(0, includeName.lastIndexOf('.'));
            String tmp = includeName.replace('\\', '/');
            includeName = tmp.substring(tmp.lastIndexOf('/') + 1);

            sb.append("\n" + "\\section{" + latexQuote(includeName) + "}");

            if (path.contains(latexRoot.getAbsolutePath())) {
                path = path.substring(latexRoot.getAbsolutePath().length());
                sb.append("\n" + "\\input{" + path.replace('\\', '/').substring(1, path.length()) + "}");
            } else {
                sb.append("\n" + "\\input{" + path.replace('\\', '/') + "}");
            }
        }

        document = document.replace(TITLE, latexQuote(title)).replace(PROJECT_INCLUDE_MODEL_FILES, sb.toString());
        writeFile(latexRoot, name + ".tex", document);
    }

    private String latexQuote(String s) {
        // Latex specials: \# \$ \% \^{} \& \_ \{ \} \~{} \\
        return s.replace("\\", "\\textbackslash ")
                .replace("#", "\\#")
                .replace("$", "\\$")
                .replace("%", "\\%")
                .replace("&", "\\&")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\~")
                .replaceAll("\\^{1}", "\\\\^{}");
    }

    private void writeFile(File outputFolder, String fileName, String content) throws IOException {
        File file = new File(outputFolder, fileName);

        if (!file.exists())
            file.createNewFile();

        FileWriter outputFileReader = new FileWriter(new File(outputFolder, fileName), false);
        BufferedWriter outputStream = new BufferedWriter(outputFileReader);
        outputStream.write(content);
        outputStream.close();
        outputFileReader.close();
    }

    public void addInclude(String path) {
        if (!includes.contains(path)) {
            includes.add(path);
        }
    }
}
