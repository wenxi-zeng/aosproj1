package socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Recycler implements Attachable {

    private final SelectionKey selectionKey;

    public Recycler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void attach(Selector selector) throws IOException {
        recycle();
    }

    private void recycle() throws IOException {
        selectionKey.cancel();
//        selectionKey.channel().close();
    }
}
