package core.processing.classloaders;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class DynamicClassLoader extends ClassLoader {
    private Set<String> classes;

    public DynamicClassLoader(Set<String> changedClasses) {
        this.classes = changedClasses;
    }

    /**
     * Dynamically loads the class from it's binary file.
     * Classes this class depends on will only be loaded dynamically when
     * they are in list of changed classes given at construction.
     * @throws ClassNotFoundException
     */
    public Class<?> dynamicallyLoadClass(String name) throws ClassNotFoundException {
        classes.add(name);
        return loadClass(name);
    }

    /**
     * Finds the class dynamically, contrary to Class.forname which can use
     * cached copies.
     * This means it forces a reload of the class data.
     * Source: http://stackoverflow.com/questions/3971534/
     */
    @Override
    protected Class<?> findClass(String s) throws ClassNotFoundException {
        try {
            byte[] bytes;
            bytes = loadClassData(s);
            return defineClass(s, bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        return loadClass(name, true);
    }

    /**
     * Overridden to not check the parent's loadClass method first.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);
        if(clazz != null)
            return clazz;

        if(classes.contains(name)) {
            clazz = findClass(name);
            resolveClass(clazz);
            return clazz;
        }
        return super.loadClass(name, resolve);
    }

    /**
     * Load class data from byte code.
     *
     * @param className
     * @return
     * @throws IOException
     */
    protected byte[] loadClassData(String className) throws IOException {
        File f = new File("OvertureProcessor/target/classes/" + className.replaceAll("\\.", "/") + ".class");
        int size = (int) f.length();
        byte buff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        dis.readFully(buff);
        dis.close();
        return buff;
    }
}
