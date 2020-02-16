package commands;

import clock.AckVector;
import clock.LogicClock;
import commonmodels.transport.Request;
import commonmodels.transport.Response;
import drivers.FileServer;
import managers.FileManager;
import util.FileHelper;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public enum CommonCommand implements Command {

    APPEND {
        @Override
        public Response execute(Request request) {
            request.withType(CommonCommand.REQUEST.name())
                    .withTimestamp(LogicClock.getInstance().getClock());
            FileServer.getInstance().broadcast(request);

            FileManager.getInstance().serve(request);
            try {
                request.setProcessed(new Semaphore(0));
                request.getProcessed().acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            request.withType(CommonCommand.RELEASE.name())
                    .withTimestamp(LogicClock.getInstance().getClock());
            FileServer.getInstance().broadcast(request);

            return new Response(request)
                    .withMessage("successful append")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    RELEASE {
        @Override
        public Response execute(Request request) {
            AckVector.getInstance().updateClock(request.getSender(), request.getTimestamp());
            return new Response(request)
                    .withMessage("successful release")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    REQUEST {
        @Override
        public Response execute(Request request) {
            FileManager.getInstance().serve(request);
            return new Response(request)
                    .withMessage("successful ack")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    }

}
