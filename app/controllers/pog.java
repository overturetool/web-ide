package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.mappers.PogMapper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import play.mvc.Result;

import java.util.List;

public class pog extends Application {
    public Result generatePog(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file).init();

        IProofObligationList pog = new ProofObligationList();
        try {
            pog = modelWrapper.getPog();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }

        PogMapper pogMapper = new PogMapper(pog);
        List<ObjectNode> jsonList = pogMapper.toJson();

        return ok(new ObjectMapper().createArrayNode().addAll(jsonList));
    }
}
