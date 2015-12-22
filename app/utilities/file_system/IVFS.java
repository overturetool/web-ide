package utilities.file_system;

import java.util.List;

public interface IVFS<T> {
    boolean appendFile(String path, String content);

    String readFile(String path);

    List<T> readdir(String path, int depth);

    boolean writeFile(String path, String content);

    boolean exists(String rel_path);

    String getExtension(String rel_path);
}
