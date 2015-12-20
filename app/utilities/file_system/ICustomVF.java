package utilities.file_system;

public interface ICustomVF<T> {
    T getFile();

    boolean exists();

    String getExtension();

    String getAbsolutePath();
}
