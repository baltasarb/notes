package semiLockFreeSynchronization.optimizedMessageQueue;

import utils.ConcurrentLinkedList;
import utils.Timer;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OptimizedMessageQueue<T> {

    private ReentrantLock monitor;

    //when a sender arrives to the queue first it will be kept here
    private volatile ConcurrentLinkedList<SenderStatus<T>> senderStatuses;

    //when a receiver arrives to te queue first it will be kept here
    private volatile ConcurrentLinkedList<ReceiverStatus<T>> receiverStatuses;

    public OptimizedMessageQueue() {
        this.monitor = new ReentrantLock();
        senderStatuses = new ConcurrentLinkedList<>();
        receiverStatuses = new ConcurrentLinkedList<>();
    }

    public SendStatus send(T sentMsg) {

        SenderStatus<T> senderStatus = new SenderStatus<>(sentMsg, monitor);
        senderStatuses.addLast(senderStatus);

        try {
            monitor.lock();

            //if done outside exclusion someone might arrive but not be added to the queue of receivers yet
            if (receiverStatuses.isEmpty()) {
                return senderStatus;
            }

            ReceiverStatus<T> receiverStatus = receiverStatuses.removeFirst();

            if (receiverStatus != null) {
                receiverStatus.sendMessageAndSignal(sentMsg);
                senderStatus.setMessageSentAndSignal();
                return receiverStatus;
            }
            /*
            do{
                ReceiverStatus<T> receiverStatus = receiverStatuses.removeFirst();
                if (receiverStatus != null) {
                    //instead of a removal the sender is set as sent
                    //the receiver will check if the chosen sender can be utilized by checking the flag
                    //because its done inside exclusion no problem should occur
                    senderStatus.setMessageSentAndSignal();

                    receiverStatus.sendMessageAndSignal(sentMsg);
                    return receiverStatus;
                }
            }while(!receiverStatuses.isEmpty());
*/
            return senderStatus;
        } finally {
            monitor.unlock();
        }
    }

    //TODO if message is already present to receive no lock
    public Optional<T> receive(int timeout) throws InterruptedException {

        while (!senderStatuses.isEmpty()) {
            SenderStatus<T> senderStatus = senderStatuses.removeFirst();
            if (!senderStatus.isSent()) {
                Optional<T> message = Optional.of(senderStatus.getMessage());
                senderStatus.setMessageSentAndSignal();
                return message;
            }
        }

        monitor.lock();
        //if a message is already waiting to be received remove it, notify the possible await() and return it
        while (!senderStatuses.isEmpty()) {
            SenderStatus<T> senderStatus = senderStatuses.removeFirst();
            if (!senderStatus.isSent()) {
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

        //TODO not yet in the list yet a end can be made
        //it will se an empty list and return normally instead of delivering the message

        //no message could be received thus far, so a receiver will be added to the queue,
        //so that when a send is made this receiver will get the message immediately
        ReceiverStatus<T> receiverStatus = new ReceiverStatus<>(monitor.newCondition());
        receiverStatuses.addLast(receiverStatus);

        try {
            while (true) {
                receiverStatus.await(timeLeftToWait);

                //upon wait exit, due to delegation of execution
                //the waiter only needs to see if a message was received and return it if yes
                if (receiverStatus.receivedMessage()) {
                    return Optional.of(receiverStatus.getMessage());
                }

                //if no one sent a message and time expired, the waiter needs to be removed from the queue and exit
                if (timer.timeExpired()) {
                    receiverStatuses.remove(receiverStatus);
                    return Optional.empty();
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (receiverStatus.receivedMessage()) {
                Thread.currentThread().interrupt();
                return Optional.of(receiverStatus.getMessage());
            }
            receiverStatuses.remove(receiverStatus);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

}
