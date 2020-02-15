package socket;

import commonmodels.TransportableString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class AsyncSocketClient extends Thread {

    private String host;
    private int port;
    private AsynchronousSocketChannel channel;
    private Queue<String> queue;
    private CountDownLatch clientLatch;

    public AsyncSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.queue = new LinkedList<>();
    }

    public AsyncSocketClient(String address) {
        String[] address1 = address.split(":");
        this.host = address1[0];
        this.port = Integer.valueOf(address1[1]);
        this.queue = new LinkedList<>();
    }

    @Override
    public void run() {
        try {
            channel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            return;
        }

        clientLatch = new CountDownLatch(1);
        channel.connect(new InetSocketAddress(host, port), this, new ConnectHandler(Thread.currentThread()));
        try {
            clientLatch.await();

            while (!queue.isEmpty()) {
                processMessage(queue.poll());
            }
        } catch (InterruptedException e) {
            clientLatch.countDown();
        }
        finally {
            closeChannel();
        }

    }

    public void send(String msg) {
        queue.add(msg);
    }

    private void processMessage(String msg) {
        CountDownLatch requestLatch = new CountDownLatch(1);
        Thread requestThread = Thread.currentThread();
        try {
            ByteBuffer buffer = JsonProtocolManager.getInstance().writeGzip(new TransportableString(msg));
            channel.write(buffer, buffer, new WriteHandler(channel, requestLatch, requestThread));
            requestLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            requestLatch.countDown();
        }
    }
    
    private void closeChannel() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {
        private AsynchronousSocketChannel channel;
        private CountDownLatch requestLatch;
        private Thread requestThread;

        public WriteHandler(AsynchronousSocketChannel clientChannel, CountDownLatch requestLatch, Thread requestThread) {
            this.channel = clientChannel;
            this.requestLatch = requestLatch;
            this.requestThread = requestThread;
        }

        @Override
        public void completed(final Integer result, ByteBuffer buffer) {
            channel.write(buffer, buffer, this);
            requestLatch.countDown();
        }

        @Override
        public void failed(Throwable e, ByteBuffer attachment) {
            requestThread.interrupt();
            closeChannel();
        }
    }

    private class ConnectHandler implements CompletionHandler<Void, AsyncSocketClient> {
        private Thread requestThread;

        public ConnectHandler(Thread requestThread) {
            this.requestThread = requestThread;
        }

        @Override
        public void completed(Void result, AsyncSocketClient attachment) {
            clientLatch.countDown();
        }

        @Override
        public void failed(Throwable e, AsyncSocketClient client) {
            this.requestThread.interrupt();
            closeChannel();
        }
    }
}