package core.vfs.commons_vfs2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.vfs.IVFS;

import java.io.File;
import java.util.List;

public class CommonsVFSUnsafe extends CommonsVFS {

    public CommonsVFSUnsafe(String path) {
        super(path);
    }

    @Override
    public boolean appendFile(String content) {
        return false;
    }

    @Override
    public String readFile() {
        return null;
    }

    @Override
    public List readdir() {
        return null;
    }

    @Override
    public List readdir(int depth) {
        return null;
    }

    @Override
    public List<File> readdirAsIOFile() {
        return null;
    }

    @Override
    public List<File> readdirAsIOFile(int depth) {
        return null;
    }

    @Override
    public List<ObjectNode> readdirAsJSONTree() {
        return null;
    }

    @Override
    public List<ObjectNode> readdirAsJSONTree(int depth) {
        return null;
    }

    @Override
    public boolean writeFile(String content) {
        return false;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public String getExtension() {
        return null;
    }

    @Override
    public File getIOFile() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getRelativePath() {
        return null;
    }

    @Override
    public String move(String destination) {
        return null;
    }

    @Override
    public String move(String destination, String collisionPolicy) {
        return null;
    }

    @Override
    public String getAbsolutePath() {
        return null;
    }

    @Override
    public String getAbsoluteUrl() {
        return null;
    }

    @Override
    public List<File> getSiblings() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean rename(String name) {
        return false;
    }

    @Override
    public String mkFile() {
        return null;
    }

    @Override
    public String mkdir() {
        return null;
    }

    @Override
    public IVFS getParent() {
        return null;
    }

    @Override
    public long lastModifiedTime() {
        return 0;
    }

    @Override
    public String pseudoIdentity() {
        return null;
    }
}
