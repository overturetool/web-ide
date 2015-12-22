package utilities.file_system.commons_vfs2;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import utilities.file_system.FSSchemes;
import utilities.file_system.IVF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonsVF implements IVF<FileObject> {
    private FileObject file;
    private File IOFile;
    private String relativePath;

    public CommonsVF(String relativePath) {
        this.IOFile = new File(relativePath);
        this.relativePath = relativePath;

        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String absolutePath = FSSchemes.File + "://" + IOFile.getAbsolutePath();
            this.file =  fsManager.resolveFile(absolutePath);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public boolean exists() {
        try {
            return file.exists();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getExtension() {
        return file.getName().getExtension();
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String getAbsolutePath() {
        try {
            return file.getURL().toString();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean isDirectory() {
        try {
            return file.getType() == FileType.FOLDER;
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public File getIOFile() {
        return IOFile;
    }

    public List<File> getSiblings() {
        FileObject parent = null;
        List<File> children = new ArrayList<>();

        try {
            parent = file.getParent();

            if (parent == null)
                return null;

            for (FileObject fo : parent.getChildren()) {
                children.add(new File(fo.getURL().getPath()));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return children;
    }
}
