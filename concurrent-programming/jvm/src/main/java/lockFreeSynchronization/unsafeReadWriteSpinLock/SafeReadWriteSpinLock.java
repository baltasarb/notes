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
            if (state.decrementAndGet() >= 0)
                return;

            state.incrementAndGet();      //undo previous decrement

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
        int observedState;

        do {
            observedState = state.get();
        } while (!state.compareAndSet(observedState, observedState + UNLOCKED));
    }

}
