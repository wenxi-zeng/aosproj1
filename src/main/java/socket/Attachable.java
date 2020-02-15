package socket;

import java.io.IOException;
import java.nio.channels.Selector;

public interface Attachable {
    void attach(Selector selector) throws IOException;
}
