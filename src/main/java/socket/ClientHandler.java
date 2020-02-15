package socket;

import commonmodels.transport.Request;

public interface ClientHandler extends Attachable {

    void put(Request data);

    boolean isConnected();

}
