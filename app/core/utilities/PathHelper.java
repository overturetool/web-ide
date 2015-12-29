package core.utilities;

import core.ServerConfigurations;

public class PathHelper {
    public static String JoinPath(String account, String path) {
        return ServerConfigurations.basePath + "/" + account + "/" + path;
    }

    public static String JoinPath(String path) {
        return ServerConfigurations.basePath + "/" + path;
    }
}
