package lockFreeSynchronization.unsafeReadWriteSpinLock;

import java.util.concurrent.atomic.AtomicInteger;

public class UnsafeReadWriteSpinLock {

    //UNLOCKED when there are no active readers or writers
    private final int UNLOCKED = 1 << 24;
    private int state = UNLOCKED;

    public void LockRead() throws InterruptedException {
        do {
            if (--state >= 0)
                return;
            state++;      //undo previous decrement
            do {
                Thread.sleep(0);
            }
            while (state < 1);
        } while (true);
    }

    public void UnlockRead() {
        state++;
    }

    public void LockWrite() throws InterruptedException {
        do {
            if ((state -= UNLOCKED) == 0) return;
            state += UNLOCKED; // undo previous subtraction
            do {
                Thread.sleep(0);
            } while (state != UNLOCKED);
        } while (true);
    }

    public void UnlockWrite() {
        state += UNLOCKED;
    }

}
