package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http;
import play.mvc.Result;
import utilities.ServerConfigurations;
import utilities.file_system.commons_vfs2.CommonsVFS;

import java.util.List;

public class vfs extends Application {
    public Result appendFile(String account, String path) {
        Http.RequestBody body = request().body();
        String relativePath = ServerConfigurations.basePath + "/" + account + "/" + path;

        CommonsVFS fileSystem = new CommonsVFS(relativePath);
        boolean success = fileSystem.appendFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }

    public Result readFile(String account, String path) {
        String relativePath = ServerConfigurations.basePath + "/" + account + "/" + path;

        CommonsVFS fileSystem = new CommonsVFS(relativePath);
        String result = fileSystem.readFile();

        return ok(result);
    }

    public Result readdir(String path, String depth) {
        int depthInt = Integer.parseInt(depth);
        String relativePath = ServerConfigurations.basePath + "/" + path;

        CommonsVFS fileSystem = new CommonsVFS(relativePath);
        List<ObjectNode> fileObjects = fileSystem.readdirAsJSONTree(depthInt);

        return ok(fileObjects.toString());
    }

    public Result writeFile(String account, String path) {
        Http.RequestBody body = request().body();
        String relativePath = ServerConfigurations.basePath + "/" + account + "/" + path;

        CommonsVFS fileSystem = new CommonsVFS(relativePath);
        boolean success = fileSystem.writeFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }
}
