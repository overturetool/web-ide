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
    private StandardFileSystemManager fsManager;
    private String relativePath;

    public CommonsVFS(String path) {
        relativePath = path;
        fsManager = new StandardFileSystemManager();

        try {
            fsManager.init();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean appendFile(String content) {
        boolean success = false;
        PrintWriter pw = null;
        OutputStream out;

        try {
            String full_path = FSSchemes.File + "://" + new File(relativePath).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            // if the file does not exist, this method creates it, and the parent folder, if necessary
            // if the file does exist, it appends whatever is written to the output stream
            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(content);
            pw.flush();

            fileObject.close();

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
    public String readFile() {
        String result = "";

        try {
            String full_path = FSSchemes.File + "://" + new File(relativePath).getAbsolutePath();
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
    public List<FileObject> readdir(int depth) {
        return readdir(relativePath, depth);
    }

    private List<FileObject> readdir(String path, int depth) {
        List<FileObject> fileObjects = new ArrayList<>();

        try {
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

    @Override
    public List<ObjectNode> readdirAsJSONTree(int depth) {
        return readdirAsJSONTree(relativePath, depth);
    }

    private List<ObjectNode> readdirAsJSONTree(String path, int depth) {
        List<ObjectNode> nodes = new ArrayList<>();

        String filteredPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        try {
            String absolutePath = FSSchemes.File + "://" + new File(filteredPath).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(absolutePath);

            for (FileObject subFileObject : fileObject.getChildren()) {
                ObjectNode jsonObject = Json.newObject();
                jsonObject.put("name", subFileObject.getName().getBaseName());
                jsonObject.put("type", subFileObject.getType() == FileType.FOLDER ? "directory" : "file");
                jsonObject.put("path", filteredPath.substring(ServerConfigurations.basePath.length() + 1, filteredPath.length()) + "/" + subFileObject.getName().getBaseName());

                if (subFileObject.getType() == FileType.FOLDER) {
                    String subPath = filteredPath + "/" + subFileObject.getName().getBaseName();

                    if (depth == -1) {
                        jsonObject.putPOJO("children", readdirAsJSONTree(subPath, -1));
                    }
                    else if (depth > 0) {
                        jsonObject.putPOJO("children", readdirAsJSONTree(subPath, depth - 1));
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
    public boolean writeFile(String content) {
        boolean success = false;
        PrintWriter pw = null;
        OutputStream out;

        try {
            String full_path = FSSchemes.File + "://" + new File(relativePath).getAbsolutePath();
            FileObject fileObject = fsManager.resolveFile(full_path);

            if (fileObject.exists())
                fileObject.delete();

            out = fileObject.getContent().getOutputStream(true);

            pw = new PrintWriter(out);
            pw.write(content);
            pw.flush();

            fileObject.close();

            success = true;
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
        }

        return success;
    }

    @Override
    public boolean exists() {
        try {
            FileObject fileObject = getFileObject();
            return fileObject != null && fileObject.exists();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getExtension() {
        FileObject fileObject = getFileObject();

        try {
            if (fileObject != null && fileObject.exists())
                return fileObject.getName().getExtension();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public File getIOFile() {
        return new File(relativePath);
    }

    @Override
    public boolean isDirectory() {
        try {
            return getFileObject().getType() == FileType.FOLDER;
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getAbsolutePath() {
        try {
            return getFileObject().getURL().toString();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<File> getSiblings() {
        FileObject parent = null;
        List<File> children = new ArrayList<>();

        try {
            parent = getFileObject().getParent();

            if (parent == null)
                return null;

            for (FileObject fo : parent.getChildren()) {
                if (fo.getType() == FileType.FILE)
                    children.add(new File(fo.getURL().getPath()));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return children;
    }

    @Override
    public String getName() {
        return getFileObject().getName().getBaseName();
    }

    private FileObject getFileObject() {
        try {
            String fullPath = FSSchemes.File + "://" + new File(relativePath).getAbsolutePath();
            return fsManager.resolveFile(fullPath);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }
}
