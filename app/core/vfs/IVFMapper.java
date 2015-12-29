package core.vfs;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

public interface IVFMapper<T> {
    List<ObjectNode> toJSONTree(List<T> fileObjects, int depth);

    List<ObjectNode> toJSONList(List<T> fileObjects);

    File toIOFile(T file);

    List<File> toIOFileList(List<T> fileObject);
}
