package managers;

import commonmodels.transport.Request;
import util.FileHelper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWorker implements Runnable{

    private final BlockingQueue<Request> queue;

    private final Semaphore semaphore;

    private final AtomicBoolean working;

    private List<Long> clocks;

    public FileWorker() {
        this.queue = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(0);
        this.working = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        while (working.get()) {
            try {
                Request request = queue.take();

                while (!ackFromAll(request)) {
                    semaphore.acquire();
                }

                operateFile(request);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void operateFile(Request request) {
        try {
            FileHelper.append(request.getHeader(), request.getAttachment());
            if (request.getProcessed() != null)
                request.getProcessed().release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ackFromAll(Request request) {
        for (long clock : clocks) {
            if (request.getTimestamp() > clock) return false;
        }

        return true;
    }

    public void serve(Request request) {
        try {
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setClocks(List<Long> clocks) {
        this.clocks = clocks;
        this.semaphore.release();
    }
}
