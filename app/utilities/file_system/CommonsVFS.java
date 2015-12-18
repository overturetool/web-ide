package utilities.file_system;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import play.libs.Json;

import java.io.*;
import java.util.*;

public class CommonsVFS implements IVFS {
    @Override
    public boolean appendFile(String path, String content) {
        StandardFileSystemManager fsManager;
        PrintWriter pw = null;
        OutputStream out;

        try {
            fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            // if the file does not exist, this method creates it, and the parent folder, if necessary
            // if the file does exist, it appends whatever is written to the output stream
            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(content);
            pw.flush();

            fileObject.close();
            fsManager.close();
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }

        return false;
    }

    @Override
    public String readFile(String path) {
        String result = "";

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            if (!fileObject.exists())
                return null;

            InputStream inputStream = fileObject.getContent().getInputStream();
            result = IOUtils.toString(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<ObjectNode> readdir(String path, int depth) {
        List<FileObject> fileObjects = new ArrayList<>();
        List<ObjectNode> jsonList = new ArrayList<>();

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            if (fileObject.getType() == FileType.FOLDER) {
                Queue<FileObject> dirs = new LinkedList<>();
                Collections.addAll(dirs, fileObject.getChildren());

                int deptCount = 0;
                while (!dirs.isEmpty()) {
                    FileObject fo = dirs.remove();
                    fileObjects.add(fo);
                    if (fo.getType() == FileType.FOLDER && depth > deptCount) {
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

        return jsonList;
    }

    @Override
    public boolean writeFile(String path, String content) {
        StandardFileSystemManager fsManager;
        PrintWriter pw = null;
        OutputStream out;

        try {
            fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            if (fileObject.exists())
                fileObject.delete();

            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(content);
            pw.flush();

            fileObject.close();
            fsManager.close();
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }

        return false;
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
}
