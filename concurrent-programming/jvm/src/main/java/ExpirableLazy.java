import java.util.function.Supplier;

public class ExpirableLazy<T> {

    private T value;
    private final Supplier<T> provider;
    private long expirationTime;
    private long timeToLive;
    private final Object monitor;

    public ExpirableLazy(Supplier<T> provider, long timeToLive) {
        this.provider = provider;
        this.timeToLive = timeToLive;
        this.monitor = new Object();
        calculateAndSetExpirationTime();
    }

    public T getValue() {
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

    private boolean timeExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    private void calculateAndSetExpirationTime() {
        expirationTime = System.currentTimeMillis() + timeToLive;
    }
}
