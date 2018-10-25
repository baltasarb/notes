package synchronizationWithMonitors.messageQueue;

import utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * This status is used when a send is made before a receive. Because the status is immediately returned
 * some waiting and canceling functionality needs to be added to it.
 *
 * @param <T> the type of the message to send
 */
public class SenderStatus<T> implements SendStatus {

    private final Lock monitor;

    private T message;
    private Consumer<SenderStatus<T>> cancelMessage;
    private Condition condition;

    private boolean isSent;
    private boolean isCanceled;

    SenderStatus(T message, ReentrantLock monitor, Consumer<SenderStatus<T>> cancelMessage) {
        this.message = message;
        this.monitor = monitor;
        this.cancelMessage = cancelMessage;
        condition = null;
        isSent = false;
        isCanceled = false;
    }

    public T getMessage() {
        return message;
    }

    //used by queue inside lock
    void setMessageSentAndSignal() {
        isSent = true;
        if(condition != null){
            condition.signal();
        }
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

        if (isSent) {
            return false;
        }

        if (!isCanceled) {
            isCanceled = true;
            cancelMessage.accept(this);
            if (condition != null) {
                condition.signal();
            }
        }

        monitor.unlock();
        return true;
    }

    @Override
    public boolean await(int timeout) throws InterruptedException {
        monitor.lock();

        if (isSent || isCanceled) {
            monitor.unlock();
            return true;
        }

        Timer timer = new Timer(timeout);

        if (timer.timeExpired()) {
            monitor.unlock();
            return false;
        }

        long timeLeftToWait = timer.getTimeLeftToWait();
        condition = monitor.newCondition();

        try {
            while (true) {
                condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                if (isSent || isCanceled) {
                    return true;
                }

                if (timer.timeExpired()) {
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (isSent || isCanceled) {
                Thread.currentThread().interrupt();
                return true;
            }
            throw e;
        } finally {
            condition = null;
            monitor.unlock();
        }
    }

}
