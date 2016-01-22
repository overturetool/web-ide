package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.outline.OutlineTreeContentProvider;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

public class outline extends Application {
    public Result file(String account, String path) {
        IVFS file = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(file);
        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list, file.getName());

        return ok(Json.newArray().addAll(jsonList));
    }
}
