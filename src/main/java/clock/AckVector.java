package clock;

import java.util.*;

public class AckVector extends Observable {

    private static volatile AckVector instance = null;

    private Map<String, Long> vector;

    private AckVector() {
        vector = new HashMap<>();
    }

    public static AckVector getInstance() {
        if (instance == null) {
            synchronized(AckVector.class) {
                if (instance == null) {
                    instance = new AckVector();
                }
            }
        }

        return instance;
    }

    public void init(List<String> nodes) {
        for (String node : nodes)
            vector.put(node, 0L);
    }

    public List<Long> getClocks() {
        return new ArrayList<>(vector.values());
    }

    public long getClock(String node) {
        return vector.get(node);
    }

    public void updateClock(String node, long vector) {
        this.vector.put(node, vector);
        this.setChanged();
        this.notifyObservers(getClocks());
    }
}
