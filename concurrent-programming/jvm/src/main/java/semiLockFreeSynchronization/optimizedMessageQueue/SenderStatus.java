package semiLockFreeSynchronization.optimizedMessageQueue;

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
    private final T message;

    private Condition condition;

    private boolean isSent;

    SenderStatus(T message, ReentrantLock monitor) {
        this.message = message;
        this.monitor = monitor;
        condition = null;
        isSent = false;
    }

    public T getMessage() {
        return message;
    }

    //used by queue inside lock
    void setMessageSentAndSignal() {
        isSent = true;
        if (condition != null) {
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
    public boolean await(int timeout) throws InterruptedException {
        monitor.lock();

        if (isSent) {
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

                if (isSent) {
                    return true;
                }

                if (timer.timeExpired()) {
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (isSent) {
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