package messageQueue;

import Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class SendStatusImplementer<T> implements SendStatus {

    private T message;
    private ReentrantLock monitor;
    private Condition receiveCondition;
    private Condition awaitCondition;
    private boolean isSent;
    private boolean messageCanceled;
    private Function<SendStatusImplementer, Boolean> messageCanceler;

    SendStatusImplementer(ReentrantLock monitor, T message, Function<SendStatusImplementer, Boolean> messageCanceler) {
        this.message = message;
        this.monitor = monitor;
        this.receiveCondition = monitor.newCondition();
        this.messageCanceler = messageCanceler;
        messageCanceled = false;
        awaitCondition = monitor.newCondition();
    }

    SendStatusImplementer(ReentrantLock monitor, Function<SendStatusImplementer, Boolean> messageCanceler) {
        this.monitor = monitor;
        this.receiveCondition = monitor.newCondition();
        this.messageCanceler = messageCanceler;
    }

    @Override
    public boolean isSent() {
        monitor.lock();
        boolean success = isSent;
        monitor.unlock();
        return success;
    }

    @Override
    public boolean tryCancel() {
        monitor.lock();
        messageCanceled = true;
        monitor.unlock();

        //this method has a lock inside, needs to be called outside the lock here
        //otherwise problems of same lock being called would happen
        return messageCanceler.apply(this);
    }

    @Override
    public boolean await(int timeout) throws InterruptedException {

        if (timeout <= 0)
            return false;

        monitor.lock();

        Timer timer = new Timer(timeout);
        long timeLeft = timer.getTimeLeftToWait();

        if (messageCanceled)
            return false;//todo : or throw exception?

        if (isSent)
            return true;

        if (timer.timeExpired())
            return false;

        try {
            while (true) {

                awaitCondition.await(timeLeft, TimeUnit.MILLISECONDS);

                if (messageCanceled)
                    return false;//todo : or throw exception?

                if (isSent)
                    return true;

                if (timer.timeExpired())
                    return false;

                timeLeft = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (messageCanceled) {//not right, if message was canceled then might
                Thread.currentThread().interrupt();
                return false;//todo : or throw exception?
            }
            if (isSent) {
                Thread.currentThread().interrupt();
                return true;
            }
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    T getMessage() {
        return message;
    }

    Condition getReceiveCondition() {
        return receiveCondition;
    }

    Condition getAwaitCondition() {
        return awaitCondition;
    }

    void setMessage(T message) {
        this.message = message;
    }

    void setSentToTrue() {
        this.isSent = true;
    }

    boolean isMessageCanceled() {
        return messageCanceled;
    }

}
