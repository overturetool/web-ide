package core.debug;

import core.ServerConfigurations;
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
}
