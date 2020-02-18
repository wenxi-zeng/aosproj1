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
            LogicClock.getInstance().increment();
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

            request.withType(CommonCommand.APPEND_ONLY.name())
                    .withTimestamp(LogicClock.getInstance().getClock());
            FileServer.getInstance().broadcast(request);

            LogicClock.getInstance().increment();
            request.withType(CommonCommand.RELEASE.name())
                    .withTimestamp(LogicClock.getInstance().getClock());
            FileServer.getInstance().broadcast(request);

            return new Response(request)
                    .withMessage("successful append")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    APPEND_ONLY {
        @Override
        public Response execute(Request request) {
            try {
                FileHelper.append(request.getHeader(), request.getAttachment());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new Response(request)
                    .withMessage("successful append")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    RELEASE {
        @Override
        public Response execute(Request request) {
            LogicClock.getInstance().increment(request.getTimestamp());
            FileManager.getInstance().release(request);
            AckVector.getInstance().updateClock(request.getSender(), request.getTimestamp());
            return new Response(request)
                    .withMessage("successful release")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    REQUEST {
        @Override
        public Response execute(Request request) {
            LogicClock.getInstance().increment(request.getTimestamp());
            FileManager.getInstance().serve(request);
            AckVector.getInstance().updateClock(request.getSender(), request.getTimestamp());

            LogicClock.getInstance().increment();
            request.withType(CommonCommand.ACK.name())
                    .withTimestamp(LogicClock.getInstance().getClock())
                    .withReceiver(request.getSender());
            FileServer.getInstance().send(request);

            return new Response(request)
                    .withMessage("successful request")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    },

    ACK {
        @Override
        public Response execute(Request request) {
            LogicClock.getInstance().increment(request.getTimestamp());
            AckVector.getInstance().updateClock(request.getSender(), request.getTimestamp());
            return new Response(request)
                    .withMessage("successful ack")
                    .withTimestamp(LogicClock.getInstance().getClock());
        }
    }

}
