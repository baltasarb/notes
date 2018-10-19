package messageQueue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ConsumerStatus<T> implements SendStatus {

    private final Condition condition;

    private T message;

    ConsumerStatus(Condition condition) {
        this.condition = condition;
        message = null;
    }

    void sendMessageAndsignal(T message) {
        this.message = message;
        condition.signal();
    }

    void await(long timeout) throws InterruptedException {
        condition.await(timeout, TimeUnit.MILLISECONDS);
    }

    boolean receivedMessage() {
        return message != null;
    }

    public T getMessage() {
        return message;
    }

    @Override
    public boolean isSent() {
        return true;
    }

    @Override
    public boolean tryCancel() {
        return false;
    }

    @Override
    public boolean await(int timeout) {
        return true;
    }

}
