package socket;

import commonmodels.Transportable;
import commonmodels.transport.Request;
import commonmodels.transport.Response;
import util.ObjectConverter;
import util.SimpleLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

public class ClientReadWriteHandler implements Runnable, ClientHandler {
    private final SocketChannel socketChannel;
    private final SocketClient.ServerCallBack callBack;
    private final Queue<Request> dataPool;

    private static final int READ_BUF_SIZE = 32 * 1024;

    private SelectionKey selectionKey;
    private ByteBuffer[] _readBuf;
    private ByteBuffer[] _writeBuf;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private Request currentData = null;
    private int size;

    public ClientReadWriteHandler(SelectionKey selectionKey, Queue<Request> dataPool, SocketClient.ServerCallBack callBack) {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel)selectionKey.channel();
        this.callBack = callBack;
        this.dataPool = new LinkedList<>();
        while(!dataPool.isEmpty())
            this.dataPool.add(dataPool.poll());
    }

    @Override
    public void put(Request data) {
        dataPool.add(data);
        if (currentData == null)
            switchMode(SelectionKey.OP_WRITE);
    }

    @Override
    public void run() {
        try {
            if (!this.selectionKey.isValid() || !this.socketChannel.isOpen()) return;
            if (this.selectionKey.isReadable()) {
                read();
            }
            else if (this.selectionKey.isWritable()) {
                write();
            }
        }
        catch (IOException ex) {
            SimpleLog.e(ex);
        }
    }

    private void process() {
        byte[] byteArray = bos.toByteArray();
        int respSize = byteArray.length;
        if (respSize == 0) return;

        Transportable o = JsonProtocolManager.getInstance().readGzip(byteArray);
        if (o instanceof Response) {
            Response resp = (Response) o;
            if (currentData.getRequestCallBack() == null)
                callBack.onResponse(currentData, resp);
            else
                currentData.getRequestCallBack().onResponse(currentData, resp);
        }
        else {
            if (currentData.getRequestCallBack() == null)
                callBack.onFailure(currentData, String.valueOf(o));
            else
                currentData.getRequestCallBack().onFailure(currentData, String.valueOf(o));
        }
    }

    private synchronized void read() throws IOException {
        if (this.socketChannel.read(_readBuf) == -1){
            this.selectionKey.cancel();
            this.socketChannel.close();
            SimpleLog.i("client read -1. closing channel");
            return;
        }

        int numBytes;
        _readBuf[1].flip();

        if (size == Integer.MIN_VALUE) {
            _readBuf[0].flip();
            size = _readBuf[0].getInt();
        }
        numBytes = _readBuf[1].remaining();

        // SimpleLog.v("[" + socketChannel.getRemoteAddress() + "] Client: read bytes " + numBytes);

        if (size < 0) {
            reset();
            return;
        }
        if (numBytes >= size) {
            // object fully arrived
            bos.write(ObjectConverter.getBytes(_readBuf[1], size));
            size = 0;
        }
        else {
            // object partially arrived, need to read again
            bos.write(ObjectConverter.getBytes(_readBuf[1], numBytes));
            size -= numBytes;
        }

        if (size <= 0 && size != Integer.MIN_VALUE) {
            process();
            reset();
            switchMode(SelectionKey.OP_WRITE);
        }
        else {
            _readBuf[1].clear();
        }
    }

    private void serveData() {
        currentData = dataPool.poll();
        if (currentData == null) return;

        this._writeBuf = new ByteBuffer[2];
        this._writeBuf[1] = JsonProtocolManager.getInstance().writeGzip(currentData);
        this._writeBuf[0] = ByteBuffer.allocate(Integer.BYTES);
        this._writeBuf[0].putInt(_writeBuf[1].remaining());
        this._writeBuf[0].flip();
        // SimpleLog.v("Client write buffer: position " + _writeBuf[1].position() + ", remaining " + _writeBuf[1].remaining());
        this._readBuf = new ByteBuffer[2];
        this._readBuf[1] = ByteBuffer.allocate(READ_BUF_SIZE);
        this._readBuf[0] = ByteBuffer.allocate(Integer.BYTES);

        this.size = Integer.MIN_VALUE;
    }

    private void write() throws IOException {
        if (currentData == null)
            serveData();
        if (currentData == null) return;

        this.socketChannel.write(_writeBuf);

        if (!_writeBuf[0].hasRemaining() && !_writeBuf[1].hasRemaining()) {
            switchMode(SelectionKey.OP_READ);
        }
    }

    private void switchMode(int mode) {
        this.selectionKey.selector().wakeup();
        this.selectionKey.interestOps(mode);
    }

    private void reset() {
        size = Integer.MIN_VALUE;
        _readBuf[0].clear();
        _readBuf[1].clear();
        _writeBuf[0].clear();
        _writeBuf[1].clear();
        bos.reset();
        currentData = null;
    }

    @Override
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isOpen() && socketChannel.isConnected();
    }

    @Override
    public void attach(Selector selector) throws IOException {
        this.selectionKey = this.socketChannel.register(selector, SelectionKey.OP_WRITE);
        this.selectionKey.attach(this);
    }
}