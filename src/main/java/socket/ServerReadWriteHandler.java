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

public class ServerReadWriteHandler implements Runnable, Attachable {
    private final SocketChannel socketChannel;
    private SelectionKey selectionKey;

    private static final int READ_BUF_SIZE = 32 * 1024;
    private static final int WRITE_BUF_SIZE = 32 * 1024;
    private ByteBuffer[] _readBuf;
    private ByteBuffer[] _writeBuf;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private SocketServer.EventHandler eventHandler;
    private boolean processScheduled;
    private int size;

    public ServerReadWriteHandler(SocketChannel socketChannel, SocketServer.EventHandler eventHandler) {
        this.socketChannel = socketChannel;
        this.eventHandler = eventHandler;
        this.processScheduled = false;

        this._writeBuf = new ByteBuffer[2];
        this._writeBuf[1] = ByteBuffer.allocate(WRITE_BUF_SIZE);
        this._writeBuf[0] = ByteBuffer.allocate(Integer.BYTES);
        this._readBuf = new ByteBuffer[2];
        this._readBuf[1] = ByteBuffer.allocate(READ_BUF_SIZE);
        this._readBuf[0] = ByteBuffer.allocate(Integer.BYTES);
        this.size = Integer.MIN_VALUE;
    }

    @Override
    public void run() {
        try {
            if (!this.selectionKey.isValid() && !this.socketChannel.isOpen()) return;
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

    private synchronized void process() {
        byte[] byteArray = bos.toByteArray();
        if (byteArray.length == 0) {
            reset();
            return;
        }

        Transportable o = JsonProtocolManager.getInstance().readGzip(byteArray);
        if (o instanceof Request) {
            Request req = (Request) o;
            Response response = eventHandler.onReceived(req);
            _writeBuf[1] = JsonProtocolManager.getInstance().writeGzip(response);
            _writeBuf[0].putInt(_writeBuf[1].remaining());
            _writeBuf[0].flip();

            switchMode(SelectionKey.OP_WRITE);
        }
        else {
            reset();
        }
    }

    private synchronized void read() throws IOException {
        if (this.socketChannel.read(_readBuf) == -1){
            this.selectionKey.cancel();
            this.socketChannel.close();
            return;
        }

        int numBytes;
        _readBuf[1].flip();

        if (size == Integer.MIN_VALUE) {
            _readBuf[0].flip();
            size = _readBuf[0].getInt();
        }
        numBytes = _readBuf[1].remaining();

         // SimpleLog.v("[" + socketChannel.getRemoteAddress() + "] Server: read bytes " + numBytes + ", total size: " + size);

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
            if (!processScheduled) {
                processScheduled = true;
                SocketServer.getWorkerPool().execute(this::process);
            }
        }
        else {
            _readBuf[1].clear();
        }
    }

    private void write() throws IOException {
        this.socketChannel.write(_writeBuf);

        if (!_writeBuf[0].hasRemaining() && !_writeBuf[1].hasRemaining()) {
            reset();
            switchMode(SelectionKey.OP_READ);
        }
    }

    private void reset() {
        size = Integer.MIN_VALUE;
        processScheduled = false;
        _readBuf[0].clear();
        _readBuf[1].clear();
        _writeBuf[0].clear();
        _writeBuf[1].clear();
        bos.reset();
    }

    private void switchMode(int mode) {
        this.selectionKey.selector().wakeup();
        this.selectionKey.interestOps(mode);
    }

    @Override
    public void attach(Selector selector) throws IOException {
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
    }
}