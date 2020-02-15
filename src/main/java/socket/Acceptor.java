package socket;

import util.SimpleLog;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable, Attachable {

    private final ServerSocketChannel serverSocketChannel;

    private final CallBack callBack;

    public Acceptor(ServerSocketChannel serverSocketChannel, CallBack callBack) {
        this.serverSocketChannel = serverSocketChannel;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                if (callBack != null)
                    callBack.onAccepted(socketChannel);
            }
        }
        catch (IOException ex) {
            SimpleLog.e(ex);
        }
    }

    @Override
    public void attach(Selector selector) throws IOException{
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(this);
    }

    public interface CallBack {
        void onAccepted(SocketChannel socketChannel);
    }
}