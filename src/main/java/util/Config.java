package util;

import commonmodels.PhysicalNode;

import java.io.IOException;
import java.util.*;

public class Config {

    private final static String CONFIG_PATH = "config.txt";

    private final static int PORT = 50050;

    private final static String[] FILES = new String[] {"f1.txt", "f2.txt", "f3.txt", "f4.txt"};

    private static volatile Config instance = null;

    private List<PhysicalNode> servers = new ArrayList<>();

    private List<String> files = new ArrayList<>();

    private String address;

    private int port;

    public Config() {
        loadConfig();
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized(Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }

        return instance;
    }

    public static void with(String address, int port) {
        Config.getInstance().address = address;
        Config.getInstance().port = port;
    }

    public static void deleteInstance() {
        instance = null;
    }

    public static String getConfigPath() {
        return CONFIG_PATH;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public List<PhysicalNode> getServers() {
        return servers;
    }

    public List<String> getFiles() {
        return files;
    }

    private void loadConfig() {
        try {
            List<String> lines = FileHelper.read(CONFIG_PATH);
            for (String line : lines) {
                String[] pair = line.split(" ");
                PhysicalNode node = new PhysicalNode(pair[0], pair[1], PORT);
                if (node.getAddress().equals(address)) continue;
                servers.add(node);
            }

            files = Arrays.asList(FILES);
        } catch (IOException e) {
            System.out.println("Config file not found or does not follow the standard format");
        }
    }
}
