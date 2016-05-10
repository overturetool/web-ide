package core.utilities;

import core.vfs.IVFS;
import org.apache.commons.vfs2.FileSystemException;
import org.overture.interpreter.runtime.ModuleInterpreter;

import java.util.concurrent.ConcurrentHashMap;

public class ResourceCache {
    private static ResourceCache instance = null;

    private static ConcurrentHashMap<String, Resource> map = new ConcurrentHashMap<>();

    public static ResourceCache getInstance() {
        if (instance == null) {
            synchronized (ResourceCache.class) {
                instance = new ResourceCache();
            }
        }
        return instance;
    }

    private ResourceCache() {
    }

    public void add(IVFS file, ModuleInterpreter interpreter) {
        try {
            map.put(file.projectId(), new Resource(file.getProjectRoot().getContent().getLastModifiedTime(), interpreter));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    public Resource get(IVFS file) {
        return map.get(file.projectId());
    }

    public synchronized boolean existsAndNotModified(IVFS file) {
        Resource resource = map.get(file.projectId());

        if (resource == null)
            return false;

        try {
            if (resource.getLastModified() == file.getProjectRoot().getContent().getLastModifiedTime())
                return true;
            else
                map.remove(file.projectId());
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return false;
    }
}
