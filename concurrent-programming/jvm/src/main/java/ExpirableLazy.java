import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ExpirableLazy<T> {

    private final Supplier<T> provider;
    private final ReentrantLock monitor;
    private long expirationTime;
    private long timeToLive;
    private T value;
    private boolean valueIsBeingCalculated;

    ExpirableLazy(Supplier<T> provider, long timeToLive) {
        this.provider = provider;
        this.timeToLive = timeToLive;
        this.monitor = new ReentrantLock();
        calculateAndSetExpirationTime();
        valueIsBeingCalculated = false;
    }

    T getValue() throws Exception {
        synchronized (monitor) {
            if (value != null && !timeExpired())
                return value;

            if (!valueIsBeingCalculated) {
                valueIsBeingCalculated = true;
            } else {
                while (true) {
                    monitor.wait();

                    if (value != null && !timeExpired())
                        return value;

                    if (!valueIsBeingCalculated) {
                        valueIsBeingCalculated = true;
                        break;
                    }
                }
            }
        }

        Exception providerException = null;
        T calculatedValue = null;

        try {
            calculatedValue = provider.get();
        } catch (Exception e) {
            providerException = e;
        }

        synchronized (monitor) {
            valueIsBeingCalculated = false;

            if (providerException != null)
                throw new IllegalStateException(providerException);

            value = calculatedValue;
            calculateAndSetExpirationTime();

            monitor.notifyAll();
            return value;
        }
    }

    private boolean timeExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    private void calculateAndSetExpirationTime() {
        expirationTime = System.currentTimeMillis() + timeToLive;
    }
}
