package utilities.file_system.commons_vfs2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import play.libs.Json;
import utilities.ServerConfigurations;
import utilities.file_system.FSSchemes;
import utilities.file_system.IVFS;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommonsVFS implements IVFS<FileObject> {
    @Override
    public boolean appendFile(String path, String content) {
        boolean success = false;
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

            success = true;
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        return success;
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
    public List<FileObject> readdir(String path, int depth) {
        List<FileObject> fileObjects = new ArrayList<>();

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String absolutePath = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(absolutePath);

            for (FileObject subFileObject : fileObject.getChildren()) {
                fileObjects.add(subFileObject);

                if (subFileObject.getType() == FileType.FOLDER) {
                    String subPath = subFileObject.getURL().getPath();

                    if (depth == -1)
                        fileObjects.addAll(readdir(subPath, depth));
                    else if (depth > 0) {
                        fileObjects.addAll(readdir(subPath, depth - 1));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileObjects;
    }

    public List<ObjectNode> readDirectoryAsJSONTree(String path, int depth) {
        List<ObjectNode> nodes = new ArrayList<>();

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String absolutePath = FSSchemes.File + "://" + new File(path).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(absolutePath);

            for (FileObject subFileObject : fileObject.getChildren()) {
                ObjectNode jsonObject = Json.newObject();
                jsonObject.put("name", subFileObject.getName().getBaseName());
                jsonObject.put("type", subFileObject.getType() == FileType.FOLDER ? "directory" : "file");
                jsonObject.put("path", path.substring(ServerConfigurations.basePath.length() + 1, path.length()) + "/" + subFileObject.getName().getBaseName());

                if (subFileObject.getType() == FileType.FOLDER) {
                    String subPath = path + "/" + subFileObject.getName().getBaseName();

                    if (depth == -1) {
                        jsonObject.putPOJO("children", readDirectoryAsJSONTree(subPath, -1));
                    }
                    else if (depth > 0) {
                        jsonObject.putPOJO("children", readDirectoryAsJSONTree(subPath, depth - 1));
                    }
                }

                nodes.add(jsonObject);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    @Override
    public boolean writeFile(String path, String content) {
        boolean success = false;
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

            success = true;
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }

        return success;
    }

    public boolean exists(String rel_path) {
        try {
            FileObject fileObject = getFileObject(rel_path);
            return fileObject != null && fileObject.exists();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getExtension(String rel_path) {
        FileObject fileObject = getFileObject(rel_path);

        try {
            if (fileObject != null && fileObject.exists())
                return fileObject.getName().getExtension();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    private FileObject getFileObject(String rel_path) {
        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(rel_path).getAbsolutePath();
            return fsManager.resolveFile(full_path);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }
}
