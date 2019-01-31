package synchronizationWithMonitors.keyedChannel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

class MessageHolder<T> {

    private Condition condition;
    private T message;

    MessageHolder(Condition condition) {
        this.condition = condition;
    }

    MessageHolder(T message) {
        this.message = message;
    }

    void setMessage(T message) {
        this.message = message;
    }

    T getMessage() {
        return message;
    }

    void signal() {
        condition.signal();
    }

    public void await(long timeLeftToWait) throws InterruptedException {
        condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);
    }
}
