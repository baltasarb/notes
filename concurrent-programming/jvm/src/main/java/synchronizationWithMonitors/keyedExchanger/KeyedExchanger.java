package synchronizationWithMonitors.keyedExchanger;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedExchanger<T> {

    private final ReentrantLock monitor;

    private HashMap<Integer, Exchanger<T>> exchangers;

    public KeyedExchanger() {
        this.monitor = new ReentrantLock();
        exchangers = new HashMap<>();
    }

    public Optional<T> exchange(int key, T myData, int timeout) throws InterruptedException {
        monitor.lock();
        Exchanger<T> exchanger = exchangers.get(key);

        try {
            if (exchanger == null) {
                exchanger = new Exchanger<>();
                exchangers.put(key, exchanger);
            }
        } finally {
            monitor.unlock();
        }

        return exchanger.exchange(myData, timeout);
    }

}
