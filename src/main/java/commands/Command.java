package commands;

import commonmodels.transport.Request;
import commonmodels.transport.Response;

public interface Command {

    Response execute(Request request);

}
