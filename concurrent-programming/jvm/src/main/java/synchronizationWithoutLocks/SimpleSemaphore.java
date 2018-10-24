package synchronizationWithoutLocks;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSemaphore {

    private volatile AtomicInteger units;

    public SimpleSemaphore(int units) {
        this.units = new AtomicInteger(units);
    }

    public void acquire() {
        if (tryAcquire()) {
            System.out.println("acquired " + units);
            return;
        }

        while (true) {
            int observedUnits = units.get();

            if (observedUnits > 0) {
                if (units.compareAndSet(observedUnits, observedUnits - 1)) {
                    System.out.println("acquired " + units + " after wait");
                    return;
                }
            }
        }
    }

    public void release() {
        int observed;

        while (true) {
            observed = units.get();

            if (units.compareAndSet(observed, observed + 1)) {
                System.out.println("released " + units);
                return;
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
