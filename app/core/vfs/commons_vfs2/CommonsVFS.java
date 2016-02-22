package core.vfs.commons_vfs2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.ServerConfigurations;
import core.vfs.CollisionPolicy;
import core.vfs.IVFS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import play.Logger;
import play.libs.Json;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonsVFS implements IVFS<FileObject> {
    private FileObject vfs;
    private String account;
    private String relativePath;
    private FileObject baseFileObject;

    public CommonsVFS(String account, String path) {
        StandardFileSystemManager fsManager = new StandardFileSystemManager();

        this.account = account;
        this.relativePath = path;
        String basePath = ServerConfigurations.basePath;

        if (account != null)
            basePath += File.separator + account;

        try {
            fsManager.init();
            fsManager.setBaseFile(new File(basePath));
            this.baseFileObject = fsManager.getBaseFile();
            this.vfs = fsManager.createVirtualFileSystem(this.baseFileObject);
        } catch (FileSystemException e) {
            Logger.error(e.getMessage(), e);
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
    public List<FileObject> readdir() {
        return readdir(0);
    }

    @Override
    public List<FileObject> readdir(int depth) {
        return readdir(relativePath, depth);
    }

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
                    else if (depth > 0)
                        fileObjects.addAll(readdir(subPath, depth - 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileObjects;
    }

    @Override
    public List<File> readdirAsIOFile() {
        return readdirAsIOFile(0);
    }

    @Override
    public List<File> readdirAsIOFile(int depth) {
        List<File> files = Collections.synchronizedList(new ArrayList<>());

        if (isDirectory()) {
            List<FileObject> fileObjects = readdir(depth);
            try {
                for (FileObject fo : fileObjects) {
                    if (fo.getType() == FileType.FILE)
                        files.add(new File(toAbsolutePath(fo.getName().getPath())));
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        } else {
            files.add(getIOFile());
        }

        return files;
    }

    @Override
    public List<ObjectNode> readdirAsJSONTree() {
        return readdirAsJSONTree(0);
    }

    @Override
    public List<ObjectNode> readdirAsJSONTree(int depth) {
        return readdirAsJSONTree(this.relativePath, depth);
    }

    private List<ObjectNode> readdirAsJSONTree(String path, int depth) {
        List<ObjectNode> nodes = new ArrayList<>();

        String filteredPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        try {
            FileObject fileObject = getFileObject(path);

            for (FileObject subFileObject : fileObject.getChildren()) {
                ObjectNode jsonObject = Json.newObject();

                String name = subFileObject.getName().getBaseName();
                boolean isFolder = subFileObject.getType() == FileType.FOLDER;
                String subPath = filteredPath + "/" + name;

                jsonObject.put("name", name);
                jsonObject.put("type", isFolder ? "directory" : "file");
                jsonObject.put("path", this.account + subPath); // TODO : Maybe omit account name

                if (isFolder) {
                    if (depth == -1)
                        jsonObject.putPOJO("children", readdirAsJSONTree(subPath, -1));
                    else if (depth > 0)
                        jsonObject.putPOJO("children", readdirAsJSONTree(subPath, depth - 1));
                }

                nodes.add(jsonObject);
            }
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
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
        return new File(getAbsolutePath());
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
        return this.relativePath;
    }

    @Override
    public String getAbsoluteUrl() {
        try {
            URI baseUri = this.baseFileObject.getURL().toURI();
            return baseUri + "/" + this.relativePath;
        } catch (URISyntaxException | FileSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getAbsolutePath() {
        return toAbsolutePath(this.relativePath);
    }

    private String toAbsolutePath(String relativePath) {
        return this.baseFileObject.getName().getPath() + "/" + relativePath;
    }

    @Override
    public List<File> getSiblings() {
        if (isDirectory())
            return null;

        IVFS<FileObject> parent = getParent();

        if (parent == null)
            return null;

        List<File> children = parent.readdirAsIOFile(-1);

        // Remove 'this' from children
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getName().equals(getName()))
                children.remove(i);
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

    @Override
    public String move(String destination) {
        return move(destination, CollisionPolicy.Stop);
    }

    @Override
    public String move(String destination, String collisionPolicy) {
        String filename;
        String newRelativePath;

        int lastIndexOfSlash = destination.lastIndexOf(File.separator);
        if (lastIndexOfSlash != -1) {
            filename = destination.substring(lastIndexOfSlash + 1);
            destination = destination.substring(0, lastIndexOfSlash);
            newRelativePath = destination + File.separator + filename;
        } else {
            filename = destination;
            newRelativePath = filename;
        }

        try {
            FileObject src = getFileObject();
            FileObject des = getFileObject(newRelativePath);

            if (src == des)
                return null;

            if(!src.exists() || des.getParent().getType() != FileType.FOLDER)
                return null;

            // A file with the same name exists - handle collision according to policy
            if (des.exists()) {
                newRelativePath = handleCollision(src, des, collisionPolicy);

                if (newRelativePath == null)
                    return null;
            }

            src.moveTo(getFileObject(newRelativePath));
            this.relativePath = newRelativePath;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }

        return filename;
    }

    @Override
    public boolean delete() {
        try {
            return getFileObject().delete(Selectors.SELECT_ALL) > 0;
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean rename(String name) {
        try {
            FileObject src = getFileObject();
            FileObject des = getFileObject(src.getName().getParent().getPath() + File.separator + name);

            if (!src.canRenameTo(des))
                return false;

            des.copyFrom(src, Selectors.SELECT_ALL);
            src.delete(Selectors.SELECT_ALL);

        } catch (FileSystemException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public String mkFile() {
        FileObject newFile;

        try {
            FileObject des = getFileObject();
            String path = des.getName().getPath();

            if (des.exists())
                path = handleCollision(des, des, CollisionPolicy.KeepBoth);

            if (path == null)
                return null;

            newFile = getFileObject(path);
            newFile.createFile();
            this.relativePath = path;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }

        return newFile.getName().getBaseName();
    }

    @Override
    public String mkdir() {
        FileObject newdir;

        try {
            FileObject des = getFileObject();
            String path = des.getName().getPath();

            if (des.exists())
                path = handleCollision(des, des, CollisionPolicy.KeepBoth);

            if (path == null)
                return null;

            newdir = getFileObject(path);
            newdir.createFolder();
            this.relativePath = path;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }

        return newdir.getName().getBaseName();
    }

    @Override
    public IVFS<FileObject> getParent() {
        try {
            return new CommonsVFS(this.account, getFileObject().getParent().getName().getPath());
        } catch (FileSystemException e) {
            Logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long lastModifiedTime() {
        try {
            return getFileObject().getContent().getLastModifiedTime();
        } catch (FileSystemException e) {
            Logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return -1;
    }

    public String pseudoIdentity() {
        return this.relativePath + "@" + this.account;
    }

    private FileObject getFileObject() throws FileSystemException {
        return getFileObject(relativePath);
    }

    private FileObject getFileObject(String path) throws FileSystemException {
        //String fullPath = FSSchemes.File + "://" + new File(path).getAbsolutePath();
        return this.vfs.resolveFile(path);
        //return this.fsManager.resolveFile(this.baseFileObject, path);
    }

    private String handleCollision(FileObject src, FileObject des, String collisionPolicy) throws FileSystemException {
        String path = des.getName().getPath();
        String filename = des.getName().getBaseName();
        String destination = des.getName().getPath().substring(0, path.length() - filename.length() - 1);
        String newRelativePath = path;

        if (collisionPolicy.equals(CollisionPolicy.Stop))
            return null;

        if (collisionPolicy.equals(CollisionPolicy.KeepBoth)) {
            if (src.getName().getExtension().length() > 0) {
                while (getFileObject(destination + File.separator + filename).exists()) {
                    filename = filenameWithExtension(filename);
                }
            } else if (src.getName().getExtension().length() == 0) {
                while (getFileObject(destination + File.separator + filename).exists()) {
                    filename = filenameWithoutExtension(filename);
                }
            }

            newRelativePath = destination + File.separator + filename;
        }

        return newRelativePath;
    }

    private String filenameWithExtension(String filename) {
        String regex = "-?\\d+\\.";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filename);
        String newFilename;

        if (m.find()) {
            String group = m.group().replace(".", "");
            int copyNumber = Integer.parseInt(group) + 1;
            newFilename = filename.replaceFirst(regex, copyNumber + ".");
        } else {
            newFilename = filename.replace(".", "1.");
        }

        return newFilename;
    }

    private String filenameWithoutExtension(String filename) {
        int len = filename.length();
        boolean isDigit = Character.isDigit(filename.toCharArray()[len - 1]);
        String newFilename;

        if (filename.length() > 1 && isDigit) {
            int num = getNumberAtEnd(filename);
            int numLen = Integer.toString(num).length();
            newFilename = filename.substring(0, filename.length() - numLen) + ++num;
        } else {
            newFilename = filename.concat("1");
        }

        return newFilename;
    }

    private int getNumberAtEnd(String s){
        int res = 0;
        int p = 1;
        int i = s.length() - 1;

        while(i >= 0){
            int d = s.charAt(i) - '0';
            if (d >= 0 && d <= 9)
                res += d * p;
            else
                break;

            i--;
            p *= 10;
        }

        return res;
    }
}
