package utilities.file_system.commons_vfs2;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import utilities.file_system.FSSchemes;
import utilities.file_system.ICustomVF;

import java.io.File;

public class CommonsVF implements ICustomVF<FileObject> {
    private FileObject file;

    public CommonsVF(String rel_path) {
        try {
            StandardFileSystemManager fsManager = new StandardFileSystemManager();
            fsManager.init();

            String full_path = FSSchemes.File + "://" + new File(rel_path).getAbsolutePath();
            this.file =  fsManager.resolveFile(full_path);
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
}
