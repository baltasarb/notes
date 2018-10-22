package synchronizationWithMonitors.messageQueue;

import synchronizationWithMonitors.Utils.Timer;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class MessageQueue<T> {

    private ReentrantLock monitor;

    //when a sender arrives to the queue first it will be kept here
    private LinkedList<SenderStatus<T>> senderStatuses;

    //when a receiver arrives to te queue first it will be kept here
    private LinkedList<ReceiverStatus<T>> receiverStatuses;

    public MessageQueue() {
        this.monitor = new ReentrantLock();
        senderStatuses = new LinkedList<>();
        receiverStatuses = new LinkedList<>();
    }

    public SendStatus send(T sentMsg) {
        monitor.lock();

        //if someone is already waiting to receive
        //give the message, signal and return
        if (!receiverStatuses.isEmpty()) {
            ReceiverStatus<T> receiverStatus = receiverStatuses.removeFirst();
            receiverStatus.sendMessageAndSignal(sentMsg);
            monitor.unlock();
            return receiverStatus;
        }

        //function used to provide functionality over the queue while limiting the queue accessibility
        Consumer<SenderStatus<T>> cancelMessage = (producer) -> senderStatuses.remove(producer);

        //no one is here to receive, create a senderStatus with thethe message for when someone arrives to receive it
        SenderStatus<T> senderStatus = new SenderStatus<>(sentMsg, monitor, cancelMessage);
        senderStatuses.addLast(senderStatus);

        monitor.unlock();
        return senderStatus;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        monitor.lock();
        //if a message is already waiting to be received remove it, notify the possible await() and return it
        if (!senderStatuses.isEmpty()) {
            SenderStatus<T> senderStatus = senderStatuses.removeFirst();
            Optional<T> message = Optional.of(senderStatus.getMessage());
            senderStatus.setMessageSentAndSignal();
            monitor.unlock();
            return message;
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

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