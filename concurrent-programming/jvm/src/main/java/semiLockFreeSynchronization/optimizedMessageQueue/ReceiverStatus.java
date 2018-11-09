package semiLockFreeSynchronization.optimizedMessageQueue;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * this status is used when a receive call is made before any send was called. The status, upon reception of the message,
 * will have a final state so there is no need for method implementation other than the correct final return
 *
 * @param <T> the type of the message to receive
 */
public class ReceiverStatus<T> implements SendStatus {

    //condition needed for the sender to notify that a message was just delivered to this receiver
    private final Condition condition;

    private T message;

    ReceiverStatus(Condition condition) {
        this.condition = condition;
        message = null;
    }

    void sendMessageAndSignal(T message) {
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
    public boolean await(int timeout) {
        return true;
    }

}

