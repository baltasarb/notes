package messageQueue;

import Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ProducerStatus<T> implements SendStatus {

    private T message;
    private final Lock monitor;
    private Consumer<ProducerStatus<T>> cancelMessage;
    private Condition condition;

    private boolean isSent;
    private boolean isCanceled;

    ProducerStatus(T message, ReentrantLock monitor, Consumer<ProducerStatus<T>> cancelMessage) {
        this.message = message;
        this.monitor = monitor;
        this.cancelMessage = cancelMessage;
        condition = null;
        isSent = false;
        isCanceled = false;
    }

    public T getMessage(){
        return message;
    }

    //used by queue inside lock
    void setMessageSent() {
        isSent = true;
    }

    @Override
    public boolean isSent() {
        monitor.lock();
        boolean success = isSent;
        monitor.unlock();
        return success;
    }

    @Override
    public boolean tryCancel() {
        monitor.lock();

        if (isSent) {
            return false;
        }

        if (!isCanceled) {
            isCanceled = true;
            cancelMessage.accept(this);
            if(condition!= null){
                condition.signal();
            }
        }

        monitor.unlock();
        return true;
    }

    @Override
    public boolean await(int timeout) throws InterruptedException {
        monitor.lock();

        if (isSent || isCanceled) {
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

                if (isSent || isCanceled) {
                    return true;
                }

                if (timer.timeExpired()) {
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (isSent || isCanceled) {
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
