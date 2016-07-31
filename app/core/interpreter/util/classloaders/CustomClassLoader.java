package core.interpreter.util.classloaders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CustomClassLoader extends ClassLoader {
//    private Set<String> changedClasses = new HashSet<>();

    public CustomClassLoader(ClassLoader parent) {
        super(CustomClassLoader.class.getClassLoader());
    }
//
//    public CustomClassLoader(Set<String> changedClasses) {
//        this.changedClasses = changedClasses;
//    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, true);
    }

    public Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
//        Class loadedClazz = findLoadedClass(className);
//        if (loadedClazz != null) {
//            System.out.println("Found loaded class: " + loadedClazz.toString());
//            return loadedClazz;
//        }

//        if (!className.equals("org.overture.webide.processor.RuntimeSocketServer")) {
//            System.out.println(className);
//            return super.loadClass(className, resolve);
//        }

        Path classPath;

        if (className.equals("org.overture.webide.processor.IRuntimeTest") ||
            className.equals("org.overture.webide.processor.ProcessingResult")) {
            return super.loadClass(className, resolve);
        } else if (className.startsWith("org.overture.webide.processor")) {
            String[] sanitizedPath = className.split("\\.");
            classPath = Paths.get(Paths.get("OvertureProcessor", "target", "classes").toString(), sanitizedPath);
        } else if (className.startsWith("org.overture")) {
            System.out.println(className);

            Path jarPath = Paths.get("lib", "Overture-2.3.6.jar");
            File file = jarPath.toFile();

            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
                JarEntry entry = jarFile.getJarEntry(className.replaceAll("\\.", "/") + ".class");
                if (entry == null)
                    return null;

                byte[] bytes = readClassData(jarFile.getInputStream(entry));
                Class<?> aClass = defineClass(className, bytes, 0, bytes.length);

                if (aClass != null) {
                    resolveClass(aClass);
                }

                return aClass;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return super.loadClass(className, resolve);
        }

        try {
            URL myUrl = new URL("file:" + classPath.toAbsolutePath().toString() + ".class");
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();

            byte[] classData = readClassData(input);
            Class clazz = defineClass(className, classData, 0, classData.length);

            if (clazz != null) {
                resolveClass(clazz);
            }

            return clazz;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] readClassData(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = inputStream.read();

        while(data != -1) {
            buffer.write(data);
            data = inputStream.read();
        }

        inputStream.close();
        return buffer.toByteArray();
    }
}
