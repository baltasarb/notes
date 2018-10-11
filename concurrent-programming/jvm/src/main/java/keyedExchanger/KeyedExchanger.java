package keyedExchanger;

import java.util.HashMap;
import java.util.Optional;

public class KeyedExchanger<T> {

    private final Object monitor;
    private HashMap<Integer, Exchanger<T>> exchangers;

    public KeyedExchanger() {
        this.monitor = new Object();
        exchangers = new HashMap<>();
    }

    public Optional<T> exchange(int key, T myData, int timeout) throws InterruptedException {
        synchronized (monitor){
            Exchanger<T> exchanger = exchangers.get(key);
            if(exchanger != null){
                return exchanger.exchange(myData, timeout);
            }

            exchanger = new Exchanger<>();
            exchangers.put(key, exchanger);
            return exchanger.exchange(myData, timeout);
        }
    }

}
