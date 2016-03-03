package core.utilities;

import core.ServerConfigurations;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathHelper {
    public static synchronized String RemoveBase(String absolute) {
        Path basePath = Paths.get(ServerConfigurations.basePath);
        Path absolutePath = Paths.get(absolute);

        URI relativeUri = basePath.toUri().relativize(absolutePath.toUri());
        Path relativePath = Paths.get(relativeUri.getPath());

        return relativePath.toString();
    }
}
