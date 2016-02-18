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
        map.putIfAbsent(file.pseudoIdentity(), new Resource(file.lastModifiedTime(), interpreter));
    }

    public Resource get(IVFS file) {
        return map.get(file.pseudoIdentity());
    }

    public synchronized boolean existsAndNotModified(IVFS file) {
        Resource resource = map.get(file.pseudoIdentity());

        if (resource == null)
            return false;

        if (resource.getLastModified() == file.lastModifiedTime())
            return true;
        else
            map.remove(file.pseudoIdentity());

        return false;
    }

    public class Resource {
        private long lastModified;
        private ModuleInterpreter interpreter;

        public Resource(long lastModified, ModuleInterpreter interpreter) {
            this.lastModified = lastModified;
            this.interpreter = interpreter;
        }

        public long getLastModified() {
            return this.lastModified;
        }

        public ModuleInterpreter getInterpreter() {
            return this.interpreter;
        }
    }
}
