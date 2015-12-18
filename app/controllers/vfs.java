package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
import java.util.*;

public class vfs extends Application {

    public Result appendFile(String account, String absPath) {
        Http.RequestBody body = request().body();

        StandardFileSystemManager fsManager;
        PrintWriter pw = null;
        OutputStream out;

        String default_ws = "workspace";

        try {
            fsManager = new StandardFileSystemManager();
            fsManager.init();

            File file = new File(default_ws);
            String path = "file://" + file.getAbsolutePath() + "/" + account + "/" + absPath;

            FileObject fileObject = fsManager.resolveFile(path);

            // if the file does not exist, this method creates it, and the parent folder, if necessary
            // if the file does exist, it appends whatever is written to the output stream
            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(body.asText());
            pw.flush();

            fileObject.close();
            fsManager.close();
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }

        return ok();
    }

    public Result readFile(String account, String absPath) {
        String default_ws = "workspace";
        String result = "";

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            File file = new File(default_ws);
            String path = "file://" + file.getAbsolutePath() + "/" + account + "/" + absPath;

            FileObject fileObject = fsManager.resolveFile(path);

            if (!fileObject.exists())
                return ok();

            InputStream inputStream = fileObject.getContent().getInputStream();
            result = IOUtils.toString(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok(result);
    }

    private ObjectNode mapToJson(FileObject fileObject) throws FileSystemException {
        ObjectNode jsonObject = Json.newObject();

        long size = 0;
        if (fileObject.getType() == FileType.FILE)
            size = fileObject.getContent().getSize();
        else
            size = fileObject.getChildren().length;

        jsonObject.put("url", fileObject.getURL().toString());
        jsonObject.put("name", fileObject.getName().getBaseName());
        jsonObject.put("extension", fileObject.getName().getExtension());
        jsonObject.put("friendly_uri", fileObject.getName().getFriendlyURI());
        jsonObject.put("size", size);
        jsonObject.put("type", fileObject.getType().toString());

        return jsonObject;
    }

    public Result readdir(String account, String absPath, String dept) {
        int dirDept = Integer.parseInt(dept);
        String default_ws = "workspace";
        List<FileObject> fileObjects = new ArrayList<FileObject>();

        List<ObjectNode> jsonList = new ArrayList<>();

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            File file = new File(default_ws);
            String path = "file://" + file.getAbsolutePath() + "/" + account + "/" + absPath;

            FileObject fileObject = fsManager.resolveFile(path);

            if (fileObject.getType() == FileType.FOLDER) {
                fileObjects.add(fileObject);

                Queue<FileObject> dirs = new LinkedList<>();
                Collections.addAll(dirs, fileObject.getChildren());

                int deptCount = 0;
                while (!dirs.isEmpty()) {
                    FileObject fo = dirs.remove();
                    fileObjects.add(fo);
                    if (fo.getType() == FileType.FOLDER && dirDept > deptCount) {
                        deptCount++;
                        Collections.addAll(dirs, fo.getChildren());
                    }
                }
            }

            for (FileObject fo : fileObjects) {
                jsonList.add(mapToJson(fo));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok(jsonList.toString());
    }

    public Result writeFile(String account, String absPath) {
        Http.RequestBody body = request().body();

        StandardFileSystemManager fsManager;
        PrintWriter pw = null;
        OutputStream out;

        String default_ws = "workspace";

        try {
            fsManager = new StandardFileSystemManager();
            fsManager.init();

            File file = new File(default_ws);
            String path = "file://" + file.getAbsolutePath() + "/" + account + "/" + absPath;

            FileObject fileObject = fsManager.resolveFile(path);

            if (fileObject.exists())
                fileObject.delete();

            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(body.asText());
            pw.flush();

            fileObject.close();
            fsManager.close();
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }


        return ok();
    }
}
