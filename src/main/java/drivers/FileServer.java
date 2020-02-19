package drivers;

import clock.AckVector;
import clock.LogicClock;
import commands.CommonCommand;
import commonmodels.PhysicalNode;
import commonmodels.transport.InvalidRequestException;
import commonmodels.transport.Request;
import commonmodels.transport.Response;
import managers.FileManager;
import org.apache.commons.lang3.StringUtils;
import socket.JsonProtocolManager;
import socket.SocketClient;
import socket.SocketServer;
import util.Config;
import util.SimpleLog;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class FileServer implements SocketServer.EventHandler, SocketClient.ServerCallBack{

    private SocketServer socketServer;

    private SocketClient socketClient;

    private String ip;

    private int port;

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    private Semaphore semaphore = new Semaphore(0);

    private SocketClient.ServerCallBack serverCallBack = new SocketClient.ServerCallBack() {
        @Override
        public void onResponse(Request request, Response response) {
            SimpleLog.i(Config.getInstance().getId() + " sent " + request.getType() + " to " + request.getReceiverId() + " at time: " + request.getTimestamp() + " successfully");
            semaphore.release();
        }

        @Override
        public void onFailure(Request request, String error) {
            SimpleLog.i(Config.getInstance().getId() + " sent " + request.getType() + " to " + request.getReceiverId() + " at time: " + request.getTimestamp() + " failed, error message: " + error);
            semaphore.release();
        }
    };

    private SocketClient.ServerCallBack asyncServerCallBack = new SocketClient.ServerCallBack() {
        @Override
        public void onResponse(Request request, Response response) {
            SimpleLog.i(Config.getInstance().getId() + " sent " + request.getType() + " to " + request.getReceiverId() + " at time: " + request.getTimestamp() + " successfully");
        }

        @Override
        public void onFailure(Request request, String error) {
            SimpleLog.i(Config.getInstance().getId() + " sent " + request.getType() + " to " + request.getReceiverId() + " at time: " + request.getTimestamp() + " failed, error message: " + error);
        }
    };

    public static void main(String[] args){
        if (args.length > 1)
        {
            System.err.println ("Usage: DataNodeDaemon [daemon port]");
            return;
        }

        int daemonPort = Config.PORT;
        if (args.length > 0)
        {
            try
            {
                daemonPort = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                System.err.println ("Invalid daemon port: " + e);
                return;
            }
            if (daemonPort <= 0 || daemonPort > 65535)
            {
                System.err.println ("Invalid daemon port");
                return;
            }
        }

        try {
            FileServer daemon = FileServer.newInstance(getAddress(), daemonPort);
            Config.with(daemon.ip, daemon.port);
            SimpleLog.with(daemon.ip, daemon.port);
            daemon.exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static volatile FileServer instance = null;

    public static FileServer getInstance() {
        return instance;
    }

    public static FileServer newInstance(String ip, int port) {
        instance = new FileServer(ip, port);
        return getInstance();
    }

    public static FileServer newInstance(String address) {
        String[] temp = address.split(":");
        instance = new FileServer(temp[0], Integer.valueOf(temp[1]));
        return getInstance();
    }

    public static void deleteInstance() {
        instance = null;
    }

    private FileServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.socketClient = SocketClient.getInstance();
        try {
            this.socketServer = new SocketServer(this.port, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        socketClient.stop();
        executor.shutdownNow();
        JsonProtocolManager.deleteInstance();
        deleteInstance();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setSocketEventHandler(SocketServer.EventHandler handler) {
        socketServer.setEventHandler(handler);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void exec() {
        initSubscriptions();
        SimpleLog.v(Config.getInstance().getId() + " starts at time " + LogicClock.getInstance().getClock());
        new Thread(this.socketServer).start();
    }

    private static String getAddress() {
        try {
            InetAddress id = InetAddress.getLocalHost();
            return id.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Response onReceived(Request o) {
        return processCommonCommand(o);
    }

    @Override
    public void onBound() {

    }

    public Response processCommonCommand(Request o) {
        try {
            CommonCommand command = CommonCommand.valueOf(o.getType());
            return command.execute(o);
        }
        catch (IllegalArgumentException e) {
            return new Response(o).withStatus(Response.STATUS_INVALID_REQUEST)
                    .withMessage(e.getMessage());
        }
    }

    public void send(String address, int port, Request request, SocketClient.ServerCallBack callBack) {
        socketClient.send(address, port, request, callBack);
    }

    public void send(String address, Request request, SocketClient.ServerCallBack callBack) {
        socketClient.send(address, request, callBack);
    }

    @Override
    public void onResponse(Request request, Response o) {
        SimpleLog.i(o);
    }

    @Override
    public void onFailure(Request request, String error) {
        SimpleLog.i(error);
    }

    public void broadcast(Request request) {
        for (PhysicalNode server : Config.getInstance().getServers()) {
            try {
                Request copy = (Request) request.clone();
                copy.withSender(Config.getInstance().getAddress())
                        .withReceiver(server.getAddress())
                        .withReceiverId(server.getId())
                        .withSenderId(Config.getInstance().getId());
                copy.setRequestCallBack(serverCallBack);
                send(server.getAddress(), server.getPort(), copy, asyncServerCallBack);
                SimpleLog.i(Config.getInstance().getId() + " sending " + request.getType() + " to " + copy.getReceiverId() + " at time: " + request.getTimestamp());
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void asyncBroadcast(Request request) {
        for (PhysicalNode server : Config.getInstance().getServers()) {
            Request copy = (Request) request.clone();
            copy.withSender(Config.getInstance().getAddress())
                    .withReceiver(server.getAddress())
                    .withReceiverId(server.getId())
                    .withSenderId(Config.getInstance().getId());
            SimpleLog.i(Config.getInstance().getId() + " sending " + request.getType() + " to " + copy.getReceiverId() + " at time: " + request.getTimestamp());
            send(server.getAddress(), server.getPort(), copy, asyncServerCallBack);
        }
    }

    public void send(Request request) {
        request.withSender(Config.getInstance().getAddress())
                    .withSenderId(Config.getInstance().getId());
        SimpleLog.i(Config.getInstance().getId() + " sending " + request.getType() + " to " + request.getReceiverId() + " at time: " + request.getTimestamp());
        send(request.getReceiver(), getPort(), request, asyncServerCallBack);
    }

    public void initSubscriptions() {
        AckVector.getInstance().init(Config.getInstance().getServers());
        FileManager.getInstance().init(Config.getInstance().getFiles());
        AckVector.getInstance().addObserver(FileManager.getInstance());
    }

}
