package utilities.file_system.commons_vfs2;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import utilities.file_system.FSSchemes;
import utilities.file_system.ICustomVF;

import java.io.File;

public class CommonsVF implements ICustomVF<FileObject> {
    private FileObject file;
    private File IOFile;

    public CommonsVF(String relativePath) {
        IOFile = new File(relativePath);

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
}
