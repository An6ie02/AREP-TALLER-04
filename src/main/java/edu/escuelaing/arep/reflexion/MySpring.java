package edu.escuelaing.arep.reflexion;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.escuelaing.arep.reflexion.components.Component;
import edu.escuelaing.arep.reflexion.components.GetMapping;
import edu.escuelaing.arep.reflexion.components.PostMapping;

/**
 * This class is used to manage the components of the application.
 */
public class MySpring {

    private static MySpring _instance;
    private static final Map<String, Method> GET_COMPONENT = new HashMap<>();
    private static final Map<String, Method> POST_COMPONENT = new HashMap<>();
    private static final String PACKAGE_NAME = "target\\classes\\edu\\escuelaing\\arep\\reflexion\\";
    

    private MySpring() throws ClassNotFoundException {
        List<Class<?>> classes = getClasses(PACKAGE_NAME);
        for (Class<?> classFound : classes) {
            loadComponent(classFound);
        }
    }

    public static MySpring getInstance() throws ClassNotFoundException {
        if (_instance == null) {
            _instance = new MySpring();
        }
        return _instance;
    }

    /**
     * This method is used to get the classes of the package.
     * @param directory is the directory of the classes.
     * @return a list with the classes of the package.
     * @throws ClassNotFoundException if the class is not found.
     */
    public static List<Class<?>> getClasses(String directory) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        File dir = new File(directory);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(getClasses(file.getPath() + "\\"));
                } else if (file.getName().endsWith(".class")) {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    className = directory.substring(directory.indexOf("classes") + 8).replace(File.separator, ".")  + className;
                    Class<?> classFound = loadClass(className, directory);
                    if (classFound != null && classFound.isAnnotationPresent(Component.class)) {
                        classes.add(classFound);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * This method is used to load a class.
     * @param className is the name of the class.
     * @param directory is the directory of the class.
     * @return the class loaded.
     * @throws ClassNotFoundException if the class is not found.
     */
    public static Class<?> loadClass(String className, String directory) throws ClassNotFoundException {
        try {
            File dir = new File(directory);
            URL url = dir.toURI().toURL();
            URL[] urls = new URL[] { url };
            ClassLoader cl = new URLClassLoader(urls);
            return cl.loadClass(className);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method is used to load the component of the class.
     * @param classFound is the class to load the component.
     * @throws ClassNotFoundException if the class is not found.
     */
    public static void loadComponent(Class<?> classFound) throws ClassNotFoundException {
        Method[] methods = classFound.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GET_COMPONENT.put(method.getAnnotation(GetMapping.class).value(), method);
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                POST_COMPONENT.put(method.getAnnotation(PostMapping.class).value(), method);
            }
        }

        printMap(GET_COMPONENT);
        printMap(POST_COMPONENT);
    }

    public static void printMap(Map<String, Method> map) {
        for (Map.Entry<String, Method> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
    }

    /**
     * This method is used to get the method of path.
     * @param path is the path of the method.
     * @param method is the method of the class.
     * @return the method of the class.
     */
    public static Method getMethod(String path, String method) {
        if (method.equals("GET")) {
            return GET_COMPONENT.get(path);
        } else if (method.equals("POST")) {
            return POST_COMPONENT.get(path);
        } else {
            return null;
        }
    }

}
