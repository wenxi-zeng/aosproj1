package socket;

import commonmodels.transport.Request;
import util.SimpleLog;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

public class Connector implements Runnable, ClientHandler {

    private final CallBack callBack;

    private final SocketChannel socketChannel;

    private SelectionKey selectionKey;

    private Queue<Request> dataPool;

    public Connector(SocketChannel socketChannel, Request data, CallBack callBack) {
        this.socketChannel = socketChannel;
        this.callBack = callBack;
        this.dataPool = new LinkedList<>();
        this.dataPool.add(data);
    }

    @Override
    public void run() {
        String address = null;
        try {
            address = socketChannel.getRemoteAddress().toString();
            socketChannel.finishConnect();

            if (callBack != null) {
                callBack.onConnected(selectionKey, dataPool);
            }
        }
        catch (IOException ex) {
            SimpleLog.v("[" + address + "] Client: connection error");
            selectionKey.cancel();
            ex.printStackTrace();
            SimpleLog.e(ex);
        }
    }

    @Override
    public void put(Request data) {
        dataPool.add(data);
    }

    @Override
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isOpen() && socketChannel.isConnected();
    }

    @Override
    public void attach(Selector selector) throws IOException {
        selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        selectionKey.attach(this);
    }

    public interface CallBack {
        void onConnected(SelectionKey selectionKey, Queue<Request> dataPool);
    }
}