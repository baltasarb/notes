package keyedExchanger;

import Utils.Timer;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedExchanger<T> {

    private final ReentrantLock monitor;
    private HashMap<Integer, Exchanger<T>> exchangers;

    public KeyedExchanger() {
        monitor = new ReentrantLock();
        exchangers = new HashMap<>();
    }

    /**
     * @param key     the pair key, belonging to at least two different threads
     * @param myData  the data that the calling thread wants to give to the other
     * @param timeout the time the current thread is willing to wait for the data of another thread
     * @return the data exchanged or an empty optional
     * @throws InterruptedException propagates thread interruption
     *                              <p>
     *                              The algorithm behaves as such:
     *                              <p>
     *                              establish waiting limit time
     *                              <p>
     *                              if time has expired
     *                              return optional.empty
     *                              <p>
     *                              if the corresponding pair exists
     *                              give the corresponding object the data
     *                              remove it from the hash map
     *                              notify it so it can return the new data.
     *                              return the data exchanged with the other object of the pair
     *                              else
     *                              create exchanger object and add to hash map
     *                              wait for another exchanger to arrive
     *                              <p>
     *                              on wait exit
     *                              if exit was from notify
     *                              return the new data
     *                              if time has expired
     *                              remove from hash map
     *                              return
     *                              else
     *                              keep waiting
     */
    public Optional<T> exchange(int key, T myData, int timeout) throws InterruptedException {
        Timer timer = new Timer(timeout);

        if (timeout <= 0) {
            return Optional.empty();
        }

        try {
            monitor.lock();

            Exchanger<T> other = exchangers.get(key);

            if (other != null) {
                exchangers.remove(key);
                Optional<T> data = Optional.of(other.data); //after being signaled data may no longer be available
                other.data = myData;
                other.condition.signal();
                return data;
            }

            if (timer.timeExpired()) {
                return Optional.empty();
            }

            Exchanger<T> me = new Exchanger<>(myData, monitor.newCondition());
            exchangers.put(key, me);

            long timeToWait = timer.getTimeLeftToWait();

            while (true) {
                try {
                    me.condition.await(timeToWait, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (!me.data.equals(myData)) {
                        Thread.currentThread().interrupt();
                        return Optional.of(me.data);
                    }
                    if (exchangers.get(key) != null)
                        exchangers.remove(key);

                    throw e;
                }

                if (!me.data.equals(myData)) {
                    return Optional.of(me.data);
                }

                timeToWait = timer.getTimeLeftToWait();

                if (timer.timeExpired() || timeToWait <= 0) {
                    exchangers.remove(key);
                    return Optional.empty();
                }

            }
        } finally {
            monitor.unlock();
        }

    }

}
