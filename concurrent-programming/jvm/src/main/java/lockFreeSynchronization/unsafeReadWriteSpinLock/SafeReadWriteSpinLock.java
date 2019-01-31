package lockFreeSynchronization.unsafeReadWriteSpinLock;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeReadWriteSpinLock {

    //UNLOCKED when there are no active readers or writers
    private final int UNLOCKED;
    private AtomicInteger state;

    public SafeReadWriteSpinLock() {
        UNLOCKED = 1 << 24;
        state = new AtomicInteger(UNLOCKED);
    }

    public void LockRead() throws InterruptedException {
        do {
            int observedState = state.get();

            int valueToUpdate = observedState - 1;

            if (valueToUpdate >= 0) {
                if(state.compareAndSet(observedState, valueToUpdate)){
                    return;
                }
            }

            do {
                Thread.sleep(0);
            }
            while (state.get() < 1);
        } while (true);
    }

    public void UnlockRead() {
        state.incrementAndGet();
    }

    public void LockWrite() throws InterruptedException {
        do {
            int observedState = state.get();
            if(observedState - UNLOCKED == 0){
                if(state.compareAndSet(observedState,0)){
                    return;
                }
            }
            do {
                Thread.sleep(0);
            } while (state.get() != UNLOCKED);
        } while (true);
    }

    public void UnlockWrite() {
        int observedState, valueToUpdate;
        do {
            observedState = state.get();
            valueToUpdate = observedState + UNLOCKED;
        } while (!state.compareAndSet(observedState, valueToUpdate));
    }

}
