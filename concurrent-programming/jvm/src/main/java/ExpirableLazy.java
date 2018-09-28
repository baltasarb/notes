import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ExpirableLazy<T> {

    private final Supplier<T> provider;
    private final ReentrantLock monitor;
    private long expirationTime;
    private long timeToLive;
    private T value;
    private boolean valueIsBeingCalculated;

    public ExpirableLazy(Supplier<T> provider, long timeToLive) {
        this.provider = provider;
        this.timeToLive = timeToLive;
        this.monitor = new ReentrantLock();
        calculateAndSetExpirationTime();
        valueIsBeingCalculated = false;
    }

    public T getValue() throws Exception {
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

    public T getValue1() {
        if (value != null && !timeExpired())
            return value;

        synchronized (monitor) {
            if (value == null || timeExpired()) {
                value = provider.get();
                calculateAndSetExpirationTime();
            }
            return value;
        }
    }

    public T getValue2() throws InterruptedException {
        if (value != null && !timeExpired())
            return value;

        try {
            while (true) {
                monitor.lock();

                //se nao ha valor, tempo expirou e ninguem a calcular, calcula
                if ((value == null || timeExpired()) && !valueIsBeingCalculated) { // se valor for null ??, outra condicao em vez de == null
                    valueIsBeingCalculated = true;
                    monitor.unlock();

                    value = provider.get();

                    monitor.lock();

                    //
                    calculateAndSetExpirationTime();
                    valueIsBeingCalculated = false;

                    monitor.notify();

                    monitor.unlock();
                    return value;
                }

                if (value != null && !timeExpired()) {
                    monitor.unlock();
                    return value;
                }

                if (valueIsBeingCalculated)
                    monitor.wait();

                monitor.unlock();
            }

        } catch (InterruptedException e) {
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    private boolean timeExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    private void calculateAndSetExpirationTime() {
        expirationTime = System.currentTimeMillis() + timeToLive;
    }
}
