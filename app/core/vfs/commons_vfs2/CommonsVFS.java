package core.vfs.commons_vfs2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.ServerConfigurations;
import core.vfs.FSSchemes;
import core.vfs.CollisionPolicy;
import core.vfs.IVFS;
import core.vfs.FileOperationResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import play.libs.Json;

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
            FileObject fileObject = getFileObject();

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
            FileObject fileObject = getFileObject();

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

    // TODO : unused - needs testing
    private List<FileObject> readdir(String path, int depth) {
        List<FileObject> fileObjects = new ArrayList<>();

        try {
            FileObject fileObject = getFileObject(path);

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
            FileObject fileObject = getFileObject(path);

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
            FileObject fileObject = getFileObject();

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
        try {
            FileObject fileObject = getFileObject();
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
    public String getRelativePath() {
        return relativePath;
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
                if (fo.getType() == FileType.FILE && fo.getName().getExtension().contains("vdm"))
                    children.add(new File(fo.getURL().getPath()));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return children;
    }

    @Override
    public String getName() {
        try {
            return getFileObject().getName().getBaseName();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String moveTo(String destination) {
        return moveTo(destination, CollisionPolicy.Stop);
    }

    public String moveTo(String destination, String collisionPolicy) {
        try {
            FileObject src = getFileObject();
            String newRelativePath = destination + "/" + src.getName().getBaseName();
            FileObject des = getFileObject(newRelativePath);

            if(!src.exists() || des.getParent().getType() != FileType.FOLDER)
                return FileOperationResult.Failure;

            if (des.exists() && !collisionPolicy.equals(CollisionPolicy.Stop)) {
                if (collisionPolicy.equals(CollisionPolicy.Override)) {
                    src.moveTo(des);
                    relativePath = newRelativePath;
                } else if (collisionPolicy.equals(CollisionPolicy.KeepBoth)) {

                    // TODO : Change this
                    String filename = src.getName().getBaseName();

                    if (filename.charAt(0) == '(') {
                        int end = filename.indexOf(')');
                        String num = filename.substring(1, end);
                        int numInc = Integer.parseInt(num) + 1;

                        newRelativePath = destination + "/(" + numInc + ")" + filename.substring(end + 1);
                        des = getFileObject(newRelativePath);
                    }

                    src.moveTo(des);
                    relativePath = newRelativePath;
                }
            } else {
                src.moveTo(des);
                relativePath = newRelativePath;
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            return FileOperationResult.Failure;
        }

        return FileOperationResult.Success;
    }

    private FileObject getFileObject() throws FileSystemException {
        String fullPath = FSSchemes.File + "://" + new File(relativePath).getAbsolutePath();
        return fsManager.resolveFile(fullPath);
    }

    private FileObject getFileObject(String path) throws FileSystemException {
        String fullPath = FSSchemes.File + "://" + new File(path).getAbsolutePath();
        return fsManager.resolveFile(fullPath);
    }
}
