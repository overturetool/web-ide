package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http;
import play.mvc.Result;
import utilities.file_system.CommonsVFS;

import java.util.List;

public class vfs extends Application {
    private final String basePath = "workspace";

    public Result appendFile(String account, String absPath) {
        Http.RequestBody body = request().body();
        String path = basePath + "/" + account + "/" + absPath;
        CommonsVFS fileSystem = new CommonsVFS();
        fileSystem.appendFile(path, body.asText());
        return ok();
    }

    public Result readFile(String account, String absPath) {
        String path = basePath + "/" + account + "/" + absPath;
        CommonsVFS fileSystem = new CommonsVFS();
        String result = fileSystem.readFile(path);
        return ok(result);
    }

    public Result readdir(String path, String depth) {
        int dirDepth = Integer.parseInt(depth);
        String full_path = basePath + "/" + path;
        CommonsVFS fileSystem = new CommonsVFS();
        List<ObjectNode> jsonList = fileSystem.readdir(full_path, dirDepth);
        return ok(jsonList.toString());
    }

    public Result writeFile(String account, String absPath) {
        Http.RequestBody body = request().body();
        String path = basePath + "/" + account + "/" + absPath;
        CommonsVFS fileSystem = new CommonsVFS();
        fileSystem.writeFile(path, body.asText());
        return ok();
    }
}
