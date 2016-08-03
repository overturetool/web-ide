package org.overture.webide.processing;

import org.overture.config.Settings;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ProcessingUtils {
    public static List<File> handleFiles(String path) {
        List<File> fileList = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles(Settings.dialect.getFilter());
            for (File f : files) {
                if (f.isFile())
                    fileList.add(f);
            }
        } else {
            fileList.add(file);
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
