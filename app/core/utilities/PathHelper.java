package core.utilities;

import core.ServerConfigurations;

public class PathHelper {
    public static synchronized String JoinPath(String account, String path) {
        return account + "/" + path;
    }

    public static synchronized String RemoveBase(String absolute) {
        String[] split = absolute.split("/");
        String relative = "";
        int baseIndex = -1;

        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(ServerConfigurations.basePath)) {
                baseIndex = i;
                break;
            }
        }

        if (baseIndex == -1)
            return relative;

        for (int i = baseIndex + 1; i < split.length; i++)
            relative += "/" + split[i];

        return relative;
    }

    public static synchronized String RelativePath(String absolute) {
        String[] split = absolute.split("/");
        String relative = "";
        int baseIndex = -1;
        boolean firstIndex = true;

        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(ServerConfigurations.basePath)) {
                baseIndex = i;
                break;
            }
        }

        if (baseIndex == -1)
            return relative;

        for (int i = baseIndex + 1; i < split.length; i++) {
            if (firstIndex) {
                relative = split[i];
                firstIndex = false;
            } else {
                relative += "/" + split[i];
            }
        }

        return relative;
    }
}
