package util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

public class SimpleLog {
    private static volatile SimpleLog instance = null;
    private String identity;
    private SimpleDateFormat dateFormatter;

    private SimpleLog() {
        dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");
    }

    public static void with(String address, int port) {
        SimpleLog.getInstance().identity = address + ":" + port;
    }

    public static SimpleLog getInstance() {
        if (instance == null) {
            synchronized(SimpleLog.class) {
                if (instance == null) {
                    instance = new SimpleLog();
                }
            }
        }

        return instance;
    }

    public static void deleteInstance() {
        instance = null;
    }

    public static synchronized void i(String message) {
        v(message);
    }

    public static synchronized void i(Object message) {
        v(String.valueOf(message));
    }

    public static synchronized void v(String message) {
        System.out.println(message);
    }

    public static synchronized void r(String message) {
        System.out.print(String.format("\033[2J"));
        System.out.print(message);
    }

    public static synchronized void l(String message) {
        System.out.println(message);
    }

    public static synchronized void e(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        v(sw.toString());
    }
}
