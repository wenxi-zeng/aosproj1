package commonmodels;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BoundedQueue<T> extends LinkedList<T> {

    private final int capacity;

    public BoundedQueue(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean add(T t) {
        if (contains(t)) return false;
        if (size() < capacity) {
            return super.add(t);
        }
        else {
            poll();
            return super.add(t);
        }
    }

    public List<T> toList() {
        return new ArrayList<>(this);
    }
}
