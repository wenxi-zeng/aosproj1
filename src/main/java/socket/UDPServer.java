package socket;

import util.ObjectConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPServer implements Runnable {

    private final Selector selector;

    private final DatagramChannel datagramChannel;

    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    public UDPServer(int port, EventHandler eventHandler) throws IOException {
        selector = Selector.open();
        datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.socket().setReuseAddress(true);
        datagramChannel.socket().bind(new InetSocketAddress(port));
        registerShutdownHook();
        new Handler(selector, datagramChannel, eventHandler);
    }

    public void run() {
        try {
            while (keepRunning.get()) {
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
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (datagramChannel != null && datagramChannel.isOpen()) {
                    selector.close();
                    datagramChannel.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public interface EventHandler {
        void onReceived(Object o);
    }

    private class Handler implements Runnable {

        private final DatagramChannel _datagramChannel;

        private final EventHandler _eventHandler;

        private final SelectionKey _selectionKey;

        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        private static final int READ_BUF_SIZE = 128 * 1024;

        private ByteBuffer _readBuf = ByteBuffer.allocate(READ_BUF_SIZE);

        private Handler(Selector selector, DatagramChannel datagramChannel, EventHandler eventHandler) throws IOException{
            _datagramChannel = datagramChannel;
            _eventHandler = eventHandler;
            _selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ);
            _selectionKey.attach(this);
            selector.wakeup();
        }

        @Override
        public void run() {
            try {
                read();
            } catch (IOException e) {
                _selectionKey.cancel();
                e.printStackTrace();
            }
        }

        private void read() throws IOException {
            SocketAddress sender =_datagramChannel.receive(_readBuf);

            if (sender != null) {
                _readBuf.flip();
                bos.write(ObjectConverter.getBytes(_readBuf));
                Object o = JsonProtocolManager.getInstance().readGzip(bos.toByteArray());
                process(o);
                _readBuf.clear();
                bos.reset();
            }
        }

        private void process(Object o) {
            if (o != null) {
                _eventHandler.onReceived(o);
            }
        }
    }
}