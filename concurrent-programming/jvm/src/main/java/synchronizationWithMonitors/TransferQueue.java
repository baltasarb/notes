package synchronizationWithMonitors;

import utils.Timer;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransferQueue<T> {

    private final ReentrantLock monitor;
    private LinkedList<Message> producers;
    private LinkedList<Message> consumers;

    public TransferQueue() {
        monitor = new ReentrantLock();
        producers = new LinkedList<>();
        consumers = new LinkedList<>();
    }

    public void Put(T msg) {
        monitor.lock();

        if (!consumers.isEmpty()) {
            Message consumer = consumers.removeFirst();
            consumer.message = msg;
            if (consumer.condition != null) {
                consumer.condition.signal();
            }
            monitor.unlock();
            return;
        }

        producers.addLast(new Message(null, msg));

        monitor.unlock();
    }

    public boolean Transfer(T msg, int timeout) throws InterruptedException {
        if (timeout <= 0) {
            return false;
        }

        monitor.lock();

        if (!consumers.isEmpty()) {
            Message consumer = consumers.removeFirst();
            consumer.message = msg;
            if (consumer.condition != null) {
                consumer.condition.signal();
            }
            monitor.unlock();
            return true;
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        Message message = new Message(monitor.newCondition(), msg);
        producers.addLast(message);

        try {
            while (true) {
                message.condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                if (message.isSent) {
                    return true;
                }

                if (timer.timeExpired()) {
                    producers.remove(message);
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (message.isSent) {
                Thread.currentThread().interrupt();
                return true;
            }
            producers.remove(message);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public boolean Take(int timeout, MessageWrapper receivedMessage) throws InterruptedException {
        if (timeout <= 0) {
            return false;
        }

        monitor.lock();
        Message message;
        if (!producers.isEmpty()) {
            message = producers.removeFirst();
            message.isSent = true;
            receivedMessage.message = message.message;
            if (message.condition != null) {
                message.condition.signal();
            }
            monitor.unlock();
            return true;
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        message = new Message(monitor.newCondition(), null);
        consumers.addLast(message);

        try {
            while (true) {
                message.condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                if (message.message != null) {
                    receivedMessage.message = message.message;
                    return true;
                }

                if(timer.timeExpired()){
                    consumers.remove(message);
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if(message.message != null){
                Thread.currentThread().interrupt();
                receivedMessage.message = message.message;
                return true;
            }
            consumers.remove(message);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public class MessageWrapper {
        private T message;

        MessageWrapper(T message) {
            this.message = message;
        }
    }

    private class Message {
        private T message;
        private Condition condition;
        private boolean isSent;

        Message(Condition condition, T message) {
            this.condition = condition;
            this.message = message;
            this.isSent = false;
        }
    }
}