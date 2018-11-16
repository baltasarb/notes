package semiLockFreeSynchronization.optimizedMessageQueue;

import utils.ConcurrentLinkedList;
import utils.Timer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OptimizedMessageQueue<T> {

    private final ReentrantLock monitor;
    private final Condition consumersCondition;

    private ConcurrentLinkedList<MessageStatus<T>> producers;

    private AtomicInteger waitingConsumers;

    public OptimizedMessageQueue() {
        this.monitor = new ReentrantLock();
        producers = new ConcurrentLinkedList<>();
        waitingConsumers = new AtomicInteger(0);
        consumersCondition = monitor.newCondition();
    }

    public SendStatus send(T sentMsg) {

        MessageStatus<T> messageStatus = new MessageStatus<>(sentMsg, monitor);
        producers.addLast(messageStatus);

        if (waitingConsumers.get() == 0) {
            return messageStatus;
        }

        monitor.lock();

        if (waitingConsumers.get() == 0) {
            monitor.unlock();
            return messageStatus;
        }

        consumersCondition.signal();

        monitor.unlock();

        return messageStatus;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        waitingConsumers.incrementAndGet();

        final boolean NO_LOCK_REQUIRED = false;
        final boolean LOCK_REQUIRED = true;

        T message = tryReceive(LOCK_REQUIRED);
        if (message != null) {
            waitingConsumers.decrementAndGet();
            return Optional.of(message);
        }

        try {
            monitor.lock();

            message = tryReceive(NO_LOCK_REQUIRED);
            if (message != null) {
                return Optional.of(message);
            }

            Timer timer = new Timer(timeout);
            long timeLeftToWait = timer.getTimeLeftToWait();

            if (timer.timeExpired()) {
                return Optional.empty();
            }

            while (true) {
                consumersCondition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                message = tryReceive(NO_LOCK_REQUIRED);
                if (message != null) {
                    return Optional.of(message);
                }

                //if no one sent a message and time expired
                if (timer.timeExpired()) {
                    return Optional.empty();
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (!producers.isEmpty()) {
                message = tryReceive(NO_LOCK_REQUIRED);
                if (message != null) {
                    Thread.currentThread().interrupt();
                    return Optional.of(message);
                }
            }
            throw e;
        } finally {
            waitingConsumers.decrementAndGet();
            monitor.unlock();
        }
    }

    /**
     *
     * @param lockRequired is used to tell the possible waiters if the method was called outside exclusion.
     *                     If that was the case the signal of the await will need to re enter exclusion before the
     *                     signaling
     * @return null if no message is present and a T value if present. A correct return will return the received message
     * otherwise the caller will continue its execution assuming no message is waiting to be received
     */
    private T tryReceive(boolean lockRequired) {
        if (!producers.isEmpty()) {
            MessageStatus<T> messageStatus = producers.removeFirst();
            //if no one got to the message first then we are free to continue and return it
            //otherwise back to waiting
            if (messageStatus != null) {
                messageStatus.sendAndSignal(lockRequired);
                return messageStatus.getMessage();
            }
        }
        return null;
    }

}
