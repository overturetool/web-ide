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
                stringArray[i] = "file:" + new File(ServerConfigurations.basePath + "/" + path).getAbsolutePath();
            }
        }

        return StringUtils.join(stringArray, " ");
    }

    public static String ConvertPathsToRelative(String message) {
        boolean scan = true;
        int scanStartIndex = 0;
        int index = message.indexOf("<");
        message = message.substring(index);
        String pattern = "filename=\"file:";

        while (scan) {
            String subString = message.substring(scanStartIndex);
            if (subString.contains(pattern)) {
                int startIndex = message.indexOf(pattern, scanStartIndex) + pattern.length();
                int endIndex = message.indexOf("\"", startIndex);

                String absolutePath = message.substring(startIndex, endIndex);
                File file = new File(absolutePath);

                if (file.isAbsolute()) {
                    String relativePath = PathHelper.RelativePath(absolutePath);
                    relativePath = relativePath.substring(ServerConfigurations.basePath.length() + 2);
                    message = message.replaceFirst("file:" + absolutePath, relativePath);
                    scanStartIndex = startIndex + relativePath.length();
                } else {
                    scanStartIndex = endIndex;
                }
            } else {
                scan = false;
            }
        }

        return message.length() + message;
    }
}
