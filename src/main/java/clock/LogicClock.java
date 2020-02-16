package clock;

public class LogicClock {

    private static volatile LogicClock instance = null;

    private static final int DELTA = 1;

    private long clock;

    private LogicClock() {
        this.clock = 0;
    }

    public static LogicClock getInstance() {
        if (instance == null) {
            synchronized(LogicClock.class) {
                if (instance == null) {
                    instance = new LogicClock();
                }
            }
        }

        return instance;
    }

    public long getClock() {
        return clock;
    }

    public void increment() {
        clock += DELTA;
    }

    public void increment(long remoteClock) {
        this.increment();
        this.clock = Math.max(clock, remoteClock + DELTA);
    }
}
