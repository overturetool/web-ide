package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import core.utilities.PathHelper;
import core.vfs.CollisionPolicy;
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

    public Result move(String account, String path) {
        JsonNode request = request().body().asJson();

        if (request == null)
            return status(StatusCode.UnprocessableEntity, "Request body empty or not valid JSON");

        JsonNode destination = request.get("destination");
        JsonNode collisionPolicy = request.get("collisionPolicy");

        if (destination == null)
            return status(StatusCode.UnprocessableEntity, "missing destination");

        if (collisionPolicy == null)
            collisionPolicy = Json.newObject().textNode(CollisionPolicy.KeepBoth);

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        String result = vfs.move(PathHelper.JoinPath(destination.asText()), collisionPolicy.asText());

        if (result == null)
            return status(StatusCode.UnprocessableEntity, "File operation failed");

        return ok(result);
    }

    public Result delete(String account, String path) {
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!vfs.delete())
            return status(StatusCode.UnprocessableEntity, "An error occurred while delete file");

        return ok();
    }

    public Result rename(String account, String path, String name) {
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!vfs.rename(name))
            return status(StatusCode.UnprocessableEntity, "An error occurred while renaming file");

        return ok();
    }

    public Result mkdir(String account, String path) {
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!vfs.mkdir())
            return status(StatusCode.UnprocessableEntity, "An error occurred while creating directory");

        return ok();
    }

    public Result mkFile(String account, String path) {
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        if (!vfs.mkFile())
            return status(StatusCode.UnprocessableEntity, "An error occurred while creating file");

        return ok();
    }
}
