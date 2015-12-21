package utilities.file_system;

import java.io.File;

public interface ICustomVF<T> {
    T getFile();

    boolean exists();

    String getExtension();

    String getAbsolutePath();

    boolean isDirectory();

    File getIOFile();
}
