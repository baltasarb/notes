package lockFreeSynchronization.unsafeSemaphore;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeSemaphore {

    private final int maxPermits;
    private final AtomicInteger permits;

    public SafeSemaphore(int initial, int maximum) {
        if (initial < 0 || initial > maximum)
            throw new IllegalArgumentException();
        permits = new AtomicInteger(initial);
        maxPermits = maximum;
    }

    public boolean tryAcquire(int acquires) {
        int observedPermits;

        while(true){
             observedPermits = permits.get();

            if (observedPermits < acquires){
                return false;
            }

            if(permits.compareAndSet(observedPermits, observedPermits - acquires)){
                return true;
            };
        }
    }

    public void release(int releases) {
        int observedPermits;

        while(true){
           observedPermits = permits.get();

            if (observedPermits + releases < observedPermits || observedPermits + releases > maxPermits)
                throw new IllegalArgumentException();

            if(permits.compareAndSet(observedPermits, observedPermits + releases)){
                return;
            }
        }
    }

}
