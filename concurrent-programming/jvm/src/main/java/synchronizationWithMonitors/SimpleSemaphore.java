package synchronizationWithMonitors;

import utils.Timer;

public class SimpleSemaphore {

    private final Object monitor;
    private int units;

    public SimpleSemaphore(int units) {
        this.monitor = new Object();
        this.units = units;
    }

    public boolean acquire(int timeout) throws InterruptedException {
        synchronized (monitor) {
            if (units > 0) {
                units--;
                return true;
            }

            if (timeout <= 0) {
                return false;
            }

            Timer timer = new Timer(timeout);
            long timeLeftToWait = timer.getTimeLeftToWait();

            try {
                while (true) {
                    monitor.wait(timeLeftToWait);

                    if (units > 0) {
                        units--;
                        return true;
                    }

                    if (timer.timeExpired()) {
                        return false;
                    }

                    timeLeftToWait = timer.getTimeLeftToWait();
                }
            } catch (InterruptedException e) {
                if (units > 0) {
                    monitor.notify();
                }
                throw e;
            }
        }
    }

    public void release() {
        synchronized (monitor) {
            units++;
            monitor.notify();
        }
    }

}
