package utilities.file_system.commons_vfs2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import utilities.file_system.FSSchemes;
import utilities.file_system.ICustomVFS;

import java.io.*;
import java.util.*;

public class CommonsVFS implements ICustomVFS<FileObject> {
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

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileObjects;
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
