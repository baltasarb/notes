package synchronizationWithoutLocks;

import utils.Timer;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSemaphore {

    private final Object monitor;
    private volatile AtomicInteger units;
    private volatile AtomicInteger numberOfWaiters;

    public SimpleSemaphore(int units) {
        this.monitor = new Object();
        this.units = new AtomicInteger(units);
        this.numberOfWaiters = new AtomicInteger(0);
    }

    public boolean acquire(int timeout) throws InterruptedException {
        if (tryAcquire()) {
            return true;
        }

        if (timeout <= 0) {
            return false;
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        synchronized (monitor) {
            numberOfWaiters.incrementAndGet();

            if(tryAcquire()){
                numberOfWaiters.decrementAndGet();
                return true;
            }

            try {
                while (true) {
                    monitor.wait(timeLeftToWait);

                    if (tryAcquire()) {
                        return true;
                    }

                    if (timer.timeExpired()) {
                        return false;
                    }
                }
            } catch (InterruptedException e) {
                if (units.get() > 0) {
                    monitor.notify();
                }
                throw e;
            } finally {
                numberOfWaiters.decrementAndGet();
            }
        }
    }

    public void release() {
        units.incrementAndGet();
        if (numberOfWaiters.get() > 0) {
            synchronized (monitor) {
                if (numberOfWaiters.get() > 0) {
                    monitor.notify();
                }
            }
        }

    }

    private boolean tryAcquire() {
        int observed;

        while (true) {
            observed = units.get();

            if (observed <= 0) {
                return false;
            }

            if (units.compareAndSet(observed, observed - 1)) {
                return true;
            }
        }
    }

}
