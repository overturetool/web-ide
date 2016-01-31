package core.utilities;

import core.ServerConfigurations;

public class PathHelper {
    public static synchronized String JoinPath(String account, String path) {
        return ServerConfigurations.basePath + "/" + account + "/" + path;
    }

    public static synchronized String JoinPath(String path) {
        return ServerConfigurations.basePath + "/" + path;
    }

    public static synchronized String RelativePath(String absolute) {
        String[] split = absolute.split("/");
        String relative = "";
        int baseIndex = -1;

        for (int i = 0; i < split.length; i++) {
            if (baseIndex == -1 && split[i].equals(ServerConfigurations.basePath))
                baseIndex = i;
            else if (baseIndex > -1)
                relative += "/" + split[i];
        }

        return relative;
    }
}
