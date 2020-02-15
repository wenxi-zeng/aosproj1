package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResourcesLoader {
    public static ResourceBundle getBundle(String filename) {
        File file;

        if (runInJar())
            file = new File(getProgramPath() + File.separator + "res");
        else
            file = new File("res");

        URL[] urls = new URL[0];
        try {
            urls = new URL[]{file.toURI().toURL()};
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ClassLoader loader = new URLClassLoader(urls);

        return ResourceBundle.getBundle(filename, Locale.getDefault(), loader);
    }

    public static String getRelativePathToRes(String filename) throws FileNotFoundException {
        if (runInJar())
            return getProgramPath() + File.separator + "res" + File.separator + filename;
        else
            return  "res" + File.separator + filename;
    }

    public static String getRelativeFileName(String filename) {
        if (runInJar())
            return filename == null ? getProgramPath() : getProgramPath() + File.separator + filename;
        else
            return filename == null ? "" : File.separator + filename;
    }

    public static String getProgramPath() {
        URL url = ResourcesLoader.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new File(jarPath).getParentFile().getPath();
    }

    public static boolean runInJar() {
        String protocol = ResourcesLoader.class.getResource("").getProtocol();

        return Objects.equals(protocol, "jar");
    }

    public static String getParentDirOfProgramPath() {
        URL url = ResourcesLoader.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new File(jarPath).getParentFile().getParentFile().getAbsolutePath();
    }
}
