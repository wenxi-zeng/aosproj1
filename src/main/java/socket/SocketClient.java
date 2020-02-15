package socket;

import commonmodels.transport.Request;
import commonmodels.transport.Response;
import util.SimpleLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketClient implements Runnable{

    private Selector selector;

    private static volatile SocketClient instance = null;

    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    private final Map<String, ClientHandler> handlerCache;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Queue<Attachable> attachments = new LinkedList<Attachable>() {
        @Override
        public boolean add(Attachable attachable) {
            boolean result = super.add(attachable);
            selector.wakeup();
            return result;
        }
    };

    private SocketClient() {
        handlerCache = new HashMap<>();

        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SocketClient getInstance() {
        if (instance == null) {
            synchronized(SocketClient.class) {
                if (instance == null) {
                    instance = new SocketClient();
                    instance.start();
                }
            }
        }

        return instance;
    }

    public static SocketClient newInstance() {
        SocketClient client = new SocketClient();
        client.start();
        return client;
    }

    public static void deleteInstance() {
        instance = null;
    }

    public void start() {
        executor.execute(this);
    }

    public void stop() {
        executor.shutdownNow();
        deleteInstance();
    }

    public void send(int port, Request data, ServerCallBack callBack) {
        send(new InetSocketAddress("localhost", port), data, callBack);
    }

    public void send(String address, int port, Request data, ServerCallBack callBack) {
        send(new InetSocketAddress(address, port), data, callBack);
    }

    public void send(String address, Request data, ServerCallBack callBack) {
        String[] args = address.split(" ");
        String[] address1 = args[0].split(":");
        send(new InetSocketAddress(address1[0], Integer.valueOf(address1[1])), data, callBack);
    }

    public void send(InetSocketAddress inetSocketAddress, Request data, ServerCallBack callBack) {
        try {
            registerRequest(inetSocketAddress, data, callBack);
        } catch (IOException e) {
            callBack.onFailure(data, "Remote " + inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort() + " throws "  + e.getMessage());
        }
    }

    private void registerRequest(InetSocketAddress remote, Request data, ServerCallBack callBack) throws IOException {
        String key = remote.getHostName() + ":" + remote.getPort();
        ClientHandler handler = handlerCache.get(key);
        if (handler == null || !handler.isConnected()) {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(remote);
            ClientHandler connector = new Connector(socketChannel, data,
                    (selectionKey, dataPool) -> {
                        ClientHandler ch = new ClientReadWriteHandler(selectionKey, dataPool, callBack);
                        handlerCache.put(key, ch);
                        attachments.add(ch);
                    });
            handlerCache.put(key, connector);
            attachments.add(connector);
        }
        else {
            handler.put(data);
        }
    }

    @Override
    public void run() {
        try {
            while (keepRunning.get()) {
                registerAttachments();
                selector.select();
                Iterator it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey sk = (SelectionKey) it.next();
                    it.remove();
                    Runnable r = (Runnable) sk.attachment(); // handler or acceptor callback/runnable
                    if (r != null) {
                        r.run();
                    }
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            SimpleLog.e(ex);
        }
    }

    private void registerAttachments() throws IOException {
        if (attachments.isEmpty()) return;

        while (!attachments.isEmpty()) {
            Attachable attachable = attachments.poll();
            if (attachable != null)
                attachable.attach(selector);
        }
    }

    public interface ServerCallBack {
        void onResponse(Request request, Response response);
        void onFailure(Request request, String error);
    }
}
