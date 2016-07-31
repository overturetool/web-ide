package core.vfs.commons.vfs2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.ServerConfigurations;
import core.vfs.CollisionPolicy;
import core.vfs.IVFS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonsVFS implements IVFS<FileObject> {
    private FileObject vfs;
    private String account;
    private String relativePath;
    private FileObject baseFileObject;
    //private final Logger logger = LoggerFactory.getLogger(CommonsVFS.class);

    protected CommonsVFS(String path) {
        StandardFileSystemManager fsManager = new StandardFileSystemManager();

        this.account = ServerConfigurations.basePath;
        this.relativePath = path;

        try {
            fsManager.init();
            fsManager.setBaseFile(new File(account));
            this.baseFileObject = fsManager.getBaseFile();
            this.vfs = fsManager.createVirtualFileSystem(this.baseFileObject);
        } catch (FileSystemException e) {
            //logger.error(e.getMessage(), e);
        }
    }

    public CommonsVFS(@NotNull String account, String path) {
        StandardFileSystemManager fsManager = new StandardFileSystemManager();

        this.account = account;
        this.relativePath = path;
        Path basePath = Paths.get(ServerConfigurations.basePath, account);

        try {
            fsManager.init();
            fsManager.setBaseFile(new File(basePath.toString()));
            this.baseFileObject = fsManager.getBaseFile();
            this.vfs = fsManager.createVirtualFileSystem(this.baseFileObject);
        } catch (FileSystemException e) {
            //logger.error(e.getMessage(), e);
        }
    }

    @Override
    public FileObject getProjectRoot() {
        Path projectRootPath = Paths.get(this.relativePath).getName(0);
        try {
            FileObject projectRootFileObject = getFileObject(projectRootPath.toString());
            if (projectRootFileObject == null || !projectRootFileObject.exists())
                return null;

            FileObject isProjectRoot = this.baseFileObject.getChild(projectRootPath.toString());
            if (isProjectRoot != null)
                return projectRootFileObject;
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<File> getProjectAsIOFile() {
        FileObject projectRoot = getProjectRoot();
        List<File> files = Collections.synchronizedList(new ArrayList<>());
        if (projectRoot == null)
            return files;

        List<FileObject> fileObjects = readdir(projectRoot.getName().getPath(), -1);
        files.addAll(fileObjects
                .stream()
                .map(fileObject -> new File(toAbsolutePath(fileObject.getName().getPath())))
                .collect(Collectors.toList()));

        return files;
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
        Path pathObject = Paths.get(path);

        try {
            FileObject fileObject = getFileObject(path);

            for (FileObject subFileObject : fileObject.getChildren()) {
                String name = subFileObject.getName().getBaseName();
                boolean isFolder = subFileObject.getType() == FileType.FOLDER;
                Path subPath = Paths.get(pathObject.toString(), name);

                ObjectNode node = new ObjectMapper().createObjectNode();
                node.put("name", name);
                node.put("type", isFolder ? "directory" : "file");
                node.put("path", Paths.get(this.account, subPath.toString()).toString());

                if (isFolder) {
                    if (depth == -1)
                        node.putPOJO("children", readdirAsJSONTree(subPath.toString(), -1));
                    else if (depth > 0)
                        node.putPOJO("children", readdirAsJSONTree(subPath.toString(), depth - 1));
                }

                nodes.add(node);
            }
        } catch (IOException e) {
            //logger.error(e.getMessage(), e);
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
            return Paths.get(baseUri.toString(), this.relativePath).toString();
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
        return Paths.get(this.baseFileObject.getName().getPath(), relativePath).toString();
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
        String filename = Paths.get(destination).getFileName().toString();
        String newRelativePath = destination;

        try {
            FileObject src = getFileObject();
            FileObject des = getFileObject(newRelativePath);

            if (!src.exists() || src == des || des.getParent().getType() != FileType.FOLDER)
                return null;

            // A file with the same name exists - handle collision according to policy
            if (des.exists()) {
                newRelativePath = handleCollision(src, des, collisionPolicy);

                if (newRelativePath == null)
                    return null;

                filename = Paths.get(newRelativePath).getFileName().toString();
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
            FileObject des = getFileObject(Paths.get(src.getName().getParent().getPath(), name).toString());

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
            //logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long lastModifiedTime() {
        try {
            return getFileObject().getContent().getLastModifiedTime();
        } catch (FileSystemException e) {
            //logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public String Id() {
        return this.relativePath + "@" + this.account;
    }

    @Override
    public String projectId() {
        FileObject projectRoot = getProjectRoot();
        String project = projectRoot != null ? projectRoot.getName().getBaseName() : "root";
        return project + "@" + this.account;
    }

    @Override
    public String copy(String destination) {
        return copy(destination, CollisionPolicy.KeepBoth);
    }

    @Override
    public String copy(String destination, String collisionPolicy) {
        String filename = Paths.get(destination).getFileName().toString();
        String newRelativePath = destination;

        try {
            FileObject src = getFileObject();
            FileObject des = getFileObject(newRelativePath);

            if (!src.exists() || src == des || des.getParent().getType() != FileType.FOLDER)
                return null;

            // A file with the same name exists - handle collision according to policy
            if (des.exists()) {
                newRelativePath = handleCollision(src, des, collisionPolicy);

                if (newRelativePath == null)
                    return null;

                filename = Paths.get(newRelativePath).getFileName().toString();
            }

            des.copyFrom(src, new FileSelector() {
                @Override
                public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                    return true;
                }

                @Override
                public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                    return true;
                }
            });
            this.relativePath = newRelativePath;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return null;
        }

        return filename;
    }

    // TODO : Not tested
    @Override
    public List<File> findFile(String filename) {
        List<File> list = new ArrayList<>();
        try {
            FileObject[] files = getFileObject().findFiles(new FileSelector() {
                @Override
                public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                    //logger.debug(fileInfo.getFile().getName().getBaseName());
                    return fileInfo.getFile().getName().getBaseName().equals(filename);
                }

                @Override
                public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                    return false;
                }
            });

            for (FileObject file : files)
                list.add(new File(file.getName().getPath()));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return list;
    }

    private FileObject getFileObject() throws FileSystemException {
        return getFileObject(relativePath);
    }

    private FileObject getFileObject(String path) throws FileSystemException {
        return this.vfs.resolveFile(path);
    }

    private String handleCollision(FileObject src, FileObject des, String collisionPolicy) throws FileSystemException {
        String path = des.getName().getPath();
        String filename = des.getName().getBaseName();
        String destination = Paths.get(path).getParent().toString();
        String newRelativePath = path;

        if (collisionPolicy.equals(CollisionPolicy.Stop))
            return null;

        if (collisionPolicy.equals(CollisionPolicy.KeepBoth)) {
            if (src.getName().getExtension().length() > 0) {
                while (getFileObject(Paths.get(destination, filename).toString()).exists()) {
                    filename = generateUniqueFilenameWithExtension(filename);
                }
            } else if (src.getName().getExtension().length() == 0) {
                while (getFileObject(Paths.get(destination, filename).toString()).exists()) {
                    filename = generateUniqueFilenameWithoutExtension(filename);
                }
            }

            newRelativePath = Paths.get(destination, filename).toString();
        }

        return newRelativePath;
    }

    private String generateUniqueFilenameWithExtension(String filename) {
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

    private String generateUniqueFilenameWithoutExtension(String filename) {
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
