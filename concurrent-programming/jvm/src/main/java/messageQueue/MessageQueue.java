package messageQueue;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Utils.Timer;


public class MessageQueue<T> {

    private ReentrantLock monitor;
    private LinkedList<Receiver> receivers;
    private LinkedList<SendStatus> messageStatuses;

    public MessageQueue() {
        this.monitor = new ReentrantLock();
        this.receivers = new LinkedList<>();
        this.messageStatuses = new LinkedList<>();
    }

    public SendStatus send(T sentMsg) {
        monitor.lock();

        MessageCanceler messageCanceler = new MessageCanceler();
        SendStatusImplementer<T> sendStatusImplementer = new SendStatusImplementer<>(sentMsg, messageCanceler);

        if (!receivers.isEmpty()) {
            Receiver receiver = receivers.removeFirst();
            receiver.message = sentMsg;
            receiver.condition.signal();
            sendStatusImplementer.setSentToTrue();
        }

        messageStatuses.addLast(sendStatusImplementer);

        monitor.unlock();

        return sendStatusImplementer;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        if (timeout <= 0) {
            return Optional.empty();
        }

        monitor.lock();

        Timer timer = new Timer(timeout);

        if (!messageStatuses.isEmpty()) {
            SendStatusImplementer<T> sendStatusImplementer = (SendStatusImplementer<T>) messageStatuses.removeFirst();
            sendStatusImplementer.setSentToTrue();

            //notification might be lost if no one is waiting
            sendStatusImplementer.signalAwait();
            monitor.unlock();
            return Optional.of(sendStatusImplementer.getMessage());
        }

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        long timeLeft = timer.getTimeLeftToWait();

        Receiver receiver = new Receiver(monitor.newCondition());
        receivers.addLast(receiver);

        try {
            while (true) {
                receiver.condition.await(timeLeft, TimeUnit.MILLISECONDS);

                if(receiver.messageCanceler.cancelationIsRequired()){
                    receivers.remove(receiver);
                    return Optional.empty();
                }

                if (receiver.message != null) {
                    return Optional.of(receiver.message);
                }

                if (timer.timeExpired()) {
                    receivers.remove(receiver);
                    return Optional.empty();
                }
            }
        } catch (InterruptedException e) {
            if (receiver.message != null) {
                Thread.currentThread().interrupt();
                T message = receiver.message;
                return Optional.of(message);
            }
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public boolean removeMessage(){


        return false;
    }

    private class Receiver {
        private Condition condition;
        private T message;
        private MessageCanceler messageCanceler;

        public Receiver(Condition condition) {
            this.condition = condition;
            messageCanceler = new MessageCanceler();
        }
    }
}
