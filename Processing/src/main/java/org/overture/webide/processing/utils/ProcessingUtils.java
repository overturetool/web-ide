package org.overture.webide.processing.utils;

import org.overture.codegen.utils.GeneralCodeGenUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ProcessingUtils {
    public static List<File> handleFiles(String path) {
        List<File> fileList = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return fileList;
            for (File f : files) {
                if (f.isFile() && GeneralCodeGenUtils.isVdmSourceFile(f))
                    fileList.add(f);
            }
        } else {
            fileList.add(file);
        }
        return fileList;
    }

    public static void validateBaseDir(File baseDirFile) {
        if (!baseDirFile.exists() || !baseDirFile.isDirectory())
            throw new IllegalArgumentException("baseDir does not exists or is not a directory");
    }

    public static Socket getSocket(String host, int port) throws IOException {
        if (host == null || port == -1)
            throw new IllegalArgumentException("Missing required arguments: host and/or port");
        return new Socket(host, port);
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
