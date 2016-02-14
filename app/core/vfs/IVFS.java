package core.vfs;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

public interface IVFS<T> {
    boolean appendFile(String content);

    String readFile();

    List<T> readdir();

    List<T> readdir(int depth);

    List<File> readdirAsIOFile();

    List<File> readdirAsIOFile(int depth);

    List<ObjectNode> readdirAsJSONTree();

    List<ObjectNode> readdirAsJSONTree(int depth);

    boolean writeFile(String content);

    boolean exists();

    String getExtension();

    File getIOFile();

    boolean isDirectory();

    String getRelativePath();

    String getAbsolutePath();

    List<File> getSiblings();

    String getName();

    String move(String destination);

    String move(String destination, String collisionPolicy);

    boolean delete();

    boolean rename(String name);

    String mkFile();

    String mkdir();

    IVFS<T> getParent();
}
