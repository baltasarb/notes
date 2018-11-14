package semiLockFreeSynchronization.optimizedMessageQueue;

import utils.ConcurrentLinkedList;
import utils.Timer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OptimizedMessageQueue<T> {

    private ReentrantLock monitor;

    //when a sender arrives to the queue first it will be kept here
    private volatile ConcurrentLinkedList<SenderStatus<T>> senderStatuses;

    //when a receiver arrives to te queue first it will be kept here
    private volatile ConcurrentLinkedList<ReceiverStatus<T>> receiverStatuses;

    private AtomicInteger waitingReceivers;

    public OptimizedMessageQueue() {
        this.monitor = new ReentrantLock();
        senderStatuses = new ConcurrentLinkedList<>();
        receiverStatuses = new ConcurrentLinkedList<>();
        waitingReceivers = new AtomicInteger(0);
    }

    public SendStatus send(T sentMsg) {

        SenderStatus<T> senderStatus = new SenderStatus<>(sentMsg, monitor);
        senderStatuses.addLast(senderStatus);

        if (waitingReceivers.get() == 0) {
            return senderStatus;
        }

        monitor.lock();

        if (waitingReceivers.get() == 0) {
            monitor.unlock();
            return senderStatus;
        }

        ReceiverStatus<T> receiverStatus = receiverStatuses.removeFirst();
        if (receiverStatus != null && receiverStatus.setIsGranted()) {
            receiverStatus.signal();
            monitor.unlock();
            return receiverStatus;
        }

        monitor.unlock();
        return senderStatus;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {

        waitingReceivers.incrementAndGet();

        SenderStatus<T> senderStatus;

        while (!senderStatuses.isEmpty()) {
            senderStatus = senderStatuses.removeFirst();
            if (senderStatus != null) {//might not be sent at this time
                waitingReceivers.decrementAndGet();
                Optional<T> message = Optional.of(senderStatus.getMessage());
                senderStatus.setMessageSentAndSignal();
                return message;
            }
        }

        monitor.lock();
        //if a message is already waiting to be received remove it, notify the possible await() and return it
        while (!senderStatuses.isEmpty()) {
            senderStatus = senderStatuses.removeFirst();
            if (senderStatus != null) {//might not be sent at this time
                waitingReceivers.decrementAndGet();
                Optional<T> message = Optional.of(senderStatus.getMessage());
                senderStatus.setMessageSentAndSignal();
                monitor.unlock();
                return message;
            }
        }


        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        //no message could be received so far, so a receiver will be added to the queue,
        //so that when a send is made this receiver will get the message immediately
        ReceiverStatus<T> receiverStatus = new ReceiverStatus<>(monitor.newCondition());
        receiverStatuses.addLast(receiverStatus);

        try {
            while (true) {
                receiverStatus.await(timeLeftToWait);

                if (receiverStatus.isGranted()) {
                    senderStatus = senderStatuses.removeFirst();
                    T message = senderStatus.getMessage();
                    receiverStatus.setMessage(message);
                    return Optional.of(message);
                }

                //if no one sent a message and time expired, the waiter needs to be removed from the queue and exit
                if (timer.timeExpired()) {
                    receiverStatus.setIsGranted();
                    return Optional.empty();
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (receiverStatus.isGranted()) {
                Thread.currentThread().interrupt();
                senderStatus = senderStatuses.removeFirst();
                T message = senderStatus.getMessage();
                receiverStatus.setMessage(message);
                return Optional.of(message);
            }
            //receiverStatuses.remove(receiverStatus);
            throw e;
        } finally {
            waitingReceivers.decrementAndGet();
            monitor.unlock();
        }
    }


}
