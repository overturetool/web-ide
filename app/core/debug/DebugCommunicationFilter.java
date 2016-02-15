package core.debug;

import core.ServerConfigurations;
import core.utilities.PathHelper;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

public class DebugCommunicationFilter {
    public static String ConvertPathToAbsolute(String message) {
        String[] stringArray = message.split("\\s");

        for (int i = 0; i < stringArray.length; i++) {
            String str = stringArray[i];
            if (str.equals("-f")) {
                String path = stringArray[++i];
                stringArray[i] = new File(ServerConfigurations.basePath + "/" + path).toURI().toString();
            }
        }

        return StringUtils.join(stringArray, " ");
    }

    public static String ConvertPathsToRelative(String message) {
        int index = message.indexOf("<");
        message = message.substring(index);

        String[] patterns = {"filename=\"file:"};

        for (String pattern : patterns)
            message = scanAndReplace(message, pattern);

        return (message.length() + 1) + message;
    }

    private static String scanAndReplace(String message, String pattern) {
        boolean scan = true;
        int scanStartIndex = 0;

        while (scan && scanStartIndex + pattern.length() < message.length()) {
            String subString = message.substring(scanStartIndex);
            if (subString.contains(pattern)) {
                int startIndex = message.indexOf(pattern, scanStartIndex) + pattern.length();
                int endIndex = message.indexOf("\"", startIndex);

                String absolutePath = message.substring(startIndex, endIndex);
                File file = new File(absolutePath);

                if (file.isAbsolute()) {
                    String relativePath = PathHelper.RemoveBase(file.getPath()).substring(1); // remove leading '/' - Should this be avoided?
                    message = message.replaceFirst(file.toURI().toString(), relativePath);
                    scanStartIndex = (startIndex + relativePath.length()) - 4;
                } else {
                    scanStartIndex = endIndex;
                }
            } else {
                scan = false;
            }
        }

        return message;
    }
}
