package org.overture.webide.processing;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ProcessingUtils {
    public static List<File> object2FileList(Object inputObject) {
        List<File> fileList = new ArrayList<File>();
        if (inputObject instanceof List<?>) {
            List<?> inputList = (List<?>) inputObject;
            if (!inputList.isEmpty() && inputList.get(0) instanceof File) {
                for (Object object : inputList)
                    fileList.add((File) object);
            }
        }
        return fileList;
    }

    public static int getPID() {
        try {
            String pidStr = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            return Integer.parseInt(pidStr);
        } catch (Exception e) {
            return -1;
        }
    }
}
