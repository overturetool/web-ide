package utilities.file_system;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface ICustomVFS {
    boolean appendFile(String path, String content);

    String readFile(String path);

    List<ObjectNode> readdir(String path, int depth);

    boolean writeFile(String path, String content);

    boolean exists(String rel_path);

    String getExtension(String rel_path);
}