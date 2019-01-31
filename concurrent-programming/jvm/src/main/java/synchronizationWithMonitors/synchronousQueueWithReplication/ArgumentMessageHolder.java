package synchronizationWithMonitors.synchronousQueueWithReplication;

public class ArgumentMessageHolder<T> {
    private final T message;

    public ArgumentMessageHolder(T message) {
        this.message = message;
    }
}
