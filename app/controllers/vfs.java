package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http;
import play.mvc.Result;
import utilities.ServerConfigurations;
import utilities.file_system.commons_vfs2.CommonsVFS;

import java.util.List;

public class vfs extends Application {
    public Result appendFile(String account, String absPath) {
        Http.RequestBody body = request().body();
        String path = ServerConfigurations.basePath + "/" + account + "/" + absPath;

        CommonsVFS fileSystem = new CommonsVFS(path);
        boolean success = fileSystem.appendFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }

    public Result readFile(String account, String absPath) {
        String path = ServerConfigurations.basePath + "/" + account + "/" + absPath;

        CommonsVFS fileSystem = new CommonsVFS(path);
        String result = fileSystem.readFile();

        return ok(result);
    }

    public Result readdir(String path, String depth) {
        int dirDepth = Integer.parseInt(depth);
        String full_path = ServerConfigurations.basePath + "/" + path;

        CommonsVFS fileSystem = new CommonsVFS(full_path);
        List<ObjectNode> fileObjects = fileSystem.readdirAsJSONTree(dirDepth);

        return ok(fileObjects.toString());
    }

    public Result writeFile(String account, String absPath) {
        Http.RequestBody body = request().body();
        String path = ServerConfigurations.basePath + "/" + account + "/" + absPath;

        CommonsVFS fileSystem = new CommonsVFS(path);
        boolean success = fileSystem.writeFile(body.asText());

        if (!success)
            return status(422);

        return ok();
    }
}
