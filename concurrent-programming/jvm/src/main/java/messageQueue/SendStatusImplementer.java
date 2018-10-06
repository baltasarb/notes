package messageQueue;

import Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class SendStatusImplementer<T> implements SendStatus {

    private T message;
    private ReentrantLock monitor;
    private Condition condition;
    private boolean isSent;
    private boolean messageCanceled;
    private Function<SendStatusImplementer, Boolean> messageCanceler;

    public SendStatusImplementer(T message, Function<SendStatusImplementer, Boolean> messageCanceler) {
        this.message = message;
        this.monitor = new ReentrantLock();
        this.condition = monitor.newCondition();
        this.messageCanceler = messageCanceler;
        messageCanceled = false;
    }

    public SendStatusImplementer(Function<SendStatusImplementer, Boolean> messageCanceler) {
        this.monitor = new ReentrantLock();
        this.condition = monitor.newCondition();
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
        return messageCanceler.apply(this);
    }

    @Override
    public boolean await(int timeout) throws InterruptedException {

        if (timeout <= 0)
            return false;

        monitor.lock();

        Timer timer = new Timer(timeout);
        long timeLeft = timer.getTimeLeftToWait();

        if (isSent)
            return true;

        if (timer.timeExpired())
            return false;

        if (messageCanceled)
            return false;//todo : or throw exception?

        try {
            while (true) {

                condition.await(timeLeft, TimeUnit.MILLISECONDS);

                if (messageCanceled)
                    return false;//todo : or throw exception?

                if (isSent)
                    return true;

                if (timer.timeExpired())
                    return false;

                timeLeft = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (messageCanceled) {
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

    public void signalAwait() {
        condition.signal();
    }

    public void setSentToTrue() {
        this.isSent = true;
    }

    public T getMessage() {
        return message;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public boolean isMessageCanceled() {
        return messageCanceled;
    }
}
