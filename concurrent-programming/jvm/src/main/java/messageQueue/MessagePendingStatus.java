package messageQueue;

import Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class MessagePendingStatus implements SendStatus {

    private ReentrantLock monitor;
    private Condition condition;
    private boolean isSent;
    private Supplier<Boolean> messageCanceler;
    private boolean messageIsCanceled;

    //  receives a lock from the queue which is shared by ALL the statuses but NOT with the queue itself
    MessagePendingStatus(ReentrantLock monitor, Supplier<Boolean> messageCanceler) {
        this.monitor = monitor;
        isSent = false;
        this.messageCanceler = messageCanceler;
        messageIsCanceled = false;
    }

    @Override
    public boolean isSent() {
        monitor.lock();
        boolean wasSent = isSent;
        monitor.unlock();
        return wasSent;
    }

    @Override
    public boolean tryCancel() {
        monitor.lock();

        if(!messageIsCanceled){
            messageIsCanceled = messageCanceler.get();
        }

        if(condition != null){
            condition.signal();
        }

        monitor.unlock();

        return messageIsCanceled;
    }

    /**
     *
     * @param timeout max time to wait
     * @return false on timeout, true on success or cancellation
     * @throws InterruptedException on await interruption
     */
    @Override
    public boolean await(int timeout) throws InterruptedException {
        monitor.lock();

        // condition only needs to exist on usage and never before
        condition = monitor.newCondition();

        // if send happens, then try cancel happens, then await. All before receive is called.
        if (messageIsCanceled || isSent)
            return true;

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        if (timer.timeExpired()) {
            return false;
        }

        try {
            while (true) {
                condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                if(messageIsCanceled || isSent){
                    return true;
                }

                if (timer.timeExpired()) {
                    return false;
                }
            }
        } catch (InterruptedException e) {
            if(isSent){
                Thread.currentThread().interrupt();
                return true;
            }
            throw  e;
        } finally {
            condition = null;
            monitor.unlock();
        }
    }

    // can only be used inside pending message that can only be used inside queue monitor
    void setSentAndSignal() {
        isSent = true;
        if(condition != null)
            condition.signal();
    }

}
