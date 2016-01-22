package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.apache.commons.vfs2.FileObject;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;

public class vfs extends Application {
    public Result appendFile(String account, String path) {
        Http.RequestBody body = request().body();

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        boolean success = vfs.appendFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }

    public Result readFile(String account, String path) {
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        String result = vfs.readFile();
        return ok(result);
    }

    public Result readdir(String path, String depth) {
        int depthInt = Integer.parseInt(depth);

        IVFS<FileObject> vfs = new CommonsVFS(PathHelper.JoinPath(path));
        List<ObjectNode> fileObjects = vfs.readdirAsJSONTree(depthInt);

        return ok(Json.newArray().addAll(fileObjects));
    }

    public Result writeFile(String account, String path) {
        Http.RequestBody body = request().body();

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        boolean success = vfs.writeFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }
}
