package lockFreeSynchronization.unsafeSpinWindowsMutex;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SafeSpinWindosMutex {

    private AtomicInteger signalState = new AtomicInteger(1);
    private AtomicReference<Thread> owner;

    public void Acquire() throws InterruptedException {
        do {
            int observedSignalState = signalState.get();

            if (observedSignalState > 0 || owner.get() == Thread.currentThread()) {

                if (observedSignalState == Integer.MIN_VALUE)
                    throw new IllegalStateException();

                if (observedSignalState - 1 == 0) {
                    if (signalState.compareAndSet(observedSignalState, 0)) {
                        owner.set(Thread.currentThread());
                    }
                }
                return;
            }
            Thread.sleep(0);
        } while (true);
    }

    public void Release() {
        if (owner.get() != Thread.currentThread())
            throw new IllegalStateException();

        if (signalState.get() == 0)
            owner.set(null);

        signalState.incrementAndGet();
    }

}
