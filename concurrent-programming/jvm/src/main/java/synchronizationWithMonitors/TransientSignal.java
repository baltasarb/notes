package synchronizationWithMonitors;

import utils.Timer;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransientSignal {

    private final ReentrantLock monitor;
    private LinkedList<Waiter> waiters;

    private final int TIME_EXPIRED = -1;

    public TransientSignal() {
        monitor = new ReentrantLock();
        waiters = new LinkedList<>();
    }

    public int await(long timeout) throws InterruptedException {
        //check invalid parameter
        if (timeout <= 0) {
            return TIME_EXPIRED;
        }

        monitor.lock();

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        Waiter waiter = new Waiter(monitor.newCondition());
        waiters.addLast(waiter);

        try {
            while (true) {
                waiter.condition.await(timeLeftToWait, TimeUnit.MILLISECONDS);

                if (waiter.reason != null) {
                    return waiter.reason;
                }

                if (timer.timeExpired()) {
                    waiters.remove(waiter);
                    return TIME_EXPIRED;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (waiter.reason != null) {
                Thread.currentThread().interrupt();
                return waiter.reason;
            }
            waiters.remove(waiter);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public void signal(int reason) {
        monitor.lock();

        if(!waiters.isEmpty()){
            Waiter waiter = waiters.removeFirst();
            waiter.reason = reason;
            waiter.condition.signal();
        }

        monitor.unlock();
    }

    public void signalAll(int reason) {
        monitor.lock();

        Waiter waiter;

        while(!waiters.isEmpty()){
            waiter = waiters.removeFirst();
            waiter.reason = reason;
            waiter.condition.signal();
        }

        monitor.unlock();
    }

    private class Waiter {
        private final Condition condition;
        private Integer reason;

        private Waiter(Condition condition) {
            this.condition = condition;
            reason = null;
        }
    }
}