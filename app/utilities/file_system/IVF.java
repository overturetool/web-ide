package utilities.file_system;

import java.io.File;
import java.util.List;

public interface IVF<T> {
    T getFile();

    String getName();

    boolean exists();

    String getExtension();

    String getRelativePath();

    String getAbsolutePath();

    boolean isDirectory();

    File getIOFile();

    List<File> getSiblings();
}
