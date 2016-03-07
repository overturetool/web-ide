package core.utilities;

import core.vfs.IVFS;
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
        map.put(file.projectId(), new Resource(file.lastModifiedTime(), interpreter));
    }

    public Resource get(IVFS file) {
        return map.get(file.projectId());
    }

    public synchronized boolean existsAndNotModified(IVFS file) {
        Resource resource = map.get(file.projectId());

        if (resource == null)
            return false;

        if (resource.getLastModified() == file.lastModifiedTime())
            return true;
        else
            map.remove(file.projectId());

        return false;
    }
}
