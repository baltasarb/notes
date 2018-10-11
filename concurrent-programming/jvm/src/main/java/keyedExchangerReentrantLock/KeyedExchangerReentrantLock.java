package keyedExchangerReentrantLock;

import Utils.Timer;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedExchangerReentrantLock<T> {

    private final ReentrantLock monitor;
    private HashMap<Integer, ExchangerReentrantLock<T>> exchangers;

    public KeyedExchangerReentrantLock() {
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

        if (myData == null || timeout <= 0) {
            String errorMessage = generateInvalidArgumentError(myData, timeout);
            throw new IllegalArgumentException(errorMessage);
        }

        monitor.lock();

        ExchangerReentrantLock<T> other = exchangers.get(key);

        if (other != null) {
            exchangers.remove(key);
            Optional<T> data = Optional.of(other.data); //after being signaled data may no longer be available
            other.data = myData;
            other.condition.signal();
            monitor.unlock();
            return data;
        }

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        ExchangerReentrantLock<T> me = new ExchangerReentrantLock<>(myData, monitor.newCondition());
        exchangers.put(key, me);

        long timeToWait = timer.getTimeLeftToWait();

        try {
            while (true) {

                me.condition.await(timeToWait, TimeUnit.MILLISECONDS);

                if (!me.data.equals(myData)) {
                    return Optional.of(me.data);
                }

                timeToWait = timer.getTimeLeftToWait();

                if (timer.timeExpired() || timeToWait <= 0) {
                    exchangers.remove(key);
                    return Optional.empty();
                }
            }
        } catch (InterruptedException e) {
            if (!me.data.equals(myData)) {
                Thread.currentThread().interrupt();
                return Optional.of(me.data);
            }
            if (exchangers.get(key) != null){
                exchangers.remove(key);
            }

            throw e;
        } finally {
            monitor.unlock();
        }
    }

    private String generateInvalidArgumentError(T data, int timeout) {
        StringBuilder stringBuilder = new StringBuilder();

        if (data == null)
            stringBuilder.append("Data cannot be null. ");
        if (timeout >= 0)
            stringBuilder.append("Timeout must be > 0.");

        return String.format("Invalid argument : %s", stringBuilder.toString());
    }

}
