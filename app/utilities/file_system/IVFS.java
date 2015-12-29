package utilities.file_system;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

public interface IVFS<T> {
    boolean appendFile(String content);

    String readFile();

    List<T> readdir(int depth);

    List<ObjectNode> readdirAsJSONTree(int depth);

    boolean writeFile(String content);

    boolean exists();

    String getExtension();

    File getIOFile();

    boolean isDirectory();

    String getAbsolutePath();

    List<File> getSiblings();

    String getName();
}
