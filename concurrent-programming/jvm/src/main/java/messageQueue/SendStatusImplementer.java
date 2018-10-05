package messageQueue;

import Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SendStatusImplementer<T> implements SendStatus {

    private final T message;
    private ReentrantLock monitor;
    private Condition condition;
    private boolean isSent;
    private MessageCanceler messageCanceler;

    public SendStatusImplementer(T message, MessageCanceler messageCanceler) {
        this.message = message;
        this.monitor = new ReentrantLock();
        this.condition = monitor.newCondition();
        this.messageCanceler = messageCanceler;
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

        messageCanceler.setCondition(monitor.newCondition());
        messageCanceler.cancelMessage();

        condition.signal();

        try{
            while(true){
                messageCanceler.getCondition().await();

                if(messageCanceler.getCancelationSuccessfull()){
                    return true;
                }
                else if(!messageCanceler.getCancelationSuccessfull()){
                    return false;
                }
            }
        }catch (InterruptedException e){

        }finally {
            monitor.unlock();
        }
    }

    @Override
    public boolean await(int timeout) throws InterruptedException {

        if (timeout <= 0)
            return false;

        monitor.lock();

        Timer timer = new Timer(timeout);
        long timeLeft = timer.getTimeLeftToWait();

        if (isSent)
            return true;

        if (timer.timeExpired())
            return false;

        try {
            while (true) {

                condition.await(timeLeft, TimeUnit.MILLISECONDS);

                if (isSent)
                    return true;

                if (timer.timeExpired())
                    return false;

                timeLeft = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (isSent) {
                Thread.currentThread().interrupt();
                return true;
            }
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public void signalAwait() {
        condition.signal();
    }

    public void setSentToTrue() {
        this.isSent = true;
    }

    public T getMessage() {
        return message;
    }
}
