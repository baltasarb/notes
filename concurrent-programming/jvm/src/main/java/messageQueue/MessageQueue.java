package messageQueue;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import Utils.Timer;


public class MessageQueue<T> {

    private ReentrantLock monitor;
    private LinkedList<SendStatusImplementer<T>> waiters;

    public MessageQueue() {
        this.monitor = new ReentrantLock();
        this.waiters = new LinkedList<>();
    }

    public SendStatus send(T sentMsg) {
        monitor.lock();

        SendStatusImplementer<T> waiter;

        if (!waiters.isEmpty()) {
            waiter = waiters.getFirst();
            waiter.setMessage(sentMsg);
            waiter.setSentToTrue();
            waiter.getReceiveCondition().signal();
        } else {
            waiter = new SendStatusImplementer<>(monitor, sentMsg, this::cancelMessage);
            waiters.addLast(waiter);
        }

        monitor.unlock();

        return waiter;
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        if (timeout <= 0) {
            return Optional.empty();
        }

        monitor.lock();

        Timer timer = new Timer(timeout);

        if (!waiters.isEmpty()) {
            SendStatusImplementer<T> sendStatusImplementer = waiters.removeFirst();

            if (sendStatusImplementer.isMessageCanceled())
                return Optional.empty(); //todo: or throw e??

            sendStatusImplementer.setSentToTrue();

            //notification might be lost if no one is waiting
            sendStatusImplementer.getAwaitCondition().signal();
            monitor.unlock();
            return Optional.of(sendStatusImplementer.getMessage());
        }

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        long timeLeft = timer.getTimeLeftToWait();

        SendStatusImplementer<T> waiter = new SendStatusImplementer<>(monitor, this::cancelMessage);
        waiters.addLast(waiter);

        try {
            while (true) {
                waiter.getReceiveCondition().await(timeLeft, TimeUnit.MILLISECONDS);

                if (waiter.isMessageCanceled()){
                    System.out.println("canceled");
                    return Optional.empty(); //todo: or throw e??
                }

                T waiterMessage = waiter.getMessage();
                if (waiterMessage != null) {
                    return Optional.of(waiterMessage);
                }

                if (timer.timeExpired()) {
                    waiters.remove(waiter);
                    return Optional.empty();
                }
            }
        } catch (InterruptedException e) {
            T waiterMessage = waiter.getMessage();
            if (waiterMessage != null) {
                Thread.currentThread().interrupt();
                return Optional.of(waiterMessage);
            }
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    private boolean cancelMessage(SendStatusImplementer waiter) {
        monitor.lock();

        boolean success = false;

        //if message is pending, otherwise no message to remove is present, waiting for receive
        if (waiter.getMessage() != null)
            success = waiters.remove(waiter);

        monitor.unlock();

        return success;
    }

}
