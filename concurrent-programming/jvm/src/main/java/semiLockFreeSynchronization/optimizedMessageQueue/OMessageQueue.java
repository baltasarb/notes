package semiLockFreeSynchronization.optimizedMessageQueue;

import utils.ConcurrentLinkedList;
import utils.Timer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OMessageQueue<T> {

    private ReentrantLock monitor;

    private ConcurrentLinkedList<MessageStatus<T>> producers;

    private AtomicInteger waitingConsumers;

    private Condition consumersCondition;

    public OMessageQueue() {
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

        T message = tryReceiveAndGet(true);
        if(message != null){
            return Optional.of(message);
        }

        monitor.lock();

        message = tryReceiveAndGet(true);
        if(message != null){
            monitor.unlock();
            return Optional.of(message);
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        try {
            while (true) {
                consumersCondition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                //todo how to handle spurious exits?

                message = tryReceiveAndGet(false);
                if(message != null){
                    return Optional.of(message);
                }

                //if no one sent a message and time expired, the waiter needs to be removed from the queue and exit
                if (timer.timeExpired()) {
                    return Optional.empty();
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (!producers.isEmpty()) {
                Thread.currentThread().interrupt();
                consumersCondition.notify();
                //todo try to resolve or delegate to another?
                return Optional.empty();
            }
            throw e;
        } finally {
            waitingConsumers.decrementAndGet();
            monitor.unlock();
        }
    }

    private T tryReceiveAndGet(boolean decrementWaiters){
        if (!producers.isEmpty()) {
            MessageStatus<T> messageStatus = producers.removeFirst();
            if (messageStatus != null) {
                if(decrementWaiters){
                    int observedConsumers = waitingConsumers.get();
                    while(!waitingConsumers.compareAndSet(observedConsumers, observedConsumers -1))
                        ;
                }
                messageStatus.setMessageSentAndSignal();
                return messageStatus.getMessage();
            }
        }
        return null;
    }

}
