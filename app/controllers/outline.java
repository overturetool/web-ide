package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.outline.OutlineTreeContentProvider;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.vfs2.FileObject;
import play.libs.Json;
import play.mvc.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class outline extends Application {
    public Result file(String account, String path) {
        IVFS<FileObject> file = new CommonsVFS(PathHelper.JoinPath(account, path));
        List<File> files = new ArrayList<>();

        if (!file.exists())
            return status(StatusCode.UnprocessableEntity, "File not found");

        if (file.isDirectory())
            files.addAll(file.readdirAsIOFile());
        else
            files.add(file.getIOFile());

        ModelWrapper modelWrapper = new ModelWrapper(files);

        OutlineTreeContentProvider outlineProvider = new OutlineTreeContentProvider(modelWrapper.getAst());
        List<Object> list = outlineProvider.getContent();
        List<ObjectNode> jsonList = outlineProvider.toJSON(list, file.getName());

        return ok(Json.newArray().addAll(jsonList));
    }
}
