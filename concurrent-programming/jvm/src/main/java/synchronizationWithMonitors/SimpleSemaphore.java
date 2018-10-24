package synchronizationWithMonitors;

public class SimpleSemaphore {

    private final Object monitor;
    private int units;

    public SimpleSemaphore(int units) {
        this.monitor = new Object();
        this.units = units;
    }

    public void acquire() throws InterruptedException {
        synchronized (monitor) {
            while (units == 0) {
                monitor.wait();
            }
            units--;
        }
    }

    public void release() {
        synchronized (monitor) {
            units++;
            monitor.notify();
        }
    }

}
