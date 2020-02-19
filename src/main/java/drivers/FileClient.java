package drivers;

import clock.LogicClock;
import commands.CommonCommand;
import commonmodels.PhysicalNode;
import commonmodels.transport.Request;
import commonmodels.transport.Response;
import socket.SocketClient;
import util.Config;
import util.MathX;
import util.SimpleLog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class FileClient {

    private SocketClient socketClient;

    private Semaphore semaphore;

    private SocketClient.ServerCallBack callBack = new SocketClient.ServerCallBack() {
        @Override
        public void onResponse(Request request, Response response) {
            LogicClock.getInstance().increment(response.getTimestamp());
            SimpleLog.v(request.getSenderId() + " receives a successful ack from " + request.getReceiverId());
            semaphore.release();
        }

        @Override
        public void onFailure(Request request, String error) {
            SimpleLog.v(request.getSenderId() + " receives failure from " + request.getReceiverId() + ", error message: " + error);
            semaphore.release();
        }
    };

    public static void main(String[] args) {
        FileClient client = new FileClient();

        if (args.length > 0)
            Config.getInstance().setId(args[0]);
        int daemonPort = Config.PORT;
        if (args.length > 1)
        {
            try
            {
                daemonPort = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                System.err.println ("Invalid server port: " + e);
                return;
            }
            if (daemonPort <= 0 || daemonPort > 65535)
            {
                System.err.println ("Invalid server port");
                return;
            }
        }

        String address = getAddress();
        Config.with(address, daemonPort);
        SimpleLog.with(address, daemonPort);
        client.start();
    }

    public FileClient() {
        socketClient = SocketClient.getInstance();
        semaphore = new Semaphore(0);
    }

    private PhysicalNode choseServer() {
        List<PhysicalNode> pnodes = Config.getInstance().getServers();
        return pnodes.get(MathX.nextInt(pnodes.size()));
    }

    private String choseFile() {
        List<String> files = Config.getInstance().getFiles();
        return files.get(MathX.nextInt(files.size()));
    }

    private void generateRequest() {
        int remainingActions = 100;

        while (remainingActions > 0) {
            try {
                Thread.sleep(MathX.nextInt(0, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            PhysicalNode node = choseServer();
            String file = choseFile();
            long timestamp = LogicClock.getInstance().getClock();

            Request request = new Request()
                    .withAttachment(Config.getInstance().getId() + " message #" + (101 - remainingActions) + " -- " + node.getId())
                    .withHeader(file)
                    .withReceiver(node.getAddress())
                    .withSender(Config.getInstance().getAddress())
                    .withReceiverId(node.getId())
                    .withSenderId(Config.getInstance().getId())
                    .withTimestamp(timestamp)
                    .withType(CommonCommand.APPEND.name());

            SimpleLog.v(Config.getInstance().getId() + " requests: \"" + request.getAttachment() + "\" "  + "for file #" + file + " at time: " + timestamp);
            socketClient.send(node.getAddress(), node.getPort(), request, callBack);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            remainingActions--;
        }
    }

    public void start() {
        SimpleLog.v(Config.getInstance().getId() + " " + "starts at time: " + LogicClock.getInstance().getClock());
        generateRequest();
        onFinished();
        SimpleLog.v(Config.getInstance().getId() + "  gracefully shutdown.");
        System.exit(0);
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

    private void onFinished(){
        socketClient.stop();
    }
}
