package utilities.file_system;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

public interface ICustomVFMapper<T> {
    List<ObjectNode> toJSONTree(List<T> fileObjects);

    List<ObjectNode> toJSONList(List<T> fileObjects);

    File toIOFile(T file);

    List<File> toIOFileList(List<T> fileObject);
}
