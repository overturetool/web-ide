package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.outline.OutlineTreeContentProvider;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.util.modules.ModuleList;
import play.mvc.Result;

import java.util.List;

public class outline extends Application {
    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        ModelWrapper modelWrapper = new ModelWrapper(file).init();

        ModuleList ast = modelWrapper.getAst();
        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(ast);
        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list, file.getName());

        return ok(new ObjectMapper().createArrayNode().addAll(jsonList));
    }
}
