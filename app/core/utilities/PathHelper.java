package core.utilities;

import core.ServerConfigurations;

public class PathHelper {
    public static synchronized String JoinPath(String account, String path) {
        return ServerConfigurations.basePath + "/" + account + "/" + path;
    }

    public static synchronized String JoinPath(String path) {
        return ServerConfigurations.basePath + "/" + path;
    }
}
