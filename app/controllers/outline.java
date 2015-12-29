package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Result;
import core.ServerConfigurations;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.outline.OutlineTreeContentProvider;

import java.util.List;

public class outline extends Application {
    public Result file(String account, String path) {
        IVFS file = new CommonsVFS(ServerConfigurations.basePath + "/" + account + "/" + path);

        if (!file.exists())
            return ok();

        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(file);
        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list, file.getName());

        return ok(jsonList.toString());
    }
}
