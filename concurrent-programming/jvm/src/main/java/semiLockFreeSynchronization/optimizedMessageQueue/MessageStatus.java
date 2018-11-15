package semiLockFreeSynchronization.optimizedMessageQueue;

import utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class MessageStatus<T> implements SendStatus {

    private final Lock monitor;
    private final T message;

    private Condition condition;

    private volatile boolean isSent;

    MessageStatus(T message, ReentrantLock monitor) {
        this.message = message;
        this.monitor = monitor;
        condition = null;
        isSent = false;
    }

    T getMessage() {
        return message;
    }

    /**
     * method used to signal any waiters on await method
     * @param lockRequired used when this method is called outside the queue's exclusion
     *                     and guarantees that the await can be signaled correctly
     */
    void sendAndSignal(boolean lockRequired) {
        isSent = true;

        if (condition != null) {
            if (lockRequired) {
                monitor.lock();
                condition.signal();
                monitor.unlock();
            } else {
                condition.signal();
            }
        }
    }

    @Override
    public boolean isSent() {
        return isSent;
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