package core.utilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileOperations {
    public synchronized static Path downloadFile(String inputUrl, String destination) {
        String filename = Paths.get(inputUrl).getFileName().toString();

        URL url;
        try {
            url = new URL(inputUrl);
        } catch (MalformedURLException e) {
            return null;
        }

        Path path;
        URLConnection con;
        try {
            con = url.openConnection();
            path = Paths.get(destination + File.separator + filename);

            try (InputStream stream = con.getInputStream()) {
                Files.copy(stream, path);
            }

            return path;
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized static String unzip(File file, String outputDir) throws IOException {
        String name = null;
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());

                if (name == null)
                name = entryDestination.getParentFile().getName();

                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        }
        return name;
    }

    public static synchronized void filterDirectoryContent(File file, String[] extensions) {
        if (!file.isDirectory())
            return;

        File[] files = file.listFiles((dir, name) ->
                !FilenameUtils.isExtension(name, extensions));

        for (File f : files)
            f.delete();
    }
}
