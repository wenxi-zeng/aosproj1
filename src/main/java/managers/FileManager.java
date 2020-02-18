package managers;

import commonmodels.transport.Request;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManager implements Observer {

    private static volatile FileManager instance = null;

    private Map<String, FileWorker> map;

    private ExecutorService executor;

    private FileManager() {
        map = new HashMap<>();
    }

    public static FileManager getInstance() {
        if (instance == null) {
            synchronized(FileManager.class) {
                if (instance == null) {
                    instance = new FileManager();
                }
            }
        }

        return instance;
    }

    public void init(List<String> files) {
        for (String file : files)
            map.put(file, new FileWorker());
        this.executor = Executors.newFixedThreadPool(files.size());
        for (FileWorker worker : map.values())
            executor.execute(worker);
    }

    @Override
    public void update(Observable o, Object arg) {
        @SuppressWarnings("unchecked")
        List<Long> clock = (List<Long>) arg;
        for (FileWorker worker : map.values()) {
            worker.setClocks(clock);
        }
    }

    public void serve(Request request) {
        FileWorker worker = map.get(request.getHeader());
        if (worker != null)
            worker.serve(request);
    }

    public void release(Request request) {
        FileWorker worker = map.get(request.getHeader());
        if (worker != null)
            worker.release(request);
    }
}
