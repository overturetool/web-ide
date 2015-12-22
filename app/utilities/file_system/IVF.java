package utilities.file_system;

import java.io.File;

public interface IVF<T> {
    T getFile();

    boolean exists();

    String getExtension();

    String getRelativePath();

    String getAbsolutePath();

    boolean isDirectory();

    File getIOFile();
}
