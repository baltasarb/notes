package messageQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

class PendingMessage<T> {

    private T message;
    private Condition condition;
    private SendStatus status;

    //used when receive arrives first
    //the user only haves a status on a send call so it is assumed that on message
    //receive the status will be sent and there is no need for a pending status
    PendingMessage(Condition condition) {
        message = null;
        this.condition = condition;
    }

    //  used when sent arrives first
    //  receives a lock from the queue which is shared by ALL the statuses but NOT with the queue itself
    // in order to separate queue exclusion from status exclusion
    PendingMessage(Condition condition, T message, ReentrantLock queueMonitor,
                   Function<PendingMessage<T>, Supplier<Boolean>> messageCanceler
    ) {
        this.status = new MessagePendingStatus(queueMonitor, messageCanceler.apply(this));
        this.message = message;
        this.condition = condition;
    }

    // can only be called inside queue monitor lock
    void sendAndSignal(T messageToSend) {
        message = messageToSend;
        condition.signal();
    }

    //Only a message with MessagePendingStatus can be signaled
    void setSentAndSignalStatus() {
        if (status instanceof MessagePendingStatus)
            ((MessagePendingStatus) status).setSentAndSignal();
        else
            throw new UnsupportedOperationException("Wrong message status type signal attempt.");
    }

    SendStatus messageSentStatus() {
        return new MessageSentStatus();
    }

    T getMessage() {
        return message;
    }

    void setMessage(T message) {
        this.message = message;
    }

    Condition getCondition() {
        return condition;
    }

    SendStatus getStatus() {
        return status;
    }

}
