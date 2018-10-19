package messageQueue;

import Utils.Timer;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class MessageQueue<T> {

    private ReentrantLock monitor;
    private LinkedList<ProducerStatus<T>> producerStatuses;
    private LinkedList<ConsumerStatus<T>> consumerStatuses;

    public MessageQueue() {
        this.monitor = new ReentrantLock();
        producerStatuses = new LinkedList<>();
        consumerStatuses = new LinkedList<>();
    }

    public SendStatus send(T sentMsg) {
        monitor.lock();
        if (!consumerStatuses.isEmpty()) {
            ConsumerStatus<T> consumerStatus = consumerStatuses.removeFirst();
            consumerStatus.sendMessageAndsignal(sentMsg);
            monitor.unlock();
            return consumerStatus;
        }

        Consumer<ProducerStatus<T>> cancelMessage = (producer) -> producerStatuses.remove(producer);

        ProducerStatus<T> producerStatus = new ProducerStatus<>(sentMsg, monitor, cancelMessage);
        producerStatuses.addLast(producerStatus);

        monitor.unlock();
        return producerStatus;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        monitor.lock();
        if (!producerStatuses.isEmpty()) {
            ProducerStatus<T> producerStatus = producerStatuses.removeFirst();
            Optional<T> message = Optional.of(producerStatus.getMessage());
            producerStatus.setMessageSent();
            monitor.unlock();
            return message;
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        ConsumerStatus<T> consumerStatus = new ConsumerStatus<>(monitor.newCondition());
        consumerStatuses.addLast(consumerStatus);

        try {
            while (true) {
                consumerStatus.await(timeLeftToWait);

                if (consumerStatus.receivedMessage()) {
                    return Optional.of(consumerStatus.getMessage());
                }

                if (timer.timeExpired()) {
                    consumerStatuses.remove(consumerStatus);
                    return Optional.empty();
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (consumerStatus.receivedMessage()) {
                Thread.currentThread().interrupt();
                return Optional.of(consumerStatus.getMessage());
            }
            consumerStatuses.remove(consumerStatus);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

}