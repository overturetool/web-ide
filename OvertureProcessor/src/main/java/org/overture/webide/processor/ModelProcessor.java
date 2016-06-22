package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.VDMJ;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import java.io.File;
import java.util.List;

public class ModelProcessor {
    private List<File> files;
    private Release release;

    public ModelProcessor() {

    }

    public ProcessingResult process() {
        Settings.dialect = Dialect.VDM_SL;
        Settings.release = this.release;

        TypeCheckResult<List<AModuleModules>> typeCheckerResult = TypeCheckerUtil.typeCheckSl(this.files, VDMJ.filecharset);
        ParserResult<List<AModuleModules>> parserResult = typeCheckerResult.parserResult;

        ProcessingResult result = new ProcessingResult();

//        result.parserWarnings = parserResult.warnings;
//        result.parserErrors = parserResult.errors;
//        result.typeCheckerWarnings = typeCheckerResult.warnings;
//        result.typeCheckerErrors = typeCheckerResult.errors;
//        result.modules = typeCheckerResult.result != null ? typeCheckerResult.result : parserResult.result;

        return result;
    }

//    private Release getRelease(IVFS<FileObject> file) {
//        try {
//            String attribute = "release";
//            FileObject projectRoot = file.getProjectRoot();
//            if (projectRoot == null)
//                return Release.DEFAULT;
//
//            FileObject projectFile = projectRoot.getChild(".project");
//            if (projectFile == null)
//                return Release.DEFAULT;
//
//            InputStream content = projectFile.getContent().getInputStream();
//            JsonNode node = new ObjectMapper().readTree(content);
//            Release release = null;
//            if (node != null && node.hasNonNull(attribute))
//                release = Release.lookup(node.get(attribute).textValue());
//
//            return release != null ? release : Release.DEFAULT;
//        } catch (IOException e) {
//            //e.printStackTrace();
//        }
//        return Release.DEFAULT;
//    }
}
