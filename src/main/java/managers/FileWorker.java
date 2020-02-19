package managers;

import commonmodels.transport.Request;
import util.Config;
import util.FileHelper;

import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWorker implements Runnable{

    private final Queue<Request> queue;

    private final Semaphore semaphore;

    private final AtomicBoolean working;

    private List<Long> clocks;

    public FileWorker() {
        this.queue = new PriorityQueue<>((a, b) ->
                (int)(a.getTimestamp() - b.getTimestamp() != 0 ?
                        a.getTimestamp() - b.getTimestamp() :
                        a.getSender().compareTo(b.getSender())));
        this.semaphore = new Semaphore(0);
        this.working = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        while (working.get()) {
            try {

                while (queue.isEmpty() ||
                        !queue.peek().getSender().equals(Config.getInstance().getAddress()) ||
                        !ackFromAll(queue.peek())) {
                    semaphore.acquire();
                }

                if (!queue.isEmpty() && queue.peek().getSender().equals(Config.getInstance().getAddress()))
                    operateFile(queue.poll());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void operateFile(Request request) {
        try {
            FileHelper.append(Config.getInstance().getId(), request.getHeader(), request.getAttachment());
            if (request.getProcessed() != null)
                request.getProcessed().release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ackFromAll(Request request) {
        if (clocks == null) return false;

        for (long clock : clocks) {
            if (request.getTimestamp() > clock) return false;
        }

        return true;
    }

    public void serve(Request request) {
        queue.add(request);
        this.semaphore.release();
    }

    public void release(Request request) {
        if (!queue.isEmpty() &&
                queue.peek().getSender().equals(request.getSender()))
            queue.poll();
    }

    public void setClocks(List<Long> clocks) {
        this.clocks = clocks;
        this.semaphore.release();
    }
}
