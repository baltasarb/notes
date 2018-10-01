import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedExchanger<T> {

    private final ReentrantLock monitor;
    private HashMap<Integer, Exchanger> exchangers;

    public KeyedExchanger() {
        monitor = new ReentrantLock();
        exchangers = new HashMap<>();
    }

    /**
     *
     * @param key the pair key, belonging to at least two different threads
     * @param myData the data that the calling thread wants to give to the other
     * @param timeout the time the current thread is willing to wait for the data of another thread
     * @return the data exchanged or an empty optional
     * @throws InterruptedException
     *
     *  The algorithm behaves as such:
     *
     *  establish waiting limit time
     *
     *  if time has expired
     *      remove the other (if present) from the hashmap
     *      return optional.empty
     *
     *  if the corresponding pair exists
     *     give the corresponding object the data
     *     remove it from the hashmap
     *     notify it so it can return the new data.
     *     return the data exchanged with the other object of the pair
     *
     *  else
     *      create exchanger object and add to hashmap
     *      wait for another exchanger to arrive
     *
     *  on wait exit
     *      if exit was from notify
     *          return the new data
     *      if time has expired
     *          remove from hasmap
     *          return
     *      else
     *          keep waiting
     */
    public Optional<T> exchange(int key, T myData, int timeout) throws InterruptedException {
        long expirationTime = getExpirationTime(timeout);

        try {
            monitor.lock();

            Exchanger other = exchangers.get(key);

            if (timeExpired(expirationTime)) {
                if (other != null) {//maybe other keeps waiting for pair??
                    exchangers.remove(key);
                    other.condition.signal();
                }
                return Optional.empty();
            }

            if (other != null) {
                System.out.println("Pair found!");
                exchangers.remove(key);
                Optional<T> otherData = Optional.of(other.myData); //after being signaled data may no longer be available
                other.otherData = myData;
                other.condition.signal();
                return otherData;
            }

            Exchanger me = new Exchanger(myData, monitor.newCondition());
            exchangers.put(key, me);

            // todo
            // !!! may be negative time !!!
            // recalculation of time left
            long timeToWait = expirationTime - System.currentTimeMillis();

            while (true) {
                System.out.println("waiting...");
                try{
                    me.condition.await(timeToWait, TimeUnit.MILLISECONDS);
                }catch (InterruptedException e){
                    if (me.otherData != null) {
                        Thread.currentThread().interrupt();
                        return Optional.of(me.otherData);
                    }
                    if(exchangers.get(key) != null)
                        exchangers.remove(key);
                    throw e;
                }

                if (me.otherData != null) {
                    return Optional.of(me.otherData);
                }

                if (timeExpired(expirationTime)) {
                    exchangers.remove(key);
                    return Optional.empty();
                }

                // todo
                // !!! may be negative time !!!
                // recalculation of time left
                timeToWait = expirationTime - System.currentTimeMillis();

            }
        } finally {
            monitor.unlock();
        }

    }

    private class Exchanger {
        T myData;
        T otherData;
        Condition condition;

        Exchanger(T myData, Condition condition) {
            this.myData = myData;
            this.condition = condition;
        }
    }

    private boolean timeExpired(long timeToExpiration) {
        return System.currentTimeMillis() > timeToExpiration;
    }

    private long getExpirationTime(int timeout) {
        return System.currentTimeMillis() + timeout;
    }
}
