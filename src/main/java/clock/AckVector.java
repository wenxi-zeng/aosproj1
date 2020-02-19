package clock;

import commonmodels.PhysicalNode;
import util.SimpleLog;

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

    public void init(List<PhysicalNode> nodes) {
        for (PhysicalNode node : nodes)
            vector.put(node.getAddress(), 0L);
    }

    public List<Long> getClocks() {
        return new ArrayList<>(vector.values());
    }

    public long getClock(String node) {
        return vector.get(node);
    }

    public void updateClock(String node, long clock) {
        this.vector.put(node, clock);
        this.setChanged();
        this.notifyObservers(getClocks());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : vector.keySet()) {
            sb.append(key).append(": ").append(vector.get(key)).append("\n");
        }
        return sb.toString();
    }
}
