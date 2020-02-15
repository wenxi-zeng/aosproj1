package util;

import commonmodels.transport.Request;
import commonmodels.transport.Response;
import socket.UDPClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SimpleLog {
    private static volatile SimpleLog instance = null;
    private String identity;
    private UDPClient socketClient;
    private SimpleDateFormat dateFormatter;
    private List<String> filter;

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
        switch (Config.getInstance().getLogMode()) {
            case Config.LOG_MODE_SCREEN:
                v(message);
                break;
            case Config.LOG_MODE_SERVER:
                getInstance().send(message);
                break;
            case Config.LOG_MODE_FILE:
                break;
            case Config.LOG_MODE_OFF:
                break;
        }
    }

    public static synchronized void i(Object message) {
        switch (Config.getInstance().getLogMode()) {
            case Config.LOG_MODE_SCREEN:
                v(String.valueOf(message));
                break;
            case Config.LOG_MODE_SERVER:
                getInstance().send(message);
                break;
            case Config.LOG_MODE_FILE:
                break;
            case Config.LOG_MODE_OFF:
                break;
        }
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
        i(sw.toString());
    }

    public synchronized void send(Object message) {
        try {
            if (socketClient == null) {
                socketClient = new UDPClient(Config.getInstance().getLogServer());
                filter = Config.getInstance().getLogFilter();
            }

            if (filter.size() > 0) {
                String header = null;
                if (message instanceof Request)
                    header = ((Request) message).getHeader();
                else if (message instanceof Response)
                    header = ((Response) message).getHeader();

                if (header != null && filter.contains(header))
                    return;
            }

            socketClient.send(String.format("[%s %s]: %s", identity, dateFormatter.format(new Date()), String.valueOf(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
