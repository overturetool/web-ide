package core.utilities;

import org.overture.interpreter.runtime.ModuleInterpreter;

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
