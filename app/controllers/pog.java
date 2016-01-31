package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.mappers.PogMapper;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.pog.pub.IProofObligationList;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class pog extends Application {
    public Result generatePog(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper;

        if (file.isDirectory())
            modelWrapper = new ModelWrapper(file.readdirAsIOFile());
        else
            modelWrapper = new ModelWrapper(file.getIOFile());

        IProofObligationList pog = modelWrapper.getPog();
        PogMapper pogMapper = new PogMapper(pog);
        List<ObjectNode> jsonList = pogMapper.toJson();

        return ok(Json.newArray().addAll(jsonList));
    }
}
