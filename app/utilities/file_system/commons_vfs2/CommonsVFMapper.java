package utilities.file_system.commons_vfs2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import play.libs.Json;
import utilities.file_system.ICustomVFMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonsVFMapper implements ICustomVFMapper<FileObject> {

    @Override
    public List<ObjectNode> toJSONList(List<FileObject> fileObjects) {
        List<ObjectNode> jsonList = new ArrayList<>();

        try {
            for (FileObject fo : fileObjects) {
                jsonList.add(mapToJson(fo));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return jsonList;
    }

    @Override
    public File toIOFile(FileObject file) {
        try {
            return new File(file.getURL().toString());
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<File> toIOFileList(List<FileObject> fileObject) {
        List<File> fileList = new ArrayList<>();

        try {
            for (FileObject fo : fileObject) {
                fileList.add(new File(fo.getURL().toString()));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return fileList;
    }

    private ObjectNode mapToJson(FileObject fileObject) throws FileSystemException {
        ObjectNode jsonObject = Json.newObject();

        long size = 0;
        if (fileObject.getType() == FileType.FILE)
            size = fileObject.getContent().getSize();
        else
            size = fileObject.getChildren().length;

        jsonObject.put("url", fileObject.getURL().toString());
        jsonObject.put("name", fileObject.getName().getBaseName());
        jsonObject.put("extension", fileObject.getName().getExtension());
        jsonObject.put("friendly_uri", fileObject.getName().getFriendlyURI());
        jsonObject.put("size", size);
        jsonObject.put("type", fileObject.getType().toString());

        return jsonObject;
    }
}
